package com.fpvdrone.data;

import com.fpvdrone.data.DefinitionFiles;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DroneDefinitions {
   private static final Logger c1jHYPb4 = LoggerFactory.getLogger(DroneDefinitions.class);
   public static final String v5suI = "default";
   public static final String ibzfxic = "thermal";
   public static final String wuhB = "mavic";
   public static final String dOfFwvo = "fp1";
   public static final String rsDd50tfV = "lytui_drun";
   public static final String uSUb = "lancet51";
   public static final String jGhI2 = "orlan10";
   public static final String wEJx3Itb = "test_target";
   static final String bAcdvq9 = "drones";
   private static final int v8ZN = 4;
   private static final DroneDefinition.DamageZones zTT3nw6 = new DroneDefinition.DamageZones(
      new DroneDefinition.DamageZones.KKeMvpuBgEY(50, 0.9F), new DroneDefinition.DamageZones.KKeMvpuBgEY(250, 0.0F), ""
   );
   private static final DroneDefinition.DamageZones vmpmU = new DroneDefinition.DamageZones(
      new DroneDefinition.DamageZones.KKeMvpuBgEY(200, 0.9F), new DroneDefinition.DamageZones.KKeMvpuBgEY(1000, 0.0F), ""
   );
   private static final Map<String, DroneDefinition> b69s = new LinkedHashMap<>();

   private DroneDefinitions() {
   }

   public static Optional<DroneDefinition> v5suI(String var0) {
      return var0 != null && !var0.isEmpty() ? Optional.ofNullable(b69s.get(var0)) : Optional.empty();
   }

   public static DroneDefinition ibzfxic(String var0) {
      return v5suI(var0).orElseGet(() -> b69s.get("default"));
   }

   public static Map<String, DroneDefinition> v5suI() {
      return Collections.unmodifiableMap(b69s);
   }

   public static ListTag ibzfxic() {
      ListTag var0 = new ListTag();

      for (DroneDefinition var2 : b69s.values()) {
         CompoundTag var3 = new CompoundTag();
         var3.putString("Id", var2.wyNDWDB());
         var3.putString("Name", var2.iRWc2Pi());
         var3.putString("Kind", var2.tpzIHm().v5suI());
         var3.putString("Model", var2.iElAbEZe());
         var3.putString("Texture", var2.sTrE());
         var3.putFloat("ModelYaw", var2.ifpFOCaAs());
         var3.putFloat("BaseWeightGrams", var2.eJDY());
         var3.putBoolean("FlightMode3d", var2.uQdlyiwB());
         var3.putString("DroneName", var2.ivDR());
         var3.putInt("ItemLifetimeTicks", var2.fvsAOg());
         var3.putInt("HitsToDowned", var2.lFt1ckP());
         var3.putString("Item", var2.xogV5KBhg() != null ? var2.xogV5KBhg() : "");
         if (var2.rYlh() != null) {
            v5suI(var3, "Tooltip", TooltipSpec.CODEC, var2.rYlh());
         }

         v5suI(var3, "Physics", DroneDefinition.Physics.ibzfxic, var2.zHBAQgZP());
         v5suI(var3, "Rates", DroneDefinition.Rates.ibzfxic, var2.we1GYY());
         v5suI(var3, "Render", DroneDefinition.Render.ibzfxic, var2.pxmzA());
         v5suI(var3, "Camera", DroneDefinition.Camera.v5suI, var2.bAcdvq9());
         v5suI(var3, "Sounds", DistanceSound.wuhB, var2.iAKNPAj());
         v5suI(var3, "DamageBox", DroneDefinition.DamageBox.ibzfxic, var2.iS1JnZov());
         v5suI(var3, "Hitbox", DroneDefinition.Hitbox.ibzfxic, var2.lfyfLwuw());
         if (var2.qU1QioDiC() != null) {
            v5suI(var3, "DamageZones", DroneDefinition.DamageZones.v5suI, var2.qU1QioDiC());
         }

         v5suI(var3, "Signal", DroneDefinition.Signal.ibzfxic, var2.vmpmU());
         v5suI(var3, "Sync", DroneDefinition.Sync.rsDd50tfV, var2.b69s());
         CompoundTag var4 = new CompoundTag();
         var4.putString("Model", var2.hOKMYHOaC());
         var4.putBoolean("Enabled", var2.pyHY5() == null || var2.pyHY5().ibzfxic());
         var3.put("Collision", var4);
         var3.putString("Guidance", var2.rOIp0p7().v5suI());
         var3.putString("Role", var2.iKXOcc().v5suI());
         ListTag var5 = new ListTag();

         for (AttachableEntry var7 : var2.n9lX8A()) {
            var5.add(v5suI(AttachableEntry.v5suI, var7));
         }

         var3.put("Batteries", var5);
         ListTag var9 = new ListTag();

         for (AttachableEntry var8 : var2.wBXn()) {
            var9.add(v5suI(AttachableEntry.v5suI, var8));
         }

         var3.put("Projectiles", var9);
         if (var2.mM7DsB9AR() != null) {
            v5suI(var3, "Mount", DroneDefinition.Mount.v5suI, var2.mM7DsB9AR());
         }

         if (var2.uKpZYj() != null) {
            v5suI(var3, "BombBay", DroneDefinition.BombBay.v5suI, var2.uKpZYj());
         }

         if (var2.jt9oI60ST() != null) {
            v5suI(var3, "RocketPod", DroneDefinition.RocketPod.v5suI, var2.jt9oI60ST());
         }

         v5suI(var3, "InteractZones", InteractZone.ibzfxic.listOf(), var2.c1jHYPb4());
         var0.add(var3);
      }

      return var0;
   }

   public static void v5suI(ListTag var0) {
      HashMap<String, DroneDefinition.LoiterFlight> var1 = new HashMap<>();
      HashMap<String, DroneDefinition.Engine> var2 = new HashMap<>();
      HashMap<String, List<ParticleSpec>> var3 = new HashMap<>();
      HashMap<String, List<DroneDefinition.DamageSource>> var4 = new HashMap<>();
      HashMap<String, Float> var5 = new HashMap<>();

      for (Entry<?, ?> var7 : b69s.entrySet()) {
         var1.put((String)var7.getKey(), ((DroneDefinition)var7.getValue()).lvk8rWUGD());
         var2.put((String)var7.getKey(), ((DroneDefinition)var7.getValue()).i263D9());
         var4.put((String)var7.getKey(), ((DroneDefinition)var7.getValue()).zgQgCA9());
         var5.put((String)var7.getKey(), ((DroneDefinition)var7.getValue()).qV33e());
         if (((DroneDefinition)var7.getValue()).jt9oI60ST() != null) {
            var3.put((String)var7.getKey(), ((DroneDefinition)var7.getValue()).jt9oI60ST().jGhI2());
         }
      }

      b69s.clear();

      for (int var32 = 0; var32 < var0.size(); var32++) {
         CompoundTag var33 = var0.getCompoundOrEmpty(var32);
         String var8 = var33.getStringOr("Id", "");
         if (!var8.isEmpty()) {
            DroneDefinition.Physics var9 = ibzfxic(var33, "Physics", DroneDefinition.Physics.ibzfxic, DroneDefinition.Physics.v5suI);
            DroneDefinition.Rates var10 = ibzfxic(var33, "Rates", DroneDefinition.Rates.ibzfxic, DroneDefinition.Rates.v5suI);
            DroneDefinition.Render var11 = ibzfxic(var33, "Render", DroneDefinition.Render.ibzfxic, DroneDefinition.Render.v5suI);
            DroneDefinition.Camera var12 = ibzfxic(var33, "Camera", DroneDefinition.Camera.v5suI, null);
            DistanceSound var13 = ibzfxic(var33, "Sounds", DistanceSound.wuhB, null);
            DroneDefinition.DamageBox var14 = ibzfxic(var33, "DamageBox", DroneDefinition.DamageBox.ibzfxic, null);
            DroneDefinition.Hitbox var15 = ibzfxic(var33, "Hitbox", DroneDefinition.Hitbox.ibzfxic, null);
            DroneDefinition.DamageZones var16 = ibzfxic(var33, "DamageZones", DroneDefinition.DamageZones.v5suI, null);
            DroneDefinition.Signal var17 = ibzfxic(var33, "Signal", DroneDefinition.Signal.ibzfxic, null);
            DroneDefinition.Sync var18 = ibzfxic(var33, "Sync", DroneDefinition.Sync.rsDd50tfV, null);
            List var19 = v5suI(var33, "InteractZones", InteractZone.ibzfxic);
            ArrayList var20 = new ArrayList();
            ListTag var21 = var33.getListOrEmpty("Batteries");

            for (int var22 = 0; var22 < var21.size(); var22++) {
               AttachableEntry.v5suI.parse(NbtOps.INSTANCE, var21.getCompoundOrEmpty(var22)).result().ifPresent(var20::add);
            }

            ArrayList var34 = new ArrayList();
            ListTag var23 = var33.getListOrEmpty("Projectiles");

            for (int var24 = 0; var24 < var23.size(); var24++) {
               AttachableEntry.v5suI.parse(NbtOps.INSTANCE, var23.getCompoundOrEmpty(var24)).result().ifPresent(var34::add);
            }

            DroneDefinition.Mount var35 = ibzfxic(var33, "Mount", DroneDefinition.Mount.v5suI, null);
            DroneDefinition.BombBay var25 = ibzfxic(var33, "BombBay", DroneDefinition.BombBay.v5suI, null);
            DroneDefinition.RocketPod var26 = ibzfxic(var33, "RocketPod", DroneDefinition.RocketPod.v5suI, null);
            if (var26 != null) {
               var26 = var26.v5suI(var3.getOrDefault(var8, List.of()));
            }

            DroneDefinition.Collision var27 = null;
            if (var33.contains("Collision")) {
               CompoundTag var28 = var33.getCompoundOrEmpty("Collision");
               var27 = new DroneDefinition.Collision(
                  var28.contains("Model") ? var28.getStringOr("Model", "") : "", !var28.contains("Enabled") || var28.getBooleanOr("Enabled", false)
               );
            }

            Guidance var36 = Guidance.v5suI(var33.getStringOr("Guidance", ""));
            DroneRole var29 = DroneRole.v5suI(var33.getStringOr("Role", ""));
            String var30 = var33.contains("Item") ? var33.getStringOr("Item", "") : "";
            TooltipSpec var31 = ibzfxic(var33, "Tooltip", TooltipSpec.CODEC, null);
            b69s.put(
               var8,
               new DroneDefinition(
                  var8,
                  var33.getStringOr("Name", ""),
                  DroneKind.v5suI(var33.getStringOr("Kind", "")),
                  var33.getFloatOr("BaseWeightGrams", 0.0F),
                  var9,
                  var10,
                  var11,
                  var33.contains("Model") ? var33.getStringOr("Model", "") : "geo/fpv_7d.geo.json",
                  var33.contains("Texture") ? var33.getStringOr("Texture", "") : "textures/entity/fpv_7d.png",
                  var33.getFloatOr("ModelYaw", 0.0F),
                  var33.getBooleanOr("FlightMode3d", false),
                  var33.getStringOr("DroneName", ""),
                  var20,
                  var34,
                  var35,
                  var25,
                  var12,
                  var14,
                  var15,
                  var33.contains("ItemLifetimeTicks") ? var33.getIntOr("ItemLifetimeTicks", 0) : 6000,
                  var33.contains("HitsToDowned") ? var33.getIntOr("HitsToDowned", 0) : 0,
                  var16,
                  var13,
                  (DroneDefinition.LoiterFlight)var1.get(var8),
                  var27,
                  var17,
                  (DroneDefinition.Engine)var2.get(var8),
                  var36,
                  var29,
                  var26,
                  var18,
                  var19,
                  var4.getOrDefault(var8, List.of()),
                  var5.getOrDefault(var8, -1.0F),
                  var30,
                  var31
               )
            );
         }
      }

      b69s.putIfAbsent("default", jGhI2());
      b69s.putIfAbsent("thermal", wEJx3Itb());
      b69s.putIfAbsent("mavic", bAcdvq9());
      b69s.putIfAbsent("fp1", c1jHYPb4());
      b69s.putIfAbsent("lytui_drun", zTT3nw6());
   }

   private static <T> void v5suI(CompoundTag var0, String var1, Codec<T> var2, T var3) {
      var2.encodeStart(NbtOps.INSTANCE, var3).result().ifPresent(var2x -> var0.put(var1, var2x));
   }

   private static <T> Tag v5suI(Codec<T> var0, T var1) {
      return (Tag)var0.encodeStart(NbtOps.INSTANCE, var1).result().orElseGet(CompoundTag::new);
   }

   private static <T> T ibzfxic(CompoundTag var0, String var1, Codec<T> var2, T var3) {
      return (T)(!var0.contains(var1) ? var3 : var2.parse(NbtOps.INSTANCE, var0.getCompoundOrEmpty(var1)).result().orElse(var3));
   }

   private static <T> List<T> v5suI(CompoundTag var0, String var1, Codec<T> var2) {
      ArrayList var3 = new ArrayList();
      ListTag var4 = var0.getListOrEmpty(var1);

      for (int var5 = 0; var5 < var4.size(); var5++) {
         var2.parse(NbtOps.INSTANCE, var4.getCompoundOrEmpty(var5)).result().ifPresent(var3::add);
      }

      return var3;
   }

   public static void wuhB() {
      Map<String, JsonObject> var0 = DefinitionFiles.v5suI("drones");
      b69s.clear();
      b69s.put("default", jGhI2());
      b69s.put("thermal", wEJx3Itb());
      b69s.put("mavic", bAcdvq9());
      b69s.put("fp1", c1jHYPb4());
      b69s.put("lytui_drun", zTT3nw6());

      for (Entry<?, ?> var2 : var0.entrySet()) {
         DefinitionFiles.<DroneDefinition>v5suI(DroneDefinition.jGhI2, (JsonObject)var2.getValue(), "drone", (String)var2.getKey()).ifPresent(var1 -> {
            var1 = var1.v5suI((String)var2.getKey());
            var1 = v5suI(var1, (JsonObject)var2.getValue());
            var1 = ibzfxic(var1, (JsonObject)var2.getValue());
            var1 = v5suI((String)var2.getKey(), (JsonObject)var2.getValue(), var1);
            b69s.put((String)var2.getKey(), var1);
         });
      }
   }

   private static DroneDefinition v5suI(DroneDefinition var0, JsonObject var1) {
      if (var0.i263D9() != null && var1.has("Engine") && var1.get("Engine").isJsonObject()) {
         JsonObject var2 = var1.getAsJsonObject("Engine");
         List var3;
         if (var2.has("particles")) {
            var3 = ParticleSpec.v5suI(var2, "particles");
         } else if (var2.has("particlesFrom")) {
            String var4 = DefinitionFiles.v5suI(var2, "particlesFrom", "");
            var3 = ProjectileDefinitions.dOfFwvo(var4);
            if (var3.isEmpty()) {
            }
         } else {
            var3 = DroneDefinition.Engine.v5suI.wEJx3Itb();
         }

         return var0.v5suI(var0.i263D9().v5suI(var3));
      } else {
         return var0;
      }
   }

   private static DroneDefinition ibzfxic(DroneDefinition var0, JsonObject var1) {
      if (var0.jt9oI60ST() != null && var1.has("RocketPod") && var1.get("RocketPod").isJsonObject()) {
         JsonObject var2 = var1.getAsJsonObject("RocketPod");
         List var3;
         if (var2.has("MuzzleFlash")) {
            var3 = ParticleSpec.v5suI(var2, "MuzzleFlash");
         } else if (var2.has("MuzzleFlashFrom")) {
            String var4 = DefinitionFiles.v5suI(var2, "MuzzleFlashFrom", "");
            var3 = ProjectileDefinitions.dOfFwvo(var4);
            if (var3.isEmpty()) {
            }
         } else {
            var3 = List.of();
         }

         return var0.v5suI(var0.jt9oI60ST().v5suI(var3));
      } else {
         return var0;
      }
   }

   private static DroneDefinition v5suI(String var0, JsonObject var1, DroneDefinition var2) {
      if (var2.lvk8rWUGD() == null) {
         return var2;
      }

      if (var1.has("LoiterFlight") && var1.get("LoiterFlight").isJsonObject()) {
         JsonObject var3 = var1.getAsJsonObject("LoiterFlight");
         if (var3.has("Warhead") && var3.get("Warhead").isJsonObject()) {
            ProjectileDefinitions.v5suI(var0, var3.getAsJsonObject("Warhead"));
            ProjectileDefinition var4 = ProjectileDefinitions.wuhB(var0);
            return var2.v5suI(var2.lvk8rWUGD().v5suI(var0, var4.hOKMYHOaC(), var4.iS1JnZov()));
         } else {
            return var2;
         }
      } else {
         return var2;
      }
   }

   private static AttachableEntry dOfFwvo() {
      return new AttachableEntry("default", "item.fpvdrone.battery", new AttachTransform("battery_mount", 0.0F, 5.75F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F));
   }

   private static AttachableEntry rsDd50tfV() {
      return new AttachableEntry("dji", "item.fpvdrone.battery_dji", AttachTransform.v5suI("battery_mount"));
   }

   private static AttachableEntry uSUb() {
      return new AttachableEntry("rpg7", "item.fpvdrone.rpg7", new AttachTransform("payload_mount", 0.0F, 1.5F, -0.21761F, 0.0F, 0.0F, 22.5F, 1.0F));
   }

   private static DroneDefinition jGhI2() {
      return new DroneDefinition(
         "default",
         "entity.fpvdrone.drone",
         DroneKind.v5suI,
         580.0F,
         DroneDefinition.Physics.v5suI,
         DroneDefinition.Rates.v5suI,
         DroneDefinition.Render.v5suI,
         "geo/fpv_7d.geo.json",
         "textures/entity/fpv_7d.png",
         0.0F,
         false,
         "KINDER",
         List.of(dOfFwvo()),
         List.of(uSUb()),
         null,
         null,
         null,
         null,
         null,
         6000,
         4,
         zTT3nw6,
         null,
         null,
         null,
         null,
         null,
         Guidance.ibzfxic,
         DroneRole.v5suI,
         null,
         DroneDefinition.Sync.ibzfxic,
         List.of(),
         DroneDefinition.ibzfxic,
         -1.0F,
         "fpvdrone:drone",
         null
      );
   }

   private static DroneDefinition wEJx3Itb() {
      return new DroneDefinition(
         "thermal",
         "item.fpvdrone.thermal_drone",
         DroneKind.v5suI,
         580.0F,
         DroneDefinition.Physics.v5suI,
         DroneDefinition.Rates.v5suI,
         DroneDefinition.Render.v5suI,
         "geo/fpv_7d.geo.json",
         "textures/entity/fpv_7d.png",
         0.0F,
         false,
         "KINDER",
         List.of(dOfFwvo()),
         List.of(uSUb()),
         null,
         null,
         null,
         null,
         null,
         6000,
         4,
         zTT3nw6,
         null,
         null,
         null,
         null,
         null,
         Guidance.ibzfxic,
         DroneRole.v5suI,
         null,
         DroneDefinition.Sync.ibzfxic,
         List.of(),
         DroneDefinition.ibzfxic,
         -1.0F,
         "fpvdrone:thermal_drone",
         null
      );
   }

   private static DroneDefinition bAcdvq9() {
      DroneDefinition.Mount var0 = new DroneDefinition.Mount(
         "geo/mount_vog.geo.json", "textures/entity/dji_mavic_3pro.png", AttachTransform.v5suI("payload_mount")
      );
      DroneDefinition.BombBay var1 = new DroneDefinition.BombBay(
         "40mm_explosive",
         List.of(
            new PayloadStation("left", new AttachTransform("payload_mount", -1.0F, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 1.0F), null, null),
            new PayloadStation("right", new AttachTransform("payload_mount", 1.0F, 0.5F, 0.5F, 0.0F, 0.0F, 0.0F, 1.0F), null, null)
         )
      );
      return new DroneDefinition(
         "mavic",
         "item.fpvdrone.mavic_drone",
         DroneKind.ibzfxic,
         895.0F,
         DroneDefinition.Physics.v5suI,
         DroneDefinition.Rates.v5suI,
         new DroneDefinition.Render(false, true),
         "geo/dji_mavic_3pro.geo.json",
         "textures/entity/dji_mavic_3pro.png",
         180.0F,
         false,
         "MAVIC",
         List.of(rsDd50tfV()),
         List.of(),
         var0,
         var1,
         null,
         null,
         null,
         6000,
         4,
         zTT3nw6,
         null,
         null,
         null,
         null,
         null,
         Guidance.ibzfxic,
         DroneRole.v5suI,
         null,
         DroneDefinition.Sync.ibzfxic,
         List.of(),
         DroneDefinition.ibzfxic,
         -1.0F,
         "fpvdrone:mavic_drone",
         null
      );
   }

   private static DroneDefinition c1jHYPb4() {
      return new DroneDefinition(
         "fp1",
         "item.fpvdrone.fp1_drone",
         DroneKind.v5suI,
         18000.0F,
         DroneDefinition.Physics.v5suI,
         DroneDefinition.Rates.v5suI,
         new DroneDefinition.Render(false, false),
         "bedrock_geo/fp1_drun.geo.json",
         "textures/entity/fp1_drun.png",
         0.0F,
         false,
         "FP-1",
         List.of(),
         List.of(),
         null,
         null,
         new DroneDefinition.Camera(
            70.0F,
            true,
            false,
            true,
            DroneDefinition.OsdProfile.v5suI,
            DroneDefinition.GimbalLimits.v5suI,
            false,
            DroneDefinition.DEFAULT_ZOOM_LEVELS,
            false,
            0.0F,
            1.4375F,
            0.25F,
            0.0F,
            true
         ),
         null,
         null,
         6000,
         0,
         vmpmU,
         null,
         DroneDefinition.LoiterFlight.v5suI,
         null,
         null,
         null,
         Guidance.wuhB,
         DroneRole.v5suI,
         v8ZN(),
         DroneDefinition.Sync.dOfFwvo,
         List.of(),
         DroneDefinition.ibzfxic,
         -1.0F,
         "fpvdrone:fp1_drone",
         null
      );
   }

   private static DroneDefinition.RocketPod v8ZN() {
      ArrayList var0 = new ArrayList();

      for (String var4 : new String[]{"right", "left"}) {
         for (int var5 = 1; var5 <= 4; var5++) {
            String var6 = var4 + "_rocket" + var5;
            var0.add(new PayloadStation(var6, AttachTransform.v5suI(var6), null, "fire_pos_" + var4 + var5));
         }
      }

      List var7 = List.of(
         new RocketMount("left_mount", "nurs_left", List.of("left_rocket1", "left_rocket2", "left_rocket3", "left_rocket4")),
         new RocketMount("right_mount", "nurs_right", List.of("right_rocket1", "right_rocket2", "right_rocket3", "right_rocket4"))
      );
      return new DroneDefinition.RocketPod("70mm_rocket", var0, var7, List.of());
   }

   private static DroneDefinition zTT3nw6() {
      return new DroneDefinition(
         "lytui_drun",
         "item.fpvdrone.lytui_drone",
         DroneKind.v5suI,
         16000.0F,
         DroneDefinition.Physics.v5suI,
         DroneDefinition.Rates.v5suI,
         new DroneDefinition.Render(false, false),
         "geo/lytui_drun.geo.json",
         "textures/entity/lytui_drun.png",
         0.0F,
         false,
         "Лютый",
         List.of(),
         List.of(),
         null,
         null,
         null,
         null,
         null,
         6000,
         0,
         null,
         null,
         DroneDefinition.LoiterFlight.v5suI,
         new DroneDefinition.Collision("geo/collision/lytui_drun.geo.json", true, Map.of(), new float[]{0.0F, 15.5F, -10.0F}),
         null,
         null,
         Guidance.ibzfxic,
         DroneRole.v5suI,
         null,
         DroneDefinition.Sync.dOfFwvo,
         List.of(),
         DroneDefinition.ibzfxic,
         -1.0F,
         "fpvdrone:lytui_drone",
         null
      );
   }

   static {
      b69s.put("default", jGhI2());
      b69s.put("thermal", wEJx3Itb());
      b69s.put("mavic", bAcdvq9());
      b69s.put("fp1", c1jHYPb4());
      b69s.put("lytui_drun", zTT3nw6());
   }
}
