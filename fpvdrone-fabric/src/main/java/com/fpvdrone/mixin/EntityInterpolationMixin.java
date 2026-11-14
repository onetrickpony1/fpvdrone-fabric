package com.fpvdrone.mixin;

import com.fpvdrone.client.FpvPlayerTracker;
import com.fpvdrone.entity.DroneEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server position updates arrive through moveOrInterpolateTo in 26.x (it replaced lerpTo). Ignore them for frozen remote
 * pilots and, per DroneEntity's rules, for drones flown or simulated client-side.
 */
@Mixin(Entity.class)
public abstract class EntityInterpolationMixin {
    @Shadow
    public abstract Level level();

    @Inject(method = "moveOrInterpolateTo(Lnet/minecraft/world/entity/PositionPath;FFZ)V", at = @At("HEAD"), cancellable = true)
    private void fpvdrone$filterServerPosition(@Nullable PositionPath position, float yRot, float xRot, boolean hasRotation, CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        if (!this.level().isClientSide()) {
            return;
        }

        if (self instanceof DroneEntity drone) {
            if (!drone.acceptServerPosition(position)) {
                ci.cancel();
            }
        } else if (self instanceof net.minecraft.client.player.RemotePlayer && FpvPlayerTracker.isFrozen(self.getUUID())) {
            ci.cancel();
        }
    }
}
