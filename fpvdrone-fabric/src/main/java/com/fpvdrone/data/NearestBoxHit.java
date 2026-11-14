package com.fpvdrone.data;

import java.util.List;
import net.minecraft.world.phys.Vec3;

public final class NearestBoxHit {
   private NearestBoxHit() {
   }

   public static NearestBoxHit.KKeMvpuBgEY v5suI(List<BoneBox> var0, List<OrientedBox> var1, Vec3 var2, Vec3 var3) {
      NearestBoxHit.KKeMvpuBgEY var4 = null;
      int var5 = Math.min(var0.size(), var1.size());

      for (int var6 = 0; var6 < var5; var6++) {
         Vec3 var7 = ((OrientedBox)var1.get(var6)).v5suI(var2, var3);
         if (var7 != null) {
            double var8 = var2.distanceToSqr(var7);
            if (var4 == null || var8 < var4.wuhB()) {
               var4 = new NearestBoxHit.KKeMvpuBgEY(((BoneBox)var0.get(var6)).rsDd50tfV(), var7, var8);
            }
         }
      }

      return var4;
   }

   public static NearestBoxHit.KKeMvpuBgEY v5suI(List<PlacedInteractZone> var0, Vec3 var1, Vec3 var2) {
      NearestBoxHit.KKeMvpuBgEY var3 = null;

      for (PlacedInteractZone var5 : var0) {
         Vec3 var6 = var5.ibzfxic().v5suI(var1, var2);
         if (var6 != null) {
            double var7 = var1.distanceToSqr(var6);
            if (var3 == null || var7 < var3.wuhB()) {
               var3 = new NearestBoxHit.KKeMvpuBgEY(var5.v5suI(), var6, var7);
            }
         }
      }

      return var3;
   }

   public static final class KKeMvpuBgEY {
      private final String v5suI;
      private final Vec3 ibzfxic;
      private final double wuhB;

      public KKeMvpuBgEY(String var1, Vec3 var2, double var3) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
      }


      public String v5suI() {

         return this.v5suI;

      }

   
      public Vec3 ibzfxic() {
         return this.ibzfxic;
      }

      public double wuhB() {
         return this.wuhB;
      }
   }
}
