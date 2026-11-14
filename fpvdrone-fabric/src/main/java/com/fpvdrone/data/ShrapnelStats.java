package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class ShrapnelStats {
   private final double lethalRange;
   private final float baseDamage;
   private final float scaleDamage;
   private final float damageCap;
   private final float blastRadius;
   private final float blastDamage;
   private final double killZone;
   private final float blockPower;
   private final boolean opensDoors;
   public static final Codec<ShrapnelStats> CODEC = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.doubleRange(0.0, 8.0).optionalFieldOf("LethalRange", 1.5).forGetter(ShrapnelStats::lethalRange),
            Codec.floatRange(0.0F, 100.0F).optionalFieldOf("BaseDamage", 10.0F).forGetter(ShrapnelStats::baseDamage),
            Codec.floatRange(0.0F, 50.0F).optionalFieldOf("ScaleDamage", 3.0F).forGetter(ShrapnelStats::scaleDamage),
            Codec.floatRange(1.0F, 200.0F).optionalFieldOf("DamageCap", 60.0F).forGetter(ShrapnelStats::damageCap),
            Codec.floatRange(1.0F, 32.0F).optionalFieldOf("BlastRadius", 10.0F).forGetter(ShrapnelStats::blastRadius),
            Codec.floatRange(0.0F, 200.0F).optionalFieldOf("BlastDamage", 40.0F).forGetter(ShrapnelStats::blastDamage),
            Codec.doubleRange(0.0, 16.0).optionalFieldOf("KillZone", 2.0).forGetter(ShrapnelStats::killZone),
            Codec.floatRange(0.0F, 2.0F).optionalFieldOf("BlockPower", 0.0F).forGetter(ShrapnelStats::blockPower),
            Codec.BOOL.optionalFieldOf("OpensDoors", true).forGetter(ShrapnelStats::opensDoors)
         )
         .apply(var0, ShrapnelStats::new)
   );

   public ShrapnelStats(double var1, float var3, float var4, float var5, float var6, float var7, double var8, float var10, boolean var11) {
      this.lethalRange = var1;
      this.baseDamage = var3;
      this.scaleDamage = var4;
      this.damageCap = var5;
      this.blastRadius = var6;
      this.blastDamage = var7;
      this.killZone = var8;
      this.blockPower = var10;
      this.opensDoors = var11;
   }

   public double lethalRange() {
      return this.lethalRange;
   }

   public float baseDamage() {
      return this.baseDamage;
   }

   public float scaleDamage() {
      return this.scaleDamage;
   }

   public float damageCap() {
      return this.damageCap;
   }

   public float blastRadius() {
      return this.blastRadius;
   }

   public float blastDamage() {
      return this.blastDamage;
   }

   public double killZone() {
      return this.killZone;
   }

   public float blockPower() {
      return this.blockPower;
   }

   public boolean opensDoors() {
      return this.opensDoors;
   }

}
