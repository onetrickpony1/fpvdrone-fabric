package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

/** 1.1.8 PaletteLUTPass: colours the grey temperature picture with the chosen palette (256x1 look-up textures). */
public class PaletteLUTPass implements ThermalPass {
   private DynamicTexture whiteHot;
   private DynamicTexture blackHot;
   private DynamicTexture ironbow;

   @Override
   public String getName() {
      return "palette_lut";
   }

   @Override
   public void apply(RenderTarget in, RenderTarget out, PostProcessContext context) {
      this.ensureTextures();
      DynamicTexture lut = switch (context.getPalette()) {
         case WHITE_HOT -> this.whiteHot;
         case BLACK_HOT -> this.blackHot;
         case IRONBOW -> this.ironbow;
      };
      if (lut == null || !FullscreenQuad.draw(ThermalPipelines.PALETTE_LUT, in, out, null, lut.getTextureView(), null)) {
         FullscreenQuad.blit(in, out);
      }
   }

   /** 1.1.8 wuhB(): made ahead of the first frame. */
   public void ensureTextures() {
      if (this.whiteHot == null) {
         this.whiteHot = createLUT("white_hot", PaletteLUTPass::whiteHotColor);
         this.blackHot = createLUT("black_hot", PaletteLUTPass::blackHotColor);
         this.ironbow = createLUT("ironbow", PaletteLUTPass::ironbowColor);
      }
   }

   private static DynamicTexture createLUT(String name, ColorMap map) {
      NativeImage image = new NativeImage(NativeImage.Format.RGBA, 256, 1, false);
      for (int i = 0; i < 256; i++) {
         int[] rgb = map.map(i);
         image.setPixelABGR(i, 0, 0xFF000000 | rgb[2] << 16 | rgb[1] << 8 | rgb[0]);
      }
      return new DynamicTexture(() -> "fpvdrone thermal lut " + name, image);
   }

   static int[] whiteHotColor(int v) {
      float t = v / 255.0F;
      return new int[]{Math.round(4.0F + t * 251.0F), Math.round(4.0F + t * 246.0F), Math.round(6.0F + t * 236.0F)};
   }

   static int[] blackHotColor(int v) {
      return whiteHotColor(255 - v);
   }

   static int[] ironbowColor(int v) {
      float t = v / 255.0F;
      float[][] stops = new float[][]{
         {0.0F, 80.0F, 0.0F, 120.0F},
         {0.2F, 0.0F, 40.0F, 200.0F},
         {0.4F, 0.0F, 180.0F, 40.0F},
         {0.6F, 220.0F, 220.0F, 0.0F},
         {0.8F, 240.0F, 120.0F, 0.0F},
         {0.95F, 220.0F, 20.0F, 10.0F},
         {1.0F, 255.0F, 255.0F, 255.0F}
      };
      int seg = 0;
      for (int i = 0; i < stops.length - 1; i++) {
         if (t >= stops[i][0] && t <= stops[i + 1][0]) {
            seg = i;
            break;
         }
      }
      float f = (t - stops[seg][0]) / (stops[seg + 1][0] - stops[seg][0]);
      f = Math.max(0.0F, Math.min(1.0F, f));
      int[] out = new int[3];
      for (int c = 0; c < 3; c++) {
         int value = Math.round(stops[seg][c + 1] + f * (stops[seg + 1][c + 1] - stops[seg][c + 1]));
         out[c] = Math.max(0, Math.min(255, value));
      }
      return out;
   }

   @Override
   public void cleanup() {
      for (DynamicTexture t : new DynamicTexture[]{this.whiteHot, this.blackHot, this.ironbow}) {
         if (t != null) {
            t.close();
         }
      }
      this.whiteHot = null;
      this.blackHot = null;
      this.ironbow = null;
   }

   @FunctionalInterface
   private interface ColorMap {
      int[] map(int value);
   }
}
