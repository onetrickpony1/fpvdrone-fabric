package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum EnergySource {
   v5suI,
   ibzfxic;

   public static final Codec<EnergySource> wuhB = Codec.STRING.xmap(EnergySource::v5suI, EnergySource::v5suI);

   public static EnergySource v5suI(String var0) {
      if (var0 == null) {
         return v5suI;
      }

      return switch (var0.trim().toLowerCase(Locale.ROOT)) {
         case "battery", "electric" -> ibzfxic;
         default -> v5suI;
      };
   }

   public String v5suI() {
      return this.name().toLowerCase(Locale.ROOT);
   }
}
