package com.fpvdrone.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class StickOverlayRenderer {
    private static final int BG_COLOR = -16777216;
    private static final int BORDER_COLOR = -1;
    private static final int UNSELECTED_COLOR = -8355712;
    private static final int DOT_COLOR = -1;
    private static final int ARROW_COLOR_BASE = 16777215;
    private static final int DOT_OUTLINE = -16777216;

    public static void renderStickGrid(GuiGraphicsExtractor graphics, int x, int y, int size, float stickX, float stickY, boolean selected) {
        int half = size / 2;
        int left = x - half;
        int top = y - half;
        int right = x + half;
        int bottom = y + half;
        int activeColor = selected ? -1 : -8355712;
        graphics.fill(left, top, right, bottom, -16777216);
        drawRect(graphics, left, top, right, bottom, activeColor);
        graphics.horizontalLine(left, right - 1, y, activeColor);
        graphics.verticalLine(x, top, bottom - 1, activeColor);
        int dotX = x + (int)(stickX * (half - 4));
        int dotY = y - (int)(stickY * (half - 4));
        graphics.fill(dotX - 2, dotY - 2, dotX + 3, dotY + 3, -16777216);
        graphics.fill(dotX - 1, dotY - 1, dotX + 2, dotY + 2, activeColor);
    }

    public static void renderProgressBar(GuiGraphicsExtractor graphics, int x, int y, int barWidth, int barHeight, float progress) {
        graphics.fill(x, y, x + barWidth, y + barHeight, -16777216);
        graphics.fill(x, y, x + (int)(barWidth * progress), y + barHeight, -1);
        drawRect(graphics, x, y, x + barWidth, y + barHeight, -1);
    }

    private static void drawRect(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom, int color) {
        graphics.horizontalLine(left, right - 1, top, color);
        graphics.horizontalLine(left, right - 1, bottom - 1, color);
        graphics.verticalLine(left, top, bottom - 1, color);
        graphics.verticalLine(right - 1, top, bottom - 1, color);
    }

    public static void renderAnimatedArrow(GuiGraphicsExtractor graphics, int x, int y, StickOverlayRenderer.Direction dir, float time) {
        float pulse = (float)(Math.sin(time * 3.5) * 0.5 + 0.5);
        int alpha = (int)(120.0F + pulse * 135.0F);
        int color = alpha << 24 | 16777215;
        int arrowSize = 6;
        int offset = (int)(pulse * 3.0F);
        switch (dir) {
            case UP:
                drawTriangle(graphics, x, y - 14 - offset, arrowSize, StickOverlayRenderer.Direction.UP, color);
                break;
            case DOWN:
                drawTriangle(graphics, x, y + 14 + offset, arrowSize, StickOverlayRenderer.Direction.DOWN, color);
                break;
            case LEFT:
                drawTriangle(graphics, x - 14 - offset, y, arrowSize, StickOverlayRenderer.Direction.LEFT, color);
                break;
            case RIGHT:
                drawTriangle(graphics, x + 14 + offset, y, arrowSize, StickOverlayRenderer.Direction.RIGHT, color);
                break;
            case CENTER:
                int s = 3 + (int)(pulse * 2.0F);
                graphics.fill(x - s, y - s, x + s, y + s, color);
        }
    }

    private static void drawTriangle(GuiGraphicsExtractor graphics, int x, int y, int size, StickOverlayRenderer.Direction dir, int color) {
        switch (dir) {
            case UP:
                for (int i = 0; i < size; i++) {
                    graphics.horizontalLine(x - i, x + i, y + i, color);
                }
                break;
            case DOWN:
                for (int i = 0; i < size; i++) {
                    graphics.horizontalLine(x - i, x + i, y - i, color);
                }
                break;
            case LEFT:
                for (int i = 0; i < size; i++) {
                    graphics.verticalLine(x + i, y - i, y + i, color);
                }
                break;
            case RIGHT:
                for (int i = 0; i < size; i++) {
                    graphics.verticalLine(x - i, y - i, y + i, color);
                }
        }
    }

    public static void renderListeningDots(GuiGraphicsExtractor graphics, int x, int y, long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        int dotCount = (int)(elapsed / 400L % 4L);
        String dots = ".".repeat(dotCount);
        graphics.text(Minecraft.getInstance().font, "Listening" + dots, x, y, 0xFF888888);
    }

    public static void renderDetected(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.text(Minecraft.getInstance().font, "Detected!", x, y, -1);
    }

    public static float getSpiralX(float t) {
        float angle = t * (float) Math.PI * 4.0F;
        float radius = 1.0F - t;
        return (float)Math.cos(angle) * radius * 0.8F;
    }

    public static float getSpiralY(float t) {
        float angle = t * (float) Math.PI * 4.0F;
        float radius = 1.0F - t;
        return (float)Math.sin(angle) * radius * 0.8F;
    }

    public static float getBoxX(float t) {
        if (t < 0.25F) {
            return t * 4.0F;
        } else if (t < 0.5F) {
            return 1.0F;
        } else {
            return t < 0.75F ? 1.0F - (t - 0.5F) * 4.0F : 0.0F;
        }
    }

    public static float getBoxY(float t) {
        if (t < 0.25F) {
            return 0.0F;
        } else if (t < 0.5F) {
            return (t - 0.25F) * 4.0F;
        } else {
            return t < 0.75F ? 1.0F : 1.0F - (t - 0.75F) * 4.0F;
        }
    }

    public static enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        CENTER;
    }
}
