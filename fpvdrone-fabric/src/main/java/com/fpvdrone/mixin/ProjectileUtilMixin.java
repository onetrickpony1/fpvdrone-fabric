package com.fpvdrone.mixin;

import com.fpvdrone.entity.DamageZoneTarget;
import com.fpvdrone.entity.ProjectileHitPoints;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 1.1.8 ProjectileUtilMixin: entities with damage zones (loitering munitions, ramps) are hit on their oriented
 * hitboxes instead of their plain bounding box, by arrows and other projectiles and by the player's pick. The hit
 * point is remembered (ProjectileHitPoints) so the entity can tell which zone was struck.
 */
@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {
   @Unique
   private static final double FPVDRONE_GATHER_INFLATE = 4.0;

   @Inject(
      method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;",
      at = @At("TAIL"),
      cancellable = true
   )
   private static void fpvdrone$obbHit(
      Entity except, Vec3 from, Vec3 to, AABB box, Predicate<Entity> matching, double maxValue, CallbackInfoReturnable<EntityHitResult> cir
   ) {
      EntityHitResult refined = fpvdrone$refine(except.level(), except, from, to, box, matching, maxValue, cir.getReturnValue());
      if (refined != cir.getReturnValue()) {
         cir.setReturnValue(refined);
      }
   }

   @Inject(
      method = "getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;",
      at = @At("TAIL"),
      cancellable = true
   )
   private static void fpvdrone$obbHitProjectile(
      Level level, Entity source, Vec3 from, Vec3 to, AABB area, Predicate<Entity> matching, float margin, CallbackInfoReturnable<EntityHitResult> cir
   ) {
      EntityHitResult refined = fpvdrone$refine(level, source, from, to, area, matching, 0.0, cir.getReturnValue());
      if (refined != cir.getReturnValue()) {
         cir.setReturnValue(refined);
      }
   }

   @Unique
   private static EntityHitResult fpvdrone$refine(
      Level level, Entity source, Vec3 from, Vec3 to, AABB area, Predicate<Entity> matching, double maxDistSq, EntityHitResult vanilla
   ) {
      EntityHitResult best = null;
      double bestSq = Double.MAX_VALUE;
      if (vanilla != null && !(vanilla.getEntity() instanceof DamageZoneTarget)) {
         best = vanilla;
         bestSq = from.distanceToSqr(vanilla.getLocation());
      }

      double limit = maxDistSq > 0.0 ? maxDistSq : Double.MAX_VALUE;

      for (Entity entity : level.getEntities(source, area.inflate(FPVDRONE_GATHER_INFLATE), matching)) {
         if (entity instanceof DamageZoneTarget target && target.v5suI()) {
            Vec3 hit = target.v5suI(from, to, 1.0F);
            if (hit != null) {
               ProjectileHitPoints.v5suI(entity.getId(), hit);
               double d = from.distanceToSqr(hit);
               if (d <= limit && d < bestSq) {
                  bestSq = d;
                  best = new EntityHitResult(entity, hit);
               }
            }
         }
      }

      return best;
   }
}
