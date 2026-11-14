package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ExperienceOrb.class})
public class XpOrbPickupMixin {
    @Inject(
        method = {"playerTouch"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockXpPickup(Player player, CallbackInfo ci) {
        if (FPVMod.getActiveDroneId(player.getUUID()).isPresent()) {
            ci.cancel();
        }
    }
}
