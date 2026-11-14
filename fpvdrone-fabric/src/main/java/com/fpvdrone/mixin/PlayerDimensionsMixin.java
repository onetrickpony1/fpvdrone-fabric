package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Avatar.class)
public abstract class PlayerDimensionsMixin {
    @Unique
    private static final EntityDimensions fpvdrone$DRONE_DIMENSIONS = EntityDimensions.fixed(0.45F, 0.25F).withEyeHeight(0.21F);

    @Inject(method = "getDefaultDimensions", at = @At("HEAD"), cancellable = true)
    private void fpvdrone$getDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if ((Object)this instanceof Player player && FPVMod.getActiveDroneId(player.getUUID()).isPresent()) {
            cir.setReturnValue(fpvdrone$DRONE_DIMENSIONS);
        }
    }
}
