package com.fpvdrone.util;

import org.joml.Quaternionf;

public final class YawPitch {
   private YawPitch() {
   }

   public static Quaternionf v5suI(float var0, float var1) {
      return new Quaternionf().rotationYXZ((float)Math.toRadians(-var0), (float)Math.toRadians(var1), 0.0F);
   }
}
