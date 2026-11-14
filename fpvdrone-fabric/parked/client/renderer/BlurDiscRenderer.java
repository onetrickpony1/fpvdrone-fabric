package com.fpvdrone.client.renderer;

import com.fpvdrone.FPVMod;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.physics.PhysicsState;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@EventBusSubscriber(
    modid = "fpvdrone",
    value = {Dist.CLIENT}
)
public class BlurDiscRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("fpvdrone", "textures/entity/fpv_7d.png");
    private static final float PROP_FADE_START = 0.25F;
    private static final float PROP_FADE_END = 0.55F;
    private static final float S = 0.0625F;
    private static final float HALF = 4.25F;
    private static final float DISC_Y = 4.25F;
    private static final float[][] PIVOTS = new float[][]{
        {-5.47049F, 3.75F, -5.74002F}, {5.47049F, 3.75F, -5.74002F}, {-5.47049F, 3.75F, 5.74002F}, {5.47049F, 3.75F, 5.74002F}
    };
    private static final float[][] UP_UVS = new float[][]{
        {16.0F, 16.0F, -5.25F, -5.25F}, {10.75F, 16.0F, 5.25F, -5.25F}, {16.0F, 10.75F, -5.25F, 5.25F}, {10.75F, 10.75F, 5.25F, 5.25F}
    };
    private static final float[][] DOWN_UVS = new float[][]{
        {32.0F, 21.5F, -10.5F, 10.5F}, {21.5F, 21.5F, 10.5F, 10.5F}, {32.0F, 32.0F, -10.5F, -10.5F}, {21.5F, 32.0F, 10.5F, -10.5F}
    };

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == Stage.AFTER_TRANSLUCENT_BLOCKS) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                float partialTick = event.getPartialTick();
                Vec3 camPos = event.getCamera().getPosition();

                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof DroneEntity drone
                        && (
                            !drone.isThermal()
                                || mc.player == null
                                || mc.options.getCameraType() != CameraType.FIRST_PERSON
                                || !FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(drone.getUUID())).orElse(false)
                        )) {
                        float throttle = getThrottleNormalized(drone);
                        if (!(throttle < 0.25F)) {
                            float blurAlpha;
                            if (throttle > 0.55F) {
                                blurAlpha = 1.0F;
                            } else {
                                blurAlpha = (throttle - 0.25F) / 0.3F;
                            }

                            renderBlurDiscs(drone, partialTick, camPos, event.getPoseStack(), throttle, blurAlpha);
                        }
                    }
                }
            }
        }
    }

    private static void renderBlurDiscs(DroneEntity drone, float partialTick, Vec3 camPos, PoseStack poseStack, float throttle, float blurAlpha) {
        double dx = Mth.lerp(partialTick, drone.xOld, drone.getX());
        double dy = Mth.lerp(partialTick, drone.yOld, drone.getY());
        double dz = Mth.lerp(partialTick, drone.zOld, drone.getZ());
        Minecraft mc = Minecraft.getInstance();
        PhysicsState physics = mc.player != null ? FPVMod.getPhysicsState(mc.player.getUUID()) : null;
        boolean isLocal = physics != null
            && mc.player != null
            && FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(drone.getUUID())).orElse(false);
        if (isLocal) {
            dx = Mth.lerp(partialTick, mc.player.xOld, mc.player.getX());
            dy = Mth.lerp(partialTick, mc.player.yOld, mc.player.getY());
            dz = Mth.lerp(partialTick, mc.player.zOld, mc.player.getZ());
        }

        poseStack.pushPose();
        poseStack.translate(dx - camPos.x, dy - camPos.y, dz - camPos.z);
        if (isLocal && physics != null) {
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
            Quaternionf quat = new Quaternionf().setFromNormalized(rot);
            poseStack.mulPose(quat);
        } else if (drone.isBeingControlled()) {
            Quaternionf quat = drone.getRemoteOrientation(partialTick);
            poseStack.mulPose(quat);
        } else {
            float yaw = Mth.rotLerp(partialTick, drone.yBodyRotO, drone.yBodyRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
            float pitch = Mth.lerp(partialTick, drone.xRotO, drone.getXRot());
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            float roll = drone.getDroneRoll(partialTick);
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        }

        BlockPos lightPos = BlockPos.containing(dx, dy + 0.25, dz);
        int light = LevelRenderer.getLightColor(drone.level(), lightPos);
        BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer vc = buffer.getBuffer(BlurDiscRenderer.Access.BLUR_RT);
        float discRotY = 0.0F;

        for (int i = 0; i < 4; i++) {
            renderDisc(poseStack, vc, light, blurAlpha, PIVOTS[i], UP_UVS[i], DOWN_UVS[i], discRotY);
        }

        buffer.endBatch(BlurDiscRenderer.Access.BLUR_RT);
        poseStack.popPose();
    }

    private static void renderDisc(PoseStack poseStack, VertexConsumer vc, int light, float alpha, float[] pivot, float[] upUV, float[] downUV, float rotY) {
        poseStack.pushPose();
        float px = pivot[0] * 0.0625F;
        float py = pivot[1] * 0.0625F;
        float pz = pivot[2] * 0.0625F;
        poseStack.translate(px, py, pz);
        if (rotY != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        }

        poseStack.translate(-px, -py, -pz);
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();
        int overlay = OverlayTexture.NO_OVERLAY;
        float y = 0.265625F;
        float x0 = (pivot[0] - 4.25F) * 0.0625F;
        float z0 = (pivot[2] - 4.25F) * 0.0625F;
        float x1 = (pivot[0] + 4.25F) * 0.0625F;
        float z1 = (pivot[2] + 4.25F) * 0.0625F;
        float uU0 = upUV[0] / 32.0F;
        float vU0 = upUV[1] / 32.0F;
        float uU1 = (upUV[0] + upUV[2]) / 32.0F;
        float vU1 = (upUV[1] + upUV[3]) / 32.0F;
        float uD0 = downUV[0] / 32.0F;
        float vD0 = downUV[1] / 32.0F;
        float uD1 = (downUV[0] + downUV[2]) / 32.0F;
        float vD1 = (downUV[1] + downUV[3]) / 32.0F;
        vertex(vc, pose, normal, light, overlay, alpha, 0.0F, 1.0F, 0.0F, x0, y, z0, uU0, vU0);
        vertex(vc, pose, normal, light, overlay, alpha, 0.0F, 1.0F, 0.0F, x0, y, z1, uU0, vU1);
        vertex(vc, pose, normal, light, overlay, alpha, 0.0F, 1.0F, 0.0F, x1, y, z1, uU1, vU1);
        vertex(vc, pose, normal, light, overlay, alpha, 0.0F, 1.0F, 0.0F, x1, y, z0, uU1, vU0);
        vertex(vc, pose, normal, light, overlay, alpha, 0.0F, -1.0F, 0.0F, x1, y, z0, uD0, vD0);
        vertex(vc, pose, normal, light, overlay, alpha, 0.0F, -1.0F, 0.0F, x1, y, z1, uD0, vD1);
        vertex(vc, pose, normal, light, overlay, alpha, 0.0F, -1.0F, 0.0F, x0, y, z1, uD1, vD1);
        vertex(vc, pose, normal, light, overlay, alpha, 0.0F, -1.0F, 0.0F, x0, y, z0, uD1, vD0);
        poseStack.popPose();
    }

    private static void vertex(
        VertexConsumer vc,
        Matrix4f pose,
        Matrix3f normal,
        int light,
        int overlay,
        float alpha,
        float nx,
        float ny,
        float nz,
        float x,
        float y,
        float z,
        float u,
        float v
    ) {
        vc.vertex(pose, x, y, z).color(1.0F, 1.0F, 1.0F, alpha).uv(u, v).overlayCoords(overlay).uv2(light).normal(normal, nx, ny, nz).endVertex();
    }

    private static float getThrottleNormalized(DroneEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            boolean isOwn = FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(entity.getUUID())).orElse(false);
            if (isOwn) {
                PhysicsState physics = FPVMod.getPhysicsState(mc.player.getUUID());
                if (physics != null && physics.isMotorsArmed()) {
                    if (physics.isThrottleCut()) {
                        return 0.0F;
                    }

                    float raw = ControllerReader.getThrottle();
                    return Mth.clamp((raw + 1.0F) / 2.0F, 0.0F, 1.0F);
                }
            }
        }

        if (entity.areMotorsArmed() && entity.isBeingControlled()) {
            return 0.7F;
        } else {
            return entity.areMotorsArmed() ? 0.15F : 0.0F;
        }
    }

    private static class Access extends RenderStateShard {
        static final RenderType BLUR_RT = RenderType.create(
            "fpvdrone_blur_no_depth",
            DefaultVertexFormat.NEW_ENTITY,
            Mode.QUADS,
            256,
            true,
            true,
            CompositeState.builder()
                .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
                .setTextureState(new TextureStateShard(Identifier.fromNamespaceAndPath("fpvdrone", "textures/entity/fpv_7d.png"), false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(true)
        );

        private Access() {
            super("dummy", () -> {}, () -> {});
        }
    }
}
