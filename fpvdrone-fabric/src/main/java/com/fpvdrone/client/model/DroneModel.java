package com.fpvdrone.client.model;

import com.fpvdrone.entity.DroneEntity;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/**
 * Model of the multirotor drone. As in 1.1.8 the model and texture come from the drone's definition ("Model" and
 * "Texture", e.g. geo/fpv_7d.geo.json, read through DroneEntity.rYlh() / sZzOK7T()); the renderer puts them into the
 * render state in its addRenderData.
 * <p>
 * 1.1.8 read every geo/ file from GeckoLib 4's own cache. GeckoLib 5 only caches geckolib/models/, so a definition's
 * path is baked on demand by {@link BedrockModels} (same loader, cached until the next resource reload).
 */
public class DroneModel extends GeoModel<DroneEntity> {
    /** 1.1.8 DroneModel.MODEL; the definition default (DroneDefinition.rsDd50tfV). */
    public static final Identifier MODEL = Identifier.fromNamespaceAndPath("fpvdrone", "geo/fpv_7d.geo.json");
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("fpvdrone", "textures/entity/fpv_7d.png");
    // GeckoLib 5 id: assets/fpvdrone/geckolib/animations/drone.animation.json
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("fpvdrone", "drone");

    /** Model path of this drone's definition (1.1.8 DroneModel.v5suI -> DroneEntity.rYlh()). */
    public static final DataTicket<Identifier> MODEL_PATH = DataTicket.create("fpvdrone_model_path", Identifier.class);
    /** Texture of this drone's definition (1.1.8 DroneModel.ibzfxic -> DroneEntity.sZzOK7T()). */
    public static final DataTicket<Identifier> TEXTURE_PATH = DataTicket.create("fpvdrone_texture_path", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(MODEL_PATH, MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE_PATH, TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(DroneEntity entity) {
        return ANIMATION;
    }

    /** Definition paths (geo/..., bedrock_geo/...) are baked by BedrockModels; anything else goes to GeckoLib's cache. */
    @Override
    public BakedGeoModel getBakedModel(Identifier location) {
        BakedGeoModel model = BedrockModels.v5suI(location);
        if (model != null) {
            return model;
        }

        if (!location.equals(MODEL)) {
            // A broken or missing custom model falls back to the default drone rather than GeckoLib's missing cube.
            BakedGeoModel fallback = BedrockModels.v5suI(MODEL);
            if (fallback != null) {
                return fallback;
            }
        }

        return super.getBakedModel(location);
    }
}
