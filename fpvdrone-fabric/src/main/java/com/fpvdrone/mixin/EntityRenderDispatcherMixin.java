package com.fpvdrone.mixin;

import com.fpvdrone.client.DronePlayerRenderer;
import com.fpvdrone.FPVClientMod;
import com.fpvdrone.entity.DroneEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Hides the model of any player who is currently flying a drone (their body is shown by the pilot-body entity instead). */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void fpvdrone$hideFpvPlayers(E entity, Frustum culler, double camX, double camY, double camZ, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && DronePlayerRenderer.shouldHide(player)) {
            cir.setReturnValue(false);
        }
        if (entity instanceof DroneEntity) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui.hud.isHidden() && mc.options.getCameraType().isFirstPerson() && mc.player != null
                && FPVClientMod.getActiveDroneId(mc.player.getUUID()).filter(entity.getUUID()::equals).isPresent()) {
                cir.setReturnValue(false);
            }
        }
    }
}
