package com.fpvdrone.mixin;

import com.fpvdrone.client.FpvPlayerTracker;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps a remote pilot's head still while they fly (and just after they exit). */
@Mixin(LivingEntity.class)
public abstract class LivingEntityHeadLerpMixin {
    @Inject(method = "lerpHeadTo", at = @At("HEAD"), cancellable = true)
    private void fpvdrone$skipHeadLerpForFpvPlayer(float yaw, int steps, CallbackInfo ci) {
        if ((Object)this instanceof RemotePlayer self
            && (FpvPlayerTracker.isFrozen(self.getUUID()) || FpvPlayerTracker.hasRecentlyExited(self.getUUID()))) {
            ci.cancel();
        }
    }
}
