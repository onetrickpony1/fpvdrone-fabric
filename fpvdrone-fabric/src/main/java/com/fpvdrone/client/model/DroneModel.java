package com.fpvdrone.client.model;

import com.fpvdrone.entity.DroneEntity;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

public class DroneModel extends GeoModel<DroneEntity> {
    // GeckoLib 5 ids: assets/fpvdrone/geckolib/models/fpv_7d.geo.json, geckolib/animations/drone.animation.json
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("fpvdrone", "fpv_7d");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("fpvdrone", "textures/entity/fpv_7d.png");
    private static final Identifier ANIMATION = Identifier.fromNamespaceAndPath("fpvdrone", "drone");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    @Override
    public Identifier getAnimationResource(DroneEntity entity) {
        return ANIMATION;
    }
}
