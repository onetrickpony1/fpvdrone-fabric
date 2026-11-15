package com.fpvdrone.entity;

import com.fpvdrone.data.OrientedBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * 1.1.8 sxez6mi/AwIR2HC8b: pushes a player out of the oriented hitboxes of munitions and ramps after each move, so
 * they can stand on them; at most 0.5 blocks per box and 4 passes. Sneaking does not push sideways or down (only up).
 */
public final class ZonePushOut {
   private static final double SEARCH = 1.0;
   private static final int PASSES = 4;
   private static final float MAX_STEP = 0.5F;
   private static final float UP_NORMAL = 0.5F;

   private ZonePushOut() {
   }

   public static void resolve(LivingEntity entity) {
      Level level = entity.level();
      boolean sneaking = entity instanceof Player player && player.isShiftKeyDown() && !player.isSpectator();
      AABB bounds = entity.getBoundingBox();
      List<Entity> nearby = level.getEntities(entity, bounds.inflate(SEARCH), e -> e instanceof DamageZoneTarget t && !t.ibzfxic());
      if (nearby.isEmpty()) {
         return;
      }

      List<OrientedBox> boxes = new ArrayList<>();
      for (Entity e : nearby) {
         boxes.addAll(((DamageZoneTarget)e).v5suI(1.0F));
      }

      if (boxes.isEmpty()) {
         return;
      }

      Vector3f half = new Vector3f((float)(bounds.getXsize() * 0.5), (float)(bounds.getYsize() * 0.5), (float)(bounds.getZsize() * 0.5));
      Vec3 centre = bounds.getCenter();
      Vec3 push = Vec3.ZERO;
      boolean landed = false;

      for (int pass = 0; pass < PASSES; pass++) {
         boolean moved = false;
         OrientedBox self = new OrientedBox(centre.add(push), new Vector3f(half), new Quaternionf());

         for (OrientedBox box : boxes) {
            Vector3f depth = self.v5suI(box);
            if (depth == null) {
               continue;
            }

            float length = depth.length();
            if (length < 1.0E-4F) {
               continue;
            }

            Vector3f normal = new Vector3f(depth).div(length);
            if (sneaking && normal.y <= UP_NORMAL) {
               continue;
            }

            if (length > MAX_STEP) {
               depth.mul(MAX_STEP / length);
            }

            push = push.add(depth.x, depth.y, depth.z);
            moved = true;
            if (normal.y > UP_NORMAL) {
               landed = true;
            }

            self = new OrientedBox(centre.add(push), new Vector3f(half), new Quaternionf());
         }

         if (!moved) {
            break;
         }
      }

      if (push.lengthSqr() < 1.0E-10) {
         return;
      }

      entity.setPos(entity.getX() + push.x, entity.getY() + push.y, entity.getZ() + push.z);
      Vec3 velocity = entity.getDeltaMovement();
      if (landed) {
         entity.setOnGround(true);
         entity.fallDistance = 0.0;
         if (velocity.y < 0.0) {
            velocity = new Vec3(velocity.x, 0.0, velocity.z);
         }
      }

      if (push.lengthSqr() > 1.0E-8) {
         Vec3 dir = push.normalize();
         double into = velocity.dot(dir);
         if (into < 0.0) {
            velocity = velocity.subtract(dir.scale(into));
         }
      }

      entity.setDeltaMovement(velocity);
   }
}
