package com.fpvdrone.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DefinitionFiles {
   private static final Logger v5suI = LoggerFactory.getLogger(DefinitionFiles.class);

   private DefinitionFiles() {
   }

   static Map<String, JsonObject> v5suI(String var0) {
      LinkedHashMap<String, JsonObject> var1 = new LinkedHashMap<>();
      Path var2 = wuhB(var0);
      if (var2 != null && Files.isDirectory(var2)) {
         try (Stream<Path> var3 = Files.list(var2)) {
            var3.filter(var0x -> var0x.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json"))
               .sorted(Comparator.comparing((Path var0x) -> var0x.getFileName().toString()))
               .forEach(var2x -> {
                  String var3x = var2x.getFileName().toString();
                  String var4 = var3x.substring(0, var3x.length() - ".json".length()).toLowerCase(Locale.ROOT);

                  try {
                     String var5 = ibzfxic(Files.readString(var2x));
                     JsonObject var6 = JsonParser.parseString(var5).getAsJsonObject();
                     var1.put(var4, var6);
                  } catch (Exception var7) {
                  }
               });
         } catch (Exception var8) {
         }

         return var1;
      } else {
         return var1;
      }
   }

   private static Path wuhB(String var0) {
      try {
         // Forge: ModList...findResource("data", "fpvdrone", dir). Same place: the mod jar only, not datapacks.
         return FabricLoader.getInstance().getModContainer("fpvdrone").flatMap(var1 -> var1.findPath("data/fpvdrone/" + var0)).orElse(null);
      } catch (Exception var2) {
         return null;
      }
   }

   static <T> Optional<T> v5suI(Codec<T> var0, JsonObject var1, String var2, String var3) {
      return var0.parse(JsonOps.INSTANCE, var1).resultOrPartial(var2x -> {});
   }

   static float v5suI(JsonObject var0, String var1, float var2) {
      return var0.has(var1) ? var0.get(var1).getAsFloat() : var2;
   }

   static int v5suI(JsonObject var0, String var1, int var2) {
      return var0.has(var1) ? var0.get(var1).getAsInt() : var2;
   }

   static boolean v5suI(JsonObject var0, String var1, boolean var2) {
      return var0.has(var1) ? var0.get(var1).getAsBoolean() : var2;
   }

   static String v5suI(JsonObject var0, String var1, String var2) {
      return var0.has(var1) ? var0.get(var1).getAsString() : var2;
   }

   static float[] v5suI(JsonObject var0, String var1, float[] var2) {
      if (var0.has(var1) && var0.get(var1).isJsonArray()) {
         JsonArray var3 = var0.getAsJsonArray(var1);
         float[] var4 = (float[])var2.clone();

         for (int var5 = 0; var5 < var4.length && var5 < var3.size(); var5++) {
            var4[var5] = var3.get(var5).getAsFloat();
         }

         return var4;
      } else {
         return (float[])var2.clone();
      }
   }

   static float[] v5suI(JsonObject var0, String var1) {
      if (var0.has(var1) && var0.get(var1).isJsonArray()) {
         JsonArray var2 = var0.getAsJsonArray(var1);
         float[] var3 = new float[3];

         for (int var4 = 0; var4 < 3 && var4 < var2.size(); var4++) {
            var3[var4] = var2.get(var4).getAsFloat();
         }

         return var3;
      } else {
         return null;
      }
   }

   static int ibzfxic(JsonObject var0, String var1, int var2) {
      if (!var0.has(var1)) {
         return var2;
      }

      JsonElement var3 = var0.get(var1);

      try {
         if (var3.isJsonArray()) {
            JsonArray var13 = var3.getAsJsonArray();
            if (var13.size() < 3) {
               return var2;
            }

            float var5 = var13.get(0).getAsFloat();
            float var6 = var13.get(1).getAsFloat();
            float var7 = var13.get(2).getAsFloat();
            boolean var8 = var5 <= 1.0F && var6 <= 1.0F && var7 <= 1.0F;
            int var9 = v5suI(Math.round(var8 ? var5 * 255.0F : var5), 0, 255);
            int var10 = v5suI(Math.round(var8 ? var6 * 255.0F : var6), 0, 255);
            int var11 = v5suI(Math.round(var8 ? var7 * 255.0F : var7), 0, 255);
            return var9 << 16 | var10 << 8 | var11;
         } else {
            String var4 = var3.getAsString().trim();
            if (var4.startsWith("#")) {
               var4 = var4.substring(1);
            } else if (var4.startsWith("0x") || var4.startsWith("0X")) {
               var4 = var4.substring(2);
            }

            return Integer.parseInt(var4, 16) & 16777215;
         }
      } catch (Exception var12) {
         return var2;
      }
   }

   static List<String> ibzfxic(JsonObject var0, String var1) {
      ArrayList<String> var2 = new ArrayList<>();
      if (var0.has(var1) && var0.get(var1).isJsonArray()) {
         for (JsonElement var4 : var0.getAsJsonArray(var1)) {
            var2.add(var4.getAsString());
         }
      }

      return var2;
   }

   static List<Integer> wuhB(JsonObject var0, String var1) {
      ArrayList<Integer> var2 = new ArrayList<>();
      if (var0.has(var1) && var0.get(var1).isJsonArray()) {
         for (JsonElement var4 : var0.getAsJsonArray(var1)) {
            var2.add(var4.getAsInt());
         }
      }

      return var2;
   }

   static float v5suI(float var0, float var1, float var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   static int v5suI(int var0, int var1, int var2) {
      return Math.max(var1, Math.min(var2, var0));
   }

   static String ibzfxic(String var0) {
      StringBuilder var1 = new StringBuilder();
      boolean var2 = false;
      boolean var3 = false;

      for (int var4 = 0; var4 < var0.length(); var4++) {
         char var5 = var0.charAt(var4);
         if (var3) {
            var1.append(var5);
            var3 = false;
         } else if (var5 == '\\' && var2) {
            var1.append(var5);
            var3 = true;
         } else if (var5 == '"') {
            var2 = !var2;
            var1.append(var5);
         } else if (!var2 && var5 == '/' && var4 + 1 < var0.length() && var0.charAt(var4 + 1) == '/') {
            while (var4 < var0.length() && var0.charAt(var4) != '\n') {
               var4++;
            }

            var1.append('\n');
         } else {
            var1.append(var5);
         }
      }

      return var1.toString();
   }
}
