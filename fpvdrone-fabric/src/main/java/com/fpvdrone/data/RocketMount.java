package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

public final class RocketMount {
   private final String ibzfxic;
   @Nullable
   private final String wuhB;
   private final List<String> dOfFwvo;
   public static final Codec<RocketMount> v5suI = RecordCodecBuilder.create(
      var0 -> var0.group(
            Codec.STRING.fieldOf("Id").forGetter(RocketMount::v5suI),
            Codec.STRING.optionalFieldOf("Bone").forGetter(var0x -> Optional.ofNullable(var0x.ibzfxic())),
            Codec.STRING.listOf().fieldOf("TubeIds").forGetter(RocketMount::wuhB)
         )
         .apply(var0, (var0x, var1, var2) -> new RocketMount(var0x, (String)var1.orElse(null), var2))
   );

   public RocketMount(String var1, @Nullable String var2, List<String> var3) {
      var3 = List.copyOf(var3);
      this.ibzfxic = var1;
      this.wuhB = var2;
      this.dOfFwvo = var3;
   }

   public String v5suI() {
      return this.ibzfxic;
   }

   @Nullable
   public String ibzfxic() {
      return this.wuhB;
   }

   public List<String> wuhB() {
      return this.dOfFwvo;
   }
}
