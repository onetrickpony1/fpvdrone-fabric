package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum DroneKind {
   FPV,
   QUADCOPTER;

   public static final Codec<DroneKind> wuhB = Codec.STRING.xmap(DroneKind::v5suI, DroneKind::v5suI);

   public static DroneKind v5suI(String var0) {
      if (var0 == null) {
         return FPV;
      }

      return switch (var0.trim().toLowerCase(Locale.ROOT)) {
         case "quadcopter", "quad", "mavic" -> QUADCOPTER;
         default -> FPV;
      };
   }

   public String v5suI() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
