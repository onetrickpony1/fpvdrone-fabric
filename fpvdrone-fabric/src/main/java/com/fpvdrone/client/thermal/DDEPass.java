package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;

/**
 * 1.1.8 DDEPass (digital detail enhancement). In 1.1.8 it only copies the picture: its shader (and the blur passes
 * it would need, Downsample/GaussianBlur/Upsample, which the chain skips) is never used. Ported the same way.
 */
public class DDEPass implements ThermalPass {
   @Override
   public String getName() {
      return "dde";
   }

   @Override
   public void apply(RenderTarget in, RenderTarget out, PostProcessContext context) {
      FullscreenQuad.blit(in, out);
   }
}
