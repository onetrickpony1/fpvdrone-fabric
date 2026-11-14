package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class BatteryDefinition {
   private final String ibzfxic;
   private final String wuhB;
   private final int dOfFwvo;
   private final int rsDd50tfV;
   private final int uSUb;
   private final float jGhI2;
   private final float wEJx3Itb;
   private final String bAcdvq9;
   private final String c1jHYPb4;
   private final String v8ZN;
   public static final Codec<BatteryDefinition> v5suI = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.optionalFieldOf("DisplayName", "item.fpvdrone.battery").forGetter(BatteryDefinition::dOfFwvo),
            Codec.intRange(1, 8).optionalFieldOf("Cells", 6).forGetter(BatteryDefinition::rsDd50tfV),
            Codec.intRange(300, 8000).optionalFieldOf("CapacityMah", 3300).forGetter(BatteryDefinition::uSUb),
            Codec.intRange(45, 150).optionalFieldOf("CRating", 75).forGetter(BatteryDefinition::jGhI2),
            Codec.floatRange(1.0F, 5.0F).optionalFieldOf("VoltagePerCell", 3.7F).forGetter(BatteryDefinition::wEJx3Itb),
            Codec.floatRange(0.0F, 100000.0F).optionalFieldOf("WeightGrams", 471.57F).forGetter(BatteryDefinition::bAcdvq9),
            Codec.STRING.fieldOf("Model").forGetter(BatteryDefinition::c1jHYPb4),
            Codec.STRING.fieldOf("Texture").forGetter(BatteryDefinition::v8ZN),
            Codec.STRING.optionalFieldOf("Item", "").forGetter(BatteryDefinition::zTT3nw6)
         )
         .apply(
            var0, (var0x, var1, var2, var3, var4, var5, var6, var7, var8) -> new BatteryDefinition("", var0x, var1, var2, var3, var4, var5, var6, var7, var8)
         )
   );

   public BatteryDefinition(String var1, String var2, int var3, int var4, int var5, float var6, float var7, String var8, String var9, String var10) {
      this.ibzfxic = var1;
      this.wuhB = var2;
      this.dOfFwvo = var3;
      this.rsDd50tfV = var4;
      this.uSUb = var5;
      this.jGhI2 = var6;
      this.wEJx3Itb = var7;
      this.bAcdvq9 = var8;
      this.c1jHYPb4 = var9;
      this.v8ZN = var10;
   }

   public float v5suI() {
      return this.dOfFwvo * this.jGhI2;
   }

   public BatteryDefinition v5suI(String var1) {
      return new BatteryDefinition(var1, this.wuhB, this.dOfFwvo, this.rsDd50tfV, this.uSUb, this.jGhI2, this.wEJx3Itb, this.bAcdvq9, this.c1jHYPb4, this.v8ZN);
   }

   public String ibzfxic() {
      return this.v8ZN != null && !this.v8ZN.isEmpty() ? this.v8ZN : "fpvdrone:" + this.ibzfxic;
   }

   public String wuhB() {
      return this.ibzfxic;
   }

   public String dOfFwvo() {
      return this.wuhB;
   }

   public int rsDd50tfV() {
      return this.dOfFwvo;
   }

   public int uSUb() {
      return this.rsDd50tfV;
   }

   public int jGhI2() {
      return this.uSUb;
   }

   public float wEJx3Itb() {
      return this.jGhI2;
   }

   public float bAcdvq9() {
      return this.wEJx3Itb;
   }

   public String c1jHYPb4() {
      return this.bAcdvq9;
   }

   public String v8ZN() {
      return this.c1jHYPb4;
   }

   public String zTT3nw6() {
      return this.v8ZN;
   }
}
