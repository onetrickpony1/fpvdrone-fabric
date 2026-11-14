package com.fpvdrone.client.thermal;

import com.fpvdrone.entity.PilotBodyEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.FPVMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

/**
 * 1.1.8 DepthOfFieldPass: the thermal lens focuses on what the camera looks at (block or entity, 2..256 blocks,
 * eased quickly) and blurs what is nearer or farther. Toggled by the thermal focus key (Home); on by default.
 */
public class DepthOfFieldPass implements ThermalPass {
   private static final DepthOfFieldPass INSTANCE = new DepthOfFieldPass();
   private static final float MAX_FOCUS = 256.0F;
   private static final float MIN_FOCUS = 2.0F;
   private static final float FOCUS_SPEED = 30.0F;
   private static final float APERTURE = 0.04F;
   private float currentFocusDistance = 20.0F;
   private RenderTarget intermediate;
   private GpuBuffer horizontalParams;
   private GpuBuffer verticalParams;
   private long lastUpdateNanos = 0L;

   public static DepthOfFieldPass getInstance() {
      return INSTANCE;
   }

   @Override
   public String getName() {
      return "depth_of_field";
   }

   @Override
   public void apply(RenderTarget in, RenderTarget out, PostProcessContext context) {
      if (ThermalCameraState.getInstance().isDepthOfField() && context.getDepth() != null && ThermalPipelines.usable(ThermalPipelines.DEPTH_OF_FIELD)) {
         this.updateFocusDistance();
         context.setFocusDistance(this.currentFocusDistance);
         if (this.blur(in, out, context, APERTURE * zoomBlurFactor())) {
            return;
         }
      }
      FullscreenQuad.blit(in, out);
   }

   private boolean blur(RenderTarget in, RenderTarget out, PostProcessContext context, float aperture) {
      this.ensureIntermediate(in.width, in.height);
      if (this.horizontalParams == null) {
         this.horizontalParams = FullscreenQuad.createParamsBuffer("dof h");
         this.verticalParams = FullscreenQuad.createParamsBuffer("dof v");
      }
      FullscreenQuad.params(this.horizontalParams, context, in, 1.0F, 0.0F, aperture);
      FullscreenQuad.params(this.verticalParams, context, in, 0.0F, 1.0F, aperture);
      return FullscreenQuad.draw(ThermalPipelines.DEPTH_OF_FIELD, in, this.intermediate, context.getDepth(), null, this.horizontalParams)
         && FullscreenQuad.draw(ThermalPipelines.DEPTH_OF_FIELD, this.intermediate, out, context.getDepth(), null, this.verticalParams);
   }

   /**
    * 1.1.8 wEJx3Itb(): a loitering munition's camera blurs more when zoomed in (its ZoomBlur). Drones have none.
    */
   private static float zoomBlurFactor() {
      float zoomBlur = com.fpvdrone.client.MunitionClientState.rsDd50tfV();
      if (zoomBlur <= 0.0F) {
         return 1.0F;
      }

      float zoom = com.fpvdrone.render.CameraZoom.getInstance().getZoom();
      float[] levels = com.fpvdrone.client.MunitionClientState.dOfFwvo();
      float maxZoom = levels[levels.length - 1];
      if (maxZoom <= 1.0F) {
         return 1.0F;
      }

      float t = Math.max(0.0F, Math.min(1.0F, (zoom - 1.0F) / (maxZoom - 1.0F)));
      return 1.0F + zoomBlur * t;
   }

   private void updateFocusDistance() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.level == null) {
         return;
      }
      // 1.1.8 aimed along the drone's look vector tilted by the camera angle; in this port the main camera already
      // carries that orientation in FPV, so its forward vector is the same ray.
      Camera camera = mc.gameRenderer.mainCamera();
      Vec3 from = camera.position();
      Vector3fc fwd = camera.forwardVector();
      Vec3 dir = new Vec3(fwd.x(), fwd.y(), fwd.z()).normalize();
      Vec3 to = from.add(dir.scale(MAX_FOCUS));
      BlockHitResult blockHit = mc.level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
      float target = blockHit.getType() == HitResult.Type.BLOCK ? (float)from.distanceTo(blockHit.getLocation()) : MAX_FOCUS;
      UUID playerId = mc.player.getUUID();
      UUID droneId = FPVMod.getActiveDroneId(playerId).orElse(null);
      if (droneId == null) {
         ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
         if (helmet.getItem() instanceof FpvGogglesItem) {
            droneId = FpvGogglesItem.getLinkedDroneId(helmet).orElse(null);
         }
      }
      UUID ownDrone = droneId;
      AABB box = new AABB(from, to).inflate(1.0);
      EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
         mc.player, from, to, box,
         e -> !e.isSpectator() && e.isPickable() && (ownDrone == null || !e.getUUID().equals(ownDrone)) && !(e instanceof PilotBodyEntity),
         65536.0
      );
      if (entityHit != null) {
         float d = (float)from.distanceTo(entityHit.getLocation());
         if (d >= MIN_FOCUS) {
            target = Math.min(target, d);
         }
      }
      target = Math.max(target, MIN_FOCUS);
      long now = System.nanoTime();
      float dt = this.lastUpdateNanos == 0L ? 0.016666668F : Math.min((now - this.lastUpdateNanos) / 1.0E9F, 0.1F);
      this.lastUpdateNanos = now;
      float k = 1.0F - (float)Math.exp(-FOCUS_SPEED * dt);
      this.currentFocusDistance += (target - this.currentFocusDistance) * k;
   }

   private void ensureIntermediate(int width, int height) {
      if (this.intermediate == null) {
         this.intermediate = new TextureTarget("fpvdrone thermal dof", width, height, GpuFormat.RGBA8_UNORM, null);
      } else if (this.intermediate.width != width || this.intermediate.height != height) {
         this.intermediate.resize(width, height);
      }
   }

   public float getCurrentFocusDistance() {
      return this.currentFocusDistance;
   }

   @Override
   public void cleanup() {
      if (this.intermediate != null) {
         this.intermediate.destroyBuffers();
         this.intermediate = null;
      }
      if (this.horizontalParams != null) {
         this.horizontalParams.close();
         this.verticalParams.close();
         this.horizontalParams = null;
         this.verticalParams = null;
      }
      this.lastUpdateNanos = 0L;
   }
}
