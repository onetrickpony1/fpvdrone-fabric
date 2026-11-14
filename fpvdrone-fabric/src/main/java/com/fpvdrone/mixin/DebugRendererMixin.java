package com.fpvdrone.mixin;

import com.fpvdrone.client.renderer.JammerZoneDebugRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds the EW jammer zone wireframes to the debug gizmos (shown with entity hitboxes, F3+B). */
@Mixin(DebugRenderer.class)
public class DebugRendererMixin {
    @Inject(method = "emitGizmos", at = @At("TAIL"))
    private void fpvdrone$jammerZones(Frustum frustum, double camX, double camY, double camZ, float partialTicks, CallbackInfo ci) {
        JammerZoneDebugRenderer.emit();
    }
}
