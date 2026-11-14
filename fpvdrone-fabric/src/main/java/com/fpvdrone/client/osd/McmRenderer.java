package com.fpvdrone.client.osd;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * Draws OSD characters from the MAX7456 (MCM) font atlas, one grid cell each. The atlas textures are
 * nearest-filtered (DynamicTexture's default), so the pixel font stays crisp at any scale.
 */
public final class McmRenderer {
    private McmRenderer() {
    }

    public static void drawPlainGrid(GuiGraphicsExtractor graphics, String text, float startX, float startY, float cellW, float cellH) {
        McmFontAtlas atlas = McmFontAtlas.getInstance();
        if (!atlas.isLoaded()) {
            return;
        }
        Identifier tex = atlas.getAtlasLocation();
        int iCellW = Math.round(cellW);
        int iCellH = Math.round(cellH);
        int iy = Math.round(startY);
        for (int i = 0; i < text.length(); i++) {
            int charCode = toMcmIndex(text.charAt(i));
            if (charCode != 32) {
                float[] uv = atlas.getPlainUV(charCode);
                int ix = Math.round(startX + i * cellW);
                graphics.blit(tex, ix, iy, ix + iCellW, iy + iCellH, uv[0], uv[2], uv[1], uv[3]);
            }
        }
    }

    public static void drawOutlinedGrid(GuiGraphicsExtractor graphics, String text, float startX, float startY, float cellW, float cellH) {
        McmFontAtlas atlas = McmFontAtlas.getInstance();
        if (!atlas.isLoaded()) {
            return;
        }
        Identifier tex = atlas.getOutlinedAtlasLocation();
        int iy = Math.round(startY);
        int nextRowY = Math.round(startY + cellH);
        int destH = nextRowY - iy;
        // The outlined cells are 14 x 20 around a 12 x 18 glyph: one source pixel of outline each side.
        int outlineExt = Math.max(1, Math.round(destH / 18.0F));
        for (int i = 0; i < text.length(); i++) {
            int charCode = toMcmIndex(text.charAt(i));
            if (charCode != 32) {
                float[] uv = atlas.getOutlinedUV(charCode);
                int ix = Math.round(startX + i * cellW);
                int nextX = Math.round(startX + (i + 1) * cellW);
                int destW = nextX - ix;
                int x0 = ix - outlineExt;
                int y0 = iy - outlineExt;
                graphics.blit(tex, x0, y0, x0 + destW + 2 * outlineExt, y0 + destH + 2 * outlineExt, uv[0], uv[2], uv[1], uv[3]);
            }
        }
    }

    private static int toMcmIndex(char c) {
        if (c >= '' && c <= '') {
            return c - 57344;
        } else {
            return c >= 97 && c <= 122 ? c - 32 : c;
        }
    }

    public static void ensureLoaded() {
        McmFontAtlas atlas = McmFontAtlas.getInstance();
        if (!atlas.isLoaded()) {
            atlas.loadFromResource("/assets/fpvdrone/font/monocraft.mcm");
        }
    }
}
