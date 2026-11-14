package com.fpvdrone.util;

import java.util.ArrayDeque;
import java.util.Deque;
import org.joml.Vector3f;

public final class UplinkQueue {
   private static final int v5suI = 24;
   private static final float ibzfxic = 0.122499995F;
   private final Deque<Vector3f> wuhB = new ArrayDeque<>(24);
   private Vector3f dOfFwvo = new Vector3f();
   private long rsDd50tfV = -1L;
   private int uSUb;

   public boolean v5suI(Vector3f var1, long var2) {
      if (this.dOfFwvo.distanceSquared(var1) < 0.122499995F && var2 - this.rsDd50tfV < 2L) {
         this.uSUb++;
         return false;
      }

      if (this.wuhB.size() >= 24) {
         this.wuhB.removeFirst();
         this.uSUb++;
      }

      this.wuhB.addLast(var1);
      this.dOfFwvo = var1;
      this.rsDd50tfV = var2;
      return true;
   }

   public Vector3f v5suI() {
      Vector3f var1 = this.wuhB.pollFirst();
      return var1 == null ? this.dOfFwvo : var1;
   }

   public int ibzfxic() {
      return this.wuhB.size();
   }

   public int wuhB() {
      return this.uSUb;
   }

   public void dOfFwvo() {
      this.wuhB.clear();
      this.uSUb = 0;
      this.rsDd50tfV = -1L;
   }

   public String rsDd50tfV() {
      if (this.uSUb == 0) {
         return "uplink nominal";
      } else {
         return this.uSUb < 10 ? "uplink degraded" : "uplink saturated, coalescing aggressively";
      }
   }
}
