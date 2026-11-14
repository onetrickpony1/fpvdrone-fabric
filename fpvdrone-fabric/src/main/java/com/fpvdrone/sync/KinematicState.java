package com.fpvdrone.sync;

import org.joml.Quaternionf;

public final class KinematicState {
   public double v5suI;
   public double ibzfxic;
   public double wuhB;
   public double dOfFwvo;
   public double rsDd50tfV;
   public double uSUb;
   public float jGhI2;
   public float wEJx3Itb;
   public float bAcdvq9;
   public final Quaternionf c1jHYPb4 = new Quaternionf();
   public boolean v8ZN;
   public boolean zTT3nw6;

   public void v5suI(KinematicState var1) {
      this.v5suI = var1.v5suI;
      this.ibzfxic = var1.ibzfxic;
      this.wuhB = var1.wuhB;
      this.dOfFwvo = var1.dOfFwvo;
      this.rsDd50tfV = var1.rsDd50tfV;
      this.uSUb = var1.uSUb;
      this.jGhI2 = var1.jGhI2;
      this.wEJx3Itb = var1.wEJx3Itb;
      this.bAcdvq9 = var1.bAcdvq9;
      this.c1jHYPb4.set(var1.c1jHYPb4);
      this.v8ZN = var1.v8ZN;
      this.zTT3nw6 = var1.zTT3nw6;
   }

   public void v5suI(double var1, double var3, double var5) {
      this.v5suI = var1;
      this.ibzfxic = var3;
      this.wuhB = var5;
   }

   public void ibzfxic(double var1, double var3, double var5) {
      this.dOfFwvo = var1;
      this.rsDd50tfV = var3;
      this.uSUb = var5;
   }

   public void v5suI(float var1, float var2, float var3) {
      this.jGhI2 = var1;
      this.wEJx3Itb = var2;
      this.bAcdvq9 = var3;
      this.v8ZN = true;
   }

   public void v5suI(float var1, float var2, float var3, float var4) {
      this.c1jHYPb4.set(var1, var2, var3, var4);
      this.zTT3nw6 = true;
   }

   public void v5suI() {
      this.v8ZN = false;
      this.zTT3nw6 = false;
   }
}
