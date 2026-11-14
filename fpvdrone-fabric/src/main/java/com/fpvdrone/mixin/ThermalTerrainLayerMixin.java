package com.fpvdrone.mixin;

import com.fpvdrone.client.thermal.ThermalPipelines;
import com.fpvdrone.client.thermal.ThermalRenderHook;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Thermal camera, vanilla chunks (1.1.8 RenderTypeShaderMixin for the solid/cutout/translucent block shaders): each
 * chunk layer draws with its thermal pipeline while a thermal frame draws. Covers both ways 26.3 draws chunks
 * (one draw per section, or multi-draw indirect).
 */
@Mixin(targets = {
    "net.minecraft.client.renderer.chunk.ChunkSectionsToRender$DrawIndirect",
    "net.minecraft.client.renderer.chunk.ChunkSectionsToRender$DrawSeparate"
})
public abstract class ThermalTerrainLayerMixin {
    @Redirect(
        method = "render",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionLayer;pipeline(Z)Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;"),
        require = 0
    )
    private RenderPipeline fpvdrone$thermalPipeline(ChunkSectionLayer layer, boolean multiDraw) {
        if (ThermalRenderHook.terrainAtlas() != null) {
            return ThermalPipelines.terrainFor(layer, multiDraw);
        }
        return layer.pipeline(multiDraw);
    }
}
