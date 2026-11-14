package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public final class PayloadStation {
   private final String ibzfxic;
   private final AttachTransform wuhB;
   private final String dOfFwvo;
   private final String rsDd50tfV;
   public static final Codec<PayloadStation> v5suI = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.fieldOf("Id").forGetter(PayloadStation::ibzfxic),
            AttachTransform.v5suI.fieldOf("Attach").forGetter(PayloadStation::wuhB),
            Codec.STRING.optionalFieldOf("Projectile").forGetter(var0x -> Optional.ofNullable(var0x.dOfFwvo())),
            Codec.STRING.optionalFieldOf("FirePos").forGetter(var0x -> Optional.ofNullable(var0x.rsDd50tfV()))
         )
         .apply(var0, (var0x, var1, var2, var3) -> new PayloadStation(var0x, var1, (String)var2.orElse(null), (String)var3.orElse(null)))
   );

   public PayloadStation(String var1, AttachTransform var2, String var3, String var4) {
      this.ibzfxic = var1;
      this.wuhB = var2;
      this.dOfFwvo = var3;
      this.rsDd50tfV = var4;
   }

   public String v5suI(String var1) {
      return this.dOfFwvo != null ? this.dOfFwvo : var1;
   }

   public String v5suI() {
      return this.rsDd50tfV != null && !this.rsDd50tfV.isEmpty() ? this.rsDd50tfV : this.wuhB.v5suI();
   }

   public String ibzfxic() {
      return this.ibzfxic;
   }

   public AttachTransform wuhB() {
      return this.wuhB;
   }

   public String dOfFwvo() {
      return this.dOfFwvo;
   }

   public String rsDd50tfV() {
      return this.rsDd50tfV;
   }

}
