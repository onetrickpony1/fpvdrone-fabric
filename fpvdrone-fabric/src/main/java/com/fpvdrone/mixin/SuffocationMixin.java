package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class})
public class SuffocationMixin {
    @Inject(
        method = {"isInWall"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$preventSuffocation(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (entity instanceof Player player && FPVMod.getActiveDroneId(player.getUUID()).isPresent()) {
            cir.setReturnValue(false);
        }
    }
}
