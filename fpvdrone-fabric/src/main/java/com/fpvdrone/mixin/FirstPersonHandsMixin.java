package com.fpvdrone.mixin;

import com.fpvdrone.client.HudHider;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsMixin {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void fpvdrone$hideHands(CallbackInfo ci) {
        if (HudHider.isWearingLinkedGoggles()) ci.cancel();
    }
}
