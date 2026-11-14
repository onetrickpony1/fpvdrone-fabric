package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A drone's own explosion breaks blocks the vanilla way, but its damage to entities is the mod's
 * shrapnel and blast pass: vanilla's entity damage is skipped while the drone detonates (the original
 * clears Forge's ExplosionEvent.Detonate entity list).
 */
@Mixin(ServerExplosion.class)
public class ServerExplosionMixin {
    @Inject(method = "hurtEntities", at = @At("HEAD"), cancellable = true)
    private void fpvdrone$skipVanillaEntityDamage(CallbackInfo ci) {
        if (FPVMod.isVanillaTntEntityDamageSuppressed()) {
            ci.cancel();
        }
    }
}
