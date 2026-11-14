package com.fpvdrone.client.thermal;

import com.fpvdrone.FPVMod;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.entity.DroneEntity;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Optional;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/**
 * 1.1.8 KeyManager thermal keys: thermal_toggle (End), thermal_nuc (U), thermal_focus_mode (Home).
 * <p>
 * 1.1.8 has no AGC key: its handler named handleThermalAgcMode is bound to thermal_focus_mode and toggles the depth of
 * field ("Focus: ON/OFF"), and its fields THERMAL_AGC_MODE_KEY / THERMAL_FOCUS_MODE_KEY are really the yaw left/right
 * flying keys (A/D). There is no palette key either: the palette stays WHOT in 1.1.8 (the cycle method has no caller).
 * Kept separate from {@link KeyManager} to keep the thermal slice self-contained.
 */
public final class ThermalKeys {
   public static final KeyMapping THERMAL_TOGGLE_KEY = new KeyMapping("key.fpvdrone.thermal_toggle", InputConstants.KEY_END, KeyManager.CATEGORY);
   public static final KeyMapping THERMAL_NUC_KEY = new KeyMapping("key.fpvdrone.thermal_nuc", InputConstants.KEY_U, KeyManager.CATEGORY);
   public static final KeyMapping THERMAL_FOCUS_KEY = new KeyMapping("key.fpvdrone.thermal_focus_mode", InputConstants.KEY_HOME, KeyManager.CATEGORY);
   private static final int MESSAGE_COLOR = 16749622;

   private ThermalKeys() {
   }

   static void init() {
      KeyMappingHelper.registerKeyMapping(THERMAL_TOGGLE_KEY);
      KeyMappingHelper.registerKeyMapping(THERMAL_NUC_KEY);
      KeyMappingHelper.registerKeyMapping(THERMAL_FOCUS_KEY);
      ClientTickEvents.END_CLIENT_TICK.register(mc -> {
         while (THERMAL_TOGGLE_KEY.consumeClick()) {
            if (mc.gui.screen() == null) {
               handleThermalToggle(mc);
            }
         }
         while (THERMAL_NUC_KEY.consumeClick()) {
            if (mc.gui.screen() == null) {
               handleThermalNUC(mc);
            }
         }
         while (THERMAL_FOCUS_KEY.consumeClick()) {
            if (mc.gui.screen() == null) {
               handleThermalFocus(mc);
            }
         }
      });
   }

   /**
    * 1.1.8 handleThermalToggle: only works while viewing a loitering munition whose camera has ThermalToggle, and
    * not on a thermal drone (thermal drones switch it on by themselves).
    * 1.1.8-PENDING (munition slice): the munition view check (GAduSQ8Kp.wuhB); until then the key does nothing.
    */
   public static void handleThermalToggle(Minecraft mc) {
      if (mc.player == null || !munitionThermalToggleAllowed()) {
         return;
      }
      Optional<DroneEntity> drone = FPVMod.getActiveDrone(mc.player.getUUID());
      if (drone.isEmpty() || !drone.get().isThermal()) {
         ThermalCameraState state = ThermalCameraState.getInstance();
         state.toggle();
         String key = state.isEnabled() ? "fpvdrone.thermal.on" : "fpvdrone.thermal.off";
         mc.player.sendOverlayMessage(Component.translatable(key).withStyle(Style.EMPTY.withColor(MESSAGE_COLOR)));
      }
   }

   /** 1.1.8-PENDING (munition slice): GAduSQ8Kp.wuhB() — viewing a munition whose camera has ThermalToggle. */
   private static boolean munitionThermalToggleAllowed() {
      return false;
   }

   /** 1.1.8 handleThermalNUC. */
   private static void handleThermalNUC(Minecraft mc) {
      if (mc.player != null && ThermalCameraState.getInstance().isEnabled()) {
         NUCManager.getInstance().triggerNUC();
      }
   }

   /** 1.1.8 handleThermalAgcMode: toggles the thermal depth of field. */
   private static void handleThermalFocus(Minecraft mc) {
      if (mc.player != null && ThermalCameraState.getInstance().isEnabled()) {
         ThermalCameraState state = ThermalCameraState.getInstance();
         state.toggleDepthOfField();
         mc.player.sendOverlayMessage(Component.literal(state.isDepthOfField() ? "Focus: ON" : "Focus: OFF").withStyle(Style.EMPTY.withColor(MESSAGE_COLOR)));
      }
   }
}
