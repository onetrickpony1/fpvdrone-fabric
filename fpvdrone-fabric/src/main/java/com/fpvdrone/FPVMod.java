package com.fpvdrone;

import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroneLinkManager;
import com.fpvdrone.entity.PilotBodyEntity;
import com.fpvdrone.entity.PilotBodyManager;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.network.ConfigSyncPacket;
import com.fpvdrone.network.DestroyedDronesSyncPacket;
import com.fpvdrone.network.FpvEntryRejectedPacket;
import com.fpvdrone.network.FpvStatePacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.physics.PhysicsState;
import com.fpvdrone.registry.ModEntities;
import com.fpvdrone.registry.ModItems;
import com.fpvdrone.registry.ModSounds;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import com.fpvdrone.entity.DroneChunkTickets;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.ServerHolder;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.ServerHolder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FPVMod implements ModInitializer {
    public static final String MODID = "fpvdrone";
    public static final Logger LOGGER = LoggerFactory.getLogger(FPVMod.class);
    private static final Map<UUID, Boolean> serverArmStates = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> serverPlayerToActiveDrone = new ConcurrentHashMap<>();
    private static final Set<UUID> pendingDroneCleanup = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<UUID, ChunkPos> serverDroneTicketChunks = new ConcurrentHashMap<>();
    private static final Map<UUID, ChunkPos> lastKnownDronePositions = new ConcurrentHashMap<>();
    private static final ThreadLocal<Boolean> SUPPRESS_VANILLA_TNT_ENTITY_DAMAGE = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<Boolean> SUPPRESS_IV_TRIGGERED_EXPLOSION = ThreadLocal.withInitial(() -> false);

    public static void suppressVanillaTntEntityDamage(boolean suppress) {
        SUPPRESS_VANILLA_TNT_ENTITY_DAMAGE.set(suppress);
    }

    public static void suppressIvTriggeredExplosion(boolean suppress) {
        SUPPRESS_IV_TRIGGERED_EXPLOSION.set(suppress);
    }

    @Override
    public void onInitialize() {
        FpvConfig.load();
        ModEntities.init();
        ModItems.init();
        ModSounds.init();
        DroneChunkTickets.init();
        ServerHolder.init();
        ModNetwork.register();
        FabricDefaultAttributeRegistry.register(ModEntities.PILOT_BODY.get(), PilotBodyEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.DRONE.get(), DroneEntity.createAttributes());
        ServerTickEvents.END_SERVER_TICK.register(server -> this.onServerTick());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> this.onPlayerLoggedIn(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> this.onPlayerLogout(handler.player));
        EntityTrackingEvents.START_TRACKING.register(this::onStartTracking);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(this::allowDamage);
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> this.allowDeath(entity));
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> onEntityLeaveLevel(entity, false));
    }

    public static boolean isArmed(UUID playerId) {
        return isClientSide() ? clientIsArmed(playerId) : serverArmStates.getOrDefault(playerId, false);
    }

    public static void setArmed(UUID playerId, boolean armed) {
        serverArmStates.put(playerId, armed);
        if (isClientSide()) {
            clientSetArmed(playerId, armed);
        }
    }

    public static Optional<UUID> getActiveDroneId(UUID playerId) {
        return isClientSide() ? clientGetActiveDroneId(playerId) : Optional.ofNullable(serverPlayerToActiveDrone.get(playerId));
    }

    public static UUID getServerActiveDroneId(UUID playerId) {
        return serverPlayerToActiveDrone.get(playerId);
    }

    public static Optional<DroneEntity> getActiveDrone(UUID playerId) {
        return isClientSide() ? clientGetActiveDrone(playerId) : Optional.empty();
    }

    public static PhysicsState getPhysicsState(UUID playerId) {
        return isClientSide() ? clientGetPhysicsState(playerId) : null;
    }

    @Nullable
    public static float[] getLastKnownDroneRotation(UUID droneId) {
        return isClientSide() ? clientGetLastKnownDroneRotation(droneId) : null;
    }

    public static void enterFPVMode(Player player, DroneEntity drone) {
        if (isClientSide()) {
            clientEnterFPVMode(player, drone);
        }
    }

    public static void finalizeDroneState(Player player) {
        if (isClientSide()) {
            clientFinalizeDroneState(player);
        }
    }

    public static Optional<DroneEntity> findDroneById(Player player, UUID droneId) {
        return isClientSide() ? clientFindDroneById(player, droneId) : Optional.empty();
    }

    public static void onDronePositionReceived(UUID droneId, double x, double y, double z) {
        if (isClientSide()) {
            clientOnDronePositionReceived(droneId, x, y, z);
        }
    }

    public static void onDroneNotFound(UUID droneId) {
        if (isClientSide()) {
            clientOnDroneNotFound(droneId);
        }
    }

    public static void onDroneDestroyed(UUID droneId) {
        onDroneDestroyedServer(droneId);
        if (isClientSide()) {
            clientOnDroneDestroyed(droneId);
        }
    }

    public static void setActiveDrone(UUID playerId, UUID droneId) {
        serverPlayerToActiveDrone.put(playerId, droneId);
    }

    public static void removeActiveDrone(UUID playerId) {
        serverPlayerToActiveDrone.remove(playerId);
    }

    public static boolean hasActiveDrone(UUID playerId) {
        return serverPlayerToActiveDrone.containsKey(playerId);
    }

    public static Set<UUID> getFpvPlayerIds() {
        return serverPlayerToActiveDrone.keySet();
    }

    public static UUID findPilotForDrone(UUID droneId) {
        for (Entry<UUID, UUID> entry : serverPlayerToActiveDrone.entrySet()) {
            if (entry.getValue().equals(droneId)) {
                return entry.getKey();
            }
        }

        return null;
    }

    public static void scheduleDroneCleanup(UUID droneId) {
        pendingDroneCleanup.add(droneId);
    }

    public static boolean consumeDroneCleanup(UUID droneId) {
        return pendingDroneCleanup.remove(droneId);
    }

    public static void updateLastKnownDronePosition(UUID droneId, double x, double z) {
        lastKnownDronePositions.put(droneId, new ChunkPos((int)Math.floor(x) >> 4, (int)Math.floor(z) >> 4));
    }

    public static void releaseServerDroneTicket(UUID droneId, ServerLevel level) {
        ChunkPos old = serverDroneTicketChunks.remove(droneId);
        lastKnownDronePositions.remove(droneId);
        if (old != null && level != null) {
            DroneChunkTickets.forceChunk(level, old.x(), old.z(), false);
        }
    }

    public static boolean isVanillaTntEntityDamageSuppressed() {
        return SUPPRESS_VANILLA_TNT_ENTITY_DAMAGE.get();
    }

    /** Called from the player drop mixin: pilots in FPV may not throw items. */
    public static boolean shouldBlockItemToss(ServerPlayer player) {
        return hasActiveDrone(player.getUUID());
    }

    private void onServerTick() {
        {
            for (Entry<UUID, UUID> entry : serverPlayerToActiveDrone.entrySet()) {
                UUID playerId = entry.getKey();
                UUID droneId = entry.getValue();
                ChunkPos target = lastKnownDronePositions.get(droneId);
                if (target != null) {
                    ChunkPos current = serverDroneTicketChunks.get(droneId);
                    if (!target.equals(current)) {
                        MinecraftServer server = ServerHolder.server();
                        if (server != null) {
                            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                            if (player != null) {
                                ServerLevel level = player.level();
                                if (current != null) {
                                    DroneChunkTickets.forceChunk(level, current.x(), current.z(), false);
                                }

                                DroneChunkTickets.forceChunk(level, target.x(), target.z(), true);
                                serverDroneTicketChunks.put(droneId, target);
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isClientSide() {
        var server = ServerHolder.server();
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT
            && (server == null || !server.isSameThread());
    }

    private static boolean clientIsArmed(UUID id) {
        return FPVClientMod.isArmed(id);
    }

    private static void clientSetArmed(UUID id, boolean a) {
        FPVClientMod.setArmed(id, a);
    }

    private static Optional<UUID> clientGetActiveDroneId(UUID id) {
        return FPVClientMod.getActiveDroneId(id);
    }

    private static Optional<DroneEntity> clientGetActiveDrone(UUID id) {
        return FPVClientMod.getActiveDrone(id);
    }

    private static PhysicsState clientGetPhysicsState(UUID id) {
        return FPVClientMod.getPhysicsState(id);
    }

    private static void clientEnterFPVMode(Player p, DroneEntity d) {
        FPVClientMod.enterFPVMode(p, d);
    }

    private static void clientFinalizeDroneState(Player p) {
        FPVClientMod.finalizeDroneState(p);
    }

    private static Optional<DroneEntity> clientFindDroneById(Player p, UUID id) {
        return FPVClientMod.findDroneById(p, id);
    }

    private static void clientOnDronePositionReceived(UUID id, double x, double y, double z) {
        FPVClientMod.onDronePositionReceived(id, x, y, z);
    }

    private static void clientOnDroneNotFound(UUID id) {
        FPVClientMod.onDroneNotFound(id);
    }

    private static void clientOnDroneDestroyed(UUID id) {
        FPVClientMod.onDroneDestroyed(id);
    }

    private static float[] clientGetLastKnownDroneRotation(UUID id) {
        return FPVClientMod.getLastKnownDroneRotation(id);
    }

    public static void onDroneDestroyedServer(UUID droneId) {
        DroneLinkManager.unlinkDrone(droneId);
    }

    private void onPlayerLoggedIn(ServerPlayer player) {
        {
            UUID playerId = player.getUUID();
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ConfigSyncPacket());
            ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getItem() instanceof FpvGogglesItem) {
                ServerLevel level = ((ServerPlayer)player).level();
                Map<Integer, UUID> linkedDrones = FpvGogglesItem.getLinkedDrones(helmet);
                List<UUID> destroyed = new ArrayList<>();

                for (UUID droneId : linkedDrones.values()) {
                    Entity entity = level.getEntity(droneId);
                    if (entity == null || !(entity instanceof DroneEntity)) {
                        destroyed.add(droneId);
                    }
                }

                Optional<UUID> legacyLink = FpvGogglesItem.getLinkedDroneId(helmet);
                if (legacyLink.isPresent() && !linkedDrones.containsValue(legacyLink.get())) {
                    Entity entity = level.getEntity(legacyLink.get());
                    if (entity == null || !(entity instanceof DroneEntity)) {
                        destroyed.add(legacyLink.get());
                    }
                }

                if (!destroyed.isEmpty()) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DestroyedDronesSyncPacket(destroyed));
                }

                Optional<UUID> linkedDroneId = FpvGogglesItem.getLinkedDroneId(helmet);
                if (linkedDroneId.isPresent()) {
                    UUID droneIdx = linkedDroneId.get();
                    Entity entity = level.getEntity(droneIdx);
                    if (entity == null || !(entity instanceof DroneEntity)) {
                        ModNetwork.CHANNEL
                            .send(
                                PacketDistributor.PLAYER.with(() -> (ServerPlayer)player),
                                new FpvEntryRejectedPacket(droneIdx, "fpvdrone.status.drone_not_found")
                            );
                        PilotBodyEntity pilotBody = PilotBodyManager.getPilotBody(playerId);
                        if (pilotBody != null && pilotBody.isAlive()) {
                            player.teleportTo(pilotBody.getX(), pilotBody.getY(), pilotBody.getZ());
                        }

                        return;
                    }
                }
            }

            if (serverPlayerToActiveDrone.containsKey(playerId)) {
                UUID droneIdx = serverPlayerToActiveDrone.get(playerId);
                ServerLevel level = ((ServerPlayer)player).level();
                Entity entity = level.getEntity(droneIdx);
                if (entity == null || !(entity instanceof DroneEntity)) {
                    serverPlayerToActiveDrone.remove(playerId);
                    serverArmStates.remove(playerId);
                    PilotBodyEntity pilotBody = PilotBodyManager.getPilotBody(playerId);
                    double returnX = player.getX();
                    double returnY = player.getY();
                    double returnZ = player.getZ();
                    boolean hasPilotBody = pilotBody != null && pilotBody.isAlive();
                    if (hasPilotBody) {
                        returnX = pilotBody.getX();
                        returnY = pilotBody.getY();
                        returnZ = pilotBody.getZ();
                    }

                    PilotBodyManager.onPlayerDisconnect(playerId);
                    ModNetwork.CHANNEL
                        .send(
                            PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new FpvEntryRejectedPacket(droneIdx, "fpvdrone.status.drone_not_found")
                        );
                    if (hasPilotBody) {
                        player.teleportTo(returnX, returnY, returnZ);
                    }
                }
            }
        }
    }

    private void onPlayerLogout(ServerPlayer serverPlayer) {
        if (serverPlayer != null) {
            UUID var10 = serverPlayer.getUUID();
            UUID droneId = serverPlayerToActiveDrone.get(var10);
            ItemStack helmet = serverPlayer.getItemBySlot(EquipmentSlot.HEAD);
            if (helmet.getItem() instanceof FpvGogglesItem) {
                serverPlayer.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                if (!serverPlayer.getInventory().add(helmet)) {
                    serverPlayer.drop(helmet, false, net.minecraft.util.Prediction.SERVER_ONLY);
                }
            }

            if (droneId != null) {
                ServerLevel level = serverPlayer.level();
                if (level.getEntity(droneId) instanceof DroneEntity drone) {
                    drone.setBeingControlled(false);
                    drone.setInFpv(false);
                    drone.setOwner(null);
                    drone.setMotorsArmed(false);
                    drone.releaseChunkLoading();
                }

                releaseServerDroneTicket(droneId, level);
            }

            PilotBodyEntity pilotBody = PilotBodyManager.getPilotBody(var10);
            if (pilotBody != null && pilotBody.isAlive()) {
                serverPlayer.teleportTo(pilotBody.getX(), pilotBody.getY(), pilotBody.getZ());
            }

            serverPlayer.noPhysics = false;
            serverPlayer.getAbilities().flying = false;
            serverPlayer.getAbilities().mayfly = serverPlayer.isCreative() || serverPlayer.isSpectator();
            serverPlayer.onUpdateAbilities();
            PilotBodyManager.onPlayerDisconnect(var10);
            serverPlayerToActiveDrone.remove(var10);
            serverArmStates.remove(var10);
        }
    }

    private void onStartTracking(Entity target, ServerPlayer observer) {
        {
            if (target instanceof ServerPlayer trackedPlayer) {
                {
                    UUID var6 = trackedPlayer.getUUID();
                    if (serverPlayerToActiveDrone.containsKey(var6)) {
                        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> observer), new FpvStatePacket(var6, true));
                    }
                }
            }
        }
    }

    private boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (entity instanceof ServerPlayer player) {
            Optional<UUID> droneIdOpt = getActiveDroneId(player.getUUID());
            if (!droneIdOpt.isEmpty()) {
                if (!source.is(DamageTypes.STARVE) && !source.is(DamageTypes.FELL_OUT_OF_WORLD)) {
                    if (player.level().getEntity(droneIdOpt.get()) instanceof DroneEntity drone && drone.isAlive()) {
                        drone.hurtServer(player.level(), source, amount);
                    }

                    return false;
                }
            }
        }

        return true;
    }

    private boolean allowDeath(LivingEntity entity) {
        if (entity instanceof PilotBodyEntity pilotBody) {
            PilotBodyManager.onPilotBodyDestroyed(pilotBody);
        }

        if (entity instanceof DroneEntity drone) {
            if (drone.isFalling()) {
                drone.setHealth(1.0F);
                drone.deathTime = 0;
                return false;
            }

            onDroneDestroyed(drone.getUUID());
        }

        return true;
    }

    /** Server unload hook; the client side calls this with clientSide=true from FPVClientMod. */
    public static void onEntityLeaveLevel(Entity entity, boolean clientSide) {
        if (entity instanceof DroneEntity drone) {
            if (clientSide) {
                UUID droneId = drone.getUUID();
                if (drone.isFalling() && !FpvStatusChecker.isDroneDestroyed(droneId)) {
                    clientOnDroneDestroyed(droneId);
                }
            } else {
                RemovalReason reason = drone.getRemovalReason();
                if (reason == RemovalReason.KILLED) {
                    onDroneDestroyedServer(drone.getUUID());
                }
            }
        }
    }
}
