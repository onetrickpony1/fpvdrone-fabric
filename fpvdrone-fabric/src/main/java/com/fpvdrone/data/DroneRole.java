package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum DroneRole {
   STRIKE,
   RECON;

   public static final Codec<DroneRole> wuhB = Codec.STRING.xmap(DroneRole::v5suI, DroneRole::v5suI);

   public static DroneRole v5suI(String var0) {
      if (var0 == null) {
         return STRIKE;
      }

      return switch (var0.trim().toLowerCase(Locale.ROOT)) {
         case "recon", "reconnaissance", "isr", "unarmed" -> RECON;
         default -> STRIKE;
      };
   }

   public String v5suI() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
