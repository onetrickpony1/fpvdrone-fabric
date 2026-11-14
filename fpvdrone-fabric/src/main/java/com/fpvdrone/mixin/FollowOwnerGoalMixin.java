package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.1.8: tamed pets do not follow (or teleport to) an owner who is flying a drone; the owner's player entity rides with
 * the drone while the pilot body stands where he is.
 */
@Mixin(FollowOwnerGoal.class)
public class FollowOwnerGoalMixin {
   @Shadow
   @Final
   private TamableAnimal tamable;

   @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
   private void fpvdrone$dontFollowFpvOwner(CallbackInfoReturnable<Boolean> cir) {
      if (this.fpvdrone$ownerIsFlying()) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "canContinueToUse", at = @At("HEAD"), cancellable = true)
   private void fpvdrone$stopFollowingFpvOwner(CallbackInfoReturnable<Boolean> cir) {
      if (this.fpvdrone$ownerIsFlying()) {
         cir.setReturnValue(false);
      }
   }

   @Unique
   private boolean fpvdrone$ownerIsFlying() {
      LivingEntity owner = this.tamable.getOwner();
      return owner instanceof Player && !owner.level().isClientSide() && FPVMod.hasActiveDrone(owner.getUUID());
   }
}
