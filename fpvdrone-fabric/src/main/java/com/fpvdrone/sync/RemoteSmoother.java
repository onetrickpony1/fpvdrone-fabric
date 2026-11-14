package com.fpvdrone.sync;

public final class RemoteSmoother {
   public static final double v5suI = 0.12;
   public static final double ibzfxic = 8.0;
   private static final double dOfFwvo = 0.02;
   public static final double wuhB = 0.5;
   private final KinematicState rsDd50tfV = new KinematicState();
   private final KinematicState uSUb = new KinematicState();
   private final FixedStepClock jGhI2 = new FixedStepClock();
   private boolean wEJx3Itb = false;
   private double bAcdvq9 = 0.12;
   private double c1jHYPb4 = 0.5;
   private double v8ZN = 0.0;
   private long zTT3nw6 = Long.MIN_VALUE;
   private boolean vmpmU = false;
   private static final float b69s = 1000.0F;
   private float cXHvq = 0.0F;
   private float rxYnaa = 0.0F;
   private float r6s7FL3 = 0.0F;

   public boolean v5suI() {
      return this.wEJx3Itb;
   }

   public void ibzfxic() {
      this.wEJx3Itb = false;
      this.v8ZN = 0.0;
      this.jGhI2.ibzfxic();
      this.cXHvq = 0.0F;
      this.rxYnaa = 0.0F;
      this.r6s7FL3 = 0.0F;
   }

   public KinematicState wuhB() {
      return this.uSUb;
   }

   public void v5suI(double var1) {
      this.bAcdvq9 = Math.max(0.001, var1);
   }

   public void ibzfxic(double var1) {
      this.c1jHYPb4 = Math.max(0.001, var1);
   }

   public void v5suI(KinematicState var1, long var2, boolean var4, double var5) {
      if (!this.vmpmU || var2 > this.zTT3nw6 || var4) {
         long var7 = this.zTT3nw6;
         boolean var9 = this.vmpmU;
         this.zTT3nw6 = var2;
         this.vmpmU = true;
         this.v8ZN = 0.0;
         if (!this.wEJx3Itb) {
            this.rsDd50tfV.v5suI(var1);
            this.uSUb.v5suI(var1);
            this.wEJx3Itb = true;
         } else {
            double var10 = var9 ? (var2 - var7) / 20.0 : 0.0;
            if (var1.v8ZN && this.rsDd50tfV.v8ZN && var10 > 0.001) {
               double var12 = 1.0 / var10;
               this.cXHvq = dOfFwvo(wuhB(var1.jGhI2 - this.rsDd50tfV.jGhI2) * var12);
               this.rxYnaa = dOfFwvo((var1.wEJx3Itb - this.rsDd50tfV.wEJx3Itb) * var12);
               this.r6s7FL3 = dOfFwvo(wuhB(var1.bAcdvq9 - this.rsDd50tfV.bAcdvq9) * var12);
            } else {
               this.cXHvq = 0.0F;
               this.rxYnaa = 0.0F;
               this.r6s7FL3 = 0.0F;
            }

            double var19 = var1.v5suI - this.uSUb.v5suI;
            double var14 = var1.ibzfxic - this.uSUb.ibzfxic;
            double var16 = var1.wuhB - this.uSUb.wuhB;
            boolean var18 = var4 || var19 * var19 + var14 * var14 + var16 * var16 > var5 * var5;
            this.rsDd50tfV.v5suI(var1);
            if (var18) {
               this.uSUb.v5suI(var1);
            }
         }
      }
   }

   public boolean v5suI(double var1, Extrapolator var3) {
      if (!this.wEJx3Itb) {
         return false;
      }

      this.v8ZN += var1;
      if (this.v8ZN > this.c1jHYPb4) {
         this.ibzfxic();
         return false;
      }

      double var4 = this.uSUb.v5suI - this.rsDd50tfV.v5suI;
      double var6 = this.uSUb.ibzfxic - this.rsDd50tfV.ibzfxic;
      double var8 = this.uSUb.wuhB - this.rsDd50tfV.wuhB;
      double var10 = 0.0;
      double var12 = 0.0;
      double var14 = 0.0;
      if (this.rsDd50tfV.v8ZN) {
         var10 = wuhB(this.uSUb.jGhI2 - this.rsDd50tfV.jGhI2);
         var12 = this.uSUb.wEJx3Itb - this.rsDd50tfV.wEJx3Itb;
         var14 = wuhB(this.uSUb.bAcdvq9 - this.rsDd50tfV.bAcdvq9);
      }

      this.jGhI2.v5suI(var1, var2 -> var3.v5suI(this.rsDd50tfV, var2));
      double var16 = var1 <= 0.0 ? 0.0 : 1.0 - Math.pow(0.02, var1 / this.bAcdvq9);
      double var18 = 1.0 - var16;
      float var20 = this.rsDd50tfV.jGhI2;
      float var21 = this.rsDd50tfV.bAcdvq9;
      if (this.rsDd50tfV.v8ZN && var1 > 0.0) {
         this.rsDd50tfV.jGhI2 = (float)wuhB(this.rsDd50tfV.jGhI2 + this.cXHvq * var1);
         this.rsDd50tfV.wEJx3Itb = this.rsDd50tfV.wEJx3Itb + (float)(this.rxYnaa * var1);
         this.rsDd50tfV.bAcdvq9 = (float)wuhB(this.rsDd50tfV.bAcdvq9 + this.r6s7FL3 * var1);
         float var22 = (float)(1.0 - var16);
         this.cXHvq *= var22;
         this.rxYnaa *= var22;
         this.r6s7FL3 *= var22;
      }

      this.uSUb.v5suI = this.rsDd50tfV.v5suI + var4 * var18;
      this.uSUb.ibzfxic = this.rsDd50tfV.ibzfxic + var6 * var18;
      this.uSUb.wuhB = this.rsDd50tfV.wuhB + var8 * var18;
      this.uSUb.dOfFwvo = this.rsDd50tfV.dOfFwvo;
      this.uSUb.rsDd50tfV = this.rsDd50tfV.rsDd50tfV;
      this.uSUb.uSUb = this.rsDd50tfV.uSUb;
      if (this.rsDd50tfV.v8ZN) {
         this.uSUb.jGhI2 = this.uSUb.jGhI2 + (float)(wuhB(this.rsDd50tfV.jGhI2 - var20) - var10 * var16);
         this.uSUb.wEJx3Itb = (float)(this.rsDd50tfV.wEJx3Itb + var12 * var18);
         this.uSUb.bAcdvq9 = this.uSUb.bAcdvq9 + (float)(wuhB(this.rsDd50tfV.bAcdvq9 - var21) - var14 * var16);
         this.uSUb.v8ZN = true;
      }

      if (this.rsDd50tfV.zTT3nw6) {
         this.uSUb.c1jHYPb4.slerp(this.rsDd50tfV.c1jHYPb4, (float)var16);
         this.uSUb.zTT3nw6 = true;
      }

      return true;
   }

   private static double wuhB(double var0) {
      double var2 = var0 % 360.0;
      if (var2 >= 180.0) {
         var2 -= 360.0;
      }

      if (var2 < -180.0) {
         var2 += 360.0;
      }

      return var2;
   }

   private static float dOfFwvo(double var0) {
      if (var0 > 1000.0) {
         return 1000.0F;
      } else {
         return var0 < -1000.0 ? -1000.0F : (float)var0;
      }
   }
}
