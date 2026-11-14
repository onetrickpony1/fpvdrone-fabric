package com.fpvdrone;

import com.fpvdrone.client.BatteryData;
import com.fpvdrone.client.BatteryManager;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.DroneConfig;
import com.fpvdrone.client.DroneSoundInstance;
import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.client.thermal.ThermalCameraState;
import com.fpvdrone.network.compat.ClientChannel;
import com.fpvdrone.render.CameraZoom;
import com.fpvdrone.render.GimbalState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import com.fpvdrone.client.SignalManager;
import com.fpvdrone.client.Transforms;
import com.fpvdrone.client.audio.DroneEngineAudioEngine;
import com.fpvdrone.client.audio.ExplosionAudioEngine;
import com.fpvdrone.client.osd.BootStateManager;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroppedBombEntity;
import com.fpvdrone.entity.FallType;
import com.fpvdrone.item.DjiControllerItem;
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
import com.fpvdrone.network.DroneCrashPacket;
import com.fpvdrone.network.RemoveGogglesPacket;
import com.fpvdrone.network.RequestDronePositionPacket;
import com.fpvdrone.network.SaveDronePositionPacket;
import com.fpvdrone.network.SwitchDronePacket;
import com.fpvdrone.physics.PhysicsState;
import com.fpvdrone.physics.PhysicsUnits;
import com.fpvdrone.render.LowResRenderer;
import com.fpvdrone.video.FrozenFrameManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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

/**
 * Client side of the mod: FPV view, physics per pilot, goggles and DJI controller handling. Rebased on 1.1.8; Forge events
 * became Fabric callbacks registered in {@link #onInitializeClient()}.
 */
public class FPVClientMod implements ClientModInitializer {
   private static FPVClientMod instance;
   private static final Map<UUID, PhysicsState> physicsStates = new ConcurrentHashMap<>();
   private static final Map<UUID, UUID> playerToActiveDrone = new ConcurrentHashMap<>();
   private static final Map<UUID, Vec3> lastKnownDronePositions = new ConcurrentHashMap<>();
   private static final Map<UUID, Boolean> pendingPositionRequests = new ConcurrentHashMap<>();
   private static final Map<UUID, Boolean> entityArmStates = new ConcurrentHashMap<>();
   private static final Map<UUID, Boolean> wasWearingGogglesMap = new ConcurrentHashMap<>();
   private static final Map<UUID, UUID> lastLinkedDroneIdMap = new ConcurrentHashMap<>();
   private static final Map<UUID, Boolean> wEJx3Itb = new ConcurrentHashMap<>();
   private static final Map<UUID, UUID> bAcdvq9 = new ConcurrentHashMap<>();
   private static final Map<UUID, int[]> forcedDroneChunks = new ConcurrentHashMap<>();
   private static final Map<UUID, Boolean> shouldRemoveGogglesMap = new ConcurrentHashMap<>();
   private static final Map<UUID, Boolean> hasPilotBodyMap = new ConcurrentHashMap<>();
   private static final Map<UUID, Vec3> pilotBodyPositionMap = new ConcurrentHashMap<>();
   private static final Map<UUID, FPVClientMod.KKeMvpuBgEY> b69s = new ConcurrentHashMap<>();
   private static final Map<UUID, FPVClientMod.Ol3TEDSog> pendingTeleports = new ConcurrentHashMap<>();
   private static final Set<UUID> lowBatteryReturnTriggered = ConcurrentHashMap.newKeySet();
   private static final int r6s7FL3 = 8;
   private static final float iAKNPAj = 0.1F;
   private static final float qlv6NKWnH = 10.0F;
   private static final double hOKMYHOaC = 6.0;
   private static final float iS1JnZov = 22.0F;
   private static final Map<UUID, Vec3> lfyfLwuw = new ConcurrentHashMap<>();
   private static final Map<UUID, Boolean> gpsPilotCache = new ConcurrentHashMap<>();
   private static final Map<UUID, float[]> pilotBodyRotationMap = new ConcurrentHashMap<>();
   private static final Map<UUID, Boolean> lastDroneBatteryStateMap = new ConcurrentHashMap<>();
   private static final Map<UUID, Integer> batteryRemovedTickMap = new ConcurrentHashMap<>();
   private static final int zHBAQgZP = 40;
   private static final Map<UUID, float[]> lastKnownDroneRotations = new ConcurrentHashMap<>();
   private static long pxmzA = System.currentTimeMillis();
   private static long lastPhysicsTime = 0L;
   private static final long sTrE = 50L;
   private static UUID lastKnownClientPlayer = null;
   private static long uQdlyiwB = 0L;
   private static double ivDR;
   private static double n9lX8A;
   private static double wBXn;
   private static final long mM7DsB9AR = 750L;
   private static final double uKpZYj = 36.0;
   private static boolean thermalAutoEnabled = false;
   private static boolean xYttSy = false;
   private static boolean prevPageDownDown = false;
   private static int fvsAOg = 0;
   private int frozenFramePruneCounter = 0;

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

   /** Once per rendered frame before the world renders (Forge RenderTickEvent START), from GameRendererMixin. */
   public static void renderTick() {
      if (instance != null) {
         instance.onRenderTick();
      }

      com.fpvdrone.client.RemoteSmoothingTicker.renderTick();
      // 1.1.8 MR7Qyh (RenderTickEvent START, low priority): find the interact zone under the crosshair.
      com.fpvdrone.client.interact.InteractMenu.onFrame();
   }

   /** Once per rendered frame after the world rendered (Forge RenderTickEvent END), from GameRendererMixin. */
   public static void renderTickEnd() {
      if (instance != null) {
         instance.onRenderTickEnd();
      }
   }

   private void onClientStarted() {
      long var0 = Minecraft.getInstance().getWindow().handle();
      ControllerReader.getInstance().init(var0);
      ExplosionAudioEngine.get().initIfNeeded();
      DroneEngineAudioEngine.get().initIfNeeded();
      FpvConfig.setMinecraftLanguage(Minecraft.getInstance().getLanguageManager().getSelected());
      // 1.1.8 also registered the thermal camera shaders here (ThermalRenderHook, OSl9AXM). 1.1.8-PENDING: thermal camera.
   }

   public static void clearClientState() {
      // 1.1.8-PENDING: loitering munition client state reset (GAduSQ8Kp.sTrE).
      entityArmStates.clear();
      physicsStates.clear();
      playerToActiveDrone.clear();
      gpsPilotCache.clear();
      lastKnownDronePositions.clear();
      pendingPositionRequests.clear();
      wasWearingGogglesMap.clear();
      lastLinkedDroneIdMap.clear();
      wEJx3Itb.clear();
      bAcdvq9.clear();
      forcedDroneChunks.clear();
      hasPilotBodyMap.clear();
      pilotBodyPositionMap.clear();
      b69s.clear();
      pendingTeleports.clear();
      pilotBodyRotationMap.clear();
      shouldRemoveGogglesMap.clear();
      lastDroneBatteryStateMap.clear();
      batteryRemovedTickMap.clear();
      lastKnownDroneRotations.clear();
      FpvStatusChecker.clearAll();
      SignalManager.getInstance().reset();
      BatteryManager.getInstance().fullReset();
      GimbalState.getInstance().reset();
      CameraZoom.getInstance().reset();
      // 1.1.8-PENDING: loitering munition view reset (GAduSQ8Kp.eJDY).
      LowResRenderer.getInstance().setEnabled(false);
      LowResRenderer.getInstance().setDjiFeed(false);
      resetThermal();
      pxmzA = System.currentTimeMillis();
      lastPhysicsTime = 0L;
      uQdlyiwB = 0L;
      lastKnownClientPlayer = null;
   }

   public static void armTeleportGuard(double var0, double var2, double var4) {
      ivDR = var0;
      n9lX8A = var2;
      wBXn = var4;
      uQdlyiwB = System.currentTimeMillis() + 750L;
   }

   public static boolean isTeleportGuardViolated(double var0, double var2, double var4) {
      if (uQdlyiwB == 0L) {
         return false;
      } else if (System.currentTimeMillis() > uQdlyiwB) {
         uQdlyiwB = 0L;
         return false;
      } else {
         double var6 = var0 - ivDR;
         double var8 = var2 - n9lX8A;
         double var10 = var4 - wBXn;
         return var6 * var6 + var8 * var8 + var10 * var10 > 36.0;
      }
   }

   public static boolean isTeleportGuardActive() {
      if (uQdlyiwB == 0L) {
         return false;
      } else if (System.currentTimeMillis() > uQdlyiwB) {
         uQdlyiwB = 0L;
         return false;
      } else {
         return true;
      }
   }

   public static void teleportPilot(Player var0, Vec3 var1, Float var2, Float var3) {
      if (var1 != null) {
         var0.setPos(var1.x, var1.y, var1.z);
         var0.setOldPosAndRot();
         var0.fallDistance = 0.0F;
      }

      Vec3 var4 = var1 != null ? var1 : var0.position();
      armTeleportGuard(var4.x, var4.y, var4.z);
      if (var2 != null && var3 != null) {
         var0.setYRot(var2);
         var0.setXRot(var3);
         var0.yRotO = var2;
         var0.xRotO = var3;
         var0.setYHeadRot(var2);
      }
   }

   public static void teleportPilotWhenLoaded(Player var0, Vec3 var1, Float var2, Float var3, Runnable var4) {
      if (var1 != null) {
         int var5 = (int)Math.floor(var1.x) >> 4;
         int var6 = (int)Math.floor(var1.z) >> 4;
         boolean var7 = var0.level().hasChunk(var5, var6);
         if (!var7) {
            pendingTeleports.put(var0.getUUID(), new FPVClientMod.Ol3TEDSog(var1, var2, var3, var4));
            ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(var5, var6, true));
            return;
         }
      }

      teleportPilot(var0, var1, var2, var3);
      var4.run();
      startPostTeleportGrace();
   }

   public static void enablePilotFlight(Player var0) {
      var0.noPhysics = true;
      var0.getAbilities().flying = true;
      var0.getAbilities().mayfly = true;
      var0.fallDistance = 0.0F;
      var0.setDeltaMovement(0.0, 0.0, 0.0);
   }

   public static void disablePilotFlight(Player var0) {
      var0.noPhysics = false;
      var0.getAbilities().flying = false;
      var0.getAbilities().mayfly = var0.isCreative() || var0.isSpectator();
      var0.onUpdateAbilities();
   }

   public static boolean isArmed(UUID var0) {
      return entityArmStates.getOrDefault(var0, false);
   }

   public static void setArmed(UUID var0, boolean var1) {
      entityArmStates.put(var0, var1);
      Minecraft var2 = Minecraft.getInstance();
      if (var2.player != null && var2.player.getUUID().equals(var0)) {
         setArmed(var2.player, var1);
      }
   }

   public static void setArmed(Player var0, boolean var1) {
      UUID var2 = var0.getUUID();
      entityArmStates.put(var2, var1);
      if (var1) {
         pxmzA = System.currentTimeMillis();
         if (var0.level().isClientSide()) {
            Minecraft var3 = Minecraft.getInstance();
            if (var3.player != null && var3.player.getUUID().equals(var2)) {
               var3.getSoundManager().play(new DroneSoundInstance(var3.player));
            }
         }
      } else if (!playerToActiveDrone.containsKey(var2)) {
         physicsStates.remove(var2);
      }
   }

   public static Optional<UUID> getActiveDroneId(UUID var0) {
      return Optional.ofNullable(playerToActiveDrone.get(var0));
   }

   public static Optional<DroneEntity> getActiveDrone(UUID var0) {
      UUID var1 = playerToActiveDrone.get(var0);
      if (var1 == null) {
         return Optional.empty();
      }

      Minecraft var2 = Minecraft.getInstance();
      if (var2.level == null) {
         return Optional.empty();
      }

      for (Entity var4 : var2.level.entitiesForRendering()) {
         if (var4 instanceof DroneEntity var5 && var5.getUUID().equals(var1)) {
            return Optional.of(var5);
         }
      }

      return Optional.empty();
   }

   public static PhysicsState getPhysicsState(UUID var0) {
      return physicsStates.get(var0);
   }

   public static PhysicsState getOrCreatePhysicsState(UUID var0, Player var1) {
      return physicsStates.computeIfAbsent(var0, var2 -> {
         PhysicsState var3 = new PhysicsState(var1);
         setupKamikazeCallback(var0, var3);
         setupEntityHitCallback(var0, var3);
         return var3;
      });
   }

   public static void markShouldRemoveGoggles(UUID var0) {
      shouldRemoveGogglesMap.put(var0, true);
   }

   public static boolean hasActiveDrone(UUID var0) {
      return playerToActiveDrone.containsKey(var0);
   }

   public static boolean isActiveDrone(UUID var0) {
      return playerToActiveDrone.containsValue(var0);
   }

   @Nullable
   public static Vec3 getPilotBodyPosition(UUID var0) {
      return pilotBodyPositionMap.get(var0);
   }

   @Nullable
   public static float[] getPilotBodyRotation(UUID var0) {
      return pilotBodyRotationMap.get(var0);
   }

   public static void setPilotBody(UUID var0, Vec3 var1, float var2, float var3) {
      pilotBodyPositionMap.put(var0, var1);
      pilotBodyRotationMap.put(var0, new float[]{var2, var3});
   }

   public static void onDroneNotFound(UUID var0) {
      pilotBodyPositionMap.remove(var0);
      pilotBodyRotationMap.remove(var0);
   }

   public static void updatePilotBodyPosition(UUID var0, double var1, double var3, double var5, float var7, float var8) {
      if (pilotBodyPositionMap.containsKey(var0)) {
         pilotBodyPositionMap.put(var0, new Vec3(var1, var3, var5));
         pilotBodyRotationMap.put(var0, new float[]{var7, var8});
      }
   }

   @Nullable
   public static float[] getLastKnownDroneRotation(UUID var0) {
      return lastKnownDroneRotations.get(var0);
   }

   public static void enterFPVMode(Player var0, DroneEntity var1) {
      UUID var2 = var0.getUUID();
      UUID var3 = var1.getUUID();
      lowBatteryReturnTriggered.remove(var2);
      if (var1.onGround()) {
         lfyfLwuw.put(var3, new Vec3(var1.getX(), var1.getY(), var1.getZ()));
      }

      playerToActiveDrone.put(var2, var3);
      var1.setOwner(var2);
      var1.setBeingControlled(true);
      gpsPilotCache.put(var2, var1.xogV5KBhg());
      PhysicsState var4 = getOrCreatePhysicsState(var2, var0);
      applyDroneConfig(var1, var4);
      var4.initFromDrone(var1);
      BatteryManager.getInstance().initFromDrone(var1);
      Vec3 var5 = pilotBodyPositionMap.get(var2);
      SignalManager.getInstance().reset();
      SignalManager.getInstance().setOrigin(var5 != null ? var5 : var0.position());
      if (var1.isThermal()) {
         ThermalCameraState var6 = ThermalCameraState.getInstance();
         if (!var6.isEnabled()) {
            var6.toggle();
         }

         thermalAutoEnabled = true;
      }

      setArmed(var0, true);
      Minecraft var7 = Minecraft.getInstance();
      if (var7.player != null && var7.player.getUUID().equals(var2)) {
         var7.player.refreshDimensions();
      }
   }

   private static void applyDroneConfig(DroneEntity var0, PhysicsState var1) {
      DroneConfig var2 = var0.getConfig();
      BatteryData var3 = var0.getBattery();

      for (int var4 = 0; var4 < 4; var4++) {
         var1.setRcRate(var4, var2.getRcRate(var4));
         var1.setSuperRate(var4, var2.getSuperRate(var4));
         var1.setExpo(var4, var2.getExpo(var4));
      }

      float var5 = var0.qz71Ga() / 1000.0F;
      var1.setMass(var5);
      var1.setMotorKv(var2.getMotorKv());
      var1.setPropDiameter(var2.getPropDiameter() * 0.0254F);
      var1.setPropPitch(var2.getPropPitch() * 0.0254F);
      var1.setDragCoefficient(var2.getDragCoefficient());
      var1.setThrustMultiplier(var2.getThrustMultiplier());
      var1.setFlightMode3d(var2.isFlightMode3d());
      var1.setGpsMode(var0.xogV5KBhg());
      ControllerReader.setGpsMode(var0.xogV5KBhg());
      var1.setBatteryCells(var3.getCells());
   }

   public static void finalizeDroneState(Player var0) {
      finalizeDroneState(var0, false);
   }

   private static boolean isGpsPilot(UUID var0) {
      return getActiveDrone(var0).map(DroneEntity::xogV5KBhg).orElse(gpsPilotCache.getOrDefault(var0, false));
   }

   public static boolean startReturnHome(Player var0) {
      UUID var1 = var0.getUUID();
      PhysicsState var2 = getPhysicsState(var1);
      if (var2 == null) {
         return false;
      }

      Vec3 var3 = getHomePoint(var1, var0);
      if (var3 == null) {
         return false;
      }

      var2.startReturnHome(var3.x, var3.y, var3.z);
      return true;
   }

   @Nullable
   private static Vec3 getHomePoint(UUID var0, Player var1) {
      UUID var2 = playerToActiveDrone.get(var0);
      Vec3 var3 = var2 != null ? lfyfLwuw.get(var2) : null;
      if (var3 == null) {
         var3 = pilotBodyPositionMap.get(var0);
      }

      if (var3 == null) {
         var3 = SignalManager.getInstance().getOrigin();
      }

      if (var3 == null) {
         var3 = var1.position();
      }

      return var3;
   }

   private static void checkLowBatteryReturnHome(Player var0, UUID var1, PhysicsState var2) {
      boolean var3 = getActiveDrone(var1).map(DroneEntity::xogV5KBhg).orElse(false);
      if (var3 && !var2.isReturningHome() && !lowBatteryReturnTriggered.contains(var1)) {
         BatteryManager var4 = BatteryManager.getInstance();
         if (var4.hasBattery()) {
            int var5 = var4.getCapacityMah();
            if (var5 > 0) {
               boolean var6 = var4.getBatteryPercent() <= 8;
               boolean var7 = false;
               Vec3 var8 = getHomePoint(var1, var0);
               if (var8 != null) {
                  double var9 = var0.position().distanceTo(var8);
                  double var11 = var9 / 10.0 + 6.0;
                  float var13 = Math.max(var4.getCurrentAmps(), 22.0F);
                  float var14 = (float)(var13 * (var11 / 3600.0) * 1000.0);
                  float var15 = var5 * 0.1F;
                  float var16 = var5 - var4.getUsedMahFloat();
                  var7 = var16 <= var14 + var15;
               }

               if (var6 || var7) {
                  lowBatteryReturnTriggered.add(var1);
                  if (startReturnHome(var0)) {
                     var0.sendOverlayMessage(Component.translatable("fpvdrone.status.rth_low_battery").withStyle(Style.EMPTY.withColor(16749622)));
                  }
               }
            }
         }
      }
   }

   public static void finalizeDroneState(Player var0, boolean var1) {
      UUID var2 = var0.getUUID();
      UUID var3 = playerToActiveDrone.get(var2);
      if (var3 != null && !FpvStatusChecker.isDroneDestroyed(var3)) {
         FpvStatusChecker.clearDroneState(var3);
      }

      double var4 = var0.getX();
      double var6 = var0.getY();
      double var8 = var0.getZ();
      float var10 = 0.0F;
      float var11 = 0.0F;
      float var12 = 0.0F;
      double var13 = 0.0;
      double var15 = 0.0;
      double var17 = 0.0;
      PhysicsState var19 = getPhysicsState(var2);
      if (var19 != null) {
         Vector3f var20 = var19.getDroneLook();
         Vector3f var21 = var19.getDroneUp();
         float[] var22 = Transforms.getWorldEulerAngles(var20, var21);
         var10 = var22[0];
         var11 = var22[1];
         var12 = var22[2];
         if (var3 != null) {
            lastKnownDroneRotations.put(var3, new float[]{var10, var11, var12});
         }

         Vector3f var23 = var19.getVelocity();
         Vec3 var24 = PhysicsUnits.toMinecraftVelocity(var23);
         var13 = var24.x;
         var15 = var24.y;
         var17 = var24.z;
      }

      float var40 = var10;
      float var41 = var11;
      float var42 = var12;
      double var43 = var13;
      double var25 = var15;
      double var27 = var17;
      if (var3 != null && var0.level().isClientSide()) {
         var var29 = findDroneById(var0, var3);
         var29.ifPresent(var15x -> {
            var15x.setPos(var4, var6, var8);
            var15x.setOldPosAndRot();
            var15x.setYRot(var40);
            var15x.setXRot(var41);
            var15x.setDroneRoll(var42);
            var15x.setYBodyRot(var40);
            var15x.yBodyRotO = var40;
            var15x.setDeltaMovement(var43, var25, var27);
            var15x.v5suI(false);
            var15x.setBeingControlled(false);
         });
         float var30 = GimbalState.getInstance().getPitch();
         float var31 = 0.0F;
         float var32 = 0.0F;
         float var33 = 0.0F;
         float var34 = 1.0F;
         if (var19 != null) {
            Vector3f var35 = var19.getDroneLook();
            Vector3f var36 = var19.getDroneUp();
            Vector3f var37 = var19.getDroneLeft();
            Matrix3f var38 = new Matrix3f();
            var38.m00(-var37.x);
            var38.m01(-var37.y);
            var38.m02(-var37.z);
            var38.m10(var36.x);
            var38.m11(var36.y);
            var38.m12(var36.z);
            var38.m20(-var35.x);
            var38.m21(-var35.y);
            var38.m22(-var35.z);
            Quaternionf var39 = new Quaternionf().setFromNormalized(var38);
            var31 = var39.x;
            var32 = var39.y;
            var33 = var39.z;
            var34 = var39.w;
         }

         ModNetwork.CHANNEL
            .sendToServer(
               new DroneSyncPacket(
                  var3, var4, var6, var8, var40, var41, var42, var43, var25, var27, false, var1, Optional.empty(), 0.0F, var31, var32, var33, var34, var30
               )
            );
         float var44 = BatteryManager.getInstance().getUsedMahFloat();
         ModNetwork.CHANNEL.sendToServer(new BatterySyncPacket(var3, var44));
      }

      setArmed(var0, false);
   }

   public static void onDroneDestroyed(UUID var0) {
      if (!FpvStatusChecker.isDroneDestroyed(var0)) {
         FpvStatusChecker.markDroneDestroyed(var0);
         lastKnownDronePositions.remove(var0);
         playerToActiveDrone.forEach((var1, var2) -> {
            if (var2.equals(var0)) {
               Minecraft var3 = Minecraft.getInstance();
               if (var3.player != null && var3.player.getUUID().equals(var1)) {
                  setArmed(var3.player, false);
               }

               SignalManager.getInstance().forceNoSignal();
               if (FrozenFrameManager.getInstance().hasFrozenFrame(var0)) {
                  LowResRenderer.getInstance().setEnabled(true);
                  LowResRenderer.getInstance().setFrozenFrameMode(true);
                  LowResRenderer.getInstance().setFrozenFrameDroneId(var0);
               }
            }
         });
      }
   }

   public static void onSignalLossDestroyed(UUID var0) {
      if (!FpvStatusChecker.isDroneDestroyed(var0)) {
         FpvStatusChecker.markDroneDestroyed(var0);
         lastKnownDronePositions.remove(var0);
         playerToActiveDrone.forEach((var1, var2) -> {
            if (var2.equals(var0)) {
               SignalManager.getInstance().forceNoSignal();
               if (FrozenFrameManager.getInstance().hasFrozenFrame(var0)) {
                  LowResRenderer.getInstance().setEnabled(true);
                  LowResRenderer.getInstance().setFrozenFrameMode(true);
                  LowResRenderer.getInstance().setFrozenFrameDroneId(var0);
               }
            }
         });
      }
   }

   private static void setupKamikazeCallback(UUID var0, PhysicsState var1) {
      var1.setOnCollision((var2, var3, var4, var5) -> {
         if (!var1.isFalling()) {
            getActiveDrone(var0).ifPresent(var4x -> {
               if (!var4x.iKXOcc()) {
                  if (var2 >= 12.0F) {
                     ModNetwork.CHANNEL.sendToServer(new DroneCrashPacket(var4x.getUUID(), var2, var3, var4, var5));
                  }
               } else {
                  ProjectileDefinition var5x = ProjectileDefinitions.getOrDefault(var4x.i263D9());
                  float var6 = var5x.detonationSpeed();
                  if (!(var2 < var6)) {
                     float var7 = var5x.explosionPower();
                     int var8 = var5x.fragmentCount();
                     ModNetwork.CHANNEL.sendToServer(new DroneExplodePacket(var4x.getUUID(), var7, var8, var3, var4, var5));
                  }
               }
            });
         }
      });
   }

   private static void setupEntityHitCallback(UUID var0, PhysicsState var1) {
      var1.setOnEntityHit((var2, var3, var4, var5, var6, var7, var9, var11) -> {
         if (!var1.isFalling()) {
            getActiveDrone(var0).ifPresent(var11x -> {
               ProjectileDefinition var12 = ProjectileDefinitions.getOrDefault(var11x.i263D9());
               float var13 = var12.detonationSpeed();
               if (var11x.iKXOcc() && var3 >= var13) {
                  float var14 = var12.explosionPower();
                  int var15 = var12.fragmentCount();
                  ModNetwork.CHANNEL.sendToServer(new DroneExplodePacket(var11x.getUUID(), var14, var15));
               } else {
                  ModNetwork.CHANNEL.sendToServer(new DroneEntityHitPacket(var11x.getUUID(), var2, var3, var4, var5, var6, var7, var9, var11));
               }
            });
         }
      });
   }

   private void handleGogglesEquipState(Player var1) {
      UUID var2 = var1.getUUID();
      ItemStack var3 = var1.getItemBySlot(EquipmentSlot.HEAD);
      boolean var4 = var3.getItem() instanceof FpvGogglesItem;
      var var5 = var4 ? FpvGogglesItem.getLinkedDroneId(var3) : Optional.empty();
      boolean var6 = wasWearingGogglesMap.getOrDefault(var2, false);
      UUID var7 = lastLinkedDroneIdMap.get(var2);
      boolean var8 = playerToActiveDrone.containsKey(var2);
      if (shouldRemoveGogglesMap.getOrDefault(var2, false)) {
         removeGoggles(var1);
         shouldRemoveGogglesMap.remove(var2);
         wasWearingGogglesMap.put(var2, false);
      } else if (var4 && !var8 && var5.isEmpty()) {
         if (!var6) {
            var1.sendOverlayMessage(Component.translatable("fpvdrone.goggles.equip_blocked.no_link").withStyle(Style.EMPTY.withColor(16749622)));
         }

         var1.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
         if (!var1.getInventory().add(var3)) {
            var1.drop(var3, false, net.minecraft.util.Prediction.SERVER_ONLY);
         }

         wasWearingGogglesMap.put(var2, false);
      } else {
         if (var4 && !var6) {
            if (var5.isPresent()) {
               UUID var11 = (UUID)var5.get();
               lastLinkedDroneIdMap.put(var2, var11);
               if (!playerToActiveDrone.containsKey(var2)) {
                  this.tryEnterGogglesMode(var1, var3, var11);
               }
            }
         } else if (!var4 && var6) {
            if (var7 != null && !FpvStatusChecker.isDroneDestroyed(var7)) {
               Vec3 var10 = var1.position();
               lastKnownDronePositions.put(var7, var10);
               ModNetwork.CHANNEL.sendToServer(new SaveDronePositionPacket(var7, var10.x, var10.y, var10.z));
            }

            exitGogglesMode(var1);
            lastLinkedDroneIdMap.remove(var2);
         } else if (var4 && var5.isPresent()) {
            UUID var9 = (UUID)var5.get();
            if (var7 != null && !var9.equals(var7)) {
               if (playerToActiveDrone.containsKey(var2)) {
                  if (switchDrone(var1, var7, var9)) {
                     lastLinkedDroneIdMap.put(var2, var9);
                  }
               } else {
                  lastLinkedDroneIdMap.put(var2, var9);
                  this.tryEnterGogglesMode(var1, var3, var9);
               }
            } else if (!playerToActiveDrone.containsKey(var2)) {
               this.tryEnterGogglesMode(var1, var3, var9);
            }
         }

         wasWearingGogglesMap.put(var2, var4);
      }
   }

   private void handleDjiControllerState(Player var1) {
      UUID var2 = var1.getUUID();
      if (!(var1.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof FpvGogglesItem)) {
         ItemStack var3 = findHeldDjiController(var1);
         boolean var4 = !var3.isEmpty();
         var var5 = var4 ? DjiControllerItem.getLinkedDroneId(var3) : Optional.empty();
         UUID var6 = bAcdvq9.get(var2);
         boolean var7 = wEJx3Itb.getOrDefault(var2, false);
         boolean var8 = playerToActiveDrone.containsKey(var2);
         if (var4 && var5.isPresent() && var8) {
            UUID var10 = (UUID)var5.get();
            if (var6 != null && !var10.equals(var6) && switchDrone(var1, var6, var10)) {
               bAcdvq9.put(var2, var10);
            }
         } else if (!var4 && var7) {
            if (var6 != null && !FpvStatusChecker.isDroneDestroyed(var6)) {
               Vec3 var9 = var1.position();
               lastKnownDronePositions.put(var6, var9);
               ModNetwork.CHANNEL.sendToServer(new SaveDronePositionPacket(var6, var9.x, var9.y, var9.z));
            }

            if (var8) {
               exitGogglesMode(var1);
            }

            bAcdvq9.remove(var2);
         }

         wEJx3Itb.put(var2, var4);
      }
   }

   private static ItemStack findHeldDjiController(Player var0) {
      ItemStack var1 = var0.getMainHandItem();
      if (var1.getItem() instanceof DjiControllerItem) {
         return var1;
      }

      ItemStack var2 = var0.getOffhandItem();
      return var2.getItem() instanceof DjiControllerItem ? var2 : ItemStack.EMPTY;
   }

   private static void enterDjiView(Player var0, UUID var1) {
      if (FpvStatusChecker.isDroneDestroyed(var1)) {
         var0.sendOverlayMessage(Component.translatable("fpvdrone.status.drone_not_found").withStyle(Style.EMPTY.withColor(16749622)));
      } else {
         var var2 = findDroneById(var0, var1);
         if (var2.isPresent()) {
            DroneEntity var3 = (DroneEntity)var2.get();
            if (!var3.idXga()) {
               var0.sendOverlayMessage(Component.translatable("fpvdrone.status.no_battery").withStyle(Style.EMPTY.withColor(16749622)));
            } else {
               enterGogglesMode(var0, var3);
               if (var3.xogV5KBhg() && var3.areMotorsArmed()) {
                  enterFPVMode(var0, var3);
                  PhysicsState var4 = getPhysicsState(var0.getUUID());
                  if (var4 != null) {
                     var4.setMotorsArmed(true);
                  }
               }
            }
         } else {
            if (!pendingPositionRequests.getOrDefault(var1, false)) {
               pendingPositionRequests.put(var1, true);
               ModNetwork.CHANNEL.sendToServer(new RequestDronePositionPacket(var1));
            }
         }
      }
   }

   private static void removeGoggles(Player var0) {
      UUID var1 = var0.getUUID();
      b69s.remove(var1);
      ItemStack var2 = var0.getItemBySlot(EquipmentSlot.HEAD);
      if (var2.getItem() instanceof FpvGogglesItem) {
         var0.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
         if (!var0.getInventory().add(var2)) {
            var0.drop(var2, false, net.minecraft.util.Prediction.SERVER_ONLY);
         }
      }

      if (isArmed(var1)) {
         finalizeDroneState(var0);
      }

      Vec3 var3 = pilotBodyPositionMap.remove(var1);
      if (var3 != null) {
         var0.setPos(var3.x, var3.y, var3.z);
         var0.setOldPosAndRot();
         var0.fallDistance = 0.0F;
      }

      Vec3 var4 = var3 != null ? var3 : var0.position();
      armTeleportGuard(var4.x, var4.y, var4.z);
      float[] var5 = pilotBodyRotationMap.remove(var1);
      if (var5 != null) {
         var0.setYRot(var5[0]);
         var0.setXRot(var5[1]);
         var0.yRotO = var5[0];
         var0.xRotO = var5[1];
         var0.setYHeadRot(var5[0]);
      }

      int[] var6 = forcedDroneChunks.remove(var1);
      if (var6 != null) {
         ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(var6[0], var6[1], false));
      }

      playerToActiveDrone.remove(var1);
      physicsStates.remove(var1);
      hasPilotBodyMap.remove(var1);
      lastLinkedDroneIdMap.remove(var1);
      disablePilotFlight(var0);
      LowResRenderer.getInstance().setEnabled(false);
      LowResRenderer.getInstance().setFrozenFrameMode(false);
      SignalManager.getInstance().reset();
      resetThermal();
      Minecraft var7 = Minecraft.getInstance();
      if (var7.player != null && var7.player.getUUID().equals(var1)) {
         var7.player.refreshDimensions();
      }
   }

   public static void exitDroneView(Player var0) {
      UUID var1 = var0.getUUID();
      ItemStack var2 = var0.getItemBySlot(EquipmentSlot.HEAD);
      if (var2.getItem() instanceof FpvGogglesItem) {
         UUID var7 = playerToActiveDrone.get(var1);
         if (var7 == null) {
            var7 = FpvGogglesItem.getLinkedDroneId(var2).orElse(null);
         }

         if (var7 != null) {
            ModNetwork.CHANNEL.sendToServer(new RemoveGogglesPacket(var7));
         }

         removeGoggles(var0);
         wasWearingGogglesMap.put(var1, false);
         var0.sendOverlayMessage(Component.translatable("fpvdrone.status.goggles_removed").withStyle(Style.EMPTY.withColor(16749622)));
      } else {
         ItemStack var3 = findHeldDjiController(var0);
         if (!var3.isEmpty()) {
            var var4 = DjiControllerItem.getLinkedDroneId(var3);
            if (var4.isEmpty()) {
               var0.sendOverlayMessage(Component.translatable("fpvdrone.status.controller_not_linked").withStyle(Style.EMPTY.withColor(16749622)));
            } else {
               UUID var5 = (UUID)var4.get();
               if (playerToActiveDrone.containsKey(var1)) {
                  Vec3 var6 = var0.position();
                  lastKnownDronePositions.put(var5, var6);
                  ModNetwork.CHANNEL.sendToServer(new SaveDronePositionPacket(var5, var6.x, var6.y, var6.z));
                  exitGogglesMode(var0);
                  bAcdvq9.remove(var1);
                  var0.sendOverlayMessage(Component.translatable("fpvdrone.status.view_off").withStyle(Style.EMPTY.withColor(16749622)));
               } else {
                  bAcdvq9.put(var1, var5);
                  wEJx3Itb.put(var1, true);
                  enterDjiView(var0, var5);
               }
            }
         }
      }
   }

   private void tryEnterGogglesMode(Player var1, ItemStack var2, UUID var3) {
      if (FpvStatusChecker.isDroneDestroyed(var3)) {
         enterNoSignalMode(var1, var3);
      } else {
         var var4 = findDroneById(var1, var3);
         if (var4.isPresent()) {
            DroneEntity var6 = (DroneEntity)var4.get();
            if (!var6.idXga()) {
               var1.sendOverlayMessage(Component.translatable("fpvdrone.status.no_battery").withStyle(Style.EMPTY.withColor(16749622)));
               var1.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
               if (!var1.getInventory().add(var2)) {
                  var1.drop(var2, false, net.minecraft.util.Prediction.SERVER_ONLY);
               }
            } else {
               enterGogglesMode(var1, var6);
            }
         } else {
            Vec3 var5 = lastKnownDronePositions.get(var3);
            if (var5 != null) {
               enterGogglesModeAtPosition(var1, var3, var5);
            } else {
               if (!pendingPositionRequests.getOrDefault(var3, false)) {
                  pendingPositionRequests.put(var3, true);
                  ModNetwork.CHANNEL.sendToServer(new RequestDronePositionPacket(var3));
               }
            }
         }
      }
   }

   public static void markDroneLost(UUID var0) {
      pendingPositionRequests.remove(var0);
      FpvStatusChecker.markDroneDestroyed(var0);
   }

   private static void enterNoSignalMode(Player var0, UUID var1) {
      UUID var2 = var0.getUUID();
      playerToActiveDrone.put(var2, var1);
      SignalManager.getInstance().forceNoSignal();
      if (FrozenFrameManager.getInstance().hasFrozenFrame(var1)) {
         LowResRenderer.getInstance().setEnabled(true);
         LowResRenderer.getInstance().setFrozenFrameMode(true);
         LowResRenderer.getInstance().setFrozenFrameDroneId(var1);
      }
   }

   public static void onDronePositionReceived(UUID var0, double var1, double var3, double var5) {
      pendingPositionRequests.remove(var0);
      Vec3 var7 = new Vec3(var1, var3, var5);
      lastKnownDronePositions.put(var0, var7);
      Minecraft var8 = Minecraft.getInstance();
      if (var8.player != null) {
         UUID var9 = var8.player.getUUID();
         UUID var10 = playerToActiveDrone.get(var9);
         if (var10 != null && var10.equals(var0)) {
            var8.player.setPos(var1, var3, var5);
            var8.player.setOldPosAndRot();
            int var14 = (int)Math.floor(var1) >> 4;
            int var16 = (int)Math.floor(var5) >> 4;
            int[] var17 = forcedDroneChunks.get(var9);
            if (var17 != null && (var17[0] != var14 || var17[1] != var16)) {
               ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(var17[0], var17[1], false));
            }

            forcedDroneChunks.put(var9, new int[]{var14, var16});
            ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(var14, var16, true));
         } else {
            ItemStack var11 = findHeldDjiController(var8.player);
            if (var11.getItem() instanceof DjiControllerItem) {
               var var15 = DjiControllerItem.getLinkedDroneId(var11);
               if (var15.isPresent() && ((UUID)var15.get()).equals(var0) && !playerToActiveDrone.containsKey(var9)) {
                  bAcdvq9.put(var9, var0);
                  enterGogglesModeAtPosition(var8.player, var0, var7);
               }
            } else {
               ItemStack var12 = var8.player.getItemBySlot(EquipmentSlot.HEAD);
               if (var12.getItem() instanceof FpvGogglesItem) {
                  var var13 = FpvGogglesItem.getLinkedDroneId(var12);
                  if (var13.isPresent() && ((UUID)var13.get()).equals(var0) && !playerToActiveDrone.containsKey(var9)) {
                     enterGogglesModeAtPosition(var8.player, var0, var7);
                  }
               }
            }
         }
      }
   }

   private static void enterGogglesMode(Player var0, DroneEntity var1) {
      UUID var2 = var0.getUUID();
      UUID var3 = var1.getUUID();
      gpsPilotCache.put(var2, var1.xogV5KBhg());
      LowResRenderer.getInstance().invalidate();
      FpvStatusChecker.clearDroneState(var3);
      pilotBodyPositionMap.put(var2, var0.position());
      pilotBodyRotationMap.put(var2, new float[]{var0.getYRot(), var0.getXRot()});
      playerToActiveDrone.put(var2, var3);
      hasPilotBodyMap.put(var2, true);
      ModNetwork.CHANNEL.sendToServer(new EnterFpvPacket(var3));
      SignalManager.getInstance().reset();
      SignalManager.getInstance().setOrigin(var0.position());
      BatteryManager.getInstance().initFromDrone(var1);
      if (var1.oEuCBB().bAcdvq9().gimbal()) {
         GimbalState.getInstance().setPitch(var1.n9lX8A());
      }

      var0.setPos(var1.getX(), var1.getY(), var1.getZ());
      var0.setOldPosAndRot();
      enablePilotFlight(var0);
      if (var1.isThermal()) {
         ThermalCameraState var4 = ThermalCameraState.getInstance();
         if (!var4.isEnabled()) {
            var4.toggle();
         }

         thermalAutoEnabled = true;
      }

      LowResRenderer.getInstance().setEnabled(true);
      LowResRenderer.getInstance().setDjiFeed(var1.xogV5KBhg());
      Minecraft var5 = Minecraft.getInstance();
      if (var5.player != null && var5.player.getUUID().equals(var2)) {
         var5.player.refreshDimensions();
      }
   }

   private static void enterGogglesModeAtPosition(Player var0, UUID var1, Vec3 var2) {
      UUID var3 = var0.getUUID();
      int var4 = (int)Math.floor(var2.x) >> 4;
      int var5 = (int)Math.floor(var2.z) >> 4;
      if (!var0.level().hasChunk(var4, var5)) {
         b69s.put(var3, new FPVClientMod.KKeMvpuBgEY(var1, var2));
         ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(var4, var5, true));
      } else {
         enterDjiViewAtPosition(var0, var1, var2);
      }
   }

   private static void enterDjiViewAtPosition(Player var0, UUID var1, Vec3 var2) {
      UUID var3 = var0.getUUID();
      LowResRenderer.getInstance().invalidate();
      FpvStatusChecker.clearDroneState(var1);
      pilotBodyPositionMap.put(var3, var0.position());
      pilotBodyRotationMap.put(var3, new float[]{var0.getYRot(), var0.getXRot()});
      playerToActiveDrone.put(var3, var1);
      hasPilotBodyMap.put(var3, true);
      ModNetwork.CHANNEL.sendToServer(new EnterFpvPacket(var1));
      SignalManager.getInstance().reset();
      SignalManager.getInstance().setOrigin(var0.position());
      BatteryManager.getInstance().fullReset();
      int var4 = (int)Math.floor(var2.x) >> 4;
      int var5 = (int)Math.floor(var2.z) >> 4;
      forcedDroneChunks.put(var3, new int[]{var4, var5});
      ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(var4, var5, true));
      var0.setPos(var2.x, var2.y, var2.z);
      var0.setOldPosAndRot();
      enablePilotFlight(var0);
      LowResRenderer.getInstance().setEnabled(true);
      Minecraft var6 = Minecraft.getInstance();
      if (var6.player != null && var6.player.getUUID().equals(var3)) {
         var6.player.refreshDimensions();
      }
   }

   private static boolean switchDrone(Player var0, UUID var1, UUID var2) {
      UUID var3 = var0.getUUID();
      var var4 = findDroneById(var0, var2);
      if (var4.isPresent() && !((DroneEntity)var4.get()).idXga()) {
         return false;
      }

      if (isArmed(var3)) {
         finalizeDroneState(var0);
      }

      playerToActiveDrone.put(var3, var2);
      ModNetwork.CHANNEL.sendToServer(new SwitchDronePacket(var1, var2));
      int[] var5 = forcedDroneChunks.remove(var3);
      if (var5 != null) {
         ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(var5[0], var5[1], false));
      }

      physicsStates.remove(var3);
      Vec3 var6 = pilotBodyPositionMap.get(var3);
      if (var6 != null) {
         SignalManager.getInstance().setOrigin(var6);
      }

      var var7 = findDroneById(var0, var2);
      if (var7.isPresent()) {
         DroneEntity var8 = (DroneEntity)var7.get();
         var0.setPos(var8.getX(), var8.getY(), var8.getZ());
         var0.setOldPosAndRot();
         BatteryManager.getInstance().initFromDrone(var8);
      } else {
         Vec3 var11 = lastKnownDronePositions.get(var2);
         if (var11 != null) {
            var0.setPos(var11.x, var11.y, var11.z);
            var0.setOldPosAndRot();
            int var9 = (int)Math.floor(var11.x) >> 4;
            int var10 = (int)Math.floor(var11.z) >> 4;
            forcedDroneChunks.put(var3, new int[]{var9, var10});
            ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(var9, var10, true));
         } else if (!pendingPositionRequests.getOrDefault(var2, false)) {
            pendingPositionRequests.put(var2, true);
            ModNetwork.CHANNEL.sendToServer(new RequestDronePositionPacket(var2));
         }
      }

      boolean var12 = var7.map(DroneEntity::isThermal).orElse(false);
      ThermalCameraState var13 = ThermalCameraState.getInstance();
      if (thermalAutoEnabled && !var12) {
         if (var13.isEnabled()) {
            var13.toggle();
         }

         thermalAutoEnabled = false;
      } else if (var12 && !thermalAutoEnabled) {
         if (!var13.isEnabled()) {
            var13.toggle();
         }

         thermalAutoEnabled = true;
      }

      LowResRenderer.getInstance().setFrozenFrameMode(false);
      if (!FpvStatusChecker.isDroneDestroyed(var1)) {
         FrozenFrameManager.getInstance().clearFrame(var1);
         FpvStatusChecker.clearDroneState(var1);
      }

      SignalManager.getInstance().reset();
      if (var6 != null) {
         SignalManager.getInstance().setOrigin(var6);
      }

      if (FpvStatusChecker.isDroneDestroyed(var2)) {
         SignalManager.getInstance().forceNoSignal();
         if (FrozenFrameManager.getInstance().hasFrozenFrame(var2)) {
            LowResRenderer.getInstance().setFrozenFrameMode(true);
            LowResRenderer.getInstance().setFrozenFrameDroneId(var2);
         }
      }

      boolean var14 = var4.map(DroneEntity::idXga).orElse(false);
      lastDroneBatteryStateMap.put(var2, var14);
      return true;
   }

   private static void exitGogglesMode(Player var0) {
      UUID var1 = var0.getUUID();
      b69s.remove(var1);
      if (isArmed(var1)) {
         finalizeDroneState(var0, isGpsPilot(var1));
      }

      Vec3 var2 = pilotBodyPositionMap.remove(var1);
      float[] var3 = pilotBodyRotationMap.remove(var1);
      teleportPilotWhenLoaded(var0, var2, var3 != null ? var3[0] : null, var3 != null ? var3[1] : null, () -> disablePilotFlight(var0));
      UUID var4 = playerToActiveDrone.get(var1);
      if (var4 != null) {
         FpvStatusChecker.clearDroneState(var4);
      }

      int[] var5 = forcedDroneChunks.remove(var1);
      if (var5 != null && var0.level().isClientSide()) {
         ModNetwork.CHANNEL.sendToServer(new ForceChunkPacket(var5[0], var5[1], false));
      }

      playerToActiveDrone.remove(var1);
      PhysicsState var6 = physicsStates.get(var1);
      boolean var7 = var6 != null && var6.isReturningHome();
      physicsStates.remove(var1);
      gpsPilotCache.remove(var1);
      boolean var8 = hasPilotBodyMap.getOrDefault(var1, false);
      hasPilotBodyMap.remove(var1);
      if (var0.level().isClientSide() && var8 && var4 != null) {
         ModNetwork.CHANNEL.sendToServer(new ExitFpvPacket(var4, var7));
      }

      boolean var9 = LowResRenderer.getInstance().isFrozenFrameMode();
      if (var9) {
         Minecraft var10 = Minecraft.getInstance();
         var10.execute(() -> var10.execute(() -> {
            LowResRenderer.getInstance().setEnabled(false);
            LowResRenderer.getInstance().setFrozenFrameMode(false);
         }));
      } else {
         LowResRenderer.getInstance().setEnabled(false);
         LowResRenderer.getInstance().setFrozenFrameMode(false);
      }

      SignalManager.getInstance().reset();
      resetThermal();
      Minecraft var11 = Minecraft.getInstance();
      if (var11.player != null && var11.player.getUUID().equals(var1)) {
         var11.player.refreshDimensions();
      }
   }

   public static void resetThermal() {
      ThermalCameraState.getInstance().disable();
      thermalAutoEnabled = false;
   }

   public static Optional<DroneEntity> findDroneByIdForClient(UUID var0) {
      Minecraft var1 = Minecraft.getInstance();
      if (var1.level != null) {
         for (Entity var3 : var1.level.entitiesForRendering()) {
            if (var3 instanceof DroneEntity var4 && var4.getUUID().equals(var0)) {
               return Optional.of(var4);
            }
         }
      }

      return Optional.empty();
   }

   public static Optional<DroneEntity> findDroneById(Player var0, UUID var1) {
      if (var0.level().isClientSide()) {
         Minecraft var2 = Minecraft.getInstance();
         if (var2.level != null) {
            for (Entity var4 : var2.level.entitiesForRendering()) {
               if (var4 instanceof DroneEntity var5 && var5.getUUID().equals(var1)) {
                  return Optional.of(var5);
               }
            }
         }
      }

      return Optional.empty();
   }

   private void onClientDisconnect() {
      Minecraft var2 = Minecraft.getInstance();
      if (var2.player != null) {
         UUID var3 = var2.player.getUUID();
         if (playerToActiveDrone.containsKey(var3)) {
            UUID var4 = playerToActiveDrone.get(var3);
            if (var4 != null) {
               PhysicsState var5 = getPhysicsState(var3);
               double var6 = var2.player.getX();
               double var8 = var2.player.getY();
               double var10 = var2.player.getZ();
               float var12 = 0.0F;
               float var13 = 0.0F;
               float var14 = 0.0F;
               double var15 = 0.0;
               double var17 = 0.0;
               double var19 = 0.0;
               if (var5 != null) {
                  Vector3f var21 = var5.getDroneLook();
                  Vector3f var22 = var5.getDroneUp();
                  float[] var23 = Transforms.getWorldEulerAngles(var21, var22);
                  var12 = var23[0];
                  var13 = var23[1];
                  var14 = var23[2];
                  Vector3f var24 = var5.getVelocity();
                  Vec3 var25 = PhysicsUnits.toMinecraftVelocity(var24);
                  var15 = var25.x;
                  var17 = var25.y;
                  var19 = var25.z;
               }

               boolean var27 = isArmed(var3) && isGpsPilot(var3);

               try {
                  ModNetwork.CHANNEL.sendToServer(new DroneSyncPacket(var4, var6, var8, var10, var12, var13, var14, var15, var17, var19, false, var27));
                  ModNetwork.CHANNEL.sendToServer(new RemoveGogglesPacket(var4));
                  ModNetwork.CHANNEL.sendToServer(new ExitFpvPacket(var4, var5 != null && var5.isReturningHome()));
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

   private void onClientTick() {
      {
         com.fpvdrone.client.DroneSurfHandler.onClientTick();
         ExplosionAudioEngine.get().tick();
         DroneEngineAudioEngine.get().tick();
         this.pruneFrozenFramesTick();
         this.tickPostTeleportGrace();
      }
   }

   public static void startPostTeleportGrace() {
      fvsAOg = 60;
   }

   private void tickPostTeleportGrace() {
      if (fvsAOg > 0) {
         fvsAOg--;
         Minecraft var1 = Minecraft.getInstance();
         if (var1.player == null) {
            fvsAOg = 0;
         }
      }
   }

   private void pruneFrozenFramesTick() {
      if (++this.frozenFramePruneCounter >= 100) {
         this.frozenFramePruneCounter = 0;
         Minecraft var1 = Minecraft.getInstance();
         if (var1.level != null) {
            HashSet<UUID> var2 = new HashSet<>(playerToActiveDrone.values());

            for (Entity var4 : var1.level.entitiesForRendering()) {
               if (var4 instanceof DroneEntity) {
                  var2.add(var4.getUUID());
               }
            }

            UUID var5 = null; // 1.1.8-PENDING: the loitering munition being flown (MunitionClientState.ivDR)
            if (var5 != null) {
               var2.add(var5);
            }

            FrozenFrameManager.getInstance().pruneStale(var2);
         }
      }
   }

   private void onPlayerTick(Player var2) {
      UUID var3 = var2.getUUID();
      if (var2.level().isClientSide()) {
         Minecraft var4 = Minecraft.getInstance();
         if (var4.player != null && var4.player.getUUID().equals(var3)) {
            FPVClientMod.KKeMvpuBgEY var5 = b69s.get(var3);
            if (var5 != null) {
               int var6 = (int)Math.floor(var5.ibzfxic().x) >> 4;
               int var7 = (int)Math.floor(var5.ibzfxic().z) >> 4;
               if (var2.level().hasChunk(var6, var7)) {
                  b69s.remove(var3);
                  enterDjiViewAtPosition(var2, var5.v5suI(), var5.ibzfxic());
               }
            }

            FPVClientMod.Ol3TEDSog var14 = pendingTeleports.get(var3);
            if (var14 != null) {
               int var15 = (int)Math.floor(var14.v5suI().x) >> 4;
               int var8 = (int)Math.floor(var14.v5suI().z) >> 4;
               if (var2.level().hasChunk(var15, var8)) {
                  pendingTeleports.remove(var3);
                  teleportPilot(var2, var14.v5suI(), var14.ibzfxic(), var14.wuhB());
                  var14.dOfFwvo().run();
                  startPostTeleportGrace();
               }
            }

            this.keepPilotFlying(var2, var3);
            {
               if (lastKnownClientPlayer == null || !lastKnownClientPlayer.equals(var3)) {
                  clearClientState();
                  lastKnownClientPlayer = var3;
               }

               this.handleGogglesEquipState(var2);
               this.handleDjiControllerState(var2);
               UUID var16 = null;
               ItemStack var17 = var2.getItemBySlot(EquipmentSlot.HEAD);
               if (var17.getItem() instanceof FpvGogglesItem) {
                  var16 = FpvGogglesItem.getLinkedDroneId(var17).orElse(null);
               }

               if (var16 == null) {
                  for (int var9 = 0; var9 < var2.getInventory().getContainerSize(); var9++) {
                     ItemStack var10 = var2.getInventory().getItem(var9);
                     if (var10.getItem() instanceof FpvGogglesItem) {
                        var16 = FpvGogglesItem.getLinkedDroneId(var10).orElse(null);
                        if (var16 != null) {
                           break;
                        }
                     }
                  }
               }

               if (var16 != null) {
                  DroneEntity var18 = null;
                  if (var4.level != null) {
                     for (Entity var11 : var4.level.entitiesForRendering()) {
                        if (var11 instanceof DroneEntity var12 && var12.getUUID().equals(var16)) {
                           var18 = var12;
                           break;
                        }
                     }
                  }

                  if (var18 != null) {
                     boolean var20 = var18.idXga();
                     Boolean var21 = lastDroneBatteryStateMap.get(var16);
                     if (var21 != null && var18.tickCount >= 3) {
                        if (var20 && !var21) {
                           BootStateManager.getInstance().start();
                        } else if (!var20 && var21) {
                           BootStateManager.getInstance().reset();
                           BatteryManager.getInstance().fullReset();
                           if (playerToActiveDrone.containsKey(var3) && playerToActiveDrone.get(var3).equals(var16)) {
                              setArmed(var2, false);
                              PhysicsState var22 = getPhysicsState(var3);
                              if (var22 != null) {
                                 var22.setMotorsArmed(false);
                              }
                           }
                        }

                        lastDroneBatteryStateMap.put(var16, var20);
                     } else {
                        lastDroneBatteryStateMap.put(var16, var20);
                     }

                     if (var21 != null && var21 && !var20 && playerToActiveDrone.containsKey(var3) && playerToActiveDrone.get(var3).equals(var16)) {
                        batteryRemovedTickMap.put(var16, 0);
                     }

                     if (var21 != null && !var21 && var20) {
                        batteryRemovedTickMap.remove(var16);
                     }

                     Integer var23 = batteryRemovedTickMap.get(var16);
                     if (var23 != null) {
                        if (var23 >= 40) {
                           ItemStack var13 = var2.getItemBySlot(EquipmentSlot.HEAD);
                           if (var13.getItem() instanceof FpvGogglesItem) {
                              var2.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                              if (!var2.getInventory().add(var13)) {
                                 var2.drop(var13, false, net.minecraft.util.Prediction.SERVER_ONLY);
                              }
                           }

                           exitGogglesMode(var2);
                           batteryRemovedTickMap.remove(var16);
                        } else {
                           batteryRemovedTickMap.put(var16, var23 + 1);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void keepPilotFlying(Player var1, UUID var2) {
      boolean var3 = playerToActiveDrone.get(var2) != null;
      boolean var4 = hasPilotBodyMap.getOrDefault(var2, false);
      if (shouldKeepPilotFlying(var3, var4)) {
         enablePilotFlight(var1);
      }
   }

   static boolean shouldKeepPilotFlying(boolean var0, boolean var1) {
      return var0 && var1;
   }

   private void onRenderTick() {
      {
         Minecraft var2 = Minecraft.getInstance();
         LocalPlayer var3 = var2.player;
         if (var3 != null && var3.level().isClientSide()) {
            UUID var4 = var3.getUUID();
            if (var2.isPaused()) {
               ControllerReader.getInstance().poll();
               pxmzA = System.currentTimeMillis();
            } else if (var2.gui.screen() != null) {
               ControllerReader.getInstance().poll();
               PhysicsState var16 = getPhysicsState(var4);
               if (var16 != null) {
                  var var20 = getActiveDroneId(var4);
                  boolean var25 = var20.isPresent() && FpvStatusChecker.hasNoSignalState((UUID)var20.get());
                  boolean var31 = var25 && SignalManager.getInstance().isForcedNoSignal();
                  long var37 = System.currentTimeMillis();
                  float var42 = Math.min((float)(var37 - pxmzA) / 1000.0F, 0.1F);
                  pxmzA = var37;
                  if (!var25 || var31) {
                     var16.tick(var42);
                     this.syncDronePosition(var4, var3);
                  }
               } else {
                  pxmzA = System.currentTimeMillis();
                  var var21 = getActiveDroneId(var4);
                  if (var21.isPresent() && !FpvStatusChecker.hasNoSignalState((UUID)var21.get())) {
                     var var26 = getActiveDrone(var4);
                     if (var26.isPresent()) {
                        DroneEntity var32 = (DroneEntity)var26.get();
                        var3.setPos(var32.getX(), var32.getY(), var32.getZ());
                        var3.setOldPosAndRot();
                        PhysicsState var38 = getOrCreatePhysicsState(var4, var3);
                        var38.initFromDrone(var32);
                        applyDroneConfig(var32, var38);
                        BatteryManager.getInstance().initFromDrone(var32);
                     }
                  }
               }
            } else {
               ControllerReader.getInstance().poll();
               ControllerReader.getInstance().handleArmToggle(var4, null);
               ControllerReader.getInstance().pollActions(var2);
               if (getActiveDrone(var4).map(var0 -> var0.oEuCBB().bAcdvq9().gimbal()).orElse(false)) {
                  boolean var5 = ControllerReader.getConfig().getGimbalChannel() >= 0 && ControllerReader.getInstance().isPhysicalControllerConnected();
                  float var6;
                  if (var5) {
                     var6 = ControllerReader.getGimbal();
                  } else {
                     var6 = 0.0F;
                     boolean var7 = KeyManager.GIMBAL_UP_KEY.isDown();
                     boolean var8 = KeyManager.GIMBAL_DOWN_KEY.isDown();
                     if (var7 && !var8) {
                        var6 = 1.0F;
                     } else if (var8 && !var7) {
                        var6 = -1.0F;
                     }
                  }

                  GimbalState.getInstance().applyRate(var6);
               }

               if (playerToActiveDrone.containsKey(var4)) {
                  long var14 = var2.getWindow().handle();
                  boolean var22 = InputConstants.isKeyDown(InputConstants.KEY_PAGEUP);
                  boolean var27 = InputConstants.isKeyDown(InputConstants.KEY_PAGEDOWN);
                  if (var22 && !xYttSy) {
                     float var9 = Math.min(60.0F, FpvConfig.getDefaultAngle() + 1.0F);
                     FpvConfig.setDefaultAngle(var9);
                     var3.sendOverlayMessage(Component.literal("Camera Angle: " + (int)var9 + "°").withStyle(Style.EMPTY.withColor(16749622)));
                  }

                  if (var27 && !prevPageDownDown) {
                     float var33 = Math.max(-60.0F, FpvConfig.getDefaultAngle() - 1.0F);
                     FpvConfig.setDefaultAngle(var33);
                     var3.sendOverlayMessage(Component.literal("Camera Angle: " + (int)var33 + "°").withStyle(Style.EMPTY.withColor(16749622)));
                  }

                  xYttSy = var22;
                  prevPageDownDown = var27;
               }

               PhysicsState var15 = getPhysicsState(var4);
               if (!isArmed(var4)) {
                  if (var15 != null) {
                     var var18 = getActiveDroneId(var4);
                     boolean var23 = var18.isPresent() && FpvStatusChecker.hasNoSignalState((UUID)var18.get());
                     boolean var29 = var23 && SignalManager.getInstance().isForcedNoSignal();
                     long var35 = System.currentTimeMillis();
                     float var41 = Math.min((float)(var35 - pxmzA) / 1000.0F, 0.1F);
                     pxmzA = var35;
                     if (!var23 || var29) {
                        var15.tick(var41);
                        this.syncDronePosition(var4, var3);
                     }
                  } else {
                     var var19 = getActiveDroneId(var4);
                     if (var19.isPresent() && !FpvStatusChecker.hasNoSignalState((UUID)var19.get())) {
                        var var24 = getActiveDrone(var4);
                        if (var24.isPresent()) {
                           DroneEntity var30 = (DroneEntity)var24.get();
                           var3.setPos(var30.getX(), var30.getY(), var30.getZ());
                           var3.setOldPosAndRot();
                           PhysicsState var36 = getOrCreatePhysicsState(var4, var3);
                           var36.initFromDrone(var30);
                           applyDroneConfig(var30, var36);
                           BatteryManager.getInstance().initFromDrone(var30);
                           if (var30.isThermal() && !thermalAutoEnabled) {
                              ThermalCameraState var39 = ThermalCameraState.getInstance();
                              if (!var39.isEnabled()) {
                                 var39.toggle();
                              }

                              thermalAutoEnabled = true;
                           }
                        }
                     }
                  }
               } else {
                  long var17 = System.currentTimeMillis();
                  float var28 = Math.min((float)(var17 - pxmzA) / 1000.0F, 0.1F);
                  pxmzA = var17;
                  if (var15 != null) {
                     this.updateSignalState(var3);
                     var var34 = getActiveDrone(var4);
                     var34.ifPresent(var1x -> {
                        float var2x = var1x.qz71Ga() / 1000.0F;
                        var15.setMass(var2x);
                     });
                     checkLowBatteryReturnHome(var3, var4, var15);
                     if (!SignalManager.getInstance().isControlBlocked()) {
                        var15.tick(var28);
                     } else if (SignalManager.getInstance().isForcedNoSignal()) {
                        var15.setMotorsArmed(false);
                        var15.tick(var28);
                     } else {
                        this.applyFailsafe(var3, var15);
                     }

                     this.syncDronePosition(var4, var3);
                     if (var2.level != null) {
                        ArrayList<DroppedBombEntity> var10 = new ArrayList<>();

                        for (Entity var12 : var2.level.entitiesForRendering()) {
                           if (var12 instanceof DroppedBombEntity var13 && var13.zTT3nw6() && !var13.isRemoved() && var3.getUUID().equals(var13.jGhI2())) {
                              var10.add(var13);
                           }
                        }

                        for (DroppedBombEntity var43 : var10) {
                           if (!var43.isRemoved()) {
                              var43.v5suI((double)var28);
                              var43.v5suI();
                           }
                        }
                     }

                     if (var15.consumeReturnHomeArrived()) {
                        var15.setMotorsArmed(false);
                        finalizeDroneState(var3);
                        var3.sendOverlayMessage(Component.translatable("fpvdrone.status.rth_landed").withStyle(Style.EMPTY.withColor(16749622)));
                     }
                  }
               }
            }
         }
      }
   }

   private void onRenderTickEnd() {
      {
         Minecraft var2 = Minecraft.getInstance();
         LocalPlayer var3 = var2.player;
         if (var3 != null && var3.level().isClientSide()) {
            KeyManager.processPendingDrop(var2);
         }
      }
   }

   private void updateSignalState(Player var1) {
      UUID var2 = var1.getUUID();
      var var3 = getActiveDrone(var2);
      boolean var4 = var3.map(DroneEntity::xogV5KBhg).orElse(false);
      SignalManager.getInstance().setDjiLink(var4);
      LowResRenderer.getInstance().setDjiFeed(var4);
      if (var3.isPresent()) {
         SignalManager.getInstance().tick((DroneEntity)var3.get());
      } else {
         var var5 = getActiveDroneId(var2);
         if (var5.isPresent() && FpvStatusChecker.isDroneFalling((UUID)var5.get())) {
            return;
         }

         SignalManager.getInstance().tick(var1.position(), var1.level().getGameTime());
      }

      if (SignalManager.getInstance().isControlBlocked()) {
         getActiveDroneId(var2).ifPresent(var0 -> {
            if (!FpvStatusChecker.hasNoSignalState(var0) && !FpvStatusChecker.isDroneFalling(var0)) {
               FpvStatusChecker.markSignalLost(var0);
            }
         });
      }
   }

   private void applyFailsafe(Player var1, PhysicsState var2) {
      var var3 = getActiveDroneId(var1.getUUID());
      var3.ifPresent(var0 -> ModNetwork.CHANNEL.sendToServer(new DroneSignalLostPacket(var0)));
      var2.setFalling(true);
      var2.setFallingParams(FallType.DEAD_DROP.getThrustFactor(), FallType.DEAD_DROP.getDragMultiplier());
      var3.ifPresent(FPVClientMod::onSignalLossDestroyed);
   }

   private void syncDronePosition(UUID var1, Player var2) {
      getActiveDrone(var1)
         .ifPresent(
            var2x -> {
               if (!FpvStatusChecker.isDroneDestroyed(var2x.getUUID())) {
                  var2x.xOld = var2.xOld;
                  var2x.yOld = var2.yOld;
                  var2x.zOld = var2.zOld;
                  var2x.xRotO = var2x.getXRot();
                  var2x.yRotO = var2x.getYRot();
                  var2x.yBodyRotO = var2x.yBodyRot;
                  var2x.yHeadRotO = var2x.getYHeadRot();
                  PhysicsState var3 = getPhysicsState(var1);
                  float var4 = 0.0F;
                  float var5 = 0.0F;
                  float var6 = 0.0F;
                  double var7 = 0.0;
                  double var9 = 0.0;
                  double var11 = 0.0;
                  if (var3 != null) {
                     Vector3f var13 = var3.getDroneLook();
                     Vector3f var14 = var3.getDroneUp();
                     float[] var15 = Transforms.getWorldEulerAngles(var13, var14);
                     var4 = var15[0];
                     var5 = var15[1];
                     var6 = var15[2];
                     var2x.setYRot(var4);
                     var2x.setXRot(var5);
                     var2x.setDroneRoll(var6);
                     lastKnownDroneRotations.put(var2x.getUUID(), new float[]{var4, var5, var6});
                     var2x.setYBodyRot(var4);
                     var2x.setYHeadRot(var4);
                     Vector3f var16 = var3.getVelocity();
                     Vec3 var17 = PhysicsUnits.toMinecraftVelocity(var16);
                     var7 = var17.x;
                     var9 = var17.y;
                     var11 = var17.z;
                     var2x.setDeltaMovement(var7, var9, var11);
                  }

                  var2x.setPos(var2.getX(), var2.getY(), var2.getZ());
                  long var25 = System.currentTimeMillis();
                  if (var25 - lastPhysicsTime >= 50L) {
                     lastPhysicsTime = var25;
                     boolean var26 = false;
                     float var27 = 0.0F;
                     float var28 = 0.0F;
                     float var18 = 0.0F;
                     float var19 = 1.0F;
                     if (var3 != null) {
                        var26 = var3.isMotorsArmed();
                        Vector3f var20 = var3.getDroneLook();
                        Vector3f var21 = var3.getDroneUp();
                        Vector3f var22 = var3.getDroneLeft();
                        Matrix3f var23 = new Matrix3f();
                        var23.m00(-var22.x);
                        var23.m01(-var22.y);
                        var23.m02(-var22.z);
                        var23.m10(var21.x);
                        var23.m11(var21.y);
                        var23.m12(var21.z);
                        var23.m20(-var20.x);
                        var23.m21(-var20.y);
                        var23.m22(-var20.z);
                        Quaternionf var24 = new Quaternionf().setFromNormalized(var23);
                        var27 = var24.x;
                        var28 = var24.y;
                        var18 = var24.z;
                        var19 = var24.w;
                     }

                     ModNetwork.CHANNEL
                        .sendToServer(
                           new DroneSyncPacket(
                              var2x.getUUID(),
                              var2.getX(),
                              var2.getY(),
                              var2.getZ(),
                              var4,
                              var5,
                              var6,
                              var7,
                              var9,
                              var11,
                              true,
                              var26,
                              Optional.empty(),
                              !var26 || var3 != null && var3.isThrottleCut()
                                 ? 0.0F
                                 : Math.max(0.1F, Mth.clamp((ControllerReader.getThrottle() + 1.0F) / 2.0F, 0.0F, 1.0F)),
                              var27,
                              var28,
                              var18,
                              var19,
                              GimbalState.getInstance().getPitch()
                           )
                        );
                  }
               }
            }
         );
   }

   // 1.1.8's EntityEvent.Size handler (pilot shrinks to 0.55 x 0.28 while flying) is PlayerDimensionsMixin.

   /** 1.1.8: a DJI view waiting for its chunk to load (drone id, drone position). */
   private record KKeMvpuBgEY(UUID v5suI, Vec3 ibzfxic) {
   }

   /** 1.1.8: a pilot teleport waiting for its chunk to load (position, yaw, pitch, what to run after). */
   private record Ol3TEDSog(Vec3 v5suI, Float ibzfxic, Float wuhB, Runnable dOfFwvo) {
   }
}
