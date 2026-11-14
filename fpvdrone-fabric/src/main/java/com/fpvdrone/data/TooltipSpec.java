package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public final class TooltipSpec {
   private final List<TooltipSpec.Section> sections;
   private final int maxRows;
   public static final TooltipSpec v5suI = new TooltipSpec(List.of(), 8);
   public static final Codec<TooltipSpec> CODEC = RecordCodecBuilder.create(
      var0 -> var0.group(
            TooltipSpec.Section.v5suI.listOf().optionalFieldOf("Sections", List.of()).forGetter(TooltipSpec::sections),
            Codec.intRange(1, 32).optionalFieldOf("MaxRows", v5suI.maxRows()).forGetter(TooltipSpec::maxRows)
         )
         .apply(var0, TooltipSpec::new)
   );

   public TooltipSpec(List<TooltipSpec.Section> var1, int var2) {
      this.sections = var1;
      this.maxRows = var2;
   }

   public boolean v5suI() {
      return this.sections.isEmpty();
   }

   public static final class Section {
      private final TooltipSource source;
      private final List<String> items;
      private final String labelKey;
      public static final Codec<TooltipSpec.Section> v5suI = RecordCodecBuilder.create(
         var0 -> var0.group(
               TooltipSource.uSUb.optionalFieldOf("Source", TooltipSource.rsDd50tfV).forGetter(TooltipSpec.Section::source),
               Codec.STRING.listOf().optionalFieldOf("Items", List.of()).forGetter(TooltipSpec.Section::items),
               Codec.STRING.optionalFieldOf("LabelKey", "").forGetter(TooltipSpec.Section::labelKey)
            )
            .apply(var0, TooltipSpec.Section::new)
      );

      public Section(TooltipSource var1, List<String> var2, String var3) {
         this.source = var1;
         this.items = var2;
         this.labelKey = var3;
      }

      public TooltipSource source() {
         return this.source;
      }

      public List<String> items() {
         return this.items;
      }

      public String labelKey() {
         return this.labelKey;
      }

   }

   public List<TooltipSpec.Section> sections() {
      return this.sections;
   }

   public int maxRows() {
      return this.maxRows;
   }

}
