package com.fpvdrone.client.map;

import com.fpvdrone.data.FlightRoute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public final class ReconMarks {
   private static final long v5suI = 2000L;
   private static final long ibzfxic = 600L;
   private static final ReconMarks wuhB = new ReconMarks();
   private final Map<Integer, ReconMarks.KKeMvpuBgEY> dOfFwvo = new HashMap<>();

   public static ReconMarks v5suI() {
      return wuhB;
   }

   private ReconMarks() {
   }

   public synchronized void v5suI(
      int var1, String var2, double var3, double var5, double var7, Identifier var9, FlightRoute var10, Vec3 var11, float var12, int var13
   ) {
      this.dOfFwvo.put(var1, new ReconMarks.KKeMvpuBgEY(var1, var2, var3, var5, var7, var9, var10, var11, var12, var13, System.currentTimeMillis(), 0L));
   }

   public synchronized ReconMarks.KKeMvpuBgEY v5suI(int var1) {
      return this.dOfFwvo.get(var1);
   }

   public static float v5suI(ReconMarks.KKeMvpuBgEY var0) {
      if (var0.zTT3nw6() == 0L) {
         return 1.0F;
      }

      double var1 = System.currentTimeMillis() - var0.zTT3nw6();
      return (float)Math.max(0.0, Math.min(1.0, 1.0 - var1 / 600.0));
   }

   public synchronized void ibzfxic(int var1) {
      this.dOfFwvo.remove(var1);
   }

   public synchronized void wuhB(int var1) {
      ReconMarks.KKeMvpuBgEY var2 = this.dOfFwvo.get(var1);
      if (var2 != null && var2.zTT3nw6() == 0L) {
         this.dOfFwvo
            .put(
               var1,
               new ReconMarks.KKeMvpuBgEY(
                  var2.v5suI(),
                  var2.ibzfxic(),
                  var2.wuhB(),
                  var2.dOfFwvo(),
                  var2.rsDd50tfV(),
                  var2.uSUb(),
                  var2.jGhI2(),
                  var2.wEJx3Itb(),
                  var2.bAcdvq9(),
                  var2.c1jHYPb4(),
                  var2.v8ZN(),
                  System.currentTimeMillis()
               )
            );
      }
   }

   public synchronized void ibzfxic() {
      this.dOfFwvo.clear();
   }

   public synchronized List<ReconMarks.KKeMvpuBgEY> v5suI(Identifier var1) {
      long var2 = System.currentTimeMillis();
      var var4 = this.dOfFwvo.values().iterator();
      var var5 = new ArrayList();

      while (var4.hasNext()) {
         ReconMarks.KKeMvpuBgEY var6 = (ReconMarks.KKeMvpuBgEY)var4.next();
         if (var6.zTT3nw6() != 0L) {
            if (var2 - var6.zTT3nw6() > 600L) {
               var4.remove();
               continue;
            }
         } else if (var2 - var6.v8ZN() > 2000L) {
            var4.remove();
            continue;
         }

         if (var1.equals(var6.uSUb())) {
            var5.add(var6);
         }
      }

      return var5;
   }

   public static final class KKeMvpuBgEY {
      private final int v5suI;
      private final String ibzfxic;
      private final double wuhB;
      private final double dOfFwvo;
      private final double rsDd50tfV;
      private final Identifier uSUb;
      private final FlightRoute jGhI2;
      private final Vec3 wEJx3Itb;
      private final float bAcdvq9;
      private final int c1jHYPb4;
      private final long v8ZN;
      private final long zTT3nw6;

      public KKeMvpuBgEY(
         int var1,
         String var2,
         double var3,
         double var5,
         double var7,
         Identifier var9,
         FlightRoute var10,
         Vec3 var11,
         float var12,
         int var13,
         long var14,
         long var16
      ) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var5;
         this.rsDd50tfV = var7;
         this.uSUb = var9;
         this.jGhI2 = var10;
         this.wEJx3Itb = var11;
         this.bAcdvq9 = var12;
         this.c1jHYPb4 = var13;
         this.v8ZN = var14;
         this.zTT3nw6 = var16;
      }


      public int v5suI() {

         return this.v5suI;

      }

   
      public String ibzfxic() {
         return this.ibzfxic;
      }

      public double wuhB() {
         return this.wuhB;
      }

      public double dOfFwvo() {
         return this.dOfFwvo;
      }

      public double rsDd50tfV() {
         return this.rsDd50tfV;
      }

      public Identifier uSUb() {
         return this.uSUb;
      }

      public FlightRoute jGhI2() {
         return this.jGhI2;
      }

      public Vec3 wEJx3Itb() {
         return this.wEJx3Itb;
      }

      public float bAcdvq9() {
         return this.bAcdvq9;
      }

      public int c1jHYPb4() {
         return this.c1jHYPb4;
      }

      public long v8ZN() {
         return this.v8ZN;
      }

      public long zTT3nw6() {
         return this.zTT3nw6;
      }
   }
}
