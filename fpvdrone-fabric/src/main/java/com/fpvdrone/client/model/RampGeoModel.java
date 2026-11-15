package com.fpvdrone.client.model;

import com.fpvdrone.data.RampDefinition;
import com.fpvdrone.entity.RampEntity;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/**
 * 1.1.8 GeoModel_TjWOaJQ: the launch ramp's model and texture come from its ramp definition ("Model" / "Texture",
 * RampDefinition.uSUb() / jGhI2()); the renderer puts them into the render state. No animations ("empty").
 * <p>
 * 1.1.8 read geo/ files from GeckoLib 4's cache; GeckoLib 5 only caches geckolib/models/, so the definition's path is
 * baked by {@link BedrockModels}, as for the drones.
 */
public class RampGeoModel extends GeoModel<RampEntity> {
    public static final Identifier MODEL = Identifier.fromNamespaceAndPath("fpvdrone", RampDefinition.v5suI);
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("fpvdrone", RampDefinition.ibzfxic);
    // GeckoLib 5 id: assets/fpvdrone/geckolib/animations/empty.animation.json
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("fpvdrone", "empty");

    public static final DataTicket<Identifier> MODEL_PATH = DataTicket.create("fpvdrone_ramp_model_path", Identifier.class);
    public static final DataTicket<Identifier> TEXTURE_PATH = DataTicket.create("fpvdrone_ramp_texture_path", Identifier.class);

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(MODEL_PATH, MODEL);
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE_PATH, TEXTURE);
    }

    @Override
    public Identifier getAnimationResource(RampEntity entity) {
        return ANIMATION;
    }

    @Override
    public BakedGeoModel getBakedModel(Identifier location) {
        BakedGeoModel model = BedrockModels.v5suI(location);
        if (model != null) {
            return model;
        }

        if (!location.equals(MODEL)) {
            BakedGeoModel fallback = BedrockModels.v5suI(MODEL);
            if (fallback != null) {
                return fallback;
            }
        }

        return super.getBakedModel(location);
    }
}
