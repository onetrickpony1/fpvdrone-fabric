package com.fpvdrone.entity;

import com.fpvdrone.FPVMod;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.network.DroneStateBroadcastPacket;
import com.fpvdrone.network.FpvStatePacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.PlayerTeleportPacket;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.PacketDistributor.TargetPoint;
import com.fpvdrone.network.compat.ServerHolder;
import com.fpvdrone.registry.ModEntities;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Server side of the pilot's body: the stand-in left where the player stood while they fly in FPV.
 * <p>
 * 1.1.8 version. Compared with 1.1.4: the body's position is remembered every tick, so the exit still puts the player
 * back there after the body was unloaded or killed; the player lands on the first free spot at or above it (up to 64
 * blocks) instead of inside a block; the body loads its chunks through {@link LookaheadChunkLoader} like drones do; it
 * sends its position to its owner once a second; a body of an invulnerable player floats.
 */
public class PilotBodyManager {
    private static final Map<UUID, PilotBodyEntity> activePilotBodies = new ConcurrentHashMap<>();
    /** Last known body position per owner: x, y, z, yaw, pitch. */
    private static final Map<UUID, double[]> lastBodyPositions = new ConcurrentHashMap<>();

    /** Every tick: remember where the body is (1.1.8 calls this onPilotBodyDestroyed, but it runs each tick). */
    public static void rememberPosition(PilotBodyEntity body) {
        UUID owner = body.getOwnerUUID();
        if (owner != null) {
            Vec3 pos = body.position();
            lastBodyPositions.put(owner, new double[]{pos.x, pos.y, pos.z, body.getYRot(), body.getXRot()});
        }
    }

    /** Once a second: tell the owner's client where their body is. */
    public static void sendPositionToOwner(PilotBodyEntity body) {
        UUID owner = body.getOwnerUUID();
        if (owner != null) {
            MinecraftServer server = ServerHolder.server();
            if (server != null) {
                ServerPlayer player = server.getPlayerList().getPlayer(owner);
                if (player != null) {
                    Vec3 pos = body.position();
                    ModNetwork.CHANNEL
                        .send(PacketDistributor.PLAYER.with(() -> player), new DroneStateBroadcastPacket(pos.x, pos.y, pos.z, body.getYRot(), body.getXRot()));
                }
            }
        }
    }

    public static void onEnterFPV(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer != null) {
            UUID playerId = serverPlayer.getUUID();
            if (!activePilotBodies.containsKey(playerId)) {
                ServerLevel level = serverPlayer.level();
                PilotBodyEntity body = new PilotBodyEntity((EntityType<? extends PilotBodyEntity>)ModEntities.PILOT_BODY.get(), level);
                body.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
                body.setOwner(serverPlayer);
                body.setSlimModel(isSlimModel(playerId));
                if (serverPlayer.getAbilities().invulnerable) {
                    body.setNoGravity(true);
                    body.setDeltaMovement(Vec3.ZERO);
                    body.fallDistance = 0.0F;
                    body.setOnGround(true);
                }

                level.addFreshEntity(body);
                activePilotBodies.put(playerId, body);
                lastBodyPositions.remove(playerId);
                LookaheadChunkLoader.v5suI(level, playerId, body.position(), Vec3.ZERO);
            }
        }
    }

    /** Leaving FPV: the player goes back to the body (or where it was last seen) and the body goes away. */
    public static void onExitFPV(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer != null) {
            UUID playerId = serverPlayer.getUUID();
            PilotBodyEntity body = activePilotBodies.remove(playerId);
            Vec3 pos = null;
            float yaw = serverPlayer.getYRot();
            float pitch = serverPlayer.getXRot();
            if (body != null && body.isAlive()) {
                pos = body.position();
                yaw = body.getYRot();
                pitch = body.getXRot();
            } else {
                double[] last = lastBodyPositions.get(playerId);
                if (last != null) {
                    pos = new Vec3(last[0], last[1], last[2]);
                    yaw = (float)last[3];
                    pitch = (float)last[4];
                }
            }

            if (pos != null) {
                ServerLevel level = serverPlayer.level();
                level.getChunk((int)Math.floor(pos.x) >> 4, (int)Math.floor(pos.z) >> 4);
                returnPlayer(serverPlayer, pos, yaw, pitch);
            }

            if (body != null && body.isAlive()) {
                body.discard();
            }

            lastBodyPositions.remove(playerId);
            releaseChunks(serverPlayer.level(), playerId);
        }
    }

    /** First spot at or up to 64 blocks above {@code pos} where a player fits; {@code pos} itself if none. */
    private static Vec3 findFreeSpot(ServerLevel level, Vec3 pos) {
        EntityDimensions size = EntityTypes.PLAYER.getDimensions();
        if (isFree(level, size.makeBoundingBox(pos))) {
            return pos;
        }

        for (int up = 1; up <= 64; up++) {
            Vec3 higher = pos.add(0.0, up, 0.0);
            if (isFree(level, size.makeBoundingBox(higher))) {
                return higher;
            }
        }

        return pos;
    }

    private static boolean isFree(ServerLevel level, AABB box) {
        for (VoxelShape shape : level.getBlockCollisions(null, box)) {
            if (!shape.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private static void returnPlayer(ServerPlayer player, Vec3 target, float yaw, float pitch) {
        UUID playerId = player.getUUID();
        ServerLevel level = player.level();
        Vec3 pos = findFreeSpot(level, target);
        if (player.isAlive()) {
            player.snapTo(pos.x, pos.y, pos.z, yaw, pitch);
            player.setOldPosAndRot();
            player.setYHeadRot(yaw);
            player.setYBodyRot(yaw);
            player.fallDistance = 0.0F;
            player.setDeltaMovement(Vec3.ZERO);
            player.connection.teleport(pos.x, pos.y, pos.z, yaw, pitch);
        }

        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PlayerTeleportPacket(playerId, pos.x, pos.y, pos.z, yaw, pitch));
        TargetPoint near = new TargetPoint(player, pos.x, pos.y, pos.z, 1024.0, level.dimension());
        ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> near), new PlayerTeleportPacket(playerId, pos.x, pos.y, pos.z, yaw, pitch));
        ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> near), new FpvStatePacket(playerId, false, yaw, pitch));
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new FpvStatePacket(playerId, false, yaw, pitch));
    }

    private static void releaseChunks(ServerLevel level, UUID playerId) {
        LookaheadChunkLoader.v5suI(level, playerId);
    }

    /** Every tick: keep the body's chunks loaded while it is the owner's active body. */
    public static void tickChunkLoading(PilotBodyEntity body) {
        UUID owner = body.getOwnerUUID();
        if (owner != null && activePilotBodies.containsKey(owner) && body.level() instanceof ServerLevel level) {
            LookaheadChunkLoader.v5suI(level, owner, body.position(), Vec3.ZERO);
        }
    }

    @Nullable
    private static ServerPlayer getServerPlayer(Player player) {
        if (player instanceof ServerPlayer sp) {
            return sp;
        } else {
            MinecraftServer server = ServerHolder.server();
            return server != null ? server.getPlayerList().getPlayer(player.getUUID()) : null;
        }
    }

    @Nullable
    public static PilotBodyEntity getPilotBody(UUID playerId) {
        return activePilotBodies.get(playerId);
    }

    /** The body died: the player is put back there, loses the goggles' link, and dies too unless invulnerable. */
    public static void onPilotBodyDestroyed(PilotBodyEntity body) {
        UUID ownerId = body.getOwnerUUID();
        if (ownerId == null) {
            return;
        }

        Vec3 deathPos = body.position();
        float deathYaw = body.getYRot();
        float deathPitch = body.getXRot();
        DamageSource killingSource = body.getLastDamageSource();
        activePilotBodies.remove(ownerId);
        FPVMod.setArmed(ownerId, false);
        MinecraftServer server = ServerHolder.server();
        if (server == null) {
            return;
        }

        ServerPlayer player = server.getPlayerList().getPlayer(ownerId);
        if (player == null) {
            return;
        }

        player.noPhysics = false;
        player.getAbilities().flying = false;
        player.getAbilities().mayfly = player.isCreative() || player.isSpectator();
        player.onUpdateAbilities();
        ServerLevel level = player.level();
        level.getChunk((int)Math.floor(deathPos.x) >> 4, (int)Math.floor(deathPos.z) >> 4);
        returnPlayer(player, deathPos, deathYaw, deathPitch);
        player.resetSentInfo();
        lastBodyPositions.put(ownerId, new double[]{deathPos.x, deathPos.y, deathPos.z, deathYaw, deathPitch});
        UUID droneId = FPVMod.getActiveDroneId(ownerId).orElse(null);
        FPVMod.removeActiveDrone(ownerId);
        player.refreshDimensions();
        if (droneId != null && level.getEntity(droneId) instanceof DroneEntity drone) {
            drone.setInFpv(false);
            drone.setOwner(null);
            drone.setBeingControlled(false);
            drone.setMotorsArmed(false);
            drone.releaseChunkLoading();
        }

        Integer munitionId = FPVMod.getMunitionEntityId(ownerId);
        if (munitionId != null) {
            // 1.1.8: a killed body also ends a munition view.
            FPVMod.removeMunitionPilot(ownerId);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new com.fpvdrone.network.MunitionViewEndedPacket(munitionId));
        }

        releaseChunks(level, ownerId);
        FPVMod.clearFpvReturnPosition(player);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof FpvGogglesItem) {
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            if (!player.getInventory().add(helmet)) {
                player.drop(helmet, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }

        if (player.isAlive() && !player.getAbilities().invulnerable) {
            player.hurt(killingSource != null ? killingSource : level.damageSources().generic(), Float.MAX_VALUE);
            // After the respawn, put the player back at the body (two ticks later, as in 1.1.8).
            server.execute(() -> server.execute(() -> {
                ServerPlayer respawned = server.getPlayerList().getPlayer(ownerId);
                if (respawned != null) {
                    Vec3 spot = findFreeSpot(respawned.level(), deathPos);
                    respawned.connection.teleport(spot.x, spot.y, spot.z, deathYaw, deathPitch);
                    respawned.setOldPosAndRot();
                    respawned.resetSentInfo();
                }
            }));
        }
    }

    /** The player left the server without the normal exit: drop the body and its chunks. */
    public static void onPlayerDisconnect(UUID playerId) {
        PilotBodyEntity body = activePilotBodies.remove(playerId);
        if (body != null) {
            if (body.level() instanceof ServerLevel level) {
                releaseChunks(level, playerId);
            }

            body.discard();
        }
    }

    public static void clear() {
        activePilotBodies.forEach((playerId, body) -> {
            if (body.isAlive()) {
                if (body.level() instanceof ServerLevel level) {
                    releaseChunks(level, playerId);
                }

                body.discard();
            }
        });
        activePilotBodies.clear();
    }

    private static boolean isSlimModel(UUID playerId) {
        return false;
    }
}
