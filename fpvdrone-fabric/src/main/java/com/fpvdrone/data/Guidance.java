package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import java.util.Locale;

public enum Guidance {
   AUTONOMOUS,
   WAYPOINT,
   PILOTABLE;

   public static final Codec<Guidance> dOfFwvo = Codec.STRING.xmap(Guidance::v5suI, Guidance::v5suI);

   public static Guidance v5suI(String var0) {
      if (var0 == null) {
         return WAYPOINT;
      }

      return switch (var0.trim().toLowerCase(Locale.ROOT)) {
         case "autonomous", "auto", "fireforget", "fire_and_forget" -> AUTONOMOUS;
         case "pilotable", "pilot", "fpv", "manual" -> PILOTABLE;
         default -> WAYPOINT;
      };
   }

   public String v5suI() {
      return this.name().toLowerCase(Locale.ROOT);
   }

   public boolean ibzfxic() {
      return this != AUTONOMOUS;
   }

   public boolean wuhB() {
      return this == PILOTABLE;
   }
}
