package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemEntity.class})
public class ItemPickupMixin {
    @Inject(
        method = {"playerTouch"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockItemPickup(Player player, CallbackInfo ci) {
        if (FPVMod.getActiveDroneId(player.getUUID()).isPresent()) {
            ci.cancel();
        }
    }
}
