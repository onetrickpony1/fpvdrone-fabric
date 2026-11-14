package com.fpvdrone.client.renderer;

import com.fpvdrone.entity.DroneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * "Kirpichiki": the two launch bricks a drone rests on before its first liftoff. They stay where the drone was armed and
 * fade out over 140 ticks after liftoff. The Forge version drew them at RenderLevelStageEvent AFTER_ENTITIES; here they are
 * submitted with the level's other features through Fabric's collect-submits event.
 */
public class KirpichikiRenderer {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("fpvdrone", "textures/entity/fpv_7d.png");
    private static final int FADE_TICKS = 140;

    private KirpichikiRenderer() {
    }

    /** Registers the level render hook; call once during client init. */
    public static void init() {
        LevelRenderEvents.COLLECT_SUBMITS.register(KirpichikiRenderer::onCollectSubmits);
    }

    private static void onCollectSubmits(LevelRenderContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Vec3 camPos = context.levelState().cameraRenderState.pos;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof DroneEntity drone && !DroneRenderer.isLocalThermalFpv(drone)) {
                renderForDrone(drone, camPos, context.poseStack(), context.submitNodeCollector());
            }
        }
    }

    private static void renderForDrone(DroneEntity drone, Vec3 camPos, PoseStack poseStack, SubmitNodeCollector collector) {
        if (!drone.isKirpichikiEligible()) {
            return;
        }

        int ticks = drone.getMotorsArmedTicks();
        int liftoffTick = drone.getKirpichikiLiftoffTick();
        boolean fading = liftoffTick >= 0;
        int fadeTicks = fading ? ticks - liftoffTick : 0;
        if (fadeTicks >= FADE_TICKS) {
            return;
        }

        float fadeAlpha = fading ? 1.0F - (float)fadeTicks / FADE_TICKS : 1.0F;
        double ax = drone.getKirpichikiAnchorX();
        double ay = drone.getKirpichikiAnchorY();
        double az = drone.getKirpichikiAnchorZ();
        poseStack.pushPose();
        poseStack.translate(ax - camPos.x, ay - camPos.y, az - camPos.z);
        poseStack.rotate(Axis.YP.rotationDegrees(-drone.yBodyRot));
        int light = LightCoordsUtil.getLightCoords(drone.level(), BlockPos.containing(ax, ay, az));
        // entityCutout in 26.x draws both sides, like the old entityCutoutNoCull.
        RenderType rt = fading ? RenderTypes.entityTranslucent(TEXTURE) : RenderTypes.entityCutout(TEXTURE);
        collector.submitCustomGeometry(poseStack, rt, (pose, vc) -> {
            PoseStack brickStack = new PoseStack();
            brickStack.last().set(pose);
            renderBricks(brickStack, vc, light, fadeAlpha);
        });
        poseStack.popPose();
    }

    private static void renderBricks(PoseStack poseStack, VertexConsumer vc, int light, float fadeAlpha) {
        float s = 0.0625F;
        poseStack.pushPose();
        poseStack.translate(-5.11732F * s, 1.25F * s, -0.42388F * s);
        poseStack.rotate(Axis.YP.rotationDegrees(-7.5F));
        poseStack.translate(5.11732F * s, -1.25F * s, 0.42388F * s);
        renderBox(
            poseStack,
            vc,
            light,
            fadeAlpha,
            -6.61732F * s,
            0.0F * s,
            -3.92388F * s,
            3.0F * s,
            3.0F * s,
            7.0F * s,
            0.0625F,
            0.3984375F,
            0.0234375F,
            0.0234375F,
            0.0F,
            0.3984375F,
            0.0546875F,
            0.0234375F,
            0.0625F,
            0.4296875F,
            0.0234375F,
            0.0234375F,
            0.0F,
            0.3984375F,
            0.0546875F,
            0.0234375F,
            0.0F,
            0.4296875F,
            0.0234375F,
            0.0546875F,
            0.03125F,
            0.484375F,
            0.0234375F,
            -0.0546875F
        );
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(5.11732F * s, 1.25F * s, -0.42388F * s);
        poseStack.rotate(Axis.YP.rotationDegrees(15.0F));
        poseStack.translate(-5.11732F * s, -1.25F * s, 0.42388F * s);
        renderBox(
            poseStack,
            vc,
            light,
            fadeAlpha,
            3.61732F * s,
            0.0F * s,
            -3.92388F * s,
            3.0F * s,
            3.0F * s,
            7.0F * s,
            0.0859375F,
            0.3984375F,
            -0.0234375F,
            0.0234375F,
            0.0546875F,
            0.3984375F,
            -0.0546875F,
            0.0234375F,
            0.0859375F,
            0.4296875F,
            -0.0234375F,
            0.0234375F,
            0.0546875F,
            0.3984375F,
            -0.0546875F,
            0.0234375F,
            0.0234375F,
            0.4296875F,
            -0.0234375F,
            0.0546875F,
            0.0546875F,
            0.484375F,
            -0.0234375F,
            -0.0546875F
        );
        poseStack.popPose();
    }

    private static void renderBox(
        PoseStack poseStack,
        VertexConsumer vc,
        int light,
        float alpha,
        float x,
        float y,
        float z,
        float w,
        float h,
        float d,
        float nu,
        float nv,
        float nuw,
        float nvh,
        float eu,
        float ev,
        float euw,
        float evh,
        float su,
        float sv,
        float suw,
        float svh,
        float wu,
        float wv,
        float wuw,
        float wvh,
        float uu,
        float uv,
        float uuw,
        float uvh,
        float du,
        float dv,
        float duw,
        float dvh
    ) {
        PoseStack.Pose pose = poseStack.last();
        int overlay = OverlayTexture.NO_OVERLAY;
        float x1 = x + w;
        float y1 = y + h;
        float z1 = z + d;
        quad(
            vc,
            pose,
            light,
            overlay,
            alpha,
            0.0F,
            0.0F,
            -1.0F,
            x1,
            y,
            z,
            nu,
            nv + nvh,
            x1,
            y1,
            z,
            nu,
            nv,
            x,
            y1,
            z,
            nu + nuw,
            nv,
            x,
            y,
            z,
            nu + nuw,
            nv + nvh
        );
        quad(
            vc,
            pose,
            light,
            overlay,
            alpha,
            0.0F,
            0.0F,
            1.0F,
            x,
            y,
            z1,
            su,
            sv + svh,
            x,
            y1,
            z1,
            su,
            sv,
            x1,
            y1,
            z1,
            su + suw,
            sv,
            x1,
            y,
            z1,
            su + suw,
            sv + svh
        );
        quad(
            vc,
            pose,
            light,
            overlay,
            alpha,
            1.0F,
            0.0F,
            0.0F,
            x1,
            y,
            z1,
            eu,
            ev + evh,
            x1,
            y1,
            z1,
            eu,
            ev,
            x1,
            y1,
            z,
            eu + euw,
            ev,
            x1,
            y,
            z,
            eu + euw,
            ev + evh
        );
        quad(
            vc,
            pose,
            light,
            overlay,
            alpha,
            -1.0F,
            0.0F,
            0.0F,
            x,
            y,
            z,
            wu,
            wv + wvh,
            x,
            y1,
            z,
            wu,
            wv,
            x,
            y1,
            z1,
            wu + wuw,
            wv,
            x,
            y,
            z1,
            wu + wuw,
            wv + wvh
        );
        quad(
            vc,
            pose,
            light,
            overlay,
            alpha,
            0.0F,
            1.0F,
            0.0F,
            x,
            y1,
            z,
            uu,
            uv,
            x,
            y1,
            z1,
            uu,
            uv + uvh,
            x1,
            y1,
            z1,
            uu + uuw,
            uv + uvh,
            x1,
            y1,
            z,
            uu + uuw,
            uv
        );
        quad(
            vc,
            pose,
            light,
            overlay,
            alpha,
            0.0F,
            -1.0F,
            0.0F,
            x,
            y,
            z1,
            du,
            dv,
            x,
            y,
            z,
            du,
            dv + dvh,
            x1,
            y,
            z,
            du + duw,
            dv + dvh,
            x1,
            y,
            z1,
            du + duw,
            dv
        );
    }

    private static void quad(
        VertexConsumer vc,
        PoseStack.Pose pose,
        int light,
        int overlay,
        float alpha,
        float nx,
        float ny,
        float nz,
        float x0,
        float y0,
        float z0,
        float u0,
        float v0,
        float x1,
        float y1,
        float z1,
        float u1,
        float v1,
        float x2,
        float y2,
        float z2,
        float u2,
        float v2,
        float x3,
        float y3,
        float z3,
        float u3,
        float v3
    ) {
        vertex(vc, pose, light, overlay, alpha, nx, ny, nz, x0, y0, z0, u0, v0);
        vertex(vc, pose, light, overlay, alpha, nx, ny, nz, x1, y1, z1, u1, v1);
        vertex(vc, pose, light, overlay, alpha, nx, ny, nz, x2, y2, z2, u2, v2);
        vertex(vc, pose, light, overlay, alpha, nx, ny, nz, x3, y3, z3, u3, v3);
    }

    private static void vertex(
        VertexConsumer vc,
        PoseStack.Pose pose,
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
        vc.addVertex(pose, x, y, z).setColor(1.0F, 1.0F, 1.0F, alpha).setUv(u, v).setOverlay(overlay).setLight(light).setNormal(pose, nx, ny, nz);
    }
}
