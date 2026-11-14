package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;

/**
 * 1.1.8 AGCPass (automatic gain control). In 1.1.8 it only copies the picture and reports the full range (0..1):
 * its histogram code (64x64 read-back, 2%/98% percentiles, asymmetric smoothing) is present but never called, and
 * its shader is never used. Ported the same way; the dead read-back is left out.
 */
public class AGCPass implements ThermalPass {
   @Override
   public String getName() {
      return "agc";
   }

   @Override
   public void apply(RenderTarget in, RenderTarget out, PostProcessContext context) {
      FullscreenQuad.blit(in, out);
      context.setAgcMin(0.0F);
      context.setAgcMax(1.0F);
   }
}
