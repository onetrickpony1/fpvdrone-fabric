package com.fpvdrone.client.renderer;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.physics.PhysicsState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Spinning-propeller blur discs: above 25% throttle a flat textured disc fades in over each prop (fully opaque from 55%).
 * The Forge version drew these in RenderLevelStageEvent (after translucent blocks); here they are submitted with the
 * level's other features through Fabric's collect-submits event, using a translucent type that does not write depth.
 */
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

    private BlurDiscRenderer() {
    }

    /** Registers the render pipeline and the level render hook; call once during client init. */
    public static void init() {
        Access.init();
        LevelRenderEvents.COLLECT_SUBMITS.register(BlurDiscRenderer::onCollectSubmits);
    }

    private static void onCollectSubmits(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 camPos = context.levelState().cameraRenderState.pos;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof DroneEntity drone && !DroneRenderer.isLocalThermalFpv(drone)) {
                float throttle = DroneRenderer.getThrottleNormalized(drone);
                if (!(throttle < PROP_FADE_START)) {
                    float blurAlpha = throttle > PROP_FADE_END ? 1.0F : (throttle - PROP_FADE_START) / (PROP_FADE_END - PROP_FADE_START);
                    renderBlurDiscs(drone, partialTick, camPos, context.poseStack(), context.submitNodeCollector(), blurAlpha);
                }
            }
        }
    }

    private static void renderBlurDiscs(
        DroneEntity drone, float partialTick, Vec3 camPos, PoseStack poseStack, SubmitNodeCollector collector, float blurAlpha
    ) {
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
        if (isLocal) {
            poseStack.rotate(DroneRenderer.physicsOrientation(physics));
        } else if (drone.isBeingControlled()) {
            poseStack.rotate(drone.getRemoteOrientation(partialTick));
        } else {
            float yaw = Mth.rotLerp(partialTick, drone.yBodyRotO, drone.yBodyRot);
            poseStack.rotate(Axis.YP.rotationDegrees(180.0F - yaw));
            float pitch = Mth.lerp(partialTick, drone.xRotO, drone.getXRot());
            poseStack.rotate(Axis.XP.rotationDegrees(pitch));
            poseStack.rotate(Axis.ZP.rotationDegrees(drone.getDroneRoll(partialTick)));
        }

        BlockPos lightPos = BlockPos.containing(dx, dy + 0.25, dz);
        int light = LightCoordsUtil.getLightCoords(drone.level(), lightPos);
        collector.submitCustomGeometry(poseStack, Access.BLUR_RT, (pose, buffer) -> {
            PoseStack discStack = new PoseStack();
            discStack.last().set(pose);
            float discRotY = 0.0F;
            for (int i = 0; i < 4; i++) {
                renderDisc(discStack, buffer, light, blurAlpha, PIVOTS[i], UP_UVS[i], DOWN_UVS[i], discRotY);
            }
        });
        poseStack.popPose();
    }

    private static void renderDisc(PoseStack poseStack, VertexConsumer vc, int light, float alpha, float[] pivot, float[] upUV, float[] downUV, float rotY) {
        poseStack.pushPose();
        float px = pivot[0] * S;
        float py = pivot[1] * S;
        float pz = pivot[2] * S;
        poseStack.translate(px, py, pz);
        if (rotY != 0.0F) {
            poseStack.rotate(Axis.YP.rotationDegrees(rotY));
        }

        poseStack.translate(-px, -py, -pz);
        PoseStack.Pose pose = poseStack.last();
        int overlay = OverlayTexture.NO_OVERLAY;
        float y = DISC_Y * S;
        float x0 = (pivot[0] - HALF) * S;
        float z0 = (pivot[2] - HALF) * S;
        float x1 = (pivot[0] + HALF) * S;
        float z1 = (pivot[2] + HALF) * S;
        float uU0 = upUV[0] / 32.0F;
        float vU0 = upUV[1] / 32.0F;
        float uU1 = (upUV[0] + upUV[2]) / 32.0F;
        float vU1 = (upUV[1] + upUV[3]) / 32.0F;
        float uD0 = downUV[0] / 32.0F;
        float vD0 = downUV[1] / 32.0F;
        float uD1 = (downUV[0] + downUV[2]) / 32.0F;
        float vD1 = (downUV[1] + downUV[3]) / 32.0F;
        vertex(vc, pose, light, overlay, alpha, 0.0F, 1.0F, 0.0F, x0, y, z0, uU0, vU0);
        vertex(vc, pose, light, overlay, alpha, 0.0F, 1.0F, 0.0F, x0, y, z1, uU0, vU1);
        vertex(vc, pose, light, overlay, alpha, 0.0F, 1.0F, 0.0F, x1, y, z1, uU1, vU1);
        vertex(vc, pose, light, overlay, alpha, 0.0F, 1.0F, 0.0F, x1, y, z0, uU1, vU0);
        vertex(vc, pose, light, overlay, alpha, 0.0F, -1.0F, 0.0F, x1, y, z0, uD0, vD0);
        vertex(vc, pose, light, overlay, alpha, 0.0F, -1.0F, 0.0F, x1, y, z1, uD0, vD1);
        vertex(vc, pose, light, overlay, alpha, 0.0F, -1.0F, 0.0F, x0, y, z1, uD1, vD1);
        vertex(vc, pose, light, overlay, alpha, 0.0F, -1.0F, 0.0F, x0, y, z0, uD1, vD0);
        poseStack.popPose();
    }

    private static void vertex(
        VertexConsumer vc, PoseStack.Pose pose, int light, int overlay, float alpha, float nx, float ny, float nz, float x, float y, float z, float u, float v
    ) {
        vc.addVertex(pose, x, y, z).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(u, v).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
    }

    /** Translucent entity type drawn from both sides, without depth writes (the original "fpvdrone_blur_no_depth"). */
    private static final class Access {
        private static final RenderPipeline PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath("fpvdrone", "pipeline/blur_no_depth"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
                .withCull(false)
                .build()
        );
        static final RenderType BLUR_RT = RenderType.create(
            "fpvdrone_blur_no_depth",
            RenderSetup.builder(PIPELINE)
                .withTexture("Sampler0", TEXTURE)
                .useLightmap()
                .useOverlay()
                .affectsCrumbling()
                .sortOnUpload()
                .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                .createRenderSetup()
        );

        private Access() {
        }

        static void init() {
            // Loading this class registers the pipeline.
        }
    }
}
