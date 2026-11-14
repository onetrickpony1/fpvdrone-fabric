package com.fpvdrone.data;

public enum RouteSpeed {
   SLOW(0.6),
   CRUISE(1.0),
   FAST(1.6);

   public final double dOfFwvo;

   RouteSpeed(double var3) {
      this.dOfFwvo = var3;
   }

   public static RouteSpeed v5suI(double var0) {
      RouteSpeed var2 = CRUISE;
      double var3 = Double.MAX_VALUE;

      for (RouteSpeed var8 : values()) {
         double var9 = Math.abs(var8.dOfFwvo - var0);
         if (var9 < var3) {
            var3 = var9;
            var2 = var8;
         }
      }

      return var2;
   }
}
