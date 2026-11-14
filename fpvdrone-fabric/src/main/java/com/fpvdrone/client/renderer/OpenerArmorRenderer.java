package com.fpvdrone.client.renderer;

import com.fpvdrone.client.model.OpenerGeoModel;
import com.fpvdrone.client.model.ProjectileModels;
import com.fpvdrone.data.ArmorDefinition;
import com.fpvdrone.data.AttachTransform;
import com.fpvdrone.data.AttachableEntry;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.item.OpenerItem;
import com.fpvdrone.registry.ModItems;
import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Optional;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

/**
 * Draws the worn "Can Opener" vest (GeckoLib armour renderer, bone "armorBody") with its mine: 1.1.8 (EMS669LQ09)
 * captured the pose of the definition's MountBone and drew the preloaded projectile's bedrock model there with its
 * AttachTransform. GeckoLib 5 has no renderRecursively hook, so the mine is a per-bone render task on the mount bone
 * (the pose stack arrives on the bone's pivot; backing off the pivot gives 1.1.8's prepMatrixForBone pose).
 */
public class OpenerArmorRenderer extends GeoArmorRenderer<OpenerItem, HumanoidRenderState> {
    public OpenerArmorRenderer() {
        super(new OpenerGeoModel());
    }

    @Override
    public void preRenderPass(RenderPassInfo<HumanoidRenderState> renderPassInfo, SubmitNodeCollector renderTasks) {
        super.preRenderPass(renderPassInfo, renderTasks);
        ArmorDefinition definition = ((OpenerItem)ModItems.OPENER.get()).getDefinition();
        if (definition == null) {
            return;
        }

        Optional<AttachableEntry> mine = definition.ibzfxic();
        if (mine.isEmpty()) {
            return;
        }

        String projectileId = mine.get().v5suI();
        ProjectileDefinition projectile = ProjectileDefinitions.find(projectileId).orElse(null);
        if (projectile == null) {
            return;
        }

        BakedGeoModel model = ProjectileModels.dOfFwvo(projectile);
        if (model == null) {
            return;
        }

        AttachTransform attach = definition.wuhB(projectileId).orElse(AttachTransform.v5suI(definition.bAcdvq9()));
        Identifier texture = ProjectileModels.ibzfxic(projectile);
        renderPassInfo.model()
            .getBone(definition.bAcdvq9())
            .ifPresent(bone -> renderPassInfo.addPerBoneRender(bone, (info, b, collector) -> submitMine(info, b, collector, model, texture, attach)));
    }

    private static void submitMine(
        RenderPassInfo<HumanoidRenderState> info, GeoBone bone, SubmitNodeCollector collector, BakedGeoModel model, Identifier texture, AttachTransform attach
    ) {
        PoseStack poseStack = new PoseStack();
        poseStack.last().set(info.poseStack().last());
        bone.translateAwayFromPivotPoint(poseStack);
        BedrockModelRenderer.ibzfxic(attach, poseStack);
        BedrockModelRenderer.v5suI(model, RenderTypes.entityCutout(texture), poseStack, collector, info.packedLight(), OverlayTexture.NO_OVERLAY);
    }
}
