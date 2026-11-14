package com.fpvdrone.video;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.util.Optional;
import net.minecraft.client.renderer.RenderPipelines;

/**
 * Copies one render target's colour into another, scaling to fit: the 26.3 stand-in for the
 * original's glBlitFramebuffer. A fullscreen triangle samples the source (linear when shrinking, as
 * the original's GL_LINEAR, nearest when enlarging, for its hard pixels) without blending.
 */
public final class GpuBlit {
    private GpuBlit() {
    }

    public static void copy(RenderTarget source, RenderTarget destination, FilterMode filter) {
        GpuTextureView from = source.getColorTextureView();
        GpuTextureView to = destination.getColorTextureView();
        if (from == null || to == null) {
            return;
        }
        RenderSystem.assertOnRenderThread();
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "fpvdrone video blit", to, Optional.empty())) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.TRACY_BLIT));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("InSampler", from, RenderSystem.getSamplerCache().getClampToEdge(filter));
            pass.draw(3, 1, 0, 0);
        }
    }
}
