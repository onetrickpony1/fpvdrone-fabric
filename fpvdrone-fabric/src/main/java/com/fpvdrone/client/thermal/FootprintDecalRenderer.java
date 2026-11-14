package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 FootprintDecalRenderer: draws the footprints as flat white quads whose brightness and opacity follow their
 * remaining heat. 1.1.8 drew them at the AFTER_WEATHER stage; here they are submitted with the level's own geometry
 * (translucent debug quads, depth tested, two-sided), only while the thermal view is on.
 */
public final class FootprintDecalRenderer {
   private FootprintDecalRenderer() {
   }

   public static void init() {
      LevelRenderEvents.COLLECT_SUBMITS.register(FootprintDecalRenderer::onCollectSubmits);
   }

   private static void onCollectSubmits(LevelRenderContext context) {
      if (!ThermalCameraState.getInstance().isEnabled()) {
         return;
      }
      FootprintSystem system = FootprintSystem.getInstance();
      if (system.getDecalCount() == 0) {
         return;
      }
      Vec3 cam = context.levelState().cameraRenderState.pos;
      PoseStack poseStack = context.poseStack();
      context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
         for (FootprintSystem.FootprintDecal decal : system.decals()) {
            float intensity = decal.getIntensity();
            if (intensity < 0.005F) {
               continue;
            }
            int c = (int)(intensity * 255.0F);
            float hw = decal.width * 0.5F;
            float hl = decal.length * 0.5F;
            float sin = (float)Math.sin(decal.angle);
            float cos = (float)Math.cos(decal.angle);
            float cx = (float)(decal.x - cam.x);
            float cy = (float)(decal.y - cam.y);
            float cz = (float)(decal.z - cam.z);
            buffer.addVertex(pose, cx + (-hw * cos - -hl * sin), cy, cz + (-hw * sin + -hl * cos)).setColor(c, c, c, c);
            buffer.addVertex(pose, cx + (hw * cos - -hl * sin), cy, cz + (hw * sin + -hl * cos)).setColor(c, c, c, c);
            buffer.addVertex(pose, cx + (hw * cos - hl * sin), cy, cz + (hw * sin + hl * cos)).setColor(c, c, c, c);
            buffer.addVertex(pose, cx + (-hw * cos - hl * sin), cy, cz + (-hw * sin + hl * cos)).setColor(c, c, c, c);
         }
      });
   }
}
