package com.fpvdrone.client.model;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.loading.loader.GeckoLibGsonLoader;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1.1.8 I978jvnnCzA: loads Bedrock geometry files (geo/*.geo.json, bedrock_geo/*.geo.json) on demand by resource path and
 * caches them until the next resource reload. Files that fail to load are remembered and not retried.
 * <p>
 * 1.1.8 parsed them with the simplebedrockmodel library (which has no Fabric 26.3 build); here GeckoLib 5's own Gson
 * loader reads and bakes them into a {@link BakedGeoModel}, drawn by {@link com.fpvdrone.client.renderer.BedrockModelRenderer}.
 * The cube format is the same. 1.1.8 also stripped the "normals" of poly meshes before parsing (the library choked on
 * them); GeckoLib parses them, so the file is passed as is.
 * <p>
 * Poly meshes (used by the bedrock_geo fixed-wing models) are not accepted by GeckoLib 5.5.7's model reader, so they are
 * removed before baking (see stripPolyMeshes) and read separately by {@link BedrockPolyMeshes} and drawn by
 * {@link com.fpvdrone.client.renderer.FixedWingModelRenderer}. {@link com.fpvdrone.client.renderer.BedrockModelRenderer}
 * still draws cubes only, which is all the projectile models have.
 */
public final class BedrockModels {
   private static final Logger v5suI = LoggerFactory.getLogger("FPVDrone/SBM");
   private static final GeckoLibGsonLoader LOADER = new GeckoLibGsonLoader();
   private static final Map<Identifier, BakedGeoModel> ibzfxic = new ConcurrentHashMap<>();
   private static final Map<Identifier, Boolean> wuhB = new ConcurrentHashMap<>();

   private BedrockModels() {
   }

   /** Clears the cache on every client resource reload (1.1.8 registered this in ClientSetup). */
   public static void init() {
      ResourceLoader.get(PackType.CLIENT_RESOURCES)
         .registerReloadListener(
            Identifier.fromNamespaceAndPath("fpvdrone", "bedrock_models"), (ResourceManagerReloadListener)resourceManager -> v5suI()
         );
   }

   public static @Nullable BakedGeoModel v5suI(Identifier var0) {
      BakedGeoModel var1 = ibzfxic.get(var0);
      if (var1 != null) {
         return var1;
      } else if (wuhB.containsKey(var0)) {
         return null;
      } else {
         BakedGeoModel var2 = ibzfxic(var0);
         if (var2 != null) {
            ibzfxic.put(var0, var2);
         } else {
            wuhB.put(var0, Boolean.TRUE);
         }

         return var2;
      }
   }

   private static @Nullable BakedGeoModel ibzfxic(Identifier var0) {
      Optional<Resource> var1 = Minecraft.getInstance().getResourceManager().getResource(var0);
      if (var1.isEmpty()) {
         return null;
      }

      try {
         JsonObject var2 = LOADER.deserializeGeckoLibModelFile(var0, var1.get());
         if (var2 == null) {
            return null;
         }

         stripPolyMeshes(var2);
         return LOADER.bakeGeckoLibModelFile(var0, var2);
      } catch (Exception var3) {
         v5suI.warn("Could not load bedrock model {}: {}", var0, var3.toString());
         return null;
      }
   }

   /**
    * GeckoLib 5.5.7 rejects the poly meshes of the bedrock_geo airframes ("Array must have size 1") and would not bake
    * them anyway; they are read by {@link BedrockPolyMeshes} instead, so the bones are baked without them. (1.1.8 had
    * to strip the meshes' normals for the simplebedrockmodel library for a similar reason.)
    */
   private static void stripPolyMeshes(JsonObject var0) {
      com.google.gson.JsonArray var1 = var0.getAsJsonArray("minecraft:geometry");
      if (var1 == null) {
         return;
      }

      for (com.google.gson.JsonElement var2 : var1) {
         if (var2.isJsonObject() && var2.getAsJsonObject().get("bones") instanceof com.google.gson.JsonArray var3) {
            for (com.google.gson.JsonElement var4 : var3) {
               if (var4.isJsonObject()) {
                  var4.getAsJsonObject().remove("poly_mesh");
               }
            }
         }
      }
   }

   public static void v5suI() {
      ibzfxic.clear();
      wuhB.clear();
      BedrockPolyMeshes.clear();
   }
}
