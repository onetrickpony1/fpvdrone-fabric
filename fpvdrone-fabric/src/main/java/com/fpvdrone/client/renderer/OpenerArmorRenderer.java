package com.fpvdrone.client.renderer;

import com.fpvdrone.client.model.OpenerGeoModel;
import com.fpvdrone.item.OpenerItem;
import com.geckolib.renderer.GeoArmorRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;

/**
 * Draws the worn "Can Opener" vest (GeckoLib armour renderer, bone "armorBody").
 * 1.1.8-PENDING: the loaded TM-62 drawn on the vest's mount bone (1.1.8 EMS669LQ09 renders the projectile's bedrock
 * model at the definition's MountBone with its AttachTransform); needs the bedrock-model renderer from the entity port.
 */
public class OpenerArmorRenderer extends GeoArmorRenderer<OpenerItem, HumanoidRenderState> {
    public OpenerArmorRenderer() {
        super(new OpenerGeoModel());
    }
}
