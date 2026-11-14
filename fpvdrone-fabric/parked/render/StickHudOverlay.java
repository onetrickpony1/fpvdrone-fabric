package com.fpvdrone.render;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.ControllerReader;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.client.event.RenderGuiOverlayEvent.Pre;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.joml.Matrix4f;

@EventBusSubscriber(
    value = {Dist.CLIENT},
    modid = "fpvdrone",
    bus = Bus.FORGE
)
public class StickHudOverlay {
    public static void setShowStickOverlay(boolean show) {
        FpvConfig.setStickOverlay(show);
    }

    public static boolean isShowStickOverlay() {
        return FpvConfig.isStickOverlay();
    }

    @SubscribeEvent
    public static void onRenderGui(Pre event) {
        if (event.getOverlay() == VanillaGuiOverlay.CHAT_PANEL.type()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                UUID playerId = mc.player.getUUID();
                if (FPVMod.isArmed(playerId)) {
                    if (FpvConfig.isStickOverlay()) {
                        GuiGraphics graphics = event.getGuiGraphics();
                        int screenWidth = mc.getWindow().getGuiScaledWidth();
                        int screenHeight = mc.getWindow().getGuiScaledHeight();
                        int centerX = screenWidth / 2;
                        int baseY;
                        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
                            baseY = screenHeight - 80;
                        } else {
                            baseY = screenHeight - 40;
                        }

                        float throttle = ControllerReader.getThrottle();
                        float yaw = ControllerReader.getYaw();
                        float pitch = ControllerReader.getPitch();
                        float roll = ControllerReader.getRoll();
                        int stickSize = 20;
                        int spacing = 30;
                        renderStickAxis(graphics, centerX - spacing, baseY, stickSize, yaw, throttle);
                        renderStickAxis(graphics, centerX + spacing, baseY, stickSize, roll, pitch);
                    }
                }
            }
        }
    }

    private static void renderStickAxis(GuiGraphics graphics, int x, int y, int size, float xValue, float yValue) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0F);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Matrix4f matrix = poseStack.last().pose();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, 0.0F, -size, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, 0.0F, size, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, -size, 0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, size, 0.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        tesselator.end();
        float dotX = xValue * size;
        float dotY = -yValue * size;
        buffer.begin(Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(matrix, dotX - 2.0F, dotY - 2.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, dotX + 2.0F, dotY - 2.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, dotX + 2.0F, dotY - 2.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, dotX + 2.0F, dotY + 2.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, dotX + 2.0F, dotY + 2.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, dotX - 2.0F, dotY + 2.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, dotX - 2.0F, dotY + 2.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        buffer.vertex(matrix, dotX - 2.0F, dotY - 2.0F, 0.0F).color(255, 255, 255, 255).endVertex();
        tesselator.end();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}
