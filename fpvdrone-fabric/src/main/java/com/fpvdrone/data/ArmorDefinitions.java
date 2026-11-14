package com.fpvdrone.data;

import com.fpvdrone.data.ArmorDefinition;
import com.fpvdrone.data.AttachTransform;
import com.fpvdrone.data.AttachableEntry;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ArmorDefinitions {
   private static final Logger wuhB = LoggerFactory.getLogger(ArmorDefinitions.class);
   public static final String v5suI = "opener";
   static final String ibzfxic = "armor";
   private static final Map<String, ArmorDefinition> dOfFwvo = new LinkedHashMap<>();

   private ArmorDefinitions() {
   }

   public static Optional<ArmorDefinition> v5suI(String var0) {
      return var0 != null && !var0.isEmpty() ? Optional.ofNullable(dOfFwvo.get(var0)) : Optional.empty();
   }

   public static ArmorDefinition ibzfxic(String var0) {
      return v5suI(var0).orElseGet(() -> dOfFwvo.get("opener"));
   }

   public static Map<String, ArmorDefinition> v5suI() {
      return Collections.unmodifiableMap(dOfFwvo);
   }

   public static void ibzfxic() {
      Map<String, JsonObject> var0 = DefinitionFiles.v5suI("armor");
      dOfFwvo.clear();
      dOfFwvo.put("opener", wuhB());

      for (Entry<?, ?> var2 : var0.entrySet()) {
         DefinitionFiles.<ArmorDefinition>v5suI(ArmorDefinition.wuhB, (JsonObject)var2.getValue(), "armor", (String)var2.getKey())
            .ifPresent(var1 -> dOfFwvo.put((String)var2.getKey(), var1.v5suI((String)var2.getKey())));
      }
   }

   private static ArmorDefinition wuhB() {
      List var0 = List.of(new AttachableEntry("tm62", "item.fpvdrone.tm62", AttachTransform.v5suI("payload_mount")));
      return new ArmorDefinition(
         "opener",
         "item.fpvdrone.opener",
         "chest",
         "geo/opener.geo.json",
         "textures/entity/opener.png",
         1200.0F,
         "payload_mount",
         true,
         "tm62",
         var0,
         "fpvdrone:opener"
      );
   }

   static {
      dOfFwvo.put("opener", wuhB());
   }
}
