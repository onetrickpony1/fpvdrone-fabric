package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import org.jspecify.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 1.1.8: mobs cannot pick a player who is flying a drone as a target (they go for the pilot body instead). */
@Mixin(TargetingConditions.class)
public class TargetingConditionsMixin {
   @Inject(method = "test", at = @At("HEAD"), cancellable = true)
   private void fpvdrone$hideFpvPilot(ServerLevel level, @Nullable LivingEntity targeter, LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
      if (target instanceof Player && !target.level().isClientSide() && FPVMod.hasActiveDrone(target.getUUID())) {
         cir.setReturnValue(false);
      }
   }
}
