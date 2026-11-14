package com.fpvdrone.render;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.ControllerReader;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * The stick overlay (Other Settings -> Stick Overlay): two crosses at the bottom of the screen with a
 * box for each stick, throttle/yaw left and pitch/roll right, while armed. Lines are one window pixel
 * wide, as the original's debug lines were.
 */
public class StickHudOverlay {
    public static void setShowStickOverlay(boolean show) {
        FpvConfig.setStickOverlay(show);
    }

    public static boolean isShowStickOverlay() {
        return FpvConfig.isStickOverlay();
    }

    public static void init() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath("fpvdrone", "stick_overlay"), StickHudOverlay::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !FPVMod.isArmed(mc.player.getUUID()) || !FpvConfig.isStickOverlay()) {
            return;
        }
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int baseY = !mc.player.isCreative() && !mc.player.isSpectator() ? screenHeight - 80 : screenHeight - 40;
        int stickSize = 20;
        int spacing = 30;
        float scale = (float) mc.getWindow().getGuiScale();
        graphics.pose().pushMatrix();
        graphics.pose().scale(1.0F / scale, 1.0F / scale);
        renderStickAxis(graphics, scale, centerX - spacing, baseY, stickSize, ControllerReader.getYaw(), ControllerReader.getThrottle());
        renderStickAxis(graphics, scale, centerX + spacing, baseY, stickSize, ControllerReader.getRoll(), ControllerReader.getPitch());
        graphics.pose().popMatrix();
    }

    /** In window pixels: the GUI-scaled position times the scale. */
    private static void renderStickAxis(GuiGraphicsExtractor graphics, float scale, int x, int y, int size, float xValue, float yValue) {
        int cx = Math.round(x * scale);
        int cy = Math.round(y * scale);
        int s = Math.round(size * scale);
        int white = 0xFFFFFFFF;
        graphics.fill(cx, cy - s, cx + 1, cy + s, white);
        graphics.fill(cx - s, cy, cx + s, cy + 1, white);
        int dx = cx + Math.round(xValue * size * scale);
        int dy = cy + Math.round(-yValue * size * scale);
        int d = Math.round(2 * scale);
        graphics.fill(dx - d, dy - d, dx + d + 1, dy - d + 1, white);
        graphics.fill(dx - d, dy + d, dx + d + 1, dy + d + 1, white);
        graphics.fill(dx - d, dy - d, dx - d + 1, dy + d + 1, white);
        graphics.fill(dx + d, dy - d, dx + d + 1, dy + d + 1, white);
    }
}
