package com.fpvdrone.data;

import java.util.List;
import org.jspecify.annotations.Nullable;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 (Record_LjdslnKBxL): the nearest box a ray from {@code from} to {@code to} hits, among a model's damage boxes or
 * its placed interact zones.
 */
public final class ZoneRayPick {
   private ZoneRayPick() {
   }

   /** Damage boxes: {@code boxes.get(i)} is the placed box of {@code bones.get(i)}. */
   public static @Nullable Hit pickBone(List<BoneBox> bones, List<OrientedBox> boxes, Vec3 from, Vec3 to) {
      Hit best = null;
      int n = Math.min(bones.size(), boxes.size());

      for (int i = 0; i < n; i++) {
         Vec3 at = boxes.get(i).v5suI(from, to);
         if (at != null) {
            double distSqr = from.distanceToSqr(at);
            if (best == null || distSqr < best.distanceSqr()) {
               best = new Hit(bones.get(i).rsDd50tfV(), at, distSqr);
            }
         }
      }

      return best;
   }

   /** Interact zones placed on the model. */
   public static @Nullable Hit pickZone(List<PlacedInteractZone> zones, Vec3 from, Vec3 to) {
      Hit best = null;

      for (PlacedInteractZone zone : zones) {
         Vec3 at = zone.ibzfxic().v5suI(from, to);
         if (at != null) {
            double distSqr = from.distanceToSqr(at);
            if (best == null || distSqr < best.distanceSqr()) {
               best = new Hit(zone.v5suI(), at, distSqr);
            }
         }
      }

      return best;
   }

   /** The id of the box hit, where, and the squared distance from the ray start. */
   public record Hit(String id, Vec3 at, double distanceSqr) {
   }
}
