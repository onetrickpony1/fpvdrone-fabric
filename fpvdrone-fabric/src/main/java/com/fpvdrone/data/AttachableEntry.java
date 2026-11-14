package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public final class AttachableEntry {
   private final String ibzfxic;
   private final String wuhB;
   private final AttachTransform dOfFwvo;
   public static final Codec<AttachableEntry> v5suI = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.fieldOf("Id").forGetter(AttachableEntry::v5suI),
            Codec.STRING.fieldOf("Name").forGetter(AttachableEntry::ibzfxic),
            AttachTransform.v5suI.optionalFieldOf("Attach").forGetter(var0x -> Optional.ofNullable(var0x.wuhB()))
         )
         .apply(var0, (var0x, var1, var2) -> new AttachableEntry(var0x, var1, (AttachTransform)var2.orElse(null)))
   );

   public AttachableEntry(String var1, String var2) {
      this(var1, var2, null);
   }

   public AttachableEntry(String var1, String var2, AttachTransform var3) {
      this.ibzfxic = var1;
      this.wuhB = var2;
      this.dOfFwvo = var3;
   }

   public String v5suI() {
      return this.ibzfxic;
   }

   public String ibzfxic() {
      return this.wuhB;
   }

   public AttachTransform wuhB() {
      return this.dOfFwvo;
   }
}
