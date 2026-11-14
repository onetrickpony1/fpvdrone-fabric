package com.fpvdrone.mixin;

import com.fpvdrone.FPVClientMod;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "update", at = @At("HEAD"))
    private void fpvdrone$advanceFlight(CallbackInfo ci) {
        FPVClientMod.renderTick();
    }

    /** Forge RenderTickEvent END: after the frame has been drawn. */
    @Inject(method = "render", at = @At("TAIL"))
    private void fpvdrone$afterFrame(CallbackInfo ci) {
        FPVClientMod.renderTickEnd();
    }
}
