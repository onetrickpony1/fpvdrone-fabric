package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.physics.PhysicsState;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MouseHandler.class})
public class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;
    @Unique
    private double fpvdrone$lastXpos = 0.0;
    @Unique
    private double fpvdrone$lastYpos = 0.0;

    @Inject(
        method = {"onMove"},
        at = {@At("HEAD")}
    )
    private void fpvdrone$captureRawMouseDelta(long windowPointer, double xpos, double ypos, double xrel, double yrel, CallbackInfo ci) {
        if (this.minecraft.player != null) {
            UUID playerId = this.minecraft.player.getUUID();
            PhysicsState physics = FPVMod.getPhysicsState(playerId);
            if (physics != null && this.minecraft.gui.screen() == null && this.minecraft.mouseHandler.isMouseGrabbed()) {
                double dx = xrel;
                double dy = yrel;
                this.fpvdrone$lastXpos = xpos;
                this.fpvdrone$lastYpos = ypos;
                double sensitivity = (Double)(Object)this.minecraft.options.sensitivity().get() * 0.6 + 0.2;
                double finalSensitivity = sensitivity * sensitivity * sensitivity * 8.0;
                float yawDelta = (float)(dx * finalSensitivity * 0.15 * 10.0);
                float pitchDelta = (float)(dy * finalSensitivity * 0.15 * 10.0);
                physics.setMouseDeltaYaw(yawDelta);
                physics.setMouseDeltaPitch(pitchDelta);
            }
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void fpvdrone$onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (handle == this.minecraft.getWindow().handle() && com.fpvdrone.client.KeyManager.onMouseScroll(yoffset)) {
            ci.cancel();
        }
    }
}
