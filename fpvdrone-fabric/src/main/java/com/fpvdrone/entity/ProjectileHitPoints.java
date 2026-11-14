package com.fpvdrone.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.phys.Vec3;

public final class ProjectileHitPoints {
   private static final Map<Integer, Vec3> v5suI = new ConcurrentHashMap<>();

   private ProjectileHitPoints() {
   }

   public static void v5suI(int var0, Vec3 var1) {
      v5suI.put(var0, var1);
   }

   public static Vec3 v5suI(int var0) {
      return v5suI.remove(var0);
   }
}
