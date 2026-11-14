package com.fpvdrone.video;

public class VideoSignalEffect {
   public static boolean shouldShowNoSignal(float signalStrength) {
      return signalStrength <= 0.0F;
   }
}
