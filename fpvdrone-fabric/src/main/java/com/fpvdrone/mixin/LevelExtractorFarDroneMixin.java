package com.fpvdrone.mixin;

import com.fpvdrone.client.FarDrones;
import com.fpvdrone.entity.DroneEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** 1.1.8 FsOzhg far-drone pass (see {@link FarDrones}): far drones pass the compiled-section check. */
@Mixin(LevelExtractor.class)
public class LevelExtractorFarDroneMixin {
    @WrapOperation(
        method = "isEntityVisible",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;isSectionCompiledAndVisible(Lnet/minecraft/core/BlockPos;J)Z")
    )
    private boolean fpvdrone$drawFarDrones(
        LevelRenderer renderer, BlockPos pos, long fade, Operation<Boolean> original, @Local(argsOnly = true) Entity entity
    ) {
        return original.call(renderer, pos, fade) || entity instanceof DroneEntity drone && FarDrones.isFar(drone);
    }
}
