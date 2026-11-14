package com.fpvdrone.render;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.client.SignalManager;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.video.FrameFreezer;
import com.fpvdrone.video.FrozenFrameManager;
import com.fpvdrone.video.VideoResolution;
import com.fpvdrone.video.VideoSignalEffect;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.fpvdrone.video.GpuBlit;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LowResRenderer {
   private static final Logger LOGGER = LoggerFactory.getLogger(LowResRenderer.class);
   private static final LowResRenderer instance = new LowResRenderer();
   private RenderTarget lowResTarget;
   private VideoResolution baseResolution = VideoResolution.RES_25;
   private boolean enabled = false;
   private boolean djiFeed = false;
   private boolean shouldProcess = false;
   private boolean noSignalActive = false;
   private boolean frozenFrameMode = false;
   private UUID frozenFrameDroneId = null;
   private int lastScreenWidth = 0;
   private int lastScreenHeight = 0;
   private boolean lastWasCreative = true;
   private float lastSurvivalScale = -1.0F;
   private int frameCount = 0;
   private int processedFrames = 0;
   private int skippedFrames = 0;
   private int lastBlitWidth = 0;
   private int lastBlitHeight = 0;

   private LowResRenderer() {
   }

   public static LowResRenderer getInstance() {
      return instance;
   }

   public void init() {
      this.baseResolution = FpvConfig.getVideoResolution();
      this.createRenderTarget();
   }

   private float getEffectiveScale() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && !mc.player.isCreative() ? this.computeSurvivalScale(mc) : this.baseResolution.getScale();
   }

   private float computeSurvivalScale(Minecraft mc) {
      int maxRange = FpvConfig.getMaxRange();
      float dist = (float)SignalManager.getInstance().getDistance();
      if (dist <= 0.0F) {
         UUID playerId = mc.player.getUUID();
         Optional<DroneEntity> droneOpt = FPVMod.getActiveDrone(playerId);
         if (droneOpt.isPresent()) {
            dist = (float)droneOpt.get().distanceToSqr(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            dist = (float)Math.sqrt(dist);
         } else if (FPVMod.getActiveDroneId(playerId).isPresent()) {
            dist = maxRange;
         }
      }

      float t = Math.min(1.0F, dist / Math.max(1, maxRange));
      float rawScale = 0.33F - 0.23000002F * t;
      return this.snapToResolutionScale(rawScale);
   }

   private float snapToResolutionScale(float rawScale) {
      VideoResolution best = VideoResolution.RES_10;
      float bestDist = Float.MAX_VALUE;

      for (VideoResolution res : VideoResolution.values()) {
         if (!(res.getScale() > 0.33F)) {
            float d = Math.abs(res.getScale() - rawScale);
            if (d < bestDist) {
               bestDist = d;
               best = res;
            }
         }
      }

      return best.getScale();
   }

   private float getFboScale() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && !mc.player.isCreative() ? 0.33F : this.baseResolution.getScale();
   }

   private void createRenderTarget() {
      Minecraft mc = Minecraft.getInstance();
      int screenWidth = mc.getWindow().getWidth();
      int screenHeight = mc.getWindow().getHeight();
      this.createRenderTarget(screenWidth, screenHeight);
   }

   private void createRenderTarget(int screenWidth, int screenHeight) {
      if (this.lowResTarget == null || this.lastScreenWidth != screenWidth || this.lastScreenHeight != screenHeight) {
         this.lastScreenWidth = screenWidth;
         this.lastScreenHeight = screenHeight;
         float maxScale = this.getFboScale();
         int width = Math.max(16, (int)(screenHeight * maxScale * screenWidth / screenHeight));
         int height = Math.max(16, (int)(screenHeight * maxScale));
         if (this.lowResTarget != null) {
            this.lowResTarget.destroyBuffers();
         }

         try {
            this.lowResTarget = new TextureTarget("fpvdrone low-res video", width, height, GpuFormat.RGBA8_UNORM, null);
         } catch (Exception e) {
            LOGGER.error("Failed to create render target", e);
            this.lowResTarget = null;
         }
      }
   }

   public void invalidate() {
      this.lastScreenWidth = 0;
      this.lastScreenHeight = 0;
      this.lastBlitWidth = 0;
      this.lastBlitHeight = 0;
   }

   public void resize(VideoResolution resolution) {
      if (resolution != this.baseResolution) {
         this.baseResolution = resolution;
         this.lastBlitWidth = 0;
         this.lastBlitHeight = 0;
         RenderTarget mainTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
         this.invalidate();
         if (mainTarget != null && mainTarget.width > 0 && mainTarget.height > 0) {
            this.createRenderTarget(mainTarget.width, mainTarget.height);
         } else {
            this.createRenderTarget();
         }
      }
   }

   public void beginRender() {
      this.shouldProcess = false;
      if (this.enabled && this.lowResTarget == null) {
         // Made on first use, on the render thread, with the configured resolution.
         this.init();
      }
      this.frameCount++;
      if (this.enabled && this.lowResTarget != null) {
         Minecraft mc = Minecraft.getInstance();
         if (mc.player == null) {
            this.skippedFrames++;
         } else {
            this.shouldProcess = true;
         }
      } else {
         this.skippedFrames++;
      }
   }

   public void endRender() {
      if (this.shouldProcess && this.lowResTarget != null) {
         this.shouldProcess = false;
         Minecraft mc = Minecraft.getInstance();
         RenderTarget mainTarget = mc.gameRenderer.mainRenderTarget();
         if (this.frozenFrameMode && this.frozenFrameDroneId != null) {
            FrozenFrameManager.getInstance().renderFrozenFrame(this.frozenFrameDroneId, mainTarget);
            this.noSignalActive = true;
         } else if (com.fpvdrone.client.CleanFeed.isActive()) {
            // Clean feed: the full-resolution picture, no stutter; still captured for the frozen frame on signal loss.
            this.applySignalEffects(mainTarget);
         } else {
            if (this.lastScreenWidth != mainTarget.width || this.lastScreenHeight != mainTarget.height) {
               this.createRenderTarget(mainTarget.width, mainTarget.height);
               this.lastBlitWidth = 0;
               this.lastBlitHeight = 0;
            }

            if (this.lowResTarget != null) {
               boolean isCreative = mc.player != null && mc.player.isCreative();
               if (isCreative != this.lastWasCreative) {
                  this.invalidate();
                  this.lastWasCreative = isCreative;
                  this.createRenderTarget(mainTarget.width, mainTarget.height);
                  if (this.lowResTarget == null) {
                     return;
                  }
               }

               float signal = SignalManager.getInstance().getSignalStrength();
               float freezeSignal = SignalManager.getInstance().getFreezeSignal();
               boolean shouldUpdate = FrameFreezer.getInstance().shouldUpdateFrame(freezeSignal);
               float scale = this.getEffectiveScale();
               if (!isCreative && scale != this.lastSurvivalScale) {
                  this.lastBlitWidth = 0;
                  this.lastBlitHeight = 0;
                  this.lastSurvivalScale = scale;
               }

               int blitWidth = Math.max(16, (int)(mainTarget.height * scale * mainTarget.width / mainTarget.height));
               int blitHeight = Math.max(16, (int)(mainTarget.height * scale));
               boolean forceUpdate = this.lastBlitWidth == 0;
               if (!shouldUpdate && !forceUpdate) {
                  this.skippedFrames++;
               } else {
                  this.processedFrames++;
                  this.lastBlitWidth = blitWidth;
                  this.lastBlitHeight = blitHeight;
                  // The scaled frame fills the whole low-res target: it is (re)made at this size.
                  if (this.lowResTarget.width != blitWidth || this.lowResTarget.height != blitHeight) {
                     this.lowResTarget.resize(blitWidth, blitHeight);
                  }
                  GpuBlit.copy(mainTarget, this.lowResTarget, FilterMode.LINEAR);
               }

               // A frozen frame keeps the last scaled picture; it is enlarged with hard pixels either way.
               GpuBlit.copy(this.lowResTarget, mainTarget, FilterMode.NEAREST);
               this.applySignalEffects(mainTarget);
            }
         }
      }
   }

   private void applySignalEffects(RenderTarget target) {
      float signal = SignalManager.getInstance().getSignalStrength();
      this.noSignalActive = VideoSignalEffect.shouldShowNoSignal(signal);
      UUID droneId = this.getCurrentDroneId();
      if (droneId != null && FpvStatusChecker.isDroneDestroyed(droneId)) {
         this.noSignalActive = true;
      } else {
         if (droneId != null) {
            Minecraft mc = Minecraft.getInstance();
            boolean entityPresent = false;
            if (mc.level != null) {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof DroneEntity d && d.getUUID().equals(droneId)) {
                     entityPresent = true;
                     break;
                  }
               }
            }

            if (entityPresent) {
               FrozenFrameManager.getInstance().captureFrame(droneId, target);
            }
         }
      }
   }

   private UUID getCurrentDroneId() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         return null;
      }

      UUID playerId = mc.player.getUUID();
      if (FPVMod.isArmed(playerId)) {
         return FPVMod.getActiveDroneId(playerId).orElse(null);
      }

      ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
      return helmet.getItem() instanceof FpvGogglesItem ? FpvGogglesItem.getLinkedDroneId(helmet).orElse(null) : null;
   }

   public void renderFrozenFrame(UUID droneId, RenderTarget mainTarget) {
      FrozenFrameManager.getInstance().renderFrozenFrame(droneId, mainTarget);
   }

   public boolean hasFrozenFrame(UUID droneId) {
      return FrozenFrameManager.getInstance().hasFrozenFrame(droneId);
   }

   public void cleanup() {
      if (this.lowResTarget != null) {
         this.lowResTarget.destroyBuffers();
         this.lowResTarget = null;
      }

      this.shouldProcess = false;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (enabled) {
         FrameFreezer.getInstance().reset();
      } else {
         this.shouldProcess = false;
         this.frozenFrameMode = false;
         this.frozenFrameDroneId = null;
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setFrozenFrameMode(boolean frozen) {
      this.frozenFrameMode = frozen;
   }

   public boolean isFrozenFrameMode() {
      return this.frozenFrameMode;
   }

   /**
    * 1.1.8 (shouldProcess): set when flying a DJI drone, whose digital feed 1.1.8 handles on a separate path in the
    * low-res pass. 1.1.8-PENDING: that path; for now the flag is only stored.
    */
   public void setDjiFeed(boolean dji) {
      this.djiFeed = dji;
   }

   public boolean isDjiFeed() {
      return this.djiFeed;
   }

   public void setFrozenFrameDroneId(UUID droneId) {
      this.frozenFrameDroneId = droneId;
   }

   public UUID getFrozenFrameDroneId() {
      return this.frozenFrameDroneId;
   }

   public void cycleResolution() {
      this.resize(this.baseResolution.next());
      LOGGER.info(
         "Resolution: {} | Frames: {} processed, {} skipped of {} total",
         new Object[]{this.baseResolution.getDisplayName(), this.processedFrames, this.skippedFrames, this.frameCount}
      );
   }

   public VideoResolution getResolution() {
      return this.baseResolution;
   }

   public boolean isRendering() {
      return this.shouldProcess;
   }
}
