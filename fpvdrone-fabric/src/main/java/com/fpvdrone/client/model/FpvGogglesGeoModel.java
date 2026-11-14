package com.fpvdrone.client.model;

import com.fpvdrone.item.FpvGogglesItem;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/** GeckoLib model for the goggles worn on the head (assets/fpvdrone/geckolib/models/goggles.geo.json). */
public class FpvGogglesGeoModel extends GeoModel<FpvGogglesItem> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("fpvdrone", "goggles");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("fpvdrone", "textures/entity/goggles.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    /** The goggles have no animations (the original returned null as well); only asked for when a controller plays one. */
    @Override
    public Identifier getAnimationResource(FpvGogglesItem item) {
        return null;
    }
}
