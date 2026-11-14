package com.fpvdrone.render;

import com.fpvdrone.entity.PilotBodyEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.UUID;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/** Draws the pilot's standing body (with their skin, armour and held items) while they fly. */
public class PilotBodyRenderer extends LivingEntityRenderer<PilotBodyEntity, AvatarRenderState, PlayerModel> {
    /** The body's dead/dying flag (the original skipped drawing a dying body). */
    private static final RenderStateDataKey<Boolean> DEAD_OR_DYING = RenderStateDataKey.create(() -> "fpvdrone:pilot_body_dead_or_dying");

    private final PlayerModel normalModel;
    private final PlayerModel slimModel;

    public PilotBodyRenderer(Context context) {
        super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        this.normalModel = this.model;
        this.slimModel = new PlayerModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                net.minecraft.client.renderer.entity.ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), part -> new PlayerModel(part, false)),
                context.getEquipmentRenderer()
            )
        );
        this.addLayer(new PlayerItemInHandLayer<>(this));
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    public void extractRenderState(PilotBodyEntity entity, AvatarRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        HumanoidMobRenderer.extractHumanoidRenderState(entity, state, partialTicks, this.itemModelResolver);
        state.leftArmPose = getArmPose(entity, HumanoidArm.LEFT);
        state.rightArmPose = getArmPose(entity, HumanoidArm.RIGHT);
        state.skin = getSkin(entity);
        state.setData(DEAD_OR_DYING, entity.isDeadOrDying());
        state.isBaby = false;
        state.isCrouching = false;
        state.swimAmount = 0.0F;
        state.showHat = true;
        state.showJacket = true;
        state.showLeftPants = true;
        state.showRightPants = true;
        state.showLeftSleeve = true;
        state.showRightSleeve = true;
        state.showCape = false;
    }

    @Override
    public void submit(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (state.getDataOrDefault(DEAD_OR_DYING, false)) {
            return;
        }

        this.model = state.skin.model() == PlayerModelType.SLIM ? this.slimModel : this.normalModel;
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState state) {
        return state.skin.body().texturePath();
    }

    /** The owner's skin from the tab list; without it, the default skin for their UUID with the arm width the server sent. */
    private static PlayerSkin getSkin(PilotBodyEntity entity) {
        PlayerInfo info = getPlayerInfo(entity);
        if (info != null) {
            return info.getSkin();
        }

        UUID ownerUUID = entity.getOwnerUUID();
        PlayerSkin fallback = ownerUUID != null ? DefaultPlayerSkin.get(ownerUUID) : DefaultPlayerSkin.getDefaultSkin();
        PlayerModelType model = isSlimModelClient(entity) ? PlayerModelType.SLIM : PlayerModelType.WIDE;
        return new PlayerSkin(fallback.body(), fallback.cape(), fallback.elytra(), model, fallback.secure());
    }

    private static boolean isSlimModelClient(PilotBodyEntity entity) {
        PlayerInfo info = getPlayerInfo(entity);
        if (info != null) {
            return info.getSkin().model() == PlayerModelType.SLIM;
        }

        return entity.isSlimModel();
    }

    private static @Nullable PlayerInfo getPlayerInfo(PilotBodyEntity entity) {
        UUID ownerUUID = entity.getOwnerUUID();
        if (ownerUUID != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getConnection() != null) {
                return mc.getConnection().getPlayerInfo(ownerUUID);
            }
        }

        return null;
    }

    private static ArmPose getArmPose(PilotBodyEntity entity, HumanoidArm arm) {
        ItemStack stack = arm == entity.getMainArm() ? entity.getMainHandItem() : entity.getOffhandItem();
        if (stack.isEmpty()) {
            return ArmPose.EMPTY;
        }

        return stack.is(Items.CROSSBOW) && CrossbowItem.isCharged(stack) ? ArmPose.CROSSBOW_HOLD : ArmPose.ITEM;
    }

    @Override
    protected void scale(AvatarRenderState state, PoseStack poseStack) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }

    @Override
    protected boolean shouldShowName(PilotBodyEntity entity, double distanceToCameraSq) {
        return distanceToCameraSq <= 4096.0 && entity.getOwnerName() != null && !entity.getOwnerName().isEmpty();
    }

    @Override
    protected @Nullable Component getNameTag(PilotBodyEntity entity) {
        String ownerName = entity.getOwnerName();
        return ownerName != null && !ownerName.isEmpty() ? Component.literal(ownerName) : null;
    }
}
