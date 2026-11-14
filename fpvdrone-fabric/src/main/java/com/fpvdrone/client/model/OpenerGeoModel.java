package com.fpvdrone.client.model;

import com.fpvdrone.item.OpenerItem;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;

/**
 * GeckoLib model for the worn "Can Opener" vest. 1.1.8 took the model and texture paths from the armour definition
 * (geo/opener.geo.json, textures/entity/opener.png); GeckoLib 5 bakes models only from geckolib/models/, so the geo is
 * copied there as opener.geo.json.
 */
public class OpenerGeoModel extends GeoModel<OpenerItem> {
    private static final Identifier MODEL = Identifier.fromNamespaceAndPath("fpvdrone", "opener");
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("fpvdrone", "textures/entity/opener.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }

    /** The vest has no animations (1.1.8 returned null too). */
    @Override
    public Identifier getAnimationResource(OpenerItem item) {
        return null;
    }
}
