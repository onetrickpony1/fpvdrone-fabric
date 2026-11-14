package com.fpvdrone.client.thermal;

import com.fpvdrone.video.GpuBlit;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.util.Optional;
import org.lwjgl.system.MemoryStack;

/**
 * 1.1.8 FullscreenQuad: draws one post pass over a whole target, or copies a target. 26.3 has no immediate-mode
 * quads or uniforms; a pass is a fullscreen triangle with its inputs and a small uniform buffer ({@link #params}).
 */
public final class FullscreenQuad {
   /** Bytes of the ThermalPost uniform block (std140, see shaders/include/thermal_post.glsl). */
   static final int POST_UBO_SIZE = 128;

   private FullscreenQuad() {
   }

   /** 1.1.8 blit(): a plain copy (glBlitFramebuffer in 1.1.8). */
   public static void blit(RenderTarget in, RenderTarget out) {
      GpuBlit.copy(in, out, FilterMode.NEAREST);
   }

   /** A uniform buffer for one pass draw; each draw in a frame has its own so values cannot overlap. */
   public static GpuBuffer createParamsBuffer(String name) {
      return RenderSystem.getDevice().createBuffer(() -> "fpvdrone thermal " + name, GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, POST_UBO_SIZE);
   }

   /** Fills a pass's ThermalPost uniform block. */
   public static void params(GpuBuffer buffer, PostProcessContext ctx, RenderTarget in, float dirX, float dirY, float aperture) {
      try (MemoryStack stack = MemoryStack.stackPush()) {
         ByteBuffer data = Std140Builder.onStack(stack, POST_UBO_SIZE)
            .putMat4f(ctx.getInvProjection())
            .putVec2(in.width, in.height)
            .putVec2(dirX, dirY)
            .putFloat(ctx.getAmbientTemp())
            .putFloat(ctx.getAtmosphericAbsorption())
            .putFloat(ctx.getFpnReduction())
            .putFloat(ctx.getFocusDistance())
            .putFloat(aperture)
            .putInt(ctx.getFrameCount())
            .putInt(RenderSystem.getDevice().getDeviceInfo().isZZeroToOne() ? 1 : 0)
            .putInt(ctx.isUseDepth() ? 1 : 0)
            .get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
      }
   }

   /**
    * 1.1.8 draw(): runs {@code pipeline} reading {@code in} (InSampler) and writing {@code out}. Optional inputs:
    * the scene depth (DepthSampler), a palette (LutSampler) and the ThermalPost block. Returns false (drawing nothing)
    * if the pipeline is not usable, so the caller can copy instead.
    */
   public static boolean draw(RenderPipeline pipeline, RenderTarget in, RenderTarget out, GpuTextureView depth, GpuTextureView lut, GpuBuffer params) {
      GpuTextureView from = in.getColorTextureView();
      GpuTextureView to = out.getColorTextureView();
      if (from == null || to == null || !ThermalPipelines.usable(pipeline)) {
         return false;
      }
      var samplers = RenderSystem.getSamplerCache();
      try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "fpvdrone thermal " + pipeline.getLocation(), to, Optional.empty())) {
         pass.setPipeline(RenderSystem.getCompiledPipeline(pipeline));
         RenderSystem.bindDefaultUniforms(pass);
         pass.setUniform("InSampler", from, samplers.getClampToEdge(FilterMode.NEAREST));
         if (depth != null) {
            pass.setUniform("DepthSampler", depth, samplers.getClampToEdge(FilterMode.NEAREST));
         }
         if (lut != null) {
            pass.setUniform("LutSampler", lut, samplers.getClampToEdge(FilterMode.NEAREST));
         }
         if (params != null) {
            pass.setUniform(ThermalPipelines.POST_UBO, params);
         }
         pass.draw(3, 1, 0, 0);
      }
      return true;
   }
}
