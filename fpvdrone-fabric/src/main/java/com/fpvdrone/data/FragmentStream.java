package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class FragmentStream {
   private final int dOfFwvo;
   private final int rsDd50tfV;
   private final float uSUb;
   public static final FragmentStream v5suI = new FragmentStream(2, 6, 0.0F);
   public static final MapCodec<FragmentStream> ibzfxic = RecordCodecBuilder.mapCodec(
      var0 -> var0.group(
            Codec.intRange(0, 8).optionalFieldOf("CoreRadius", v5suI.wuhB()).forGetter(FragmentStream::wuhB),
            Codec.intRange(0, 16).optionalFieldOf("RingRadius", v5suI.dOfFwvo()).forGetter(FragmentStream::dOfFwvo),
            Codec.floatRange(0.0F, 30.0F).optionalFieldOf("TerrainLeadSeconds", v5suI.rsDd50tfV()).forGetter(FragmentStream::rsDd50tfV)
         )
         .apply(var0, FragmentStream::new)
   );
   public static final Codec<FragmentStream> wuhB = ibzfxic.codec();

   public FragmentStream(int var1, int var2, float var3) {
      this.dOfFwvo = var1;
      this.rsDd50tfV = var2;
      this.uSUb = var3;
   }

   public Record_IukVkAm v5suI() {
      return new Record_IukVkAm(this.dOfFwvo, this.rsDd50tfV, this.uSUb);
   }

   public boolean ibzfxic() {
      return this.uSUb > 0.0F;
   }

   public int wuhB() {
      return this.dOfFwvo;
   }

   public int dOfFwvo() {
      return this.rsDd50tfV;
   }

   public float rsDd50tfV() {
      return this.uSUb;
   }
}
