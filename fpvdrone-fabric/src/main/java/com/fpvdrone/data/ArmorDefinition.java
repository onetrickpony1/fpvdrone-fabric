package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

public final class ArmorDefinition {
   private final String dOfFwvo;
   private final String rsDd50tfV;
   private final String uSUb;
   private final String jGhI2;
   private final String wEJx3Itb;
   private final float bAcdvq9;
   private final String c1jHYPb4;
   private final boolean v8ZN;
   private final String zTT3nw6;
   private final List<AttachableEntry> vmpmU;
   private final String b69s;
   public static final String v5suI = "payload_mount";
   public static final boolean ibzfxic = true;
   public static final Codec<ArmorDefinition> wuhB = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.fieldOf("DisplayName").forGetter(ArmorDefinition::dOfFwvo),
            Codec.STRING.optionalFieldOf("Slot", "chest").forGetter(ArmorDefinition::rsDd50tfV),
            Codec.STRING.fieldOf("Model").forGetter(ArmorDefinition::uSUb),
            Codec.STRING.fieldOf("Texture").forGetter(ArmorDefinition::jGhI2),
            Codec.floatRange(0.0F, 100000.0F).optionalFieldOf("BaseWeightGrams", 1000.0F).forGetter(ArmorDefinition::wEJx3Itb),
            Codec.STRING.optionalFieldOf("MountBone", "payload_mount").forGetter(ArmorDefinition::bAcdvq9),
            Codec.BOOL.optionalFieldOf("CookOffOnLethal", true).forGetter(ArmorDefinition::c1jHYPb4),
            Codec.STRING.optionalFieldOf("Preloaded", "").forGetter(ArmorDefinition::v8ZN),
            AttachableEntry.v5suI.listOf().optionalFieldOf("Projectiles", List.of()).forGetter(ArmorDefinition::zTT3nw6),
            Codec.STRING.optionalFieldOf("Item", "").forGetter(ArmorDefinition::vmpmU)
         )
         .apply(
            var0,
            (var0x, var1, var2, var3, var4, var5, var6, var7, var8, var9) -> new ArmorDefinition("", var0x, var1, var2, var3, var4, var5, var6, var7, var8, var9)
         )
   );

   public ArmorDefinition(
      String var1,
      String var2,
      String var3,
      String var4,
      String var5,
      float var6,
      String var7,
      boolean var8,
      String var9,
      List<AttachableEntry> var10,
      String var11
   ) {
      this.dOfFwvo = var1;
      this.rsDd50tfV = var2;
      this.uSUb = var3;
      this.jGhI2 = var4;
      this.wEJx3Itb = var5;
      this.bAcdvq9 = var6;
      this.c1jHYPb4 = var7;
      this.v8ZN = var8;
      this.zTT3nw6 = var9;
      this.vmpmU = var10;
      this.b69s = var11;
   }

   public ArmorDefinition v5suI(String var1) {
      return new ArmorDefinition(
         var1, this.rsDd50tfV, this.uSUb, this.jGhI2, this.wEJx3Itb, this.bAcdvq9, this.c1jHYPb4, this.v8ZN, this.zTT3nw6, this.vmpmU, this.b69s
      );
   }

   public String v5suI() {
      return this.b69s != null && !this.b69s.isEmpty() ? this.b69s : "fpvdrone:" + this.dOfFwvo;
   }

   public boolean ibzfxic(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         for (AttachableEntry var3 : this.vmpmU) {
            if (var3.v5suI().equals(var1)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public Optional<AttachTransform> wuhB(String var1) {
      if (var1 != null && !var1.isEmpty()) {
         for (AttachableEntry var3 : this.vmpmU) {
            if (var3.v5suI().equals(var1)) {
               return Optional.ofNullable(var3.wuhB());
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public Optional<AttachableEntry> ibzfxic() {
      if (this.vmpmU.isEmpty()) {
         return Optional.empty();
      }

      if (this.zTT3nw6 != null && !this.zTT3nw6.isEmpty()) {
         for (AttachableEntry var2 : this.vmpmU) {
            if (var2.v5suI().equals(this.zTT3nw6)) {
               return Optional.of(var2);
            }
         }
      }

      return Optional.of(this.vmpmU.get(0));
   }

   public String wuhB() {
      return this.dOfFwvo;
   }

   public String dOfFwvo() {
      return this.rsDd50tfV;
   }

   public String rsDd50tfV() {
      return this.uSUb;
   }

   public String uSUb() {
      return this.jGhI2;
   }

   public String jGhI2() {
      return this.wEJx3Itb;
   }

   public float wEJx3Itb() {
      return this.bAcdvq9;
   }

   public String bAcdvq9() {
      return this.c1jHYPb4;
   }

   public boolean c1jHYPb4() {
      return this.v8ZN;
   }

   public String v8ZN() {
      return this.zTT3nw6;
   }

   public List<AttachableEntry> zTT3nw6() {
      return this.vmpmU;
   }

   public String vmpmU() {
      return this.b69s;
   }
}
