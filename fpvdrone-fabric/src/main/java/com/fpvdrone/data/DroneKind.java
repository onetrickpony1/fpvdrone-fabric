package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum DroneKind {
   v5suI,
   ibzfxic;

   public static final Codec<DroneKind> wuhB = Codec.STRING.xmap(DroneKind::v5suI, DroneKind::v5suI);

   public static DroneKind v5suI(String var0) {
      if (var0 == null) {
         return v5suI;
      }

      return switch (var0.trim().toLowerCase(Locale.ROOT)) {
         case "quadcopter", "quad", "mavic" -> ibzfxic;
         default -> v5suI;
      };
   }

   public String v5suI() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
