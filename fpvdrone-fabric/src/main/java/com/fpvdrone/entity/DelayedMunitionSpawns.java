package com.fpvdrone.entity;

import com.fpvdrone.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;

/**
 * 1.1.8 Record_SeAND2OQ: loitering munitions spawned after a delay of 3 seconds (60 ticks), ticked by the server.
 */
public final class DelayedMunitionSpawns {
   private static final int DELAY_TICKS = 60;
   private static final List<Pending> PENDING = new ArrayList<>();

   private DelayedMunitionSpawns() {
   }

   public static void schedule(ServerLevel level, double x, double y, double z, float yaw, String droneType) {
      PENDING.add(new Pending(level, x, y, z, yaw, droneType, DELAY_TICKS));
   }

   /** Server tick (1.1.8 FPVMod server tick). */
   public static void tick() {
      for (int i = PENDING.size() - 1; i >= 0; i--) {
         Pending next = PENDING.get(i).withTicks(PENDING.get(i).ticksLeft() - 1);
         if (next.ticksLeft() <= 0) {
            PENDING.remove(i);
            spawn(next);
         } else {
            PENDING.set(i, next);
         }
      }
   }

   private static void spawn(Pending p) {
      LoiteringMunitionEntity munition = new LoiteringMunitionEntity(ModEntities.LOITERING_MUNITION.get(), p.level());
      munition.ibzfxic(p.droneType());
      munition.snapTo(p.x(), p.y(), p.z(), p.yaw(), 0.0F);
      p.level().addFreshEntity(munition);
   }

   private record Pending(ServerLevel level, double x, double y, double z, float yaw, String droneType, int ticksLeft) {
      Pending withTicks(int ticks) {
         return new Pending(this.level, this.x, this.y, this.z, this.yaw, this.droneType, ticks);
      }
   }
}
