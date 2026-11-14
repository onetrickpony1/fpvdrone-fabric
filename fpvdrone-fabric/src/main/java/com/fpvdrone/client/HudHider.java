package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import com.fpvdrone.item.FpvGogglesItem;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class HudHider {
    public static boolean isWearingLinkedGoggles() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        } else {
            ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
            return helmet.getItem() instanceof FpvGogglesItem && FpvGogglesItem.isLinked(helmet);
        }
    }

    public static boolean shouldBlockInput() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        } else if (MunitionClientState.iElAbEZe()) {
            // 1.1.8: flying a loitering munition hides the HUD
            return true;
        } else if (FPVMod.isArmed(mc.player.getUUID())) {
            return true;
        } else if (FPVMod.getActiveDroneId(mc.player.getUUID()).isPresent()) {
            // 1.1.8: in a drone's view the HUD stays hidden while it has signal
            return !FpvStatusChecker.hasNoSignalState(FPVMod.getActiveDroneId(mc.player.getUUID()).get());
        } else {
            ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
            if (!(helmet.getItem() instanceof FpvGogglesItem)) {
                return false;
            } else if (!FpvGogglesItem.isLinked(helmet)) {
                return false;
            } else {
                Optional<UUID> linkedId = FpvGogglesItem.getLinkedDroneId(helmet);
                return linkedId.isPresent() && !FpvStatusChecker.hasNoSignalState(linkedId.get());
            }
        }
    }

    public static void init() {
        for (var id : java.util.List.of(
            VanillaHudElements.HOTBAR, VanillaHudElements.HELD_ITEM_TOOLTIP,
            VanillaHudElements.EXPERIENCE_LEVEL, VanillaHudElements.INFO_BAR,
            VanillaHudElements.FOOD_BAR, VanillaHudElements.HEALTH_BAR,
            VanillaHudElements.ARMOR_BAR, VanillaHudElements.CROSSHAIR)) {
            HudElementRegistry.replaceElement(id, original -> (graphics, delta) -> {
                if (!shouldBlockInput()) original.extractRenderState(graphics, delta);
            });
        }
    }
}
