package com.fpvdrone.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.phys.Vec3;

public final class IvHitInfo {
   private static final Map<Integer, IvHitInfo.KKeMvpuBgEY> v5suI = new ConcurrentHashMap<>();
   private static final Map<Integer, Vec3> ibzfxic = new ConcurrentHashMap<>();

   private IvHitInfo() {
   }

   public static void v5suI(int var0, float var1, boolean var2, double var3, Vec3 var5) {
      v5suI.put(var0, new IvHitInfo.KKeMvpuBgEY(var1, var2, var3, var5));
   }

   public static IvHitInfo.KKeMvpuBgEY v5suI(int var0) {
      return v5suI.remove(var0);
   }

   public static void v5suI(int var0, Vec3 var1) {
      ibzfxic.put(var0, var1);
   }

   public static Vec3 ibzfxic(int var0) {
      return ibzfxic.remove(var0);
   }

   public static final class KKeMvpuBgEY {
      private final float v5suI;
      private final boolean ibzfxic;
      private final double wuhB;
      private final Vec3 dOfFwvo;

      public KKeMvpuBgEY(float var1, boolean var2, double var3, Vec3 var5) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var5;
      }

      public float v5suI() {
         return this.v5suI;
      }

      public boolean ibzfxic() {
         return this.ibzfxic;
      }

      public double wuhB() {
         return this.wuhB;
      }

      public Vec3 dOfFwvo() {
         return this.dOfFwvo;
      }

   }
}
