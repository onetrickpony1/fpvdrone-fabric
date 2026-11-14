package com.fpvdrone.mixin;

import com.fpvdrone.client.thermal.ThermalRenderHook;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Thermal camera, vanilla chunks (1.1.8 ShaderInstanceMixin, which bound the thermal atlas in place of the block
 * atlas): while a thermal frame draws, the chunk layers sample the thermal atlas and get the thermal shader values.
 * Not used when Sodium draws the chunks (the screen-space fallback handles that case).
 */
@Mixin(ChunkSectionsToRender.class)
public abstract class ThermalTerrainMixin {
    @ModifyVariable(method = "renderGroup", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0)
    private GpuTextureView fpvdrone$thermalAtlas(GpuTextureView atlas, ChunkSectionLayerGroup group, RenderPass renderPass, GpuSampler sampler, GpuTextureView sameAtlas, boolean wireframe) {
        GpuTextureView thermal = ThermalRenderHook.terrainAtlas();
        return thermal != null && !wireframe ? thermal : atlas;
    }

    @Inject(method = "renderGroup", at = @At("HEAD"), require = 0)
    private void fpvdrone$thermalUniforms(ChunkSectionLayerGroup group, RenderPass renderPass, GpuSampler sampler, GpuTextureView atlas, boolean wireframe, CallbackInfo ci) {
        if (ThermalRenderHook.terrainAtlas() != null && !wireframe) {
            ThermalRenderHook.bindTerrainUniforms(renderPass);
        }
    }
}
