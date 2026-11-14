package com.fpvdrone.mixin;

import com.fpvdrone.client.thermal.ThermalRenderHook;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Thermal camera (1.1.8 GameRendererMixin beforeWorldRender/afterWorldRender + ThermalRenderHook AFTER_SKY):
 * wraps the world drawing. Before it, the thermal frame is prepared; the sky is skipped and the picture cleared to
 * near black; after it, the thermal post-processing runs (before the hand and the GUI, as in 1.1.8).
 */
@Mixin(GameRenderer.class)
public abstract class ThermalGameRendererMixin {
    @WrapOperation(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V"
        ),
        require = 0
    )
    private void fpvdrone$thermalWorld(
        LevelRenderer levelRenderer, GraphicsResourceAllocator allocator, boolean renderOutline, CameraRenderState cameraState,
        GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, Operation<Void> original
    ) {
        ThermalRenderHook.onBeforeWorldRender();
        if (ThermalRenderHook.isRenderingWorld()) {
            original.call(levelRenderer, allocator, renderOutline, cameraState, terrainFog, new Vector4f(ThermalRenderHook.SKY_CLEAR), false, consistentDepthRequired);
            ThermalRenderHook.onAfterWorldRender(new Matrix4f(cameraState.projectionMatrix));
        } else {
            original.call(levelRenderer, allocator, renderOutline, cameraState, terrainFog, fogColor, shouldRenderSky, consistentDepthRequired);
        }
    }
}
