package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import com.fpvdrone.client.osd.BootStateManager;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.network.ChangeGogglesChannelPacket;
import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.physics.PhysicsState;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class KeyManager {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("fpvdrone", "fpvdrone"));
    public static final KeyMapping FPV_TOGGLE_KEY = new KeyMapping("key.fpvdrone.arm_motors", com.mojang.blaze3d.platform.InputConstants.KEY_N, CATEGORY);

    public static final KeyMapping SETTINGS_KEY = new KeyMapping("key.fpvdrone.flight_settings", com.mojang.blaze3d.platform.InputConstants.KEY_F8, CATEGORY);

    public static void init() {
        KeyMappingHelper.registerKeyMapping(SETTINGS_KEY);
        KeyMappingHelper.registerKeyMapping(FPV_TOGGLE_KEY);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (SETTINGS_KEY.consumeClick()) {
                if (mc.player != null && !FPVMod.isArmed(mc.player.getUUID())) {
                    mc.gui.setScreen(new com.fpvdrone.client.gui.FlightSettingsScreen());
                }
            }
            while (FPV_TOGGLE_KEY.consumeClick()) {
                if (mc.player != null) {
                    handleMotorToggle(mc);
                }
            }
        });
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

    private static void handleMotorToggle(Minecraft mc) {
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
}
