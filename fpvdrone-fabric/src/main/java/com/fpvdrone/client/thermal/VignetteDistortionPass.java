package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;

/** 1.1.8 VignetteDistortionPass: slight barrel distortion and darker corners of the thermal lens; the last pass. */
public class VignetteDistortionPass implements ThermalPass {
   @Override
   public String getName() {
      return "vignette_distortion";
   }

   @Override
   public void apply(RenderTarget in, RenderTarget out, PostProcessContext context) {
      if (!FullscreenQuad.draw(ThermalPipelines.VIGNETTE_DISTORTION, in, out, null, null, null)) {
         FullscreenQuad.blit(in, out);
      }
   }
}
