package com.fpvdrone.sync;

import java.util.function.DoublePredicate;

public final class FixedStepClock {
   public static final double v5suI = 0.004166666666666667;
   public static final double ibzfxic = 0.25;
   private static final double wuhB = 1.0E-9;
   private double dOfFwvo = 0.0;

   public boolean v5suI(double var1, DoublePredicate var3) {
      if (var1 > 0.0) {
         this.dOfFwvo = Math.min(this.dOfFwvo + var1, 0.25);
      }

      while (this.dOfFwvo >= 0.004166666666666667) {
         this.dOfFwvo -= 0.004166666666666667;
         if (!var3.test(0.004166666666666667)) {
            return false;
         }
      }

      if (this.dOfFwvo > 1.0E-9) {
         double var4 = this.dOfFwvo;
         this.dOfFwvo = 0.0;
         if (!var3.test(var4)) {
            return false;
         }
      }

      return true;
   }

   public double v5suI() {
      return this.dOfFwvo;
   }

   public void ibzfxic() {
      this.dOfFwvo = 0.0;
   }
}
