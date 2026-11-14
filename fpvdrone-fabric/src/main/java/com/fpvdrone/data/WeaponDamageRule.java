package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class WeaponDamageRule {
   private final String ibzfxic;
   private final WeaponDamageRule.Match wuhB;
   private final String dOfFwvo;
   private final float rsDd50tfV;
   private final float uSUb;
   public static final Codec<WeaponDamageRule> CODEC = RecordCodecBuilder.create(
      var0 -> var0.group(
            WeaponDamageRule.Match.ibzfxic.optionalFieldOf("Match", WeaponDamageRule.Match.v5suI).forGetter(WeaponDamageRule::ibzfxic),
            Codec.STRING.optionalFieldOf("Formula", "flat").forGetter(WeaponDamageRule::wuhB),
            Codec.FLOAT.optionalFieldOf("Damage", 0.0F).forGetter(WeaponDamageRule::dOfFwvo),
            Codec.FLOAT.optionalFieldOf("Coefficient", 0.0F).forGetter(WeaponDamageRule::rsDd50tfV)
         )
         .apply(var0, (var0x, var1, var2, var3) -> new WeaponDamageRule("", var0x, var1, var2, var3))
   );

   public WeaponDamageRule(String var1, WeaponDamageRule.Match var2, String var3, float var4, float var5) {
      this.ibzfxic = var1;
      this.wuhB = var2;
      this.dOfFwvo = var3;
      this.rsDd50tfV = var4;
      this.uSUb = var5;
   }

   public boolean v5suI(IvHitInfo.KKeMvpuBgEY var1) {
      return this.wuhB.v5suI(var1);
   }

   public float ibzfxic(IvHitInfo.KKeMvpuBgEY var1) {
      return "velocity".equalsIgnoreCase(this.dOfFwvo) ? (float)(var1.wuhB() * this.uSUb) : this.rsDd50tfV;
   }

   public WeaponDamageRule v5suI(String var1) {
      return new WeaponDamageRule(var1, this.wuhB, this.dOfFwvo, this.rsDd50tfV, this.uSUb);
   }

   public String v5suI() {
      return this.ibzfxic;
   }

   public WeaponDamageRule.Match ibzfxic() {
      return this.wuhB;
   }

   public String wuhB() {
      return this.dOfFwvo;
   }

   public float dOfFwvo() {
      return this.rsDd50tfV;
   }

   public float rsDd50tfV() {
      return this.uSUb;
   }

   public static final class Match {
      private final float diameter;
      private final boolean match;
      public static final WeaponDamageRule.Match v5suI = new WeaponDamageRule.Match(0.0F, false);
      public static final Codec<WeaponDamageRule.Match> ibzfxic = RecordCodecBuilder.create(
         var0 -> var0.group(
               Codec.floatRange(0.0F, 10000.0F).optionalFieldOf("Diameter", v5suI.diameter()).forGetter(WeaponDamageRule.Match::diameter),
               Codec.BOOL.optionalFieldOf("HasProximityFuze", v5suI.match()).forGetter(WeaponDamageRule.Match::match)
            )
            .apply(var0, WeaponDamageRule.Match::new)
      );

      public Match(float var1, boolean var2) {
         this.diameter = var1;
         this.match = var2;
      }

      boolean v5suI(IvHitInfo.KKeMvpuBgEY var1) {
         return this.diameter > 0.0F && Math.abs(var1.v5suI() - this.diameter) > 0.01F ? false : !this.match || var1.ibzfxic();
      }

      public float diameter() {
         return this.diameter;
      }

      public boolean match() {
         return this.match;
      }

   }
}
