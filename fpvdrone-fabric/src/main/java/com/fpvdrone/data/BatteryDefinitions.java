package com.fpvdrone.data;

import com.fpvdrone.data.DefinitionFiles;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BatteryDefinitions {
   private static final Logger wuhB = LoggerFactory.getLogger(BatteryDefinitions.class);
   public static final String v5suI = "default";
   static final String ibzfxic = "batteries";
   private static final Map<String, BatteryDefinition> dOfFwvo = new LinkedHashMap<>();

   private BatteryDefinitions() {
   }

   public static Optional<BatteryDefinition> v5suI(String var0) {
      return var0 != null && !var0.isEmpty() ? Optional.ofNullable(dOfFwvo.get(var0)) : Optional.empty();
   }

   public static BatteryDefinition ibzfxic(String var0) {
      return v5suI(var0).orElseGet(() -> dOfFwvo.get("default"));
   }

   public static Map<String, BatteryDefinition> v5suI() {
      return Collections.unmodifiableMap(dOfFwvo);
   }

   public static ListTag ibzfxic() {
      ListTag var0 = new ListTag();

      for (BatteryDefinition var2 : dOfFwvo.values()) {
         BatteryDefinition.v5suI.encodeStart(NbtOps.INSTANCE, var2).result().ifPresent(var2x -> {
            CompoundTag var3 = (CompoundTag)var2x;
            var3.putString("Id", var2.wuhB());
            var0.add(var3);
         });
      }

      return var0;
   }

   public static void v5suI(ListTag var0) {
      dOfFwvo.clear();

      for (int var1 = 0; var1 < var0.size(); var1++) {
         CompoundTag var2 = var0.getCompoundOrEmpty(var1);
         String var3 = var2.getStringOr("Id", "");
         if (!var3.isEmpty()) {
            BatteryDefinition.v5suI.parse(NbtOps.INSTANCE, var2).result().ifPresent(var1x -> dOfFwvo.put(var3, var1x.v5suI(var3)));
         }
      }

      dOfFwvo.putIfAbsent("default", dOfFwvo());
   }

   public static void wuhB() {
      Map<String, JsonObject> var0 = DefinitionFiles.v5suI("batteries");
      dOfFwvo.clear();
      dOfFwvo.put("default", dOfFwvo());

      for (Entry<?, ?> var2 : var0.entrySet()) {
         DefinitionFiles.<BatteryDefinition>v5suI(BatteryDefinition.v5suI, (JsonObject)var2.getValue(), "battery", (String)var2.getKey())
            .ifPresent(var1 -> dOfFwvo.put((String)var2.getKey(), var1.v5suI((String)var2.getKey())));
      }
   }

   private static BatteryDefinition dOfFwvo() {
      return new BatteryDefinition(
         "default",
         "item.fpvdrone.battery",
         6,
         3300,
         75,
         3.7F,
         471.57F,
         "geo/default_battery.geo.json",
         "textures/entity/default_battery.png",
         "fpvdrone:battery"
      );
   }

   static {
      dOfFwvo.put("default", dOfFwvo());
   }
}
