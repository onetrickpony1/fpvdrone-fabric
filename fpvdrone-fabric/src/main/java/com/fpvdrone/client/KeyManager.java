package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
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
        // 1.1.8-PENDING: exit the drone view. With goggles worn: take them off (RemoveGogglesPacket,
        // "fpvdrone.status.goggles_removed"); with a DJI controller in hand: toggle the Mavic view. Needs FPVClientMod.
        // In a loitering munition: leave the munition view (tactical map).
    }

    static void handleDropBomb(Minecraft mc) {
        // 1.1.8-PENDING: bomb bay / payload release (DroppedBombEntity, projectile definitions) and the Opener mine vest.
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
        // 1.1.8-PENDING: return to home on GPS drones (Mavic): needs DroneEntity's drone kind and PhysicsState RTH.
    }

    static void handleHoverHold(Minecraft mc) {
        // 1.1.8-PENDING: hover hold (brake) on GPS drones (Mavic): needs DroneEntity's drone kind and PhysicsState hover hold.
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
