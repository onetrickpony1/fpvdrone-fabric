package com.fpvdrone.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class GuiBoxRenderer {
    private GuiBoxRenderer() {
    }

    public static void renderBox(
        GuiGraphics graphics, Font font, int x, int y, int w, int h, String title, int screenWidth, int colorSurface200, int colorSurface400, int colorPrimary
    ) {
        renderFrame(graphics, x, y, w, h, colorSurface200, colorSurface400);
        renderTitle(graphics, font, x, y, w, title, screenWidth, colorPrimary);
    }

    public static void renderFrame(GuiGraphics graphics, int x, int y, int w, int h, int colorSurface200, int colorSurface400) {
        int titleH = 14;
        int border = 2;
        int boxY = y + titleH / 2;
        int boxH = h - titleH / 2;
        graphics.fill(x, boxY, x + w, boxY + boxH, colorSurface400);
        graphics.fill(x + border, boxY + border, x + w - border, boxY + boxH - border, colorSurface200);
    }

    public static void renderTitle(GuiGraphics graphics, Font font, int x, int y, int boxW, String title, int screenWidth, int colorPrimary) {
        int titleH = 14;
        int textW = font.width(title);
        int maxPillW = Math.max(20, boxW - 4);
        int pillW = Math.min(textW + 10, maxPillW);
        int pillX = x + 8;
        if (pillX + pillW > screenWidth - 2) {
            pillW = screenWidth - 2 - pillX;
        }

        if (pillW >= 10) {
            graphics.fill(pillX, y, pillX + pillW, y + titleH, colorPrimary);
            graphics.enableScissor(pillX, y, pillX + pillW, y + titleH);
            int textY = y + (titleH - 9) / 2 + 1;
            int textX = pillX + (pillW - textW) / 2;
            graphics.drawString(font, title, textX, textY, -16777216, false);
            graphics.disableScissor();
        }
    }
}
