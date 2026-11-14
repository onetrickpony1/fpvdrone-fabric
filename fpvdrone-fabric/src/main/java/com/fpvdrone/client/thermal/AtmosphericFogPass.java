package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.buffers.GpuBuffer;

/** 1.1.8 AtmosphericFogPass: a very light uniform blend towards the ambient temperature, stronger in rain. */
public class AtmosphericFogPass implements ThermalPass {
   private GpuBuffer params;

   @Override
   public String getName() {
      return "atmospheric_fog";
   }

   @Override
   public void apply(RenderTarget in, RenderTarget out, PostProcessContext context) {
      if (this.params == null) {
         this.params = FullscreenQuad.createParamsBuffer(this.getName());
      }
      FullscreenQuad.params(this.params, context, in, 0.0F, 0.0F, 0.0F);
      if (!FullscreenQuad.draw(ThermalPipelines.ATMOSPHERIC_FOG, in, out, null, null, this.params)) {
         FullscreenQuad.blit(in, out);
      }
   }

   @Override
   public void cleanup() {
      if (this.params != null) {
         this.params.close();
         this.params = null;
      }
   }
}
