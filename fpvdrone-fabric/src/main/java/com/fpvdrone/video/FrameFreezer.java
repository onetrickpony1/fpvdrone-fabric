package com.fpvdrone.video;

import com.fpvdrone.FpvConfig;

public class FrameFreezer {
   private static final FrameFreezer instance = new FrameFreezer();
   private long lastFrameTime;
   private long currentHoldDuration;
   private static final long BASE_FRAME_INTERVAL_MS = 16L;

   private FrameFreezer() {
      this.reset();
   }

   public static FrameFreezer getInstance() {
      return instance;
   }

   public static long calculateHoldDuration(float signalStrength, long baseInterval) {
      float signal = Math.max(0.05F, Math.min(1.0F, signalStrength));
      float factor = 1.0F + (1.0F - signal) * FpvConfig.getFreezeIntensity();
      return (long)((float)baseInterval * factor);
   }

   public long applyVariation(long baseDuration) {
      return baseDuration;
   }

   public boolean shouldUpdateFrame(float signalStrength) {
      if (signalStrength <= 0.0F) {
         return false;
      } else if (signalStrength >= 1.0F) {
         this.lastFrameTime = System.currentTimeMillis();
         return true;
      } else {
         long now = System.currentTimeMillis();
         if (now - this.lastFrameTime >= this.currentHoldDuration) {
            this.lastFrameTime = now;
            long baseDuration = calculateHoldDuration(signalStrength, 16L);
            this.currentHoldDuration = baseDuration;
            return true;
         } else {
            return false;
         }
      }
   }

   public void reset() {
      this.lastFrameTime = System.currentTimeMillis();
      this.currentHoldDuration = 16L;
   }
}
