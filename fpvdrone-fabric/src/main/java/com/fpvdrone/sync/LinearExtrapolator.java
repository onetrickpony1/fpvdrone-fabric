package com.fpvdrone.sync;

public final class LinearExtrapolator implements Extrapolator {
   public static final LinearExtrapolator v5suI = new LinearExtrapolator();

   private LinearExtrapolator() {
   }

   @Override
   public boolean v5suI(KinematicState var1, double var2) {
      double var4 = var2 * 20.0;
      var1.v5suI = var1.v5suI + var1.dOfFwvo * var4;
      var1.ibzfxic = var1.ibzfxic + var1.rsDd50tfV * var4;
      var1.wuhB = var1.wuhB + var1.uSUb * var4;
      return true;
   }
}
