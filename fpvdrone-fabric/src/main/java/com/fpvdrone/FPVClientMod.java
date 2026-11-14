package com.fpvdrone;

import com.fpvdrone.client.BatteryData;
import com.fpvdrone.client.BatteryManager;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.DroneConfig;
import com.fpvdrone.client.DroneSoundInstance;
import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.client.SignalManager;
import com.fpvdrone.client.Transforms;
import com.fpvdrone.client.audio.DroneEngineAudioEngine;
import com.fpvdrone.client.audio.ExplosionAudioEngine;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.FallType;
import com.fpvdrone.entity.IVDamageHelper;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.network.BatterySyncPacket;
import com.fpvdrone.network.DroneEntityHitPacket;
import com.fpvdrone.network.DroneExplodePacket;
import com.fpvdrone.network.DroneSignalLostPacket;
import com.fpvdrone.network.DroneSyncPacket;
import com.fpvdrone.network.EnterFpvPacket;
import com.fpvdrone.network.ExitFpvPacket;
import com.fpvdrone.network.ForceChunkPacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.RemoveGogglesPacket;
import com.fpvdrone.network.RequestDronePositionPacket;
import com.fpvdrone.network.SaveDronePositionPacket;
import com.fpvdrone.network.SwitchDronePacket;
import com.fpvdrone.physics.PhysicsState;
import com.fpvdrone.physics.PhysicsUnits;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.blaze3d.platform.InputConstants;
import org.slf4j.LoggerFactory;
import com.fpvdrone.network.compat.ClientChannel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class FPVClientMod implements ClientModInitializer {
    private static FPVClientMod instance;
    private static final Map<UUID, PhysicsState> physicsStates = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> playerToActiveDrone = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> lastKnownDronePositions = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> pendingPositionRequests = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> entityArmStates = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> wasWearingGogglesMap = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> lastLinkedDroneIdMap = new ConcurrentHashMap<>();
    private static final Map<UUID, int[]> forcedDroneChunks = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> shouldRemoveGogglesMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> hasPilotBodyMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3> pilotBodyPositionMap = new ConcurrentHashMap<>();
    private static final Map<UUID, float[]> pilotBodyRotationMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> lastDroneBatteryStateMap = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> batteryRemovedTickMap = new ConcurrentHashMap<>();
    private static final int BATTERY_GRACE_TICKS = 40;
    private static final Map<UUID, float[]> lastKnownDroneRotations = new ConcurrentHashMap<>();
    private static long lastPhysicsTime = System.currentTimeMillis();
    private static int syncCounter = 0;
    private static UUID lastKnownClientPlayer = null;
    private static boolean prevPageUpDown = false;
    private static boolean prevPageDownDown = false;

    @Override
    public void onInitializeClient() {
        instance = this;
        ClientChannel.init();
        com.fpvdrone.client.ClientSetup.init();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> this.onClientStarted());
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> client.execute(FPVClientMod::clearClientState));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> this.onClientDisconnect());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            com.fpvdrone.client.RemoteSmoothingTicker.clientTick();
            this.onClientTick();
            if (client.player != null) {
                this.onPlayerTick(client.player);
            }
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> FPVMod.onEntityLeaveLevel(entity, true));
    }

    /** Called once per rendered frame before the world renders (Forge RenderTickEvent START), from GameRendererMixin. */
    public static void renderTick() {
        if (instance != null) {
            instance.onRenderTick();
        }
        com.fpvdrone.client.RemoteSmoothingTicker.renderTick();
    }

    private void onClientStarted() {
        {
            long window = Minecraft.getInstance().getWindow().handle();
            ControllerReader.getInstance().init(window);
            ExplosionAudioEngine.get().initIfNeeded();
            DroneEngineAudioEngine.get().initIfNeeded();
            FpvConfig.setMinecraftLanguage(Minecraft.getInstance().getLanguageManager().getSelected());
        }
    }

    public static void clearClientState() {
        entityArmStates.clear();
        physicsStates.clear();
        playerToActiveDrone.clear();
        lastKnownDronePositions.clear();
        pendingPositionRequests.clear();
        wasWearingGogglesMap.clear();
        lastLinkedDroneIdMap.clear();
        forcedDroneChunks.clear();
        hasPilotBodyMap.clear();
        pilotBodyPositionMap.clear();
        pilotBodyRotationMap.clear();
        shouldRemoveGogglesMap.clear();
        lastDroneBatteryStateMap.clear();
        batteryRemovedTickMap.clear();
        lastKnownDroneRotations.clear();
        FpvStatusChecker.clearAll();
        SignalManager.getInstance().reset();
        BatteryManager.getInstance().fullReset();
        com.fpvdrone.render.LowResRenderer.getInstance().setEnabled(false);

        lastPhysicsTime = System.currentTimeMillis();
        syncCounter = 0;
        lastKnownClientPlayer = null;
    }

    public static boolean isArmed(UUID playerId) {
        return entityArmStates.getOrDefault(playerId, false);
    }

    public static void setArmed(UUID playerId, boolean armed) {
        entityArmStates.put(playerId, armed);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(playerId)) {
            setArmed(mc.player, armed);
        }
    }

    public static void setArmed(Player player, boolean armed) {
        UUID playerId = player.getUUID();
        entityArmStates.put(playerId, armed);
        if (armed) {
            lastPhysicsTime = System.currentTimeMillis();
            if (player.level().isClientSide()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.player.getUUID().equals(playerId)) {
                    mc.getSoundManager().play(new DroneSoundInstance(mc.player));
                }
            }
        } else if (!playerToActiveDrone.containsKey(playerId)) {
            physicsStates.remove(playerId);
        }
    }

    public static Optional<UUID> getActiveDroneId(UUID playerId) {
        return Optional.ofNullable(playerToActiveDrone.get(playerId));
    }

    public static Optional<DroneEntity> getActiveDrone(UUID playerId) {
        UUID droneId = playerToActiveDrone.get(playerId);
        if (droneId == null) {
            return Optional.empty();
        } else {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return Optional.empty();
            } else {
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof DroneEntity drone && drone.getUUID().equals(droneId)) {
                        return Optional.of(drone);
                    }
                }

                return Optional.empty();
            }
        }
    }

    public static PhysicsState getPhysicsState(UUID playerId) {
        return physicsStates.get(playerId);
    }

    public static PhysicsState getOrCreatePhysicsState(UUID playerId, Player player) {
        return physicsStates.computeIfAbsent(playerId, id -> {
            PhysicsState state = new PhysicsState(player);
            setupKamikazeCallback(playerId, state);
            setupEntityHitCallback(playerId, state);
            return state;
        });
    }

    public static void markShouldRemoveGoggles(UUID playerId) {
        shouldRemoveGogglesMap.put(playerId, true);
    }

    public static boolean hasActiveDrone(UUID playerId) {
        return playerToActiveDrone.containsKey(playerId);
    }

    public static boolean isActiveDrone(UUID droneId) {
        return playerToActiveDrone.containsValue(droneId);
    }

    @Nullable
    public static Vec3 getPilotBodyPosition(UUID playerId) {
        return pilotBodyPositionMap.get(playerId);
    }

    @Nullable
    public static float[] getLastKnownDroneRotation(UUID droneId) {
        return lastKnownDroneRotations.get(droneId);
    }

    public static void enterFPVMode(Player player, DroneEntity drone) {
        UUID playerId = player.getUUID();
        UUID droneId = drone.getUUID();
        playerToActiveDrone.put(playerId, droneId);
        drone.setOwner(playerId);
        drone.setBeingControlled(true);
        PhysicsState physics = getOrCreatePhysicsState(playerId, player);
        applyDroneConfig(drone, physics);
        physics.initFromDrone(drone);
        BatteryManager.getInstance().initFromDrone(drone);
        Vec3 pilotPos = pilotBodyPositionMap.get(playerId);
        SignalManager.getInstance().reset();
        SignalManager.getInstance().setOrigin(pilotPos != null ? pilotPos : player.position());

        setArmed(player, true);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(playerId)) {
            mc.player.refreshDimensions();
        }
    }

    private static void applyDroneConfig(DroneEntity drone, PhysicsState physics) {
        DroneConfig config = drone.getConfig();
        BatteryData battery = drone.getBattery();

        for (int ch = 0; ch < 4; ch++) {
            physics.setRcRate(ch, config.getRcRate(ch));
            physics.setSuperRate(ch, config.getSuperRate(ch));
            physics.setExpo(ch, config.getExpo(ch));
        }

        float totalWeightKg = drone.getTotalWeight() / 1000.0F;
        physics.setMass(totalWeightKg);
        physics.setMotorKv(config.getMotorKv());
        physics.setPropDiameter(config.getPropDiameter() * 0.0254F);
        physics.setPropPitch(config.getPropPitch() * 0.0254F);
        physics.setDragCoefficient(config.getDragCoefficient());
        physics.setThrustMultiplier(config.getThrustMultiplier());
        physics.setFlightMode3d(config.isFlightMode3d());
        physics.setBatteryCells(battery.getCells());
    }

    public static void finalizeDroneState(Player player) {
        UUID playerId = player.getUUID();
        UUID droneId = playerToActiveDrone.get(playerId);
        if (droneId != null && !FpvStatusChecker.isDroneDestroyed(droneId)) {
            FpvStatusChecker.clearDroneState(droneId);
        }

        double droneX = player.getX();
        double droneY = player.getY();
        double droneZ = player.getZ();
        float yaw = 0.0F;
        float pitch = 0.0F;
        float roll = 0.0F;
        double vx = 0.0;
        double vy = 0.0;
        double vz = 0.0;
        PhysicsState physics = getPhysicsState(playerId);
        if (physics != null) {
            Vector3f look = physics.getDroneLook();
            Vector3f up = physics.getDroneUp();
            float[] angles = Transforms.getWorldEulerAngles(look, up);
            yaw = angles[0];
            pitch = angles[1];
            roll = angles[2];
            if (droneId != null) {
                lastKnownDroneRotations.put(droneId, new float[]{yaw, pitch, roll});
            }

            Vector3f vel = physics.getVelocity();
            Vec3 mcVel = PhysicsUnits.toMinecraftVelocity(vel);
            vx = mcVel.x;
            vy = mcVel.y;
            vz = mcVel.z;
        }

        float finalYaw = yaw;
        float finalPitch = pitch;
        float finalRoll = roll;
        double finalVx = vx;
        double finalVy = vy;
        double finalVz = vz;
        if (droneId != null && player.level().isClientSide()) {
            Optional<DroneEntity> droneOpt = findDroneById(player, droneId);
            droneOpt.ifPresent(drone -> {
                drone.setPos(droneX, droneY, droneZ);
                drone.setOldPosAndRot();
                drone.setYRot(finalYaw);
                drone.setXRot(finalPitch);
                drone.setDroneRoll(finalRoll);
                drone.setYBodyRot(finalYaw);
                drone.yBodyRotO = finalYaw;
                drone.setDeltaMovement(finalVx, finalVy, finalVz);
                drone.setInFpv(false);
                drone.setBeingControlled(false);
            });
            ModNetwork.CHANNEL
                .sendToServer(new DroneSyncPacket(droneId, droneX, droneY, droneZ, finalYaw, finalPitch, finalRoll, finalVx, finalVy, finalVz, false, false));
            float clientUsedMah = BatteryManager.getInstance().getUsedMahFloat();
            ModNetwork.CHANNEL.sendToServer(new BatterySyncPacket(droneId, clientUsedMah));
        }

        setArmed(player, false);
    }

    public static void onDroneDestroyed(UUID droneId) {
        if (!FpvStatusChecker.isDroneDestroyed(droneId)) {
            FpvStatusChecker.markDroneDestroyed(droneId);
            lastKnownDronePositions.remove(droneId);
            playerToActiveDrone.forEach((playerId, activeDroneId) -> {
                if (activeDroneId.equals(droneId)) {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null && mc.player.getUUID().equals(playerId)) {
                        setArmed(mc.player, false);
                    }

                    SignalManager.getInstance().forceNoSignal();
                    if (com.fpvdrone.video.FrozenFrameManager.getInstance().hasFrozenFrame(droneId)) {
                        com.fpvdrone.render.LowResRenderer.getInstance().setEnabled(true);
                        com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameMode(true);
                        com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameDroneId(droneId);
                    }
                }
            });
        }
    }

    public static void onSignalLossDestroyed(UUID droneId) {
        if (!FpvStatusChecker.isDroneDestroyed(droneId)) {
            FpvStatusChecker.markDroneDestroyed(droneId);
            lastKnownDronePositions.remove(droneId);
            playerToActiveDrone.forEach((playerId, activeDroneId) -> {
                if (activeDroneId.equals(droneId)) {
                    SignalManager.getInstance().forceNoSignal();
                    if (com.fpvdrone.video.FrozenFrameManager.getInstance().hasFrozenFrame(droneId)) {
                        com.fpvdrone.render.LowResRenderer.getInstance().setEnabled(true);
                        com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameMode(true);
                        com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameDroneId(droneId);
                    }
                }
            });
        }
    }

    private static void setupKamikazeCallback(UUID playerId, PhysicsState physics) {
        physics.setOnCollision(impactSpeed -> {
            if (!physics.isFalling()) {
                getActiveDrone(playerId).ifPresent(drone -> {
                    if (drone.hasRpg()) {
                        if (!(impactSpeed < 2.0F)) {
                            float power = FpvConfig.getKamikazeExplosionPower();
                            int fragments = FpvConfig.getShrapnelCount();
                            ModNetwork.CHANNEL.sendToServer(new DroneExplodePacket(drone.getUUID(), power, fragments));
                        }
                    }
                });
            }
        });
    }

    private static void setupEntityHitCallback(UUID playerId, PhysicsState physics) {
        physics.setOnEntityHit(
            (targetId, speed, velX, velY, velZ, posX, posY, posZ) -> {
                if (!physics.isFalling()) {
                    getActiveDrone(playerId)
                        .ifPresent(
                            drone -> {
                                if (drone.hasRpg() && speed >= 2.0F) {
                                    float power = FpvConfig.getKamikazeExplosionPower();
                                    int fragments = FpvConfig.getShrapnelCount();
                                    Minecraft mc = Minecraft.getInstance();
                                    UUID vehicleHitId = null;
                                    if (mc.level != null && targetId != null) {
                                        Entity hitEntity = (Entity)mc.level
                                            .getEntitiesOfClass(Entity.class, drone.getBoundingBox().inflate(4.0), e -> e.getUUID().equals(targetId))
                                            .stream()
                                            .findFirst()
                                            .orElse(null);
                                        if (hitEntity != null && IVDamageHelper.isIVEntity(hitEntity)) {
                                            vehicleHitId = targetId;
                                        }
                                    }

                                    ModNetwork.CHANNEL.sendToServer(new DroneExplodePacket(drone.getUUID(), power, fragments, vehicleHitId));
                                } else {
                                    ModNetwork.CHANNEL
                                        .sendToServer(new DroneEntityHitPacket(drone.getUUID(), targetId, speed, velX, velY, velZ, posX, posY, posZ));
                                }
                            }
                        );
                }
            }
        );
    }

    private void handleGogglesEquipState(Player player) {
        UUID playerId = player.getUUID();
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean wearingGoggles = helmet.getItem() instanceof FpvGogglesItem;
        Optional<UUID> linkedDroneId = wearingGoggles ? FpvGogglesItem.getLinkedDroneId(helmet) : Optional.empty();
        boolean wasWearing = wasWearingGogglesMap.getOrDefault(playerId, false);
        UUID lastLinked = lastLinkedDroneIdMap.get(playerId);
        boolean inActiveDrone = playerToActiveDrone.containsKey(playerId);
        if (shouldRemoveGogglesMap.getOrDefault(playerId, false)) {
            removeGoggles(player);
            shouldRemoveGogglesMap.remove(playerId);
            wasWearingGogglesMap.put(playerId, false);
        } else if (wearingGoggles && !inActiveDrone && linkedDroneId.isEmpty()) {
            if (!wasWearing) {
                player.sendOverlayMessage(Component.translatable("fpvdrone.goggles.equip_blocked.no_link").withStyle(Style.EMPTY.withColor(16749622)));
            }

            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            if (!player.getInventory().add(helmet)) {
                player.drop(helmet, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }

            wasWearingGogglesMap.put(playerId, false);
        } else {
            if (wearingGoggles && !wasWearing) {
                if (linkedDroneId.isPresent()) {
                    UUID droneId = linkedDroneId.get();
                    lastLinkedDroneIdMap.put(playerId, droneId);
                    if (!playerToActiveDrone.containsKey(playerId)) {
                        this.tryEnterGogglesMode(player, helmet, droneId);
                    }
                }
            } else if (!wearingGoggles && wasWearing) {
                if (lastLinked != null && !FpvStatusChecker.isDroneDestroyed(lastLinked)) {
                    Vec3 dronePos = player.position();
                    lastKnownDronePositions.put(lastLinked, dronePos);
                    ModNetwork.CHANNEL.sendToServer(new SaveDronePositionPacket(lastLinked, dronePos.x, dronePos.y, dronePos.z));
                }

                exitGogglesMode(player);
                lastLinkedDroneIdMap.remove(playerId);
            } else if (wearingGoggles && linkedDroneId.isPresent()) {
                UUID droneId = linkedDroneId.get();
                if (lastLinked != null && !droneId.equals(lastLinked)) {
                    if (playerToActiveDrone.containsKey(playerId)) {
                        if (switchDrone(player, lastLinked, droneId)) {
                            lastLinkedDroneIdMap.put(playerId, droneId);
                        }
                    } else {
                        lastLinkedDroneIdMap.put(playerId, droneId);
                        this.tryEnterGogglesMode(player, helmet, droneId);
                    }
                } else if (!playerToActiveDrone.containsKey(playerId)) {
                    this.tryEnterGogglesMode(player, helmet, droneId);
                }
            }

            wasWearingGogglesMap.put(playerId, wearingGoggles);
        }
    }

    private static void removeGoggles(Player player) {
        UUID playerId = player.getUUID();
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof FpvGogglesItem) {
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            if (!player.getInventory().add(helmet)) {
                player.drop(helmet, false, net.minecraft.util.Prediction.SERVER_ONLY);
            }
        }

        if (isArmed(playerId)) {
            finalizeDroneState(player);
        }

        Vec3 savedPos = pilotBodyPositionMap.remove(playerId);
        if (savedPos != null) {
            player.setPos(savedPos.x, savedPos.y, savedPos.z);
            player.setOldPosAndRot();
            player.fallDistance = 0.0F;
        }

        float[] savedRot = pilotBodyRotationMap.remove(playerId);
        if (savedRot != null) {
            player.setYRot(savedRot[0]);
            player.setXRot(savedRot[1]);
            player.yRotO = savedRot[0];
            player.xRotO = savedRot[1];
            player.setYHeadRot(savedRot[0]);
        }

        int[] chunk = forcedDroneChunks.remove(playerId);
        if (chunk != null) {
            ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(chunk[0], chunk[1], false));
        }

        playerToActiveDrone.remove(playerId);
        physicsStates.remove(playerId);
        hasPilotBodyMap.remove(playerId);
        lastLinkedDroneIdMap.remove(playerId);
        player.noPhysics = false;
        com.fpvdrone.render.LowResRenderer.getInstance().setEnabled(false);
        com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameMode(false);
        SignalManager.getInstance().reset();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(playerId)) {
            mc.player.refreshDimensions();
        }
    }

    private void tryEnterGogglesMode(Player player, ItemStack goggles, UUID droneId) {
        if (FpvStatusChecker.isDroneDestroyed(droneId)) {
            enterNoSignalMode(player, droneId);
        } else {
            Optional<DroneEntity> droneOpt = findDroneById(player, droneId);
            if (droneOpt.isPresent()) {
                DroneEntity drone = droneOpt.get();
                if (!drone.hasBattery()) {
                    player.sendOverlayMessage(Component.translatable("fpvdrone.status.no_battery").withStyle(Style.EMPTY.withColor(16749622)));
                    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    if (!player.getInventory().add(goggles)) {
                        player.drop(goggles, false, net.minecraft.util.Prediction.SERVER_ONLY);
                    }
                } else {
                    enterGogglesMode(player, drone);
                }
            } else {
                Vec3 lastPos = lastKnownDronePositions.get(droneId);
                if (lastPos != null) {
                    enterGogglesModeAtPosition(player, droneId, lastPos);
                } else {
                    if (!pendingPositionRequests.getOrDefault(droneId, false)) {
                        pendingPositionRequests.put(droneId, true);
                        ModNetwork.CHANNEL.sendToServer(new RequestDronePositionPacket(droneId));
                    }
                }
            }
        }
    }

    public static void onDroneNotFound(UUID droneId) {
        pendingPositionRequests.remove(droneId);
        FpvStatusChecker.markDroneDestroyed(droneId);
    }

    private static void enterNoSignalMode(Player player, UUID droneId) {
        UUID playerId = player.getUUID();
        playerToActiveDrone.put(playerId, droneId);
        SignalManager.getInstance().forceNoSignal();
        if (com.fpvdrone.video.FrozenFrameManager.getInstance().hasFrozenFrame(droneId)) {
            com.fpvdrone.render.LowResRenderer.getInstance().setEnabled(true);
            com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameMode(true);
            com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameDroneId(droneId);
        }
    }

    public static void onDronePositionReceived(UUID droneId, double x, double y, double z) {
        pendingPositionRequests.remove(droneId);
        Vec3 pos = new Vec3(x, y, z);
        lastKnownDronePositions.put(droneId, pos);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            UUID playerId = mc.player.getUUID();
            UUID activeDrone = playerToActiveDrone.get(playerId);
            if (activeDrone != null && activeDrone.equals(droneId)) {
                mc.player.setPos(x, y, z);
                mc.player.setOldPosAndRot();
                int chunkX = (int)Math.floor(x) >> 4;
                int chunkZ = (int)Math.floor(z) >> 4;
                int[] oldChunk = forcedDroneChunks.get(playerId);
                if (oldChunk != null && (oldChunk[0] != chunkX || oldChunk[1] != chunkZ)) {
                    ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(oldChunk[0], oldChunk[1], false));
                }

                forcedDroneChunks.put(playerId, new int[]{chunkX, chunkZ});
                ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(chunkX, chunkZ, true));
            } else {
                ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.getItem() instanceof FpvGogglesItem) {
                    Optional<UUID> linkedId = FpvGogglesItem.getLinkedDroneId(helmet);
                    if (linkedId.isPresent() && linkedId.get().equals(droneId) && !playerToActiveDrone.containsKey(playerId)) {
                        enterGogglesModeAtPosition(mc.player, droneId, pos);
                    }
                }
            }
        }
    }

    private static void enterGogglesMode(Player player, DroneEntity drone) {
        UUID playerId = player.getUUID();
        UUID droneId = drone.getUUID();
        com.fpvdrone.render.LowResRenderer.getInstance().invalidate();
        FpvStatusChecker.clearDroneState(droneId);
        pilotBodyPositionMap.put(playerId, player.position());
        pilotBodyRotationMap.put(playerId, new float[]{player.getYRot(), player.getXRot()});
        playerToActiveDrone.put(playerId, droneId);
        hasPilotBodyMap.put(playerId, true);
        ModNetwork.CHANNEL.sendToServer(new EnterFpvPacket(droneId));
        SignalManager.getInstance().reset();
        SignalManager.getInstance().setOrigin(player.position());
        BatteryManager.getInstance().initFromDrone(drone);
        player.setPos(drone.getX(), drone.getY(), drone.getZ());
        player.setOldPosAndRot();
        player.noPhysics = true;
        player.getAbilities().flying = true;
        player.getAbilities().mayfly = true;
        player.fallDistance = 0.0F;
        player.setDeltaMovement(0.0, 0.0, 0.0);

        com.fpvdrone.render.LowResRenderer.getInstance().setEnabled(true);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(playerId)) {
            mc.player.refreshDimensions();
        }
    }

    private static void enterGogglesModeAtPosition(Player player, UUID droneId, Vec3 position) {
        UUID playerId = player.getUUID();
        com.fpvdrone.render.LowResRenderer.getInstance().invalidate();
        FpvStatusChecker.clearDroneState(droneId);
        pilotBodyPositionMap.put(playerId, player.position());
        pilotBodyRotationMap.put(playerId, new float[]{player.getYRot(), player.getXRot()});
        playerToActiveDrone.put(playerId, droneId);
        hasPilotBodyMap.put(playerId, true);
        ModNetwork.CHANNEL.sendToServer(new EnterFpvPacket(droneId));
        SignalManager.getInstance().reset();
        SignalManager.getInstance().setOrigin(player.position());
        BatteryManager.getInstance().fullReset();
        int chunkX = (int)Math.floor(position.x) >> 4;
        int chunkZ = (int)Math.floor(position.z) >> 4;
        forcedDroneChunks.put(playerId, new int[]{chunkX, chunkZ});
        ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(chunkX, chunkZ, true));
        player.setPos(position.x, position.y, position.z);
        player.setOldPosAndRot();
        player.noPhysics = true;
        player.getAbilities().flying = true;
        player.getAbilities().mayfly = true;
        player.fallDistance = 0.0F;
        player.setDeltaMovement(0.0, 0.0, 0.0);
        com.fpvdrone.render.LowResRenderer.getInstance().setEnabled(true);
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(playerId)) {
            mc.player.refreshDimensions();
        }
    }

    private static boolean switchDrone(Player player, UUID oldDroneId, UUID newDroneId) {
        UUID playerId = player.getUUID();
        Optional<DroneEntity> newDroneCheck = findDroneById(player, newDroneId);
        if (newDroneCheck.isPresent() && !newDroneCheck.get().hasBattery()) {
            return false;
        } else {
            if (isArmed(playerId)) {
                finalizeDroneState(player);
            }

            playerToActiveDrone.put(playerId, newDroneId);
            ModNetwork.CHANNEL.sendToServer(new SwitchDronePacket(oldDroneId, newDroneId));
            int[] chunk = forcedDroneChunks.remove(playerId);
            if (chunk != null) {
                ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(chunk[0], chunk[1], false));
            }

            physicsStates.remove(playerId);
            Vec3 pilotPos = pilotBodyPositionMap.get(playerId);
            if (pilotPos != null) {
                SignalManager.getInstance().setOrigin(pilotPos);
            }

            Optional<DroneEntity> newDroneOpt = findDroneById(player, newDroneId);
            if (newDroneOpt.isPresent()) {
                DroneEntity newDrone = newDroneOpt.get();
                player.setPos(newDrone.getX(), newDrone.getY(), newDrone.getZ());
                player.setOldPosAndRot();
                BatteryManager.getInstance().initFromDrone(newDrone);
            } else {
                Vec3 lastPos = lastKnownDronePositions.get(newDroneId);
                if (lastPos != null) {
                    player.setPos(lastPos.x, lastPos.y, lastPos.z);
                    player.setOldPosAndRot();
                    int chunkX = (int)Math.floor(lastPos.x) >> 4;
                    int chunkZ = (int)Math.floor(lastPos.z) >> 4;
                    forcedDroneChunks.put(playerId, new int[]{chunkX, chunkZ});
                    ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(chunkX, chunkZ, true));
                } else if (!pendingPositionRequests.getOrDefault(newDroneId, false)) {
                    pendingPositionRequests.put(newDroneId, true);
                    ModNetwork.CHANNEL.sendToServer(new RequestDronePositionPacket(newDroneId));
                }
            }

            com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameMode(false);
            if (!FpvStatusChecker.isDroneDestroyed(oldDroneId)) {
                com.fpvdrone.video.FrozenFrameManager.getInstance().clearFrame(oldDroneId);
                FpvStatusChecker.clearDroneState(oldDroneId);
            }

            SignalManager.getInstance().reset();
            if (pilotPos != null) {
                SignalManager.getInstance().setOrigin(pilotPos);
            }

            if (FpvStatusChecker.isDroneDestroyed(newDroneId)) {
                SignalManager.getInstance().forceNoSignal();
                if (com.fpvdrone.video.FrozenFrameManager.getInstance().hasFrozenFrame(newDroneId)) {
                    com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameMode(true);
                    com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameDroneId(newDroneId);
                }
            }

            boolean newDroneHasBattery = newDroneCheck.map(DroneEntity::hasBattery).orElse(false);
            lastDroneBatteryStateMap.put(newDroneId, newDroneHasBattery);
            return true;
        }
    }

    private static void exitGogglesMode(Player player) {
        UUID playerId = player.getUUID();
        if (isArmed(playerId)) {
            finalizeDroneState(player);
        }

        Vec3 savedPos = pilotBodyPositionMap.remove(playerId);
        if (savedPos != null) {
            player.setPos(savedPos.x, savedPos.y, savedPos.z);
            player.setOldPosAndRot();
            player.fallDistance = 0.0F;
        }

        float[] savedRot = pilotBodyRotationMap.remove(playerId);
        if (savedRot != null) {
            player.setYRot(savedRot[0]);
            player.setXRot(savedRot[1]);
            player.yRotO = savedRot[0];
            player.xRotO = savedRot[1];
            player.setYHeadRot(savedRot[0]);
        }

        UUID droneId = playerToActiveDrone.get(playerId);
        if (droneId != null) {
            FpvStatusChecker.clearDroneState(droneId);
        }

        int[] chunk = forcedDroneChunks.remove(playerId);
        if (chunk != null && player.level().isClientSide()) {
            ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(chunk[0], chunk[1], false));
        }

        playerToActiveDrone.remove(playerId);
        physicsStates.remove(playerId);
        boolean hadPilotBody = hasPilotBodyMap.getOrDefault(playerId, false);
        hasPilotBodyMap.remove(playerId);
        if (player.level().isClientSide() && hadPilotBody && droneId != null) {
            ModNetwork.CHANNEL.sendToServer(new ExitFpvPacket(droneId));
        }

        player.noPhysics = false;
        // Leaving a frozen frame waits two frames, so the last one is still shown while the view switches back.
        boolean wasFrozenFrame = com.fpvdrone.render.LowResRenderer.getInstance().isFrozenFrameMode();
        if (wasFrozenFrame) {
            Minecraft mc2 = Minecraft.getInstance();
            mc2.execute(() -> mc2.execute(() -> {
                com.fpvdrone.render.LowResRenderer.getInstance().setEnabled(false);
                com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameMode(false);
            }));
        } else {
            com.fpvdrone.render.LowResRenderer.getInstance().setEnabled(false);
            com.fpvdrone.render.LowResRenderer.getInstance().setFrozenFrameMode(false);
        }
        SignalManager.getInstance().reset();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.getUUID().equals(playerId)) {
            mc.player.refreshDimensions();
        }
    }

    public static Optional<DroneEntity> findDroneByIdForClient(UUID droneId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof DroneEntity drone && drone.getUUID().equals(droneId)) {
                    return Optional.of(drone);
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<DroneEntity> findDroneById(Player player, UUID droneId) {
        if (player.level().isClientSide()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof DroneEntity drone && drone.getUUID().equals(droneId)) {
                        return Optional.of(drone);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private void onClientDisconnect() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            UUID playerId = mc.player.getUUID();
            if (playerToActiveDrone.containsKey(playerId)) {
                UUID droneId = playerToActiveDrone.get(playerId);
                if (droneId != null) {
                    PhysicsState physics = getPhysicsState(playerId);
                    double x = mc.player.getX();
                    double y = mc.player.getY();
                    double z = mc.player.getZ();
                    float yaw = 0.0F;
                    float pitch = 0.0F;
                    float roll = 0.0F;
                    double vx = 0.0;
                    double vy = 0.0;
                    double vz = 0.0;
                    if (physics != null) {
                        Vector3f look = physics.getDroneLook();
                        Vector3f up = physics.getDroneUp();
                        float[] angles = Transforms.getWorldEulerAngles(look, up);
                        yaw = angles[0];
                        pitch = angles[1];
                        roll = angles[2];
                        Vector3f vel = physics.getVelocity();
                        Vec3 mcVel = PhysicsUnits.toMinecraftVelocity(vel);
                        vx = mcVel.x;
                        vy = mcVel.y;
                        vz = mcVel.z;
                    }

                    try {
                        ModNetwork.CHANNEL.sendToServer(new DroneSyncPacket(droneId, x, y, z, yaw, pitch, roll, vx, vy, vz, false, false));
                        ModNetwork.CHANNEL.sendToServer(new RemoveGogglesPacket(droneId));
                        ModNetwork.CHANNEL.sendToServer(new ExitFpvPacket(droneId));
                    } catch (Exception var26) {
                        LoggerFactory.getLogger(FPVClientMod.class).warn("Failed to send disconnect packets", var26);
                    }
                }
            }
        }

        ExplosionAudioEngine.get().onDisconnect();
        DroneEngineAudioEngine.get().onDisconnect();
        clearClientState();
    }

    private static int frozenFramePruneCounter = 0;

    /** Every 100 ticks, frozen frames of drones no longer around are released. */
    private static void pruneFrozenFramesTick() {
        if (++frozenFramePruneCounter >= 100) {
            frozenFramePruneCounter = 0;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                java.util.Set<UUID> retain = new java.util.HashSet<>(playerToActiveDrone.values());
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof DroneEntity) {
                        retain.add(entity.getUUID());
                    }
                }
                com.fpvdrone.video.FrozenFrameManager.getInstance().pruneStale(retain);
            }
        }
    }

    private void onClientTick() {
        {
            ExplosionAudioEngine.get().tick();
            DroneEngineAudioEngine.get().tick();
            com.fpvdrone.client.DroneSurfHandler.onClientTick();
            pruneFrozenFramesTick();
        }
    }

    private void onPlayerTick(Player player) {
        {
            UUID playerId = player.getUUID();
            if (player.level().isClientSide()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.player.getUUID().equals(playerId)) {
                    if (lastKnownClientPlayer == null || !lastKnownClientPlayer.equals(playerId)) {
                        clearClientState();
                        lastKnownClientPlayer = playerId;
                    }

                    this.handleGogglesEquipState(player);

                    UUID linkedDroneId = null;
                    ItemStack helmetForBattery = player.getItemBySlot(EquipmentSlot.HEAD);
                    if (helmetForBattery.getItem() instanceof FpvGogglesItem) {
                        linkedDroneId = FpvGogglesItem.getLinkedDroneId(helmetForBattery).orElse(null);
                    }

                    if (linkedDroneId == null) {
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (stack.getItem() instanceof FpvGogglesItem) {
                                linkedDroneId = FpvGogglesItem.getLinkedDroneId(stack).orElse(null);
                                if (linkedDroneId != null) {
                                    break;
                                }
                            }
                        }
                    }

                    if (linkedDroneId != null) {
                        DroneEntity linkedDrone = null;
                        if (mc.level != null) {
                            for (Entity entity : mc.level.entitiesForRendering()) {
                                if (entity instanceof DroneEntity d && d.getUUID().equals(linkedDroneId)) {
                                    linkedDrone = d;
                                    break;
                                }
                            }
                        }

                        if (linkedDrone != null) {
                            boolean currentBattery = linkedDrone.hasBattery();
                            Boolean previousBattery = lastDroneBatteryStateMap.get(linkedDroneId);
                            if (previousBattery != null && linkedDrone.tickCount >= 3) {
                                if (currentBattery && !previousBattery) {
                                } else if (!currentBattery && previousBattery) {
                                    BatteryManager.getInstance().fullReset();
                                }

                                lastDroneBatteryStateMap.put(linkedDroneId, currentBattery);
                            } else {
                                lastDroneBatteryStateMap.put(linkedDroneId, currentBattery);
                            }

                            if (previousBattery != null
                                && previousBattery
                                && !currentBattery
                                && playerToActiveDrone.containsKey(playerId)
                                && playerToActiveDrone.get(playerId).equals(linkedDroneId)) {
                                batteryRemovedTickMap.put(linkedDroneId, 0);
                            }

                            if (previousBattery != null && !previousBattery && currentBattery) {
                                batteryRemovedTickMap.remove(linkedDroneId);
                            }

                            Integer graceTick = batteryRemovedTickMap.get(linkedDroneId);
                            if (graceTick != null) {
                                if (graceTick >= 40) {
                                    ItemStack helmetStack = player.getItemBySlot(EquipmentSlot.HEAD);
                                    if (helmetStack.getItem() instanceof FpvGogglesItem) {
                                        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                                        if (!player.getInventory().add(helmetStack)) {
                                            player.drop(helmetStack, false, net.minecraft.util.Prediction.SERVER_ONLY);
                                        }
                                    }

                                    exitGogglesMode(player);
                                    batteryRemovedTickMap.remove(linkedDroneId);
                                } else {
                                    batteryRemovedTickMap.put(linkedDroneId, graceTick + 1);
                                }
                            }
                        }
                    }

                    ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                    boolean wearingGoggles = helmet.getItem() instanceof FpvGogglesItem && FpvGogglesItem.getLinkedDroneId(helmet).isPresent();
                    if (wearingGoggles && playerToActiveDrone.containsKey(playerId)) {
                        UUID droneId = playerToActiveDrone.get(playerId);
                        if (droneId != null && !FpvStatusChecker.isDroneDestroyed(droneId)) {
                            player.noPhysics = true;
                            player.getAbilities().flying = true;
                            player.getAbilities().mayfly = true;
                            player.fallDistance = 0.0F;
                        } else if (droneId != null && FpvStatusChecker.isDroneDestroyed(droneId)) {
                            player.noPhysics = true;
                            player.getAbilities().flying = true;
                            player.getAbilities().mayfly = true;
                            player.fallDistance = 0.0F;
                        }
                    }
                }
            }
        }
    }

    private void onRenderTick() {
        {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (player != null && player.level().isClientSide()) {
                UUID playerId = player.getUUID();
                if (!minecraft.isPaused() && minecraft.gui.screen() == null) {
                    ControllerReader.getInstance().poll();
                    ControllerReader.getInstance().handleArmToggle(playerId, null);
                    if (playerToActiveDrone.containsKey(playerId)) {
                        boolean pageUpNow = InputConstants.isKeyDown(InputConstants.KEY_PAGEUP);
                        boolean pageDownNow = InputConstants.isKeyDown(InputConstants.KEY_PAGEDOWN);
                        if (pageUpNow && !prevPageUpDown) {
                            float angle = Math.min(60.0F, FpvConfig.getDefaultAngle() + 1.0F);
                            FpvConfig.setDefaultAngle(angle);
                            player.sendOverlayMessage(Component.literal("Camera Angle: " + (int)angle + "°").withStyle(Style.EMPTY.withColor(16749622)));
                        }

                        if (pageDownNow && !prevPageDownDown) {
                            float angle = Math.max(-60.0F, FpvConfig.getDefaultAngle() - 1.0F);
                            FpvConfig.setDefaultAngle(angle);
                            player.sendOverlayMessage(Component.literal("Camera Angle: " + (int)angle + "°").withStyle(Style.EMPTY.withColor(16749622)));
                        }

                        prevPageUpDown = pageUpNow;
                        prevPageDownDown = pageDownNow;
                    }

                    PhysicsState physics = getPhysicsState(playerId);
                    if (isArmed(playerId)) {
                        long currentTime = System.currentTimeMillis();
                        int elapsedMs = (int)(currentTime - lastPhysicsTime);
                        float elapsed = Math.min(Math.max(elapsedMs / 1000.0F, 0.0F), 0.1F);
                        lastPhysicsTime = currentTime;
                        if (physics != null) {
                            this.updateSignalState(player);
                            Optional<DroneEntity> droneOpt = getActiveDrone(playerId);
                            droneOpt.ifPresent(drone -> {
                                float totalWeightKg = drone.getTotalWeight() / 1000.0F;
                                physics.setMass(totalWeightKg);
                            });
                            if (!SignalManager.getInstance().isControlBlocked()) {
                                physics.tick(elapsed);
                            } else if (SignalManager.getInstance().isForcedNoSignal()) {
                                physics.setMotorsArmed(false);
                                physics.tick(elapsed);
                            } else {
                                this.applyFailsafe(player, physics);
                            }

                            this.syncDronePosition(playerId, player);
                        }
                    } else {
                        if (physics != null) {
                            Optional<UUID> droneId = getActiveDroneId(playerId);
                            boolean frozen = droneId.isPresent() && FpvStatusChecker.hasNoSignalState(droneId.get());
                            boolean batteryDead = frozen && SignalManager.getInstance().isForcedNoSignal();
                            long currentTime = System.currentTimeMillis();
                            float elapsed = Math.min((float)(currentTime - lastPhysicsTime) / 1000.0F, 0.1F);
                            lastPhysicsTime = currentTime;
                            if (!frozen || batteryDead) {
                                physics.tick(elapsed);
                                this.syncDronePosition(playerId, player);
                            }
                        } else {
                            Optional<UUID> droneId = getActiveDroneId(playerId);
                            if (droneId.isPresent() && !FpvStatusChecker.hasNoSignalState(droneId.get())) {
                                Optional<DroneEntity> droneOpt = getActiveDrone(playerId);
                                if (droneOpt.isPresent()) {
                                    DroneEntity drone = droneOpt.get();
                                    player.setPos(drone.getX(), drone.getY(), drone.getZ());
                                    player.setOldPosAndRot();
                                    PhysicsState newPhysics = getOrCreatePhysicsState(playerId, player);
                                    newPhysics.initFromDrone(drone);
                                    applyDroneConfig(drone, newPhysics);
                                    BatteryManager.getInstance().initFromDrone(drone);
                                }
                            }
                        }
                    }
                } else {
                    PhysicsState pausedPhysics = getPhysicsState(playerId);
                    if (pausedPhysics != null) {
                        Optional<UUID> droneId = getActiveDroneId(playerId);
                        boolean frozen = droneId.isPresent() && FpvStatusChecker.hasNoSignalState(droneId.get());
                        boolean batteryDead = frozen && SignalManager.getInstance().isForcedNoSignal();
                        long currentTime = System.currentTimeMillis();
                        float elapsed = Math.min((float)(currentTime - lastPhysicsTime) / 1000.0F, 0.1F);
                        lastPhysicsTime = currentTime;
                        if (!frozen || batteryDead) {
                            if (!isArmed(playerId)) {
                                pausedPhysics.tick(elapsed);
                                this.syncDronePosition(playerId, player);
                            } else {
                                pausedPhysics.tick(elapsed);
                                this.syncDronePosition(playerId, player);
                            }
                        }
                    } else {
                        lastPhysicsTime = System.currentTimeMillis();
                        Optional<UUID> droneId = getActiveDroneId(playerId);
                        if (droneId.isPresent() && !FpvStatusChecker.hasNoSignalState(droneId.get())) {
                            Optional<DroneEntity> droneOpt = getActiveDrone(playerId);
                            if (droneOpt.isPresent()) {
                                DroneEntity drone = droneOpt.get();
                                player.setPos(drone.getX(), drone.getY(), drone.getZ());
                                player.setOldPosAndRot();
                                PhysicsState newPhysics = getOrCreatePhysicsState(playerId, player);
                                newPhysics.initFromDrone(drone);
                                applyDroneConfig(drone, newPhysics);
                                BatteryManager.getInstance().initFromDrone(drone);
                            }
                        }
                    }
                }
            }
        }
    }

    private void updateSignalState(Player player) {
        UUID playerId = player.getUUID();
        Optional<DroneEntity> droneOpt = getActiveDrone(playerId);
        if (droneOpt.isPresent()) {
            SignalManager.getInstance().tick(droneOpt.get());
        } else {
            Optional<UUID> droneId = getActiveDroneId(playerId);
            if (droneId.isPresent() && FpvStatusChecker.isDroneFalling(droneId.get())) {
                return;
            }

            SignalManager.getInstance().tick(player.position(), player.level().getGameTime());
        }

        if (SignalManager.getInstance().isControlBlocked()) {
            getActiveDroneId(playerId).ifPresent(droneIdx -> {
                if (!FpvStatusChecker.hasNoSignalState(droneIdx) && !FpvStatusChecker.isDroneFalling(droneIdx)) {
                    FpvStatusChecker.markSignalLost(droneIdx);
                }
            });
        }
    }

    private void applyFailsafe(Player player, PhysicsState physics) {
        Optional<UUID> droneIdOpt = getActiveDroneId(player.getUUID());
        droneIdOpt.ifPresent(droneId -> ModNetwork.CHANNEL.sendToServer(new DroneSignalLostPacket(droneId)));
        physics.setFalling(true);
        physics.setFallingParams(FallType.DEAD_DROP.getThrustFactor(), FallType.DEAD_DROP.getDragMultiplier());
        droneIdOpt.ifPresent(FPVClientMod::onSignalLossDestroyed);
    }

    private void syncDronePosition(UUID playerId, Player player) {
        syncCounter++;
        getActiveDrone(playerId)
            .ifPresent(
                drone -> {
                    if (!FpvStatusChecker.isDroneDestroyed(drone.getUUID())) {
                        drone.xOld = player.xOld;
                        drone.yOld = player.yOld;
                        drone.zOld = player.zOld;
                        drone.xRotO = drone.getXRot();
                        drone.yRotO = drone.getYRot();
                        drone.yBodyRotO = drone.yBodyRot;
                        drone.yHeadRotO = drone.getYHeadRot();
                        PhysicsState physics = getPhysicsState(playerId);
                        float yaw = 0.0F;
                        float pitch = 0.0F;
                        float roll = 0.0F;
                        double vx = 0.0;
                        double vy = 0.0;
                        double vz = 0.0;
                        if (physics != null) {
                            Vector3f look = physics.getDroneLook();
                            Vector3f up = physics.getDroneUp();
                            float[] angles = Transforms.getWorldEulerAngles(look, up);
                            yaw = angles[0];
                            pitch = angles[1];
                            roll = angles[2];
                            drone.setYRot(yaw);
                            drone.setXRot(pitch);
                            drone.setDroneRoll(roll);
                            lastKnownDroneRotations.put(drone.getUUID(), new float[]{yaw, pitch, roll});
                            drone.setYBodyRot(yaw);
                            drone.setYHeadRot(yaw);
                            Vector3f vel = physics.getVelocity();
                            Vec3 mcVel = PhysicsUnits.toMinecraftVelocity(vel);
                            vx = mcVel.x;
                            vy = mcVel.y;
                            vz = mcVel.z;
                            drone.setDeltaMovement(vx, vy, vz);
                        }

                        drone.setPos(player.getX(), player.getY(), player.getZ());
                        if (syncCounter % 3 == 0) {
                            boolean motorsArmed = false;
                            float sqx = 0.0F;
                            float sqy = 0.0F;
                            float sqz = 0.0F;
                            float sqw = 1.0F;
                            if (physics != null) {
                                motorsArmed = physics.isMotorsArmed();
                                Vector3f look = physics.getDroneLook();
                                Vector3f up = physics.getDroneUp();
                                Vector3f left = physics.getDroneLeft();
                                Matrix3f rot = new Matrix3f();
                                rot.m00(-left.x);
                                rot.m01(-left.y);
                                rot.m02(-left.z);
                                rot.m10(up.x);
                                rot.m11(up.y);
                                rot.m12(up.z);
                                rot.m20(-look.x);
                                rot.m21(-look.y);
                                rot.m22(-look.z);
                                Quaternionf q = new Quaternionf().setFromNormalized(rot);
                                sqx = q.x;
                                sqy = q.y;
                                sqz = q.z;
                                sqw = q.w;
                            }

                            ModNetwork.CHANNEL
                                .sendToServer(
                                    new DroneSyncPacket(
                                        drone.getUUID(),
                                        player.getX(),
                                        player.getY(),
                                        player.getZ(),
                                        yaw,
                                        pitch,
                                        roll,
                                        vx,
                                        vy,
                                        vz,
                                        true,
                                        motorsArmed,
                                        Optional.empty(),
                                        !motorsArmed || physics != null && physics.isThrottleCut()
                                            ? 0.0F
                                            : Mth.clamp((ControllerReader.getThrottle() + 1.0F) / 2.0F, 0.0F, 1.0F),
                                        sqx,
                                        sqy,
                                        sqz,
                                        sqw
                                    )
                                );
                        }
                    }
                }
            );
    }

}
