package com.fpvdrone.client.model;

import com.fpvdrone.data.ProjectileDefinition;
import com.geckolib.cache.model.BakedGeoModel;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 UTdEkuUY: where a projectile's model and texture live (its definition's "Model" and "Texture", relative to
 * assets/fpvdrone/).
 * <p>
 * 1.1.8 drew the payload on the normal drone with GeckoLib 4 (a GeoModel per projectile, read from GeckoLib's own cache,
 * which held every geo/ file) and everything else (dropped bombs, fixed-wing payloads, the vest's TM-62) with the
 * simplebedrockmodel library. GeckoLib 5 only caches geckolib/models/, so both routes now go through
 * {@link BedrockModels}, which bakes any path with GeckoLib's own loader. The per-projectile GeoModel cache (wuhB, and
 * its clear on definition sync) is therefore gone.
 */
public final class ProjectileModels {
   private ProjectileModels() {
   }

   /** Model path of a projectile, e.g. fpvdrone:geo/rgd5.geo.json. */
   public static Identifier v5suI(ProjectileDefinition var0) {
      return Identifier.fromNamespaceAndPath("fpvdrone", var0.zHBAQgZP());
   }

   /** Texture of a projectile. */
   public static Identifier ibzfxic(ProjectileDefinition var0) {
      return Identifier.fromNamespaceAndPath("fpvdrone", var0.we1GYY());
   }

   /** The baked model of a projectile, or null when its file is missing or broken. */
   public static @Nullable BakedGeoModel dOfFwvo(ProjectileDefinition var0) {
      return BedrockModels.v5suI(v5suI(var0));
   }
}
