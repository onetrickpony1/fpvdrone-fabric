package com.fpvdrone.data;

import com.fpvdrone.data.BoneBox;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GeoBoneBoxes {
   private static final Logger LOGGER = LoggerFactory.getLogger(GeoBoneBoxes.class);
   public static final String v5suI = "collision";
   public static final String ibzfxic = "wheels";
   public static final String wuhB = "_interact";
   private static final Map<String, List<BoneBox>> rsDd50tfV = new ConcurrentHashMap<>();
   private static final Map<String, List<BoneBox>> uSUb = new ConcurrentHashMap<>();
   private static final Map<String, List<BoneBox>> jGhI2 = new ConcurrentHashMap<>();
   private static final Map<String, Boolean> wEJx3Itb = new ConcurrentHashMap<>();
   private static final Map<String, Matrix4f> bAcdvq9 = new ConcurrentHashMap<>();
   private static final Map<String, Matrix4f> c1jHYPb4 = new ConcurrentHashMap<>();

   private GeoBoneBoxes() {
   }

   public static void v5suI() {
      rsDd50tfV.clear();
      uSUb.clear();
      jGhI2.clear();
      wEJx3Itb.clear();
      bAcdvq9.clear();
      c1jHYPb4.clear();
   }

   public static Matrix4f v5suI(String var0, String var1) {
      if (var0 != null && !var0.isEmpty() && var1 != null && !var1.isEmpty()) {
         String var2 = var0 + "#" + var1.toLowerCase(Locale.ROOT);
         return new Matrix4f((Matrix4fc)bAcdvq9.computeIfAbsent(var2, var2x -> wuhB(var0, var1.toLowerCase(Locale.ROOT))));
      } else {
         return new Matrix4f();
      }
   }

   private static Matrix4f wuhB(String var0, String var1) {
      String var2 = "/assets/fpvdrone/" + var0;

      try (InputStream var3 = GeoBoneBoxes.class.getResourceAsStream(var2)) {
         if (var3 == null) {
            return new Matrix4f();
         }

         JsonObject var4 = JsonParser.parseReader(new InputStreamReader(var3, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonArray var5 = var4.getAsJsonArray("minecraft:geometry");
         if (var5 != null && !var5.isEmpty()) {
            JsonArray var19 = var5.get(0).getAsJsonObject().getAsJsonArray("bones");
            if (var19 == null) {
               return new Matrix4f();
            }

            LinkedHashMap<String, JsonObject> var7 = new LinkedHashMap<>();
            HashMap<String, String> var8 = new HashMap<>();

            for (JsonElement var10 : var19) {
               JsonObject var11 = var10.getAsJsonObject();
               String var12 = v5suI(var11, "name", "");
               if (!var12.isEmpty()) {
                  String var13 = var12.toLowerCase(Locale.ROOT);
                  var7.put(var13, var11);
                  String var14 = v5suI(var11, "parent", "");
                  if (!var14.isEmpty()) {
                     var8.put(var13, var14.toLowerCase(Locale.ROOT));
                  }
               }
            }

            return !var7.containsKey(var1) ? new Matrix4f() : v5suI(var1, var7, var8);
         } else {
            return new Matrix4f();
         }
      } catch (Exception var17) {
         return new Matrix4f();
      }
   }

   public static Matrix4f ibzfxic(String var0, String var1) {
      if (var0 != null && !var0.isEmpty() && var1 != null && !var1.isEmpty()) {
         String var2 = var0 + "#" + var1.toLowerCase(Locale.ROOT);
         return new Matrix4f((Matrix4fc)c1jHYPb4.computeIfAbsent(var2, var2x -> dOfFwvo(var0, var1.toLowerCase(Locale.ROOT))));
      } else {
         return new Matrix4f();
      }
   }

   private static Matrix4f dOfFwvo(String var0, String var1) {
      String var2 = "/assets/fpvdrone/" + var0;

      try (InputStream var3 = GeoBoneBoxes.class.getResourceAsStream(var2)) {
         if (var3 == null) {
            return new Matrix4f();
         }

         JsonObject var4 = JsonParser.parseReader(new InputStreamReader(var3, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonArray var5 = var4.getAsJsonArray("minecraft:geometry");
         if (var5 != null && !var5.isEmpty()) {
            JsonArray var19 = var5.get(0).getAsJsonObject().getAsJsonArray("bones");
            if (var19 == null) {
               return new Matrix4f();
            }

            LinkedHashMap<String, JsonObject> var7 = new LinkedHashMap<>();
            HashMap<String, String> var8 = new HashMap<>();

            for (JsonElement var10 : var19) {
               JsonObject var11 = var10.getAsJsonObject();
               String var12 = v5suI(var11, "name", "");
               if (!var12.isEmpty()) {
                  String var13 = var12.toLowerCase(Locale.ROOT);
                  var7.put(var13, var11);
                  String var14 = v5suI(var11, "parent", "");
                  if (!var14.isEmpty()) {
                     var8.put(var13, var14.toLowerCase(Locale.ROOT));
                  }
               }
            }

            JsonObject var21 = (JsonObject)var7.get(var1);
            if (var21 == null) {
               return new Matrix4f();
            }

            Matrix4f var22 = v5suI(var1, var7, var8);
            float[] var24 = v5suI(var21, "pivot");
            return var22.translate(-var24[0] / 16.0F, var24[1] / 16.0F, var24[2] / 16.0F);
         } else {
            return new Matrix4f();
         }
      } catch (Exception var17) {
         return new Matrix4f();
      }
   }

   public static boolean v5suI(String var0) {
      return var0 != null && !var0.isEmpty() ? wEJx3Itb.computeIfAbsent(var0, GeoBoneBoxes::rsDd50tfV) : false;
   }

   private static boolean rsDd50tfV(String var0) {
      String var1 = "/assets/fpvdrone/" + var0;

      try (InputStream var2 = GeoBoneBoxes.class.getResourceAsStream(var1)) {
         if (var2 == null) {
            return false;
         }

         JsonObject var3 = JsonParser.parseReader(new InputStreamReader(var2, StandardCharsets.UTF_8)).getAsJsonObject();
         JsonArray var4 = var3.getAsJsonArray("minecraft:geometry");
         if (var4 != null && !var4.isEmpty()) {
            JsonArray var14 = var4.get(0).getAsJsonObject().getAsJsonArray("bones");
            if (var14 == null) {
               return false;
            }

            for (JsonElement var7 : var14) {
               String var8 = v5suI(var7.getAsJsonObject(), "name", "");
               if (var8.toLowerCase(Locale.ROOT).equals("wheels")) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      } catch (Exception var12) {
         return false;
      }
   }

   public static List<BoneBox> ibzfxic(String var0) {
      return var0 != null && !var0.isEmpty() ? rsDd50tfV.computeIfAbsent(var0, GeoBoneBoxes::uSUb) : List.of();
   }

   public static List<BoneBox> wuhB(String var0) {
      return var0 != null && !var0.isEmpty() ? uSUb.computeIfAbsent(var0, var0x -> v5suI(var0x, GeoBoneBoxes.KKeMvpuBgEY.ibzfxic)) : List.of();
   }

   public static List<BoneBox> dOfFwvo(String var0) {
      return var0 != null && !var0.isEmpty() ? jGhI2.computeIfAbsent(var0, var0x -> v5suI(var0x, GeoBoneBoxes.KKeMvpuBgEY.wuhB)) : List.of();
   }

   private static List<BoneBox> uSUb(String var0) {
      return v5suI(var0, GeoBoneBoxes.KKeMvpuBgEY.v5suI);
   }

   private static List<BoneBox> v5suI(String var0, GeoBoneBoxes.KKeMvpuBgEY var1) {
      String var2 = "/assets/fpvdrone/" + var0;

      try (InputStream var3 = GeoBoneBoxes.class.getResourceAsStream(var2)) {
         if (var3 == null) {
            return List.of();
         }

         JsonObject var4 = JsonParser.parseReader(new InputStreamReader(var3, StandardCharsets.UTF_8)).getAsJsonObject();
         return v5suI(var4, var1);
      } catch (Exception var8) {
         return List.of();
      }
   }

   private static List<BoneBox> v5suI(JsonObject var0, GeoBoneBoxes.KKeMvpuBgEY var1) {
      JsonArray var2 = var0.getAsJsonArray("minecraft:geometry");
      if (var2 != null && !var2.isEmpty()) {
         JsonObject var3 = var2.get(0).getAsJsonObject();
         JsonArray var4 = var3.getAsJsonArray("bones");
         if (var4 == null) {
            return List.of();
         }

         LinkedHashMap<String, JsonObject> var5 = new LinkedHashMap<>();
         HashMap<String, String> var6 = new HashMap<>();

         for (JsonElement var8 : var4) {
            JsonObject var9 = var8.getAsJsonObject();
            String var10 = v5suI(var9, "name", "");
            if (!var10.isEmpty()) {
               String var11 = var10.toLowerCase(Locale.ROOT);
               var5.put(var11, var9);
               String var12 = v5suI(var9, "parent", "");
               if (!var12.isEmpty()) {
                  var6.put(var11, var12.toLowerCase(Locale.ROOT));
               }
            }
         }

         ArrayList<BoneBox> var17 = new ArrayList<>();

         for (Entry<?, ?> var19 : var5.entrySet()) {
            if (v5suI((String)var19.getKey(), var6, "collision")) {
               boolean var20 = ((String)var19.getKey()).endsWith("_interact");
               if (var1 == GeoBoneBoxes.KKeMvpuBgEY.wuhB ? var20 : !var20 && (var1 != GeoBoneBoxes.KKeMvpuBgEY.ibzfxic || !v5suI((String)var19.getKey(), var6, "wheels"))) {
                  String var21 = var20
                     ? ((String)var19.getKey()).substring(0, ((String)var19.getKey()).length() - "_interact".length())
                     : (String)var19.getKey();
                  Matrix4f var22 = v5suI((String)var19.getKey(), var5, var6);
                  JsonArray var13 = ((JsonObject)var19.getValue()).getAsJsonArray("cubes");
                  if (var13 != null) {
                     for (JsonElement var15 : var13) {
                        BoneBox var16 = v5suI(var15.getAsJsonObject(), var22, var21);
                        if (var16 != null) {
                           var17.add(var16);
                        }
                     }
                  }
               }
            }
         }

         return var17.isEmpty() ? List.of() : List.copyOf(var17);
      } else {
         return List.of();
      }
   }

   private static boolean v5suI(String var0, Map<String, String> var1, String var2) {
      String var3 = var0;
      int var4 = 0;

      while (var3 != null && var4++ < 256) {
         if (var3.equals(var2)) {
            return true;
         }

         var3 = (String)var1.get(var3);
      }

      return false;
   }

   private static Matrix4f v5suI(String var0, Map<String, JsonObject> var1, Map<String, String> var2) {
      ArrayList<String> var3 = new ArrayList<>();
      String var4 = var0;
      int var5 = 0;

      while (var4 != null && var5++ < 256) {
         var3.add(0, var4);
         var4 = (String)var2.get(var4);
      }

      Matrix4f var6 = new Matrix4f();

      for (String var8 : var3) {
         JsonObject var9 = (JsonObject)var1.get(var8);
         if (var9 != null) {
            v5suI(var6, v5suI(var9, "pivot"), v5suI(var9, "rotation"));
         }
      }

      return var6;
   }

   private static void v5suI(Matrix4f var0, float[] var1, float[] var2) {
      float var3 = var1[0] / 16.0F;
      float var4 = var1[1] / 16.0F;
      float var5 = var1[2] / 16.0F;
      var0.translate(-var3, var4, var5);
      var0.rotateZ((float)Math.toRadians(var2[2]));
      var0.rotateY((float)Math.toRadians(-var2[1]));
      var0.rotateX((float)Math.toRadians(-var2[0]));
      var0.translate(var3, -var4, -var5);
   }

   private static BoneBox v5suI(JsonObject var0, Matrix4f var1, String var2) {
      float[] var3 = v5suI(var0, "origin");
      float[] var4 = v5suI(var0, "size");
      float[] var5 = v5suI(var0, "pivot");
      float[] var6 = v5suI(var0, "rotation");
      float var7 = -(var3[0] + var4[0] * 0.5F) / 16.0F;
      float var8 = (var3[1] + var4[1] * 0.5F) / 16.0F;
      float var9 = (var3[2] + var4[2] * 0.5F) / 16.0F;
      Vector3f var10 = new Vector3f(Math.abs(var4[0]) / 32.0F, Math.abs(var4[1]) / 32.0F, Math.abs(var4[2]) / 32.0F);
      if (var10.x < 1.0E-5F && var10.y < 1.0E-5F && var10.z < 1.0E-5F) {
         return null;
      }

      Matrix4f var11 = new Matrix4f(var1);
      v5suI(var11, var5, var6);
      Vector3f var12 = var11.transformPosition(new Vector3f(var7, var8, var9));
      Quaternionf var13 = var11.getNormalizedRotation(new Quaternionf());
      double var14 = Math.abs((double)var4[0] * var4[1] * var4[2]);
      double var16 = Math.max(var14, 0.001);
      return new BoneBox(var12, var10, var13, var16, var2);
   }

   private static String v5suI(JsonObject var0, String var1, String var2) {
      return var0.has(var1) && var0.get(var1).isJsonPrimitive() ? var0.get(var1).getAsString() : var2;
   }

   private static float[] v5suI(JsonObject var0, String var1) {
      float[] var2 = new float[3];
      if (var0.has(var1) && var0.get(var1).isJsonArray()) {
         JsonArray var3 = var0.getAsJsonArray(var1);

         for (int var4 = 0; var4 < 3 && var4 < var3.size(); var4++) {
            var2[var4] = var3.get(var4).getAsFloat();
         }
      }

      return var2;
   }

   private enum KKeMvpuBgEY {
      v5suI,
      ibzfxic,
      wuhB;
   }
}
