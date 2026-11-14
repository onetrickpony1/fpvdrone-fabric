package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.physics.PhysicsState;
import com.fpvdrone.render.CameraManager;
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
        PhysicsState physics = this.minecraft.player == null ? null : FPVMod.getPhysicsState(this.minecraft.player.getUUID());
        // 1.1.8: holding Free Look in third person turns the mouse into an orbit camera instead of the sticks.
        boolean freeLook = CameraManager.isFreeLookActive(this.minecraft);
        // 1.1.8-PENDING: a loitering munition being flown also takes the mouse (GAduSQ8Kp), and the Lancet aim screen does not block it.
        boolean active = (physics != null || freeLook)
            && this.minecraft.gui.screen() == null
            && this.minecraft.mouseHandler.isMouseGrabbed()
            && this.minecraft.isWindowActive();
        if (!active) {
            if (physics != null) {
                physics.clearMouseDelta();
            }

            return;
        }

        this.fpvdrone$lastXpos = xpos;
        this.fpvdrone$lastYpos = ypos;
        double sensitivity = (Double)(Object)this.minecraft.options.sensitivity().get() * 0.6 + 0.2;
        double finalSensitivity = sensitivity * sensitivity * sensitivity * 8.0;
        if (freeLook) {
            CameraManager.addFreeLook((float)(xrel * finalSensitivity * 0.15), (float)(yrel * finalSensitivity * 0.15));
            if (physics != null) {
                physics.clearMouseDelta();
            }
        } else {
            physics.setMouseDeltaYaw((float)(xrel * finalSensitivity * 0.15 * 10.0));
            physics.setMouseDeltaPitch((float)(yrel * finalSensitivity * 0.15 * 10.0));
        }
    }

    /** 1.1.8 MR7Qyh (InputEvent.MouseButton.Pre): left clicks count towards interact options that need several. */
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void fpvdrone$interactMenuClick(long handle, net.minecraft.client.input.MouseButtonInfo button, int action, CallbackInfo ci) {
        if (handle == this.minecraft.getWindow().handle() && action == 1 && button.button() == 0
            && com.fpvdrone.client.interact.InteractMenu.onLeftClick()) {
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void fpvdrone$onScroll(long handle, double xoffset, double yoffset, CallbackInfo ci) {
        if (handle == this.minecraft.getWindow().handle()) {
            // 1.1.8: gimbal tilt (and later the interact menu / munition zoom) gets the wheel first.
            if (CameraManager.onMouseScroll(yoffset) || com.fpvdrone.client.KeyManager.onMouseScroll(yoffset)) {
                ci.cancel();
            }
        }
    }
}
