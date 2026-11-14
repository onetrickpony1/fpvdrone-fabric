package com.fpvdrone.data;

import com.mojang.serialization.Codec;

public enum TooltipSource {
   v5suI("batteries"),
   ibzfxic("projectiles"),
   wuhB("bombbay"),
   dOfFwvo("rocketpod"),
   rsDd50tfV("explicit");

   private final String jGhI2;
   public static final Codec<TooltipSource> uSUb = Codec.STRING.xmap(TooltipSource::v5suI, TooltipSource::v5suI);

   TooltipSource(String var3) {
      this.jGhI2 = var3;
   }

   public String v5suI() {
      return this.jGhI2;
   }

   public static TooltipSource v5suI(String var0) {
      if (var0 != null) {
         for (TooltipSource var4 : values()) {
            if (var4.jGhI2.equalsIgnoreCase(var0.trim())) {
               return var4;
            }
         }
      }

      return rsDd50tfV;
   }
}
