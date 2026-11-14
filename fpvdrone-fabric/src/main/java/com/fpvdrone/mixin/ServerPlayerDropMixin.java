package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A pilot flying a drone can't throw items (the original cancels Forge's ItemTossEvent). */
@Mixin(ServerPlayer.class)
public class ServerPlayerDropMixin {
    @Inject(method = "drop(Z)V", at = @At("HEAD"), cancellable = true)
    private void fpvdrone$noTossInFpv(boolean all, CallbackInfo ci) {
        if (FPVMod.shouldBlockItemToss((ServerPlayer) (Object) this)) {
            ci.cancel();
        }
    }
}
