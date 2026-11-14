package com.fpvdrone.client.renderer;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.function.Function;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

/**
 * Render type for spinning-propeller blur: translucent, two-sided, without depth writes, so the blur shows over the
 * drone's own frame and blends with what is behind it.
 * <p>
 * 1.1.8 draws the blur bones of each drone's own model (its BlurDiscRenderer filters the "*blur" bones and uses the
 * drone's texture). {@link DroneRenderer} does that drawing; this class only provides the render type, one per texture.
 * The 1.1.4 port drew four fixed quads shaped like the FPV drone's props, which was wrong for the Mavic.
 */
public final class BlurDiscRenderer {
    private static final RenderPipeline PIPELINE = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("fpvdrone", "pipeline/blur_no_depth"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
            .withCull(false)
            .build()
    );
    private static final Function<Identifier, RenderType> BLUR_TYPES = Util.memoize(
        texture -> RenderType.create(
            "fpvdrone_blur_no_depth",
            RenderSetup.builder(PIPELINE)
                .withTexture("Sampler0", texture)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                .createRenderSetup()
        )
    );

    private BlurDiscRenderer() {
    }

    /** Loading this class registers the pipeline; called at client setup. */
    public static void init() {
    }

    public static RenderType blurType(Identifier texture) {
        return BLUR_TYPES.apply(texture);
    }
}
