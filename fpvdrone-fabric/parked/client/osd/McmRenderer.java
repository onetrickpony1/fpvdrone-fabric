package com.fpvdrone.client.osd;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import org.lwjgl.opengl.GL11;

public final class McmRenderer {
    private McmRenderer() {
    }

    public static void drawPlainGrid(GuiGraphics graphics, String text, float startX, float startY, float cellW, float cellH) {
        McmFontAtlas atlas = McmFontAtlas.getInstance();
        if (atlas.isLoaded()) {
            Identifier tex = atlas.getAtlasLocation();
            int atlasW = atlas.getPlainAtlasWidth();
            int atlasH = atlas.getPlainAtlasHeight();
            RenderSystem.setShaderTexture(0, tex);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableBlend();
            GlStateManager._bindTexture(RenderSystem.getShaderTexture(0));
            GL11.glTexParameteri(3553, 10241, 9728);
            GL11.glTexParameteri(3553, 10240, 9728);
            int iCellW = Math.round(cellW);
            int iCellH = Math.round(cellH);
            int iy = Math.round(startY);

            for (int i = 0; i < text.length(); i++) {
                int charCode = toMcmIndex(text.charAt(i));
                if (charCode != 32) {
                    float[] uv = atlas.getPlainUV(charCode);
                    int ix = Math.round(startX + i * cellW);
                    int srcX = (int)(uv[0] * atlasW);
                    int srcY = (int)(uv[1] * atlasH);
                    graphics.blit(tex, ix, iy, iCellW, iCellH, srcX, srcY, 12, 18, atlasW, atlasH);
                }
            }

            RenderSystem.disableBlend();
        }
    }

    public static void drawOutlinedGrid(GuiGraphics graphics, String text, float startX, float startY, float cellW, float cellH) {
        McmFontAtlas atlas = McmFontAtlas.getInstance();
        if (atlas.isLoaded()) {
            Identifier tex = atlas.getOutlinedAtlasLocation();
            int atlasW = atlas.getOutlinedAtlasWidth();
            int atlasH = atlas.getOutlinedAtlasHeight();
            int srcCellW = 14;
            int srcCellH = 20;
            RenderSystem.setShaderTexture(0, tex);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.enableBlend();
            GlStateManager._bindTexture(RenderSystem.getShaderTexture(0));
            GL11.glTexParameteri(3553, 10241, 9728);
            GL11.glTexParameteri(3553, 10240, 9728);
            int iy = Math.round(startY);
            int nextRowY = Math.round(startY + cellH);
            int destH = nextRowY - iy;
            int outlineExtX = Math.max(1, Math.round(destH / 18.0F));
            int outlineExtY = outlineExtX;

            for (int i = 0; i < text.length(); i++) {
                int charCode = toMcmIndex(text.charAt(i));
                if (charCode != 32) {
                    float[] uv = atlas.getOutlinedUV(charCode);
                    int ix = Math.round(startX + i * cellW);
                    int nextX = Math.round(startX + (i + 1) * cellW);
                    int destW = nextX - ix;
                    int srcX = (int)(uv[0] * atlasW);
                    int srcY = (int)(uv[1] * atlasH);
                    int fullW = destW + 2 * outlineExtX;
                    int fullH = destH + 2 * outlineExtY;
                    graphics.blit(tex, ix - outlineExtX, iy - outlineExtY, fullW, fullH, srcX, srcY, srcCellW, srcCellH, atlasW, atlasH);
                }
            }

            RenderSystem.disableBlend();
        }
    }

    private static int toMcmIndex(char c) {
        if (c >= '\ue000' && c <= '\ue1ff') {
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
