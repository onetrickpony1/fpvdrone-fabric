package com.fpvdrone.mixin;

import com.fpvdrone.entity.ZonePushOut;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 1.1.8 EntityMixin: after a player's own move, push them out of munition and ramp hitboxes. */
@Mixin(Entity.class)
public class EntityZoneCollisionMixin {
   @Inject(method = "move", at = @At("TAIL"))
   private void fpvdrone$resolveObbCollision(MoverType type, Vec3 delta, CallbackInfo ci) {
      if ((type == MoverType.SELF || type == MoverType.PLAYER) && (Object)this instanceof Player player) {
         ZonePushOut.resolve(player);
      }
   }
}
