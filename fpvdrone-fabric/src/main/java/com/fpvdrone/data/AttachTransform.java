package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public final class AttachTransform {
   private final String ibzfxic;
   private final float wuhB;
   private final float dOfFwvo;
   private final float rsDd50tfV;
   private final float uSUb;
   private final float jGhI2;
   private final float wEJx3Itb;
   private final float bAcdvq9;
   private static final float[] c1jHYPb4 = new float[]{0.0F, 0.0F, 0.0F};
   private static final Codec<float[]> v8ZN = Codec.FLOAT
      .listOf()
      .xmap(
         var0 -> new float[]{
            var0.size() > 0 ? (Float)var0.get(0) : 0.0F, var0.size() > 1 ? (Float)var0.get(1) : 0.0F, var0.size() > 2 ? (Float)var0.get(2) : 0.0F
         },
         var0 -> List.of(var0[0], var0[1], var0[2])
      );
   public static final Codec<AttachTransform> v5suI = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.fieldOf("Bone").forGetter(AttachTransform::v5suI),
            v8ZN.optionalFieldOf("Offset", c1jHYPb4).forGetter(var0x -> new float[]{var0x.ibzfxic(), var0x.wuhB(), var0x.dOfFwvo()}),
            v8ZN.optionalFieldOf("Rotation", c1jHYPb4).forGetter(var0x -> new float[]{var0x.rsDd50tfV(), var0x.uSUb(), var0x.jGhI2()}),
            Codec.floatRange(0.01F, 100.0F).optionalFieldOf("Scale", 1.0F).forGetter(AttachTransform::wEJx3Itb)
         )
         .apply(var0, (var0x, var1, var2, var3) -> new AttachTransform(var0x, var1[0], var1[1], var1[2], var2[0], var2[1], var2[2], var3))
   );

   public AttachTransform(String var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      this.ibzfxic = var1;
      this.wuhB = var2;
      this.dOfFwvo = var3;
      this.rsDd50tfV = var4;
      this.uSUb = var5;
      this.jGhI2 = var6;
      this.wEJx3Itb = var7;
      this.bAcdvq9 = var8;
   }

   public static AttachTransform v5suI(String var0) {
      return new AttachTransform(var0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public String v5suI() {
      return this.ibzfxic;
   }

   public float ibzfxic() {
      return this.wuhB;
   }

   public float wuhB() {
      return this.dOfFwvo;
   }

   public float dOfFwvo() {
      return this.rsDd50tfV;
   }

   public float rsDd50tfV() {
      return this.uSUb;
   }

   public float uSUb() {
      return this.jGhI2;
   }

   public float jGhI2() {
      return this.wEJx3Itb;
   }

   public float wEJx3Itb() {
      return this.bAcdvq9;
   }
}
