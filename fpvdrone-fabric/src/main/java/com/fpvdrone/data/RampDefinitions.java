package com.fpvdrone.data;

import com.fpvdrone.data.DefinitionFiles;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RampDefinitions {
   private static final Logger wuhB = LoggerFactory.getLogger(RampDefinitions.class);
   public static final String v5suI = "default";
   static final String ibzfxic = "ramps";
   private static final Map<String, RampDefinition> dOfFwvo = new LinkedHashMap<>();

   private RampDefinitions() {
   }

   public static Optional<RampDefinition> v5suI(String var0) {
      return var0 != null && !var0.isEmpty() ? Optional.ofNullable(dOfFwvo.get(var0)) : Optional.empty();
   }

   public static RampDefinition ibzfxic(String var0) {
      return v5suI(var0).orElseGet(() -> dOfFwvo.get("default"));
   }

   public static Map<String, RampDefinition> v5suI() {
      return Collections.unmodifiableMap(dOfFwvo);
   }

   public static void ibzfxic() {
      Map<String, JsonObject> var0 = DefinitionFiles.v5suI("ramps");
      dOfFwvo.clear();
      dOfFwvo.put("default", wuhB());

      for (Entry<?, ?> var2 : var0.entrySet()) {
         DefinitionFiles.<RampDefinition>v5suI(RampDefinition.wEJx3Itb, (JsonObject)var2.getValue(), "ramp", (String)var2.getKey())
            .ifPresent(var1 -> dOfFwvo.put((String)var2.getKey(), var1.wuhB((String)var2.getKey())));
      }
   }

   private static RampDefinition wuhB() {
      return new RampDefinition(
         "default",
         "item.fpvdrone.ramp",
         "geo/ramp_drun.geo.json",
         "textures/entity/ramp_drones.png",
         RampDefinition.dOfFwvo,
         Map.of(),
         1.176325,
         1.658017,
         0.0,
         List.of("fp1"),
         List.of(),
         "fpvdrone:ramp"
      );
   }

   static {
      dOfFwvo.put("default", wuhB());
   }
}
