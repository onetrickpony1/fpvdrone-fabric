package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class InteractZone {
   private final String wuhB;
   private final String dOfFwvo;
   private final InteractZone.Trigger rsDd50tfV;
   private final String uSUb;
   private final String jGhI2;
   private final Map<String, String> wEJx3Itb;
   public static final String v5suI = "static";
   public static final Codec<InteractZone> ibzfxic = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.fieldOf("Id").forGetter(InteractZone::v5suI),
            Codec.STRING.optionalFieldOf("Bone").forGetter(var0x -> Optional.ofNullable(var0x.ibzfxic().equals(var0x.v5suI()) ? null : var0x.ibzfxic())),
            InteractZone.Trigger.wuhB.optionalFieldOf("Trigger", InteractZone.Trigger.HOVER).forGetter(InteractZone::wuhB),
            Codec.STRING.optionalFieldOf("Content", "static").forGetter(InteractZone::dOfFwvo),
            Codec.STRING.optionalFieldOf("Text").forGetter(var0x -> Optional.ofNullable(var0x.rsDd50tfV())),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("RequiredItems", Map.of()).forGetter(InteractZone::uSUb)
         )
         .apply(
            var0, (var0x, var1, var2, var3, var4, var5) -> new InteractZone(var0x, (String)var1.orElse(null), var2, var3, (String)var4.orElse(null), var5)
         )
   );

   public InteractZone(String var1, String var2, InteractZone.Trigger var3, String var4, String var5, Map<String, String> var6) {
      if (var2 == null || var2.isEmpty()) {
         var2 = var1;
      }

      if (var4 == null || var4.isEmpty()) {
         var4 = "static";
      }

      if (var6 == null) {
         var6 = Map.of();
      }

      this.wuhB = var1;
      this.dOfFwvo = var2;
      this.rsDd50tfV = var3;
      this.uSUb = var4;
      this.jGhI2 = var5;
      this.wEJx3Itb = var6;
   }

   public InteractZone(String var1, String var2, InteractZone.Trigger var3, String var4, String var5) {
      this(var1, var2, var3, var4, var5, Map.of());
   }

   public String v5suI() {
      return this.wuhB;
   }

   public String ibzfxic() {
      return this.dOfFwvo;
   }

   public InteractZone.Trigger wuhB() {
      return this.rsDd50tfV;
   }

   public String dOfFwvo() {
      return this.uSUb;
   }

   public String rsDd50tfV() {
      return this.jGhI2;
   }

   public Map<String, String> uSUb() {
      return this.wEJx3Itb;
   }

   public enum Trigger {
      HOVER,
      CLICK;

      public static final Codec<InteractZone.Trigger> wuhB = Codec.STRING.xmap(InteractZone.Trigger::v5suI, InteractZone.Trigger::v5suI);

      public static InteractZone.Trigger v5suI(String var0) {
         if (var0 == null) {
            return HOVER;
         }

         return switch (var0.trim().toLowerCase(Locale.ROOT)) {
            case "click" -> CLICK;
            default -> HOVER;
         };
      }

      public String v5suI() {
         return this.name().toLowerCase(Locale.ROOT);
      }
   }
}
