package com.fpvdrone.data;

/** A flight-route point (x, y, z, loiter radius or -1, speed factor). 1.1.8 Waypoint. */
public final class Waypoint {
   public final double v5suI;
   public final double ibzfxic;
   public final double wuhB;
   public final double dOfFwvo;
   public final double rsDd50tfV;

   public Waypoint(double var1, double var3, double var5) {
      this(var1, var3, var5, -1.0, RouteSpeed.CRUISE.dOfFwvo);
   }

   public Waypoint(double var1, double var3, double var5, double var7) {
      this(var1, var3, var5, var7, RouteSpeed.CRUISE.dOfFwvo);
   }

   public Waypoint(double var1, double var3, double var5, double var7, double var9) {
      this.v5suI = var1;
      this.ibzfxic = var3;
      this.wuhB = var5;
      this.dOfFwvo = var7;
      this.rsDd50tfV = var9;
   }

   public Waypoint v5suI(double var1) {
      return new Waypoint(this.v5suI, var1, this.wuhB, this.dOfFwvo, this.rsDd50tfV);
   }

   public Waypoint ibzfxic(double var1) {
      return new Waypoint(this.v5suI, this.ibzfxic, this.wuhB, var1, this.rsDd50tfV);
   }

   public Waypoint wuhB(double var1) {
      return new Waypoint(this.v5suI, this.ibzfxic, this.wuhB, this.dOfFwvo, var1);
   }
}
