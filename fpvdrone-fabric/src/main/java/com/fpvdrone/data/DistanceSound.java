package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DistanceSound {
   private final float dOfFwvo;
   private final float rsDd50tfV;
   private final List<DistanceSound.KKeMvpuBgEY> uSUb;
   public static final DistanceSound v5suI = v5suI("drone_engine", 80.0F, 1.2F);
   public static final DistanceSound ibzfxic = v5suI("bomblet_close", "bomblet_distant", 48.0F, 128.0F, 32.0F, 1.6F);
   private static final float jGhI2 = 16.0F;
   private static final Codec<DistanceSound.Rdl0X5jeod> CODEC = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.fieldOf("SoundId").forGetter(DistanceSound.Rdl0X5jeod::soundId),
            Codec.FLOAT.fieldOf("MaxRange").forGetter(DistanceSound.Rdl0X5jeod::maxRange)
         )
         .apply(var0, DistanceSound.Rdl0X5jeod::new)
   );
   private static final Codec<DistanceSound.Ol3TEDSog> bAcdvq9 = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.optionalFieldOf("SoundId").forGetter(DistanceSound.Ol3TEDSog::soundId),
            Codec.floatRange(1.0F, 1024.0F).fieldOf("MaxRange").forGetter(DistanceSound.Ol3TEDSog::maxRange),
            Codec.floatRange(0.0F, 8.0F).optionalFieldOf("Volume", 1.0F).forGetter(DistanceSound.Ol3TEDSog::volume),
            Codec.floatRange(1.0F, 1024.0F).optionalFieldOf("CrossFadeWidth", 16.0F).forGetter(DistanceSound.Ol3TEDSog::crossFadeWidth),
            CODEC.listOf().optionalFieldOf("Layers", List.of()).forGetter(DistanceSound.Ol3TEDSog::layers)
         )
         .apply(var0, DistanceSound.Ol3TEDSog::new)
   );
   public static final Codec<DistanceSound> wuhB = bAcdvq9.comapFlatMap((DistanceSound.Ol3TEDSog var0) -> v5suI(var0), (DistanceSound var0) -> v5suI(var0));

   public DistanceSound(float var1, float var2, List<DistanceSound.KKeMvpuBgEY> var3) {
      if (var3 != null && !var3.isEmpty()) {
         this.dOfFwvo = var1;
         this.rsDd50tfV = var2;
         this.uSUb = var3;
      } else {
         throw new IllegalArgumentException("SoundProfile needs at least one layer");
      }
   }

   public static DistanceSound v5suI(String var0, float var1, float var2) {
      return new DistanceSound(var1, var2, List.of(new DistanceSound.KKeMvpuBgEY(var0, 0.0F, 0.0F, var1, var1)));
   }

   public static DistanceSound v5suI(String var0, String var1, float var2, float var3, float var4, float var5) {
      return v5suI(List.of(new DistanceSound.Rdl0X5jeod(var0, var2), new DistanceSound.Rdl0X5jeod(var1, var3)), var3, var4, var5);
   }

   public boolean v5suI() {
      return this.uSUb.size() > 1;
   }

   private static DataResult<DistanceSound> v5suI(DistanceSound.Ol3TEDSog var0) {
      if (!var0.layers().isEmpty()) {
         return DataResult.success(v5suI(var0.layers(), var0.maxRange(), var0.crossFadeWidth(), var0.volume()));
      } else {
         return var0.soundId().isEmpty()
            ? DataResult.error(() -> "SoundProfile needs either SoundId or a non-empty Layers list")
            : DataResult.success(v5suI(var0.soundId().get(), var0.maxRange(), var0.volume()));
      }
   }

   private static DistanceSound v5suI(List<DistanceSound.Rdl0X5jeod> var0, float var1, float var2, float var3) {
      float var4 = Math.min(var2, var1) * 0.5F;
      ArrayList var5 = new ArrayList();
      float var6 = 0.0F;

      for (int var7 = 0; var7 < var0.size(); var7++) {
         DistanceSound.Rdl0X5jeod var8 = (DistanceSound.Rdl0X5jeod)var0.get(var7);
         boolean var9 = var7 == 0;
         boolean var10 = var7 == var0.size() - 1;
         float var11 = var10 ? var1 : Math.max(var6, Math.min(var8.maxRange(), var1));
         float var12 = var9 ? 0.0F : var6 - var4;
         float var13 = var9 ? 0.0F : var6 + var4;
         float var14 = var10 ? var1 : var11 - var4;
         float var15 = var10 ? var1 : var11 + var4;
         var5.add(new DistanceSound.KKeMvpuBgEY(var8.soundId(), var12, var13, var14, var15));
         var6 = var11;
      }

      return new DistanceSound(var1, var3, var5);
   }

   private static DistanceSound.Ol3TEDSog v5suI(DistanceSound var0) {
      List var1 = var0.dOfFwvo();
      if (var1.size() == 1) {
         return new DistanceSound.Ol3TEDSog(Optional.of(((DistanceSound.KKeMvpuBgEY)var1.get(0)).v5suI()), var0.ibzfxic(), var0.wuhB(), 16.0F, List.of());
      }

      float var2 = ((DistanceSound.KKeMvpuBgEY)var1.get(0)).rsDd50tfV() - ((DistanceSound.KKeMvpuBgEY)var1.get(0)).dOfFwvo();
      ArrayList var3 = new ArrayList();

      for (int var4 = 0; var4 < var1.size(); var4++) {
         DistanceSound.KKeMvpuBgEY var5 = (DistanceSound.KKeMvpuBgEY)var1.get(var4);
         float var6 = var4 == var1.size() - 1 ? var0.ibzfxic() : (var5.dOfFwvo() + var5.rsDd50tfV()) / 2.0F;
         var3.add(new DistanceSound.Rdl0X5jeod(var5.v5suI(), var6));
      }

      return new DistanceSound.Ol3TEDSog(Optional.empty(), var0.ibzfxic(), var0.wuhB(), var2, var3);
   }

   public float ibzfxic() {
      return this.dOfFwvo;
   }

   public float wuhB() {
      return this.rsDd50tfV;
   }

   public List<DistanceSound.KKeMvpuBgEY> dOfFwvo() {
      return this.uSUb;
   }

   public static final class KKeMvpuBgEY {
      private final String v5suI;
      private final float ibzfxic;
      private final float wuhB;
      private final float dOfFwvo;
      private final float rsDd50tfV;

      public KKeMvpuBgEY(String var1, float var2, float var3, float var4, float var5) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var5;
      }

      public float v5suI(float var1) {
         if (var1 < this.ibzfxic || var1 > this.rsDd50tfV) {
            return 0.0F;
         } else if (var1 < this.wuhB && this.wuhB > this.ibzfxic) {
            float var3 = (var1 - this.ibzfxic) / (this.wuhB - this.ibzfxic);
            return (float)Math.sin(var3 * (Math.PI / 2));
         } else if (var1 > this.dOfFwvo && this.rsDd50tfV > this.dOfFwvo) {
            float var2 = (var1 - this.dOfFwvo) / (this.rsDd50tfV - this.dOfFwvo);
            return (float)Math.cos(var2 * (Math.PI / 2));
         } else {
            return 1.0F;
         }
      }

      public String v5suI() {
         return this.v5suI;
      }

      public float ibzfxic() {
         return this.ibzfxic;
      }

      public float wuhB() {
         return this.wuhB;
      }

      public float dOfFwvo() {
         return this.dOfFwvo;
      }

      public float rsDd50tfV() {
         return this.rsDd50tfV;
      }

   }

   private static final class Ol3TEDSog {
      private final Optional<String> soundId;
      private final float maxRange;
      private final float volume;
      private final float crossFadeWidth;
      private final List<DistanceSound.Rdl0X5jeod> layers;

      private Ol3TEDSog(Optional<String> var1, float var2, float var3, float var4, List<DistanceSound.Rdl0X5jeod> var5) {
         this.soundId = var1;
         this.maxRange = var2;
         this.volume = var3;
         this.crossFadeWidth = var4;
         this.layers = var5;
      }

      public Optional<String> soundId() {
         return this.soundId;
      }

      public float maxRange() {
         return this.maxRange;
      }

      public float volume() {
         return this.volume;
      }

      public float crossFadeWidth() {
         return this.crossFadeWidth;
      }

      public List<DistanceSound.Rdl0X5jeod> layers() {
         return this.layers;
      }

   }

   private static final class Rdl0X5jeod {
      private final String soundId;
      private final float maxRange;

      private Rdl0X5jeod(String var1, float var2) {
         this.soundId = var1;
         this.maxRange = var2;
      }

      public String soundId() {
         return this.soundId;
      }

      public float maxRange() {
         return this.maxRange;
      }

   }
}
