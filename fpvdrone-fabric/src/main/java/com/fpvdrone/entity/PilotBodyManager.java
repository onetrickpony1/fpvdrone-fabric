package com.fpvdrone.entity;

import com.fpvdrone.FPVMod;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.network.FpvStatePacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.PlayerTeleportPacket;
import com.fpvdrone.registry.ModEntities;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.PacketDistributor.TargetPoint;
import com.fpvdrone.network.compat.ServerHolder;

public class PilotBodyManager {
    private static final Map<UUID, PilotBodyEntity> activePilotBodies = new ConcurrentHashMap<>();
    private static final Map<UUID, ChunkPos> forcedChunks = new ConcurrentHashMap<>();

    public static void onEnterFPV(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer != null) {
            UUID playerUUID = serverPlayer.getUUID();
            if (!activePilotBodies.containsKey(playerUUID)) {
                ServerLevel level = serverPlayer.level();
                PilotBodyEntity pilotBody = new PilotBodyEntity((EntityType<? extends PilotBodyEntity>)ModEntities.PILOT_BODY.get(), level);
                pilotBody.setPos(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ());
                pilotBody.setOwner(serverPlayer);
                pilotBody.setSlimModel(isSlimModel(serverPlayer.getUUID()));
                level.addFreshEntity(pilotBody);
                activePilotBodies.put(playerUUID, pilotBody);
                ChunkPos chunkPos = pilotBody.chunkPosition();
                DroneChunkTickets.forceChunk(level, chunkPos.x(), chunkPos.z(), true);
                forcedChunks.put(playerUUID, chunkPos);
            }
        }
    }

    public static void onExitFPV(Player player) {
        ServerPlayer serverPlayer = getServerPlayer(player);
        if (serverPlayer != null) {
            UUID playerUUID = serverPlayer.getUUID();
            PilotBodyEntity pilotBody = activePilotBodies.remove(playerUUID);
            if (pilotBody != null && pilotBody.isAlive()) {
                Vec3 pos = pilotBody.position();
                ServerLevel level = serverPlayer.level();
                int chunkX = (int)Math.floor(pos.x) >> 4;
                int chunkZ = (int)Math.floor(pos.z) >> 4;
                level.getChunk(chunkX, chunkZ);
                if (serverPlayer.isAlive()) {
                    float bodyYaw = pilotBody.getYRot();
                    float headPitch = pilotBody.getXRot();
                    serverPlayer.snapTo(pos.x, pos.y, pos.z, bodyYaw, headPitch);
                    serverPlayer.setOldPosAndRot();
                    serverPlayer.setYHeadRot(bodyYaw);
                    serverPlayer.setYBodyRot(bodyYaw);
                    serverPlayer.connection.teleport(pos.x, pos.y, pos.z, bodyYaw, headPitch);
                }

                TargetPoint tpPoint = new TargetPoint(serverPlayer, pos.x, pos.y, pos.z, 1024.0, level.dimension());
                ModNetwork.CHANNEL
                    .send(
                        PacketDistributor.NEAR.with(() -> tpPoint),
                        new PlayerTeleportPacket(playerUUID, pos.x, pos.y, pos.z, pilotBody.getYRot(), pilotBody.getXRot())
                    );
                TargetPoint statePoint = new TargetPoint(serverPlayer, pos.x, pos.y, pos.z, 1024.0, level.dimension());
                ModNetwork.CHANNEL
                    .send(PacketDistributor.NEAR.with(() -> statePoint), new FpvStatePacket(playerUUID, false, pilotBody.getYRot(), pilotBody.getXRot()));
                pilotBody.discard();
            }

            releaseChunk(serverPlayer.level(), playerUUID);
        }
    }

    private static void releaseChunk(ServerLevel level, UUID playerUUID) {
        ChunkPos chunkPos = forcedChunks.remove(playerUUID);
        if (chunkPos != null) {
            DroneChunkTickets.forceChunk(level, chunkPos.x(), chunkPos.z(), false);
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
    public static PilotBodyEntity getPilotBody(UUID playerUUID) {
        return activePilotBodies.get(playerUUID);
    }

    public static void onPilotBodyDestroyed(PilotBodyEntity entity) {
        UUID ownerUUID = entity.getOwnerUUID();
        if (ownerUUID != null) {
            Vec3 deathPos = entity.position();
            float deathYaw = entity.getYRot();
            float deathPitch = entity.getXRot();
            DamageSource killingSource = entity.getLastDamageSource();
            activePilotBodies.remove(ownerUUID);
            FPVMod.setArmed(ownerUUID, false);
            MinecraftServer server = ServerHolder.server();
            if (server != null) {
                ServerPlayer serverPlayer = server.getPlayerList().getPlayer(ownerUUID);
                if (serverPlayer != null) {
                    serverPlayer.noPhysics = false;
                    serverPlayer.getAbilities().flying = false;
                    serverPlayer.getAbilities().mayfly = serverPlayer.isCreative() || serverPlayer.isSpectator();
                    serverPlayer.onUpdateAbilities();
                    ServerLevel level = serverPlayer.level();
                    int chunkX = (int)Math.floor(deathPos.x) >> 4;
                    int chunkZ = (int)Math.floor(deathPos.z) >> 4;
                    level.getChunk(chunkX, chunkZ);
                    serverPlayer.snapTo(deathPos.x, deathPos.y, deathPos.z, deathYaw, deathPitch);
                    serverPlayer.setOldPosAndRot();
                    serverPlayer.setYHeadRot(deathYaw);
                    serverPlayer.setYBodyRot(deathYaw);
                    serverPlayer.connection.teleport(deathPos.x, deathPos.y, deathPos.z, deathYaw, deathPitch);
                    serverPlayer.resetSentInfo();
                    TargetPoint tpPoint = new TargetPoint(serverPlayer, deathPos.x, deathPos.y, deathPos.z, 1024.0, level.dimension());
                    ModNetwork.CHANNEL
                        .send(
                            PacketDistributor.NEAR.with(() -> tpPoint),
                            new PlayerTeleportPacket(ownerUUID, deathPos.x, deathPos.y, deathPos.z, deathYaw, deathPitch)
                        );
                    TargetPoint statePoint = new TargetPoint(serverPlayer, deathPos.x, deathPos.y, deathPos.z, 1024.0, level.dimension());
                    ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> statePoint), new FpvStatePacket(ownerUUID, false, deathYaw, deathPitch));
                    UUID droneId = FPVMod.getActiveDroneId(ownerUUID).orElse(null);
                    FPVMod.removeActiveDrone(ownerUUID);
                    serverPlayer.refreshDimensions();
                    if (droneId != null && level.getEntity(droneId) instanceof DroneEntity drone) {
                        drone.setInFpv(false);
                        drone.setOwner(null);
                        drone.setBeingControlled(false);
                        drone.setMotorsArmed(false);
                        drone.releaseChunkLoading();
                    }

                    releaseChunk(level, ownerUUID);
                    ItemStack helmet = serverPlayer.getItemBySlot(EquipmentSlot.HEAD);
                    if (helmet.getItem() instanceof FpvGogglesItem) {
                        serverPlayer.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        if (!serverPlayer.getInventory().add(helmet)) {
                            serverPlayer.drop(helmet, false, net.minecraft.util.Prediction.SERVER_ONLY);
                        }
                    }

                    if (serverPlayer.isAlive() && !serverPlayer.getAbilities().invulnerable) {
                        if (killingSource != null) {
                            serverPlayer.hurt(killingSource, Float.MAX_VALUE);
                        } else {
                            serverPlayer.hurt(level.damageSources().generic(), Float.MAX_VALUE);
                        }

                        MinecraftServer mcServer = ServerHolder.server();
                        if (mcServer != null) {
                            mcServer.execute(() -> mcServer.execute(() -> {
                                ServerPlayer respawnedPlayer = mcServer.getPlayerList().getPlayer(ownerUUID);
                                if (respawnedPlayer != null) {
                                    respawnedPlayer.connection.teleport(deathPos.x, deathPos.y, deathPos.z, deathYaw, deathPitch);
                                    respawnedPlayer.setOldPosAndRot();
                                    respawnedPlayer.resetSentInfo();
                                }
                            }));
                        }
                    }
                }
            }
        }
    }

    public static void onPlayerDisconnect(UUID playerUUID) {
        PilotBodyEntity pilotBody = activePilotBodies.remove(playerUUID);
        if (pilotBody != null) {
            if (pilotBody.level() instanceof ServerLevel level) {
                releaseChunk(level, playerUUID);
            }

            pilotBody.discard();
        }

        forcedChunks.remove(playerUUID);
    }

    public static void clear() {
        activePilotBodies.forEach((uuid, entity) -> {
            if (entity.isAlive()) {
                if (entity.level() instanceof ServerLevel level) {
                    releaseChunk(level, uuid);
                }

                entity.discard();
            }
        });
        activePilotBodies.clear();
        forcedChunks.clear();
    }

    private static boolean isSlimModel(UUID playerUUID) {
        return false;
    }
}
