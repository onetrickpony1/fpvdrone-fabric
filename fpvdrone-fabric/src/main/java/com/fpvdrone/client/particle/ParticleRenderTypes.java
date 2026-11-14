package com.fpvdrone.client.particle;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

/**
 * Render types for the data-driven particles, one per texture and blend mode (1.1.8's particle render types):
 * translucent or additive ("isBlended" on a bright particle), depth-writing for fully opaque ones, and a depth bias
 * for decals so they do not z-fight with the block face. All use the vanilla particle shader and vertex format; bright
 * particles get full-bright light instead of 1.1.8's separate unlit shader.
 */
final class ParticleRenderTypes {
    private static final Map<String, RenderType> CACHE = new ConcurrentHashMap<>();
    private static final RenderPipeline[] PIPELINES = new RenderPipeline[8];

    static {
        for (int i = 0; i < 8; i++) {
            boolean additive = (i & 1) != 0;
            boolean depth = (i & 2) != 0;
            boolean decal = (i & 4) != 0;
            String name = "pipeline/fpv_particle" + (additive ? "_add" : "_alpha") + (depth ? "_depth" : "") + (decal ? "_decal" : "");
            DepthStencilState depthState = decal
                ? new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, depth, 1.0F, 10.0F)
                : new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, depth);
            PIPELINES[i] = RenderPipelines.register(
                RenderPipeline.builder(RenderPipelines.PARTICLE_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("fpvdrone", name))
                    .withColorTargetState(new ColorTargetState(additive ? BlendFunction.LIGHTNING : BlendFunction.TRANSLUCENT))
                    .withDepthStencilState(depthState)
                    .withCull(false)
                    .build()
            );
        }
    }

    private ParticleRenderTypes() {
    }

    /** Loading the class registers the pipelines; call during client init. */
    static void init() {
    }

    static RenderType get(Identifier texture, boolean bright, boolean blended, boolean writesDepth, boolean decal) {
        boolean additive = bright && blended && !writesDepth;
        int index = (additive ? 1 : 0) | (writesDepth ? 2 : 0) | (decal ? 4 : 0);
        String key = texture + "|" + index;
        return CACHE.computeIfAbsent(
            key,
            k -> RenderType.create(
                "fpvdrone_particle_" + index,
                RenderSetup.builder(PIPELINES[index]).withTexture("Sampler0", texture).useLightmap().sortOnUpload().createRenderSetup()
            )
        );
    }
}
