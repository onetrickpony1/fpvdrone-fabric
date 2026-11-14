package com.fpvdrone.data;

import com.fpvdrone.data.Detonator;
import com.fpvdrone.data.DefinitionFiles;
import com.fpvdrone.data.ShrapnelDetonator;
import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProjectileDefinitions {
   private static final Logger jGhI2 = LoggerFactory.getLogger(ProjectileDefinitions.class);
   public static final String RPG7 = "rpg7";
   public static final String RGD5 = "rgd5";
   public static final String GRENADE_40MM = "40mm_explosive";
   public static final String ROCKET_70MM = "70mm_rocket";
   public static final String TM62 = "tm62";
   static final String uSUb = "projectiles";
   private static final Map<String, Detonator> wEJx3Itb = new LinkedHashMap<>();
   private static final Map<String, ProjectileDefinition> bAcdvq9 = new LinkedHashMap<>();
   private static final Map<String, List<ParticleSpec>> c1jHYPb4 = new LinkedHashMap<>();
   private static final Map<String, List<ParticleSpec>> v8ZN = new LinkedHashMap<>();

   private ProjectileDefinitions() {
   }

   public static void registerDetonator(String var0, Detonator var1) {
      wEJx3Itb.put(var0, var1);
   }

   public static Detonator detonator(String var0) {
      Detonator var1 = wEJx3Itb.get(var0);
      return var1 != null ? var1 : wEJx3Itb.get("shrapnel");
   }

   public static Optional<ProjectileDefinition> find(String var0) {
      return var0 != null && !var0.isEmpty() ? Optional.ofNullable(bAcdvq9.get(var0)) : Optional.empty();
   }

   public static ProjectileDefinition getOrDefault(String var0) {
      return find(var0).orElseGet(() -> bAcdvq9.get("rpg7"));
   }

   public static Map<String, ProjectileDefinition> all() {
      return Collections.unmodifiableMap(bAcdvq9);
   }

   public static ListTag toNbt() {
      ListTag var0 = new ListTag();

      for (ProjectileDefinition var2 : bAcdvq9.values()) {
         ProjectileDefinition.ibzfxic.encodeStart(NbtOps.INSTANCE, var2).result().ifPresent(var2x -> {
            CompoundTag var3 = (CompoundTag)var2x;
            var3.putString("Id", var2.id());
            var0.add(var3);
         });
      }

      return var0;
   }

   public static void fromNbt(ListTag var0) {
      bAcdvq9.clear();

      for (int var1 = 0; var1 < var0.size(); var1++) {
         CompoundTag var2 = var0.getCompoundOrEmpty(var1);
         String var3 = var2.getStringOr("Id", "");
         if (!var3.isEmpty()) {
            ProjectileDefinition.ibzfxic.parse(NbtOps.INSTANCE, var2).result().ifPresent(var1x -> bAcdvq9.put(var3, var1x.withId(var3)));
         }
      }

      bAcdvq9.putIfAbsent("rpg7", builtInRpg7());
   }

   public static ShrapnelStats defaultStats() {
      return new ShrapnelStats(1.5, 10.0F, 3.0F, 60.0F, 10.0F, 40.0F, 2.0, 0.0F, true);
   }

   public static void load() {
      Map<String, JsonObject> var0 = DefinitionFiles.v5suI("projectiles");
      bAcdvq9.clear();
      c1jHYPb4.clear();
      v8ZN.clear();
      bAcdvq9.put("rpg7", builtInRpg7());

      for (Entry<?, ?> var2 : var0.entrySet()) {
         DefinitionFiles.<ProjectileDefinition>v5suI(ProjectileDefinition.ibzfxic, (JsonObject)var2.getValue(), "projectile", (String)var2.getKey()).ifPresent(var1 -> {
            bAcdvq9.put((String)var2.getKey(), var1.withId((String)var2.getKey()));
            readParticles((String)var2.getKey(), (JsonObject)var2.getValue());
         });
      }
   }

   public static List<ParticleSpec> impactParticles(String var0) {
      return var0 == null ? List.of() : c1jHYPb4.getOrDefault(var0, List.of());
   }

   public static List<ParticleSpec> motorParticles(String var0) {
      return var0 == null ? List.of() : v8ZN.getOrDefault(var0, List.of());
   }

   public static void registerInline(String var0, JsonObject var1) {
      if (var0 != null && !var0.isEmpty() && var1 != null) {
         DefinitionFiles.<ProjectileDefinition>v5suI(ProjectileDefinition.ibzfxic, var1, "inline projectile", var0).ifPresent(var2 -> {
            bAcdvq9.put(var0, var2.withId(var0));
            readParticles(var0, var1);
         });
      }
   }

   private static void readParticles(String var0, JsonObject var1) {
      List var2 = ParticleSpec.listFrom(var1, "particles");
      if (var2.isEmpty()) {
         String var3 = DefinitionFiles.v5suI(var1, "particlesFrom", "");
         if (!var3.isEmpty()) {
            var2 = c1jHYPb4.getOrDefault(var3, List.of());
            if (var2.isEmpty()) {
            }
         }
      }

      if (!var2.isEmpty()) {
         c1jHYPb4.put(var0, var2);
      } else {
         c1jHYPb4.remove(var0);
      }

      List var4 = readMotorParticles(var1);
      if (!var4.isEmpty()) {
         v8ZN.put(var0, var4);
      } else {
         v8ZN.remove(var0);
      }
   }

   private static List<ParticleSpec> readMotorParticles(JsonObject var0) {
      if (var0.has("Motor") && var0.get("Motor").isJsonObject()) {
         JsonObject var1 = var0.getAsJsonObject("Motor");
         if (var1.has("particles")) {
            return ParticleSpec.listFrom(var1, "particles");
         }

         if (var1.has("particlesFrom")) {
            String var2 = DefinitionFiles.v5suI(var1, "particlesFrom", "");
            List var3 = c1jHYPb4.getOrDefault(var2, List.of());
            if (var3.isEmpty()) {
            }

            return var3;
         } else {
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static ProjectileDefinition builtInRpg7() {
      return new ProjectileDefinition(
         "rpg7",
         "item.fpvdrone.rpg7",
         1000.0F,
         4.0F,
         128,
         2.0F,
         250.0F,
         200.0F,
         "shrapnel",
         new ShrapnelStats(1.5, 10.0F, 3.0F, 60.0F, 10.0F, 40.0F, 2.0, 0.0F, true),
         "geo/rpg7.geo.json",
         "textures/entity/rpg7.png",
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         null,
         "fpvdrone:rpg7"
      );
   }

   static {
      registerDetonator("shrapnel", new ShrapnelDetonator());
      bAcdvq9.put("rpg7", builtInRpg7());
   }
}
