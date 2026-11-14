package com.fpvdrone.mixin;

import com.fpvdrone.FpvConfig;
import com.fpvdrone.entity.DroneEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
    targets = {"net.minecraft.server.level.ChunkMap$TrackedEntity"}
)
public class DroneTrackingRangeMixin {
    @Shadow
    @Final
    Entity entity;

    @Inject(
        method = {"getEffectiveRange"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void fpvdrone$liftRangeForDrones(CallbackInfoReturnable<Integer> cir) {
        if (this.entity instanceof DroneEntity) {
            int target = FpvConfig.getDroneObserverRange();
            if (cir.getReturnValueI() < target) {
                cir.setReturnValue(target);
            }
        }
    }
}
