package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.buffers.GpuBuffer;

/**
 * 1.1.8 FallbackThermalPass: the first pass. In the screen-space fallback (Sodium/Iris) it turns the normally drawn
 * picture into temperatures from brightness and distance; with the thermal chunk shaders it only copies.
 */
public class FallbackThermalPass implements ThermalPass {
   private GpuBuffer params;

   @Override
   public String getName() {
      return "fallback_thermal";
   }

   @Override
   public void apply(RenderTarget in, RenderTarget out, PostProcessContext context) {
      if (FallbackManager.isScreenspace() && context.getDepth() != null) {
         if (this.params == null) {
            this.params = FullscreenQuad.createParamsBuffer(this.getName());
         }
         FullscreenQuad.params(this.params, context, in, 0.0F, 0.0F, 0.0F);
         if (FullscreenQuad.draw(ThermalPipelines.FALLBACK_THERMAL, in, out, context.getDepth(), null, this.params)) {
            return;
         }
      }
      FullscreenQuad.blit(in, out);
   }

   @Override
   public void cleanup() {
      if (this.params != null) {
         this.params.close();
         this.params = null;
      }
   }
}
