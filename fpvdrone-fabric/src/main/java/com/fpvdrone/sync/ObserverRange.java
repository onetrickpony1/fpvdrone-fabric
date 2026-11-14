package com.fpvdrone.sync;

import com.fpvdrone.util.ParticleSendRange;

public interface ObserverRange {
   default boolean wuhB(double var1) {
      double var3 = ParticleSendRange.get();
      return var1 < var3 * var3;
   }
}
