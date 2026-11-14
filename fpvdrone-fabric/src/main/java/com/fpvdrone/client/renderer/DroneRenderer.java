package com.fpvdrone.client.renderer;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.model.DroneModel;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.physics.PhysicsState;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * GeckoLib 5 renderer for the drone. Everything the Forge renderer read from the entity at draw time is captured into the
 * render state in {@link #addRenderData} and applied in {@link #adjustModelBonesForRender} / {@link #applyRotations}.
 */
public class DroneRenderer extends GeoEntityRenderer<DroneEntity, LivingEntityRenderState> {
    private static final Set<String> PROP_BONES = Set.of("vintleftfront", "vintrightfront", "vintleftback", "vintrightback");
    private static final Set<String> BLUR_BONES = Set.of("vintleftfrontblur", "vintrightfrontblur", "vintleftbackblur", "vintrightbackblur");
    private static final float PROP_HIDE_THROTTLE = 0.4F;

    private static final DataTicket<Quaternionf> ORIENTATION = DataTicket.create("fpvdrone_orientation", Quaternionf.class);
    private static final DataTicket<Vec3> LOCAL_OFFSET = DataTicket.create("fpvdrone_local_offset", Vec3.class);
    private static final DataTicket<Boolean> HAS_BATTERY = DataTicket.create("fpvdrone_has_battery", Boolean.class);
    private static final DataTicket<Boolean> HAS_RPG = DataTicket.create("fpvdrone_has_rpg", Boolean.class);
    private static final DataTicket<Boolean> HIDE_CAMERA = DataTicket.create("fpvdrone_hide_camera", Boolean.class);
    private static final DataTicket<Float> PROP_ANGLE = DataTicket.create("fpvdrone_prop_angle", Float.class);
    private static final DataTicket<Float> THROTTLE = DataTicket.create("fpvdrone_throttle", Float.class);

    public DroneRenderer(Context context) {
        super(context, new DroneModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public @Nullable RenderType getRenderType(LivingEntityRenderState renderState, Identifier texture) {
        return RenderTypes.entityTranslucentCull(texture);
    }

    @Override
    public void addRenderData(DroneEntity entity, @Nullable Void relatedObject, LivingEntityRenderState renderState, float partialTick) {
        GeoRenderState state = (GeoRenderState)renderState;
        Minecraft mc = Minecraft.getInstance();
        boolean isOwnDrone = mc.player != null
            && FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(entity.getUUID())).orElse(false);
        PhysicsState physics = isOwnDrone ? FPVMod.getPhysicsState(mc.player.getUUID()) : null;

        Quaternionf orientation;
        if (physics != null) {
            // The local pilot's drone follows the client physics directly, at the player's interpolated position.
            Vector3f look = physics.getDroneLook();
            Vector3f up = physics.getDroneUp();
            Vector3f left = physics.getDroneLeft();
            Matrix3f rot = new Matrix3f();
            rot.m00(-left.x);
            rot.m01(-left.y);
            rot.m02(-left.z);
            rot.m10(up.x);
            rot.m11(up.y);
            rot.m12(up.z);
            rot.m20(-look.x);
            rot.m21(-look.y);
            rot.m22(-look.z);
            orientation = new Quaternionf().setFromNormalized(rot);
            state.addGeckolibData(
                LOCAL_OFFSET,
                new Vec3(
                    Mth.lerp(partialTick, mc.player.xOld, mc.player.getX()) - Mth.lerp(partialTick, entity.xOld, entity.getX()),
                    Mth.lerp(partialTick, mc.player.yOld, mc.player.getY()) - Mth.lerp(partialTick, entity.yOld, entity.getY()),
                    Mth.lerp(partialTick, mc.player.zOld, mc.player.getZ()) - Mth.lerp(partialTick, entity.zOld, entity.getZ())
                )
            );
        } else if (entity.isBeingControlled()) {
            orientation = new Quaternionf(entity.getRemoteOrientation(partialTick));
        } else {
            float yaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
            float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            orientation = new Quaternionf()
                .rotateY((180.0F - yaw) * Mth.DEG_TO_RAD)
                .rotateX(pitch * Mth.DEG_TO_RAD)
                .rotateZ(entity.getDroneRoll(partialTick) * Mth.DEG_TO_RAD);
        }

        state.addGeckolibData(ORIENTATION, orientation);
        state.addGeckolibData(HAS_BATTERY, entity.hasBattery());
        state.addGeckolibData(HAS_RPG, entity.hasRpg());
        state.addGeckolibData(HIDE_CAMERA, isLookingThroughCamera(mc));
        state.addGeckolibData(PROP_ANGLE, entity.getPropAngle(partialTick));
        state.addGeckolibData(THROTTLE, getThrottleNormalized(entity, isOwnDrone, physics));
    }

    @Override
    protected void applyRotations(RenderPassInfo<LivingEntityRenderState> renderPassInfo, PoseStack poseStack, float nativeScale) {
        LivingEntityRenderState renderState = renderPassInfo.renderState();
        GeoRenderState state = (GeoRenderState)renderState;
        Vec3 offset = state.getGeckolibData(LOCAL_OFFSET);
        if (offset != null) {
            poseStack.translate(offset.x, offset.y, offset.z);
        }

        Quaternionf orientation = state.getGeckolibData(ORIENTATION);
        if (orientation != null) {
            poseStack.rotate(orientation);
        } else {
            poseStack.rotate(Axis.YP.rotationDegrees(180.0F - renderState.bodyRot));
        }
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<LivingEntityRenderState> renderPassInfo, BoneSnapshots snapshots) {
        GeoRenderState state = (GeoRenderState)renderPassInfo.renderState();
        boolean hasBattery = state.getOrDefaultGeckolibData(HAS_BATTERY, true);
        boolean hasRpg = state.getOrDefaultGeckolibData(HAS_RPG, false);
        boolean hideCamera = state.getOrDefaultGeckolibData(HIDE_CAMERA, false);
        float propAngle = state.getOrDefaultGeckolibData(PROP_ANGLE, 0.0F);
        float throttle = state.getOrDefaultGeckolibData(THROTTLE, 0.0F);
        snapshots.ifPresent("batka", b -> b.skipRender(!hasBattery));
        snapshots.ifPresent("rpg", b -> b.skipRender(!hasRpg));
        snapshots.ifPresent("camera", b -> b.skipRender(hideCamera).setRotX((float)Math.toRadians(FpvConfig.getDefaultAngle())));
        snapshots.ifPresent("kirpichiki", b -> b.skipRender(true));
        for (String blur : BLUR_BONES) {
            snapshots.ifPresent(blur, b -> b.skipRender(true));
        }

        for (String prop : PROP_BONES) {
            snapshots.ifPresent(prop, b -> b.skipRender(throttle > PROP_HIDE_THROTTLE).setRotY(propAngle));
        }
    }

    /** First-person view through the drone camera: hide the camera bone so it does not block the view. */
    private static boolean isLookingThroughCamera(Minecraft mc) {
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            return false;
        }

        if (FPVMod.getPhysicsState(mc.player.getUUID()) != null) {
            return true;
        }

        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        return helmet.getItem() instanceof FpvGogglesItem && FpvGogglesItem.getLinkedDroneId(helmet).isPresent();
    }

    private static float getThrottleNormalized(DroneEntity entity, boolean isOwn, @Nullable PhysicsState physics) {
        if (isOwn && physics != null && physics.isMotorsArmed()) {
            return physics.isThrottleCut() ? 0.0F : Mth.clamp((ControllerReader.getThrottle() + 1.0F) / 2.0F, 0.0F, 1.0F);
        }

        if (entity.areMotorsArmed() && entity.isBeingControlled()) {
            return 0.7F;
        }

        return entity.areMotorsArmed() ? 0.15F : 0.0F;
    }
}
