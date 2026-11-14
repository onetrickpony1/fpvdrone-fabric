package com.fpvdrone.client.renderer;

import com.fpvdrone.client.model.FpvGogglesGeoModel;
import com.fpvdrone.item.FpvGogglesItem;
import com.geckolib.renderer.GeoArmorRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/** Draws the goggles model on the wearer's head (GeckoLib armour renderer, bone "armorHead"). */
public class FpvGogglesArmorRenderer extends GeoArmorRenderer<FpvGogglesItem, HumanoidRenderState> {
    public FpvGogglesArmorRenderer() {
        super(new FpvGogglesGeoModel());
    }
}
