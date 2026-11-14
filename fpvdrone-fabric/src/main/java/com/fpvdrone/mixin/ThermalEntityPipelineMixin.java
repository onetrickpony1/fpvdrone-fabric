package com.fpvdrone.mixin;

import com.fpvdrone.client.thermal.ThermalPipelines;
import com.fpvdrone.client.thermal.ThermalRenderHook;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Thermal camera (1.1.8 RenderTypeShaderMixin, entity shader getters): while a thermal frame draws the world,
 * entity render types draw with their thermal twin pipeline (one hot temperature). This works whatever draws the
 * chunks, so it is also active in the screen-space fallback (as in 1.1.8), but not under an Iris shader pack.
 */
@Mixin(PreparedRenderType.class)
public abstract class ThermalEntityPipelineMixin {
    @ModifyVariable(method = "draw", at = @At("HEAD"), argsOnly = true, require = 0)
    private RenderPipeline fpvdrone$thermalEntity(RenderPipeline pipeline) {
        return ThermalRenderHook.overrideEntities() ? ThermalPipelines.entityFor(pipeline) : pipeline;
    }
}
