package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum EnergySource {
   FUEL,
   BATTERY;

   public static final Codec<EnergySource> wuhB = Codec.STRING.xmap(EnergySource::v5suI, EnergySource::v5suI);

   public static EnergySource v5suI(String var0) {
      if (var0 == null) {
         return FUEL;
      }

      return switch (var0.trim().toLowerCase(Locale.ROOT)) {
         case "battery", "electric" -> BATTERY;
         default -> FUEL;
      };
   }

   public String v5suI() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
