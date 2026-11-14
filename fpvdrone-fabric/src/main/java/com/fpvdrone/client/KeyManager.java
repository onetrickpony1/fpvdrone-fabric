package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import com.fpvdrone.render.GimbalState;
import com.fpvdrone.physics.PhysicsUnits;
import com.fpvdrone.network.DropPayloadPacket;
import com.fpvdrone.network.BombBayDropPacket;
import com.fpvdrone.entity.DroppedBombEntity;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.PayloadGeometry;
import com.fpvdrone.data.AttachTransform;
import com.fpvdrone.client.osd.BootStateManager;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.network.ChangeGogglesChannelPacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.physics.PhysicsState;
import com.fpvdrone.render.CameraManager;
import com.fpvdrone.render.CameraZoom;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class KeyManager {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("fpvdrone", "fpvdrone"));
    private static final int UNBOUND = InputConstants.UNKNOWN.getValue();

    // 1.1.8: every drone action has a key (and optionally a controller switch, see DroneAction).
    public static final KeyMapping FPV_TOGGLE_KEY = new KeyMapping("key.fpvdrone.bind.arm", InputConstants.KEY_N, CATEGORY);
    public static final KeyMapping EXIT_KEY = new KeyMapping("key.fpvdrone.bind.exit", InputConstants.KEY_G, CATEGORY);
    public static final KeyMapping BOMB_KEY = new KeyMapping("key.fpvdrone.bind.bomb", InputConstants.KEY_B, CATEGORY);
    public static final KeyMapping ZOOM_KEY = new KeyMapping("key.fpvdrone.bind.zoom", InputConstants.KEY_C, CATEGORY);
    public static final KeyMapping RTH_KEY = new KeyMapping("key.fpvdrone.bind.rth", InputConstants.KEY_P, CATEGORY);
    public static final KeyMapping HOVER_KEY = new KeyMapping("key.fpvdrone.bind.hover", InputConstants.KEY_H, CATEGORY);
    public static final KeyMapping FIRE_ROCKET_KEY = new KeyMapping("key.fpvdrone.bind.fire_rocket", InputConstants.KEY_R, CATEGORY);
    public static final KeyMapping FOCUS_KEY = new KeyMapping("key.fpvdrone.bind.focus", InputConstants.KEY_F, CATEGORY);
    public static final KeyMapping INTERACT_KEY = new KeyMapping("key.fpvdrone.bind.interact", InputConstants.KEY_F, CATEGORY);

    // 1.1.8: camera keys.
    public static final KeyMapping FREE_LOOK_KEY = new KeyMapping("key.fpvdrone.free_look", InputConstants.KEY_C, CATEGORY);
    public static final KeyMapping GIMBAL_UP_KEY = new KeyMapping("key.fpvdrone.gimbal_up", UNBOUND, CATEGORY);
    public static final KeyMapping GIMBAL_DOWN_KEY = new KeyMapping("key.fpvdrone.gimbal_down", UNBOUND, CATEGORY);

    // 1.1.8: keyboard flying keys (they were hard-wired to W/S/A/D before). Climb/descend are for GPS-mode drones (DJI).
    public static final KeyMapping THROTTLE_UP_KEY = new KeyMapping("key.fpvdrone.throttle_up", InputConstants.KEY_W, CATEGORY);
    public static final KeyMapping THROTTLE_DOWN_KEY = new KeyMapping("key.fpvdrone.throttle_down", InputConstants.KEY_S, CATEGORY);
    public static final KeyMapping THROTTLE_CLIMB_KEY = new KeyMapping("key.fpvdrone.throttle_climb", InputConstants.KEY_SPACE, CATEGORY);
    public static final KeyMapping THROTTLE_DESCEND_KEY = new KeyMapping("key.fpvdrone.throttle_descend", InputConstants.KEY_LALT, CATEGORY);
    public static final KeyMapping YAW_LEFT_KEY = new KeyMapping("key.fpvdrone.yaw_left", InputConstants.KEY_A, CATEGORY);
    public static final KeyMapping YAW_RIGHT_KEY = new KeyMapping("key.fpvdrone.yaw_right", InputConstants.KEY_D, CATEGORY);
    // 1.1.8-PENDING: key.fpvdrone.mouse_stick_center (loitering munitions), open_map (tactical map), thermal_* (thermal camera).

    // Port additions.
    public static final KeyMapping CLEAN_FEED_KEY = new KeyMapping("key.fpvdrone.clean_feed", InputConstants.KEY_F6, CATEGORY);
    public static final KeyMapping CYCLE_RESOLUTION_KEY = new KeyMapping("key.fpvdrone.cycle_resolution", InputConstants.KEY_F7, CATEGORY);
    public static final KeyMapping SETTINGS_KEY = new KeyMapping("key.fpvdrone.flight_settings", InputConstants.KEY_F8, CATEGORY);

    private static final Map<DroneAction, KeyMapping> ACTION_KEYS = new EnumMap<>(DroneAction.class);

    static {
        ACTION_KEYS.put(DroneAction.ARM, FPV_TOGGLE_KEY);
        ACTION_KEYS.put(DroneAction.EXIT, EXIT_KEY);
        ACTION_KEYS.put(DroneAction.BOMB, BOMB_KEY);
        ACTION_KEYS.put(DroneAction.ZOOM, ZOOM_KEY);
        ACTION_KEYS.put(DroneAction.RTH, RTH_KEY);
        ACTION_KEYS.put(DroneAction.HOVER, HOVER_KEY);
        ACTION_KEYS.put(DroneAction.FIRE_ROCKET, FIRE_ROCKET_KEY);
        ACTION_KEYS.put(DroneAction.FOCUS, FOCUS_KEY);
        ACTION_KEYS.put(DroneAction.INTERACT, INTERACT_KEY);
    }

    public static void init() {
        KeyMappingHelper.registerKeyMapping(SETTINGS_KEY);
        for (KeyMapping key : ACTION_KEYS.values()) {
            KeyMappingHelper.registerKeyMapping(key);
        }

        KeyMappingHelper.registerKeyMapping(FREE_LOOK_KEY);
        KeyMappingHelper.registerKeyMapping(GIMBAL_UP_KEY);
        KeyMappingHelper.registerKeyMapping(GIMBAL_DOWN_KEY);
        KeyMappingHelper.registerKeyMapping(THROTTLE_UP_KEY);
        KeyMappingHelper.registerKeyMapping(THROTTLE_DOWN_KEY);
        KeyMappingHelper.registerKeyMapping(THROTTLE_CLIMB_KEY);
        KeyMappingHelper.registerKeyMapping(THROTTLE_DESCEND_KEY);
        KeyMappingHelper.registerKeyMapping(YAW_LEFT_KEY);
        KeyMappingHelper.registerKeyMapping(YAW_RIGHT_KEY);
        KeyMappingHelper.registerKeyMapping(CYCLE_RESOLUTION_KEY);
        KeyMappingHelper.registerKeyMapping(CLEAN_FEED_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            tickBombRepeat(mc);
            while (SETTINGS_KEY.consumeClick()) {
                if (mc.player != null && !FPVMod.isArmed(mc.player.getUUID())) {
                    mc.gui.setScreen(new com.fpvdrone.client.gui.FlightSettingsScreen());
                }
            }
            while (CYCLE_RESOLUTION_KEY.consumeClick()) {
                if (mc.player != null) {
                    handleCycleResolution(mc);
                }
            }
            while (CLEAN_FEED_KEY.consumeClick()) {
                if (mc.player != null) {
                    CleanFeed.toggle(mc);
                }
            }
            for (Map.Entry<DroneAction, KeyMapping> entry : ACTION_KEYS.entrySet()) {
                while (entry.getValue().consumeClick()) {
                    if (mc.player != null && mc.gui.screen() == null) {
                        entry.getKey().run(mc);
                    }
                }
            }
        });
    }

    public static KeyMapping getKey(DroneAction action) {
        return ACTION_KEYS.get(action);
    }

    /** Short name of a key for on-screen hints ("N", "Left Alt"). */
    public static String keyName(InputConstants.Key key) {
        return key.getDisplayName().getString();
    }

    /** Name of a mapping's key, or the translated "None". */
    public static String keyName(KeyMapping mapping) {
        return mapping == null || mapping.isUnbound() ? I18n.get("fpvdrone.bind.none") : keyName(KeyMappingHelper.getBoundKeyOf(mapping));
    }

    /** What triggers an action: "CH5" when a controller channel is bound, else the key, else "None". */
    public static String bindingLabel(DroneAction action) {
        int channel = action.getChannel(ControllerReader.getConfig());
        if (channel >= 0) {
            return "CH" + (channel + 1);
        }

        KeyMapping mapping = ACTION_KEYS.get(action);
        return mapping != null && !mapping.isUnbound() ? keyName(KeyMappingHelper.getBoundKeyOf(mapping)) : "None";
    }

    /** Binding hint for the exit-view action (goggles/controller tooltips). */
    public static String exitLabel() {
        return bindingLabel(DroneAction.EXIT);
    }

    public static void bindKey(KeyMapping mapping, InputConstants.Key key) {
        mapping.setKey(key);
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    /** True when a key mapping (not a mouse button) is held; falls back to the default key when unbound. */
    public static boolean isKeyHeld(KeyMapping mapping, int defaultKey) {
        int key = defaultKey;
        if (mapping != null && !mapping.isUnbound()) {
            InputConstants.Key bound = KeyMappingHelper.getBoundKeyOf(mapping);
            if (bound.getType() != InputConstants.Type.KEYBOARD) {
                return mapping.isDown();
            }

            key = bound.getValue();
        }

        return key > 0 && InputConstants.isKeyDown(key);
    }

    /** F7 in creative while flying: the next video resolution (survival scales it with range instead). */
    private static void handleCycleResolution(Minecraft mc) {
        if (mc.player.isCreative() && !FPVMod.getActiveDroneId(mc.player.getUUID()).isEmpty()) {
            com.fpvdrone.render.LowResRenderer renderer = com.fpvdrone.render.LowResRenderer.getInstance();
            renderer.cycleResolution();
            com.fpvdrone.FpvConfig.setVideoResolution(renderer.getResolution());
            mc.player.sendOverlayMessage(Component.literal("Resolution: " + renderer.getResolution().getDisplayName())
                .withStyle(Style.EMPTY.withColor(16749622)));
        }
    }

    /** Shift+scroll with goggles worn or held cycles the goggles channel. Returns true when the scroll was consumed. */
    public static boolean onMouseScroll(double scrollDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.gui.screen() == null) {
            if (mc.hasShiftDown()) {
                ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
                ItemStack mainHand = mc.player.getMainHandItem();
                if (helmet.getItem() instanceof FpvGogglesItem || mainHand.getItem() instanceof FpvGogglesItem) {
                    ModNetwork.CHANNEL.sendToServer(new ChangeGogglesChannelPacket(scrollDelta > 0.0 ? 1 : -1));
                    return true;
                }
            }

            return FPVMod.getActiveDroneId(mc.player.getUUID()).isPresent();
        }

        return false;
    }

    // ---- action handlers ----

    public static void handleMotorToggle(Minecraft mc) {
        if (mc.player != null) {
            UUID playerId = mc.player.getUUID();
            if (!FPVMod.getActiveDroneId(playerId).isEmpty()) {
                if (!BootStateManager.getInstance().isBootActive()) {
                    Optional<DroneEntity> droneOpt = FPVMod.getActiveDrone(playerId);
                    if (!droneOpt.isEmpty()) {
                        DroneEntity drone = droneOpt.get();
                        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
                        if (helmet.getItem() instanceof FpvGogglesItem) {
                            Optional<UUID> linkedDroneId = FpvGogglesItem.getLinkedDroneId(helmet);
                            if (linkedDroneId.isPresent() && !RemoteStateChecker.hasLinkedRemote(mc.player, linkedDroneId.get())) {
                                mc.player
                                    .sendOverlayMessage(Component.translatable("fpvdrone.status.connect_remote").withStyle(Style.EMPTY.withColor(16749622)));
                                return;
                            }
                        }

                        boolean newState = !drone.areMotorsArmed();
                        if (newState && !drone.hasBattery()) {
                            mc.player.sendOverlayMessage(Component.translatable("fpvdrone.status.no_battery").withStyle(Style.EMPTY.withColor(16749622)));
                        } else {
                            drone.setMotorsArmed(newState);
                            if (newState) {
                                mc.player.setPos(drone.getX(), drone.getY(), drone.getZ());
                                mc.player.setOldPosAndRot();
                                FPVMod.enterFPVMode(mc.player, drone);
                                PhysicsState physics = FPVMod.getPhysicsState(playerId);
                                if (physics != null) {
                                    physics.setMotorsArmed(true);
                                }
                            } else {
                                PhysicsState physics = FPVMod.getPhysicsState(playerId);
                                if (physics != null) {
                                    physics.setMotorsArmed(false);
                                }

                                FPVMod.finalizeDroneState(mc.player);
                            }
                        }
                    }
                }
            }
        }
    }

    static void handleExitView(Minecraft mc) {
        // 1.1.8: take the goggles off / leave the DJI view. 1.1.8-PENDING: in a loitering munition, leave the munition view.
        if (mc.player != null) {
            com.fpvdrone.FPVClientMod.exitDroneView(mc.player);
        }
    }

    private static boolean dropRequested = false;
    private static int bombRepeatTicks = 0;

    static void handleDropBomb(Minecraft mc) {
        // 1.1.8: the release happens at the end of the frame (processPendingDrop), after the renderer has written where
        // each payload hangs, so the predicted bomb starts exactly there. 1.1.8-PENDING: loitering munition payloads.
        dropRequested = true;
        dropOpenerMine(mc);
    }

    /** 1.1.8: holding the bomb key on a drone with a bomb bay keeps dropping, one bomb every 3 ticks. */
    public static void tickBombRepeat(Minecraft mc) {
        if (mc.player != null && mc.gui.screen() == null && BOMB_KEY.isDown()) {
            Optional<DroneEntity> drone = FPVMod.getActiveDrone(mc.player.getUUID());
            if (drone.isPresent() && drone.get().pyHY5()) {
                if (++bombRepeatTicks >= 3) {
                    bombRepeatTicks = 0;
                    dropRequested = true;
                }
            } else {
                bombRepeatTicks = 0;
            }
        } else {
            bombRepeatTicks = 0;
        }
    }

    /** Called once per frame after the world rendered (1.1.8 KeyManager.ForgeEvents.ibzfxic). */
    public static void processPendingDrop(Minecraft mc) {
        if (dropRequested) {
            dropRequested = false;
            dropPayload(mc);
        }
    }

    private static void dropOpenerMine(Minecraft mc) {
        // 1.1.8-PENDING: with the "Can Opener" vest worn, the drop key also sets off its TM-62 (Packet_Vvqb4Q5l).
    }

    /** 1.1.8 (KeyManager.ForgeEvents.rxYnaa): release the hanging payload or the next bomb-bay station. */
    private static void dropPayload(Minecraft var0) {
       if (var0.player != null) {
          UUID var1 = var0.player.getUUID();
          if (!FPVMod.getActiveDroneId(var1).isEmpty()) {
             var var2 = FPVMod.getActiveDrone(var1);
             if (!var2.isEmpty()) {
                DroneEntity var3 = (DroneEntity)var2.get();
                Vec3 var4 = PayloadGeometry.v5suI(var0);
                PhysicsState var5 = FPVMod.getPhysicsState(var1);
                if (var5 != null) {
                   int var6 = var3.mM7DsB9AR() ? var3.zQOhlhTZx() : -1;
                   boolean var8 = false;
                   ProjectileDefinition var9 = null;
                   AttachTransform var7;
                   if (var6 >= 0) {
                      var7 = var3.oEuCBB().uKpZYj().stations().get(var6).wuhB();
                   } else {
                      String var10 = var3.i263D9();
                      ProjectileDefinition var11 = var10.isEmpty() ? null : ProjectileDefinitions.ibzfxic(var10).orElse(null);
                      if (var11 == null || !var11.rsDd50tfV()) {
                         var0.player.sendOverlayMessage(Component.translatable("fpvdrone.status.no_bombs").withStyle(Style.EMPTY.withColor(16749622)));
                         return;
                      }

                      var7 = var3.oEuCBB().dOfFwvo(var10).orElse(AttachTransform.v5suI("payload_mount"));
                      var8 = true;
                      var9 = var11;
                   }

                   String var46 = var8 ? var3.i263D9() : var3.oEuCBB().uKpZYj().projectile();
                   float var47 = ProjectileDefinitions.ibzfxic(var46).map(ProjectileDefinition::qlv6NKWnH).orElse(0.0F);
                   if (var47 > 0.0F) {
                      float var12 = var7.ibzfxic() / 16.0F;
                      float var13 = var7.dOfFwvo() / 16.0F;
                      float var14 = Mth.clamp(0.1F * var47 * var12, -8.0F, 8.0F);
                      float var15 = Mth.clamp(0.035F * var47 * var13, -8.0F, 8.0F);
                      var5.applyReleaseKick(var15, var14);
                      if (var3.oEuCBB().bAcdvq9().gimbal()) {
                         GimbalState.getInstance().kick(var15 * 0.4F, var14 * 0.4F);
                      }
                   }

                   Vector3f var48 = var5.getDroneLook();
                   Vector3f var49 = var5.getDroneUp();
                   Vector3f var50 = var5.getDroneLeft();
                   Vec3 var51 = new Vec3(var48.x, var48.y, var48.z);
                   Vec3 var16 = new Vec3(var49.x, var49.y, var49.z);
                   Vec3 var17 = new Vec3(var50.x, var50.y, var50.z);
                   Vec3 var18 = PayloadGeometry.v5suI(var3, var7, var17, var16, var51);
                   Vec3 var19 = PhysicsUnits.toMinecraftVelocity(var5.getVelocity());
                   if (var8) {
                      ProjectileDefinition.HangSwing var20 = var9.bAcdvq9();
                      if (var20 == null) {
                         var20 = ProjectileDefinition.HangSwing.v5suI;
                      }

                      double var21 = var20.pivotY() / 16.0;
                      Vec3 var23 = var4.add(var18).add(var16.x * var21, var16.y * var21, var16.z * var21);
                      float[] var24 = var3.vmpmU();
                      double var25 = var24 != null ? Math.toRadians(var24[0]) : 0.0;
                      double var27 = var24 != null ? Math.toRadians(var24[1]) : 0.0;
                      double var29 = -var21 * Math.cos(var25) * Math.sin(var27);
                      double var31 = -var21 * Math.cos(var25) * Math.cos(var27);
                      double var33 = -var21 * Math.sin(var25);
                      Vec3 var35 = var23.add(var29, var31, var33);
                      Vec3 var36 = PayloadAnchors.v5suI(var3.getUUID());
                      Vec3 var37 = var36 != null ? var36 : var35;
                      var37 = var37.subtract(0.0, 0.07F, 0.0);
                      // 1.1.8 also remembered the release point for its F3+B debug marker (X8zQ2N); not ported.
                      Quaternionf var38 = PayloadAnchors.ibzfxic(var3.getUUID());
                      float var39 = var38 != null ? var38.x : 0.0F;
                      float var40 = var38 != null ? var38.y : 0.0F;
                      float var41 = var38 != null ? var38.z : 0.0F;
                      float var42 = var38 != null ? var38.w : 1.0F;
                      long var43 = var3.level().getGameTime();
                      DroppedBombEntity var45 = DroppedBombEntity.v5suI(
                         var3.level(),
                         var37,
                         var19,
                         new Vec3(var51.x, var51.y, var51.z),
                         var46,
                         var0.player.getUUID(),
                         -1,
                         var39,
                         var40,
                         var41,
                         var42,
                         var43
                      );
                      PredictedBombs.v5suI(var45);
                      ModNetwork.CHANNEL
                         .sendToServer(
                            new DropPayloadPacket(
                               var3.getUUID(),
                               var37.x,
                               var37.y,
                               var37.z,
                               var19.x,
                               var19.y,
                               var19.z,
                               (float)var51.x,
                               (float)var51.y,
                               (float)var51.z,
                               var39,
                               var40,
                               var41,
                               var42,
                               var43
                            )
                         );
                      var3.l2IWOAGO();
                   } else {
                      Vec3 var52 = PayloadAnchors.v5suI(var3.getUUID(), var6);
                      Vec3 var53 = var52 != null ? var52 : var4.add(var18);
                      // 1.1.8 also remembered the release point for its F3+B debug marker (X8zQ2N); not ported.
                      long var22 = var3.level().getGameTime();
                      DroppedBombEntity var54 = DroppedBombEntity.v5suI(
                         var3.level(),
                         var53,
                         var19,
                         new Vec3(var51.x, var51.y, var51.z),
                         var46,
                         var0.player.getUUID(),
                         var6,
                         0.0F,
                         0.0F,
                         0.0F,
                         1.0F,
                         var22
                      );
                      PredictedBombs.v5suI(var54);
                      ModNetwork.CHANNEL
                         .sendToServer(
                            new BombBayDropPacket(
                               var3.getUUID(),
                               var6,
                               var53.x,
                               var53.y,
                               var53.z,
                               var19.x,
                               var19.y,
                               var19.z,
                               (float)var51.x,
                               (float)var51.y,
                               (float)var51.z,
                               var22
                            )
                         );
                      var3.v5suI(var6);
                   }
                }
             }
          }
       }
    }


    static void handleZoom(Minecraft mc) {
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        UUID playerId = mc.player.getUUID();
        Optional<CameraManager.DroneCamera> camera = FPVMod.getActiveDrone(playerId).map(CameraManager::cameraOf);
        if (camera.isPresent() && camera.get().gimbal()) {
            CameraZoom zoom = CameraZoom.getInstance();
            zoom.cycle(camera.get().zoomLevels());
            mc.player.sendOverlayMessage(Component.literal("Zoom: " + zoom.getLabel()).withStyle(Style.EMPTY.withColor(16749622)));
        }
    }

    static void handleReturnToHome(Minecraft mc) {
        // 1.1.8: GPS drones (the Mavic) only; a second press cancels.
        if (mc.player != null) {
            UUID playerId = mc.player.getUUID();
            Optional<DroneEntity> drone = FPVMod.getActiveDrone(playerId);
            if (drone.isPresent()) {
                if (!drone.get().xogV5KBhg()) {
                    mc.player.sendOverlayMessage(Component.translatable("fpvdrone.status.rth_unavailable").withStyle(Style.EMPTY.withColor(16749622)));
                } else if (FPVMod.isArmed(playerId)) {
                    PhysicsState physics = FPVMod.getPhysicsState(playerId);
                    if (physics != null) {
                        if (physics.isReturningHome()) {
                            physics.stopReturnHome();
                            mc.player.sendOverlayMessage(Component.translatable("fpvdrone.status.rth_cancelled").withStyle(Style.EMPTY.withColor(16749622)));
                        } else if (com.fpvdrone.FPVClientMod.startReturnHome(mc.player)) {
                            mc.player.sendOverlayMessage(Component.translatable("fpvdrone.status.rth_active").withStyle(Style.EMPTY.withColor(16749622)));
                        }
                    }
                }
            }
        }
    }

    static void handleHoverHold(Minecraft mc) {
        // 1.1.8: GPS drones (the Mavic) only; toggles the brake.
        if (mc.player != null) {
            UUID playerId = mc.player.getUUID();
            Optional<DroneEntity> drone = FPVMod.getActiveDrone(playerId);
            if (drone.isPresent()) {
                if (!drone.get().xogV5KBhg()) {
                    mc.player.sendOverlayMessage(Component.translatable("fpvdrone.status.hover_unavailable").withStyle(Style.EMPTY.withColor(16749622)));
                } else {
                    PhysicsState physics = FPVMod.getPhysicsState(playerId);
                    if (physics != null) {
                        boolean on = !physics.isHoverHold();
                        physics.setHoverHold(on);
                        mc.player.sendOverlayMessage(
                            Component.translatable(on ? "fpvdrone.status.hover_on" : "fpvdrone.status.hover_off").withStyle(Style.EMPTY.withColor(16749622))
                        );
                    }
                }
            }
        }
    }

    static void handleFireRocket(Minecraft mc) {
        // 1.1.8-PENDING: fire a rocket from a loitering munition's rocket pod.
    }

    static void handleFocus(Minecraft mc) {
        // 1.1.8-PENDING: camera focus toggle on loitering munitions.
    }

    static void handleInteract(Minecraft mc) {
        // 1.1.8-PENDING: open the interact (hover) menu on the drone being looked at.
    }
}
