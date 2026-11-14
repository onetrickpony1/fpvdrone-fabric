package com.fpvdrone.client.osd;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

public final class OsdTextRenderer {
    public static final int OUTLINE_COLOR = -16777216;
    public static final int TEXT_COLOR = -1;
    public static final Identifier OSD_FONT = Identifier.fromNamespaceAndPath("fpvdrone", "osd");
    public static final Style OSD_STYLE = Style.EMPTY.withFont(OSD_FONT);

    private OsdTextRenderer() {
    }

    public static void drawOutlinedString(GuiGraphics graphics, Font font, String text, int x, int y) {
        graphics.drawString(font, text, x - 1, y, -16777216, false);
        graphics.drawString(font, text, x + 1, y, -16777216, false);
        graphics.drawString(font, text, x, y - 1, -16777216, false);
        graphics.drawString(font, text, x, y + 1, -16777216, false);
        graphics.drawString(font, text, x, y, -1, false);
    }

    public static void drawOutlinedOsd(GuiGraphics graphics, Font font, String text, int x, int y) {
        Component comp = Component.literal(text).withStyle(OSD_STYLE);
        Component outlineComp = Component.literal(text).withStyle(OSD_STYLE.withColor(-16777216));
        graphics.drawString(font, outlineComp, x - 1, y, -16777216, false);
        graphics.drawString(font, outlineComp, x + 1, y, -16777216, false);
        graphics.drawString(font, outlineComp, x, y - 1, -16777216, false);
        graphics.drawString(font, outlineComp, x, y + 1, -16777216, false);
        graphics.drawString(font, comp, x, y, -1, false);
    }

    public static int osdWidth(Font font, String text) {
        return font.width(Component.literal(text).withStyle(OSD_STYLE));
    }

    public static void drawOsdGrid(GuiGraphics graphics, Font font, String text, float startX, float startY, float cellW, float cellH) {
        float fontScale = cellH / 9.0F;

        for (int i = 0; i < text.length(); i++) {
            String ch = text.substring(i, i + 1);
            Component comp = Component.literal(ch).withStyle(OSD_STYLE);
            float cx = startX + i * cellW;
            float glyphW = font.width(comp) * fontScale;
            float offsetX = (cellW - glyphW) / 2.0F;
            graphics.pose().pushPose();
            graphics.pose().translate(cx + offsetX, startY, 0.0F);
            graphics.pose().scale(fontScale, fontScale, 1.0F);
            graphics.drawString(font, comp, 0, 0, -1, false);
            graphics.pose().popPose();
        }
    }

    public static void drawOutlinedOsdGrid(GuiGraphics graphics, Font font, String text, float startX, float startY, float cellW, float cellH) {
        float fontScale = cellH / 9.0F;

        for (int i = 0; i < text.length(); i++) {
            String ch = text.substring(i, i + 1);
            Component comp = Component.literal(ch).withStyle(OSD_STYLE);
            Component outlineComp = Component.literal(ch).withStyle(OSD_STYLE.withColor(-16777216));
            float cx = startX + i * cellW;
            float glyphW = font.width(comp) * fontScale;
            float offsetX = (cellW - glyphW) / 2.0F;
            graphics.pose().pushPose();
            graphics.pose().translate(cx + offsetX, startY, 0.0F);
            graphics.pose().scale(fontScale, fontScale, 1.0F);
            graphics.drawString(font, outlineComp, -1, 0, -16777216, false);
            graphics.drawString(font, outlineComp, 1, 0, -16777216, false);
            graphics.drawString(font, outlineComp, 0, -1, -16777216, false);
            graphics.drawString(font, outlineComp, 0, 1, -16777216, false);
            graphics.drawString(font, comp, 0, 0, -1, false);
            graphics.pose().popPose();
        }
    }

    public static float calculateFontScale(int screenHeight, float unused) {
        float cellH = screenHeight / 16.0F;
        return cellH / 9.0F;
    }
}
