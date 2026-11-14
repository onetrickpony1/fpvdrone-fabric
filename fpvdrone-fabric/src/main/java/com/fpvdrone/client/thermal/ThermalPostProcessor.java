package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1.1.8 ThermalPostProcessor: runs the thermal passes over the rendered world, ping-ponging between two screen-size
 * targets; the last pass writes back into the main picture. The order is 1.1.8's: fallback, atmospheric fog, AGC,
 * DDE, depth of field, NETD noise, FPN, palette, vignette. 1.1.8's list also held Downsample, GaussianBlur and
 * Upsample, which its loop skipped, and ThermalBloomPass, which was never in the list; none of them are ported.
 */
public class ThermalPostProcessor {
   private static final Logger LOGGER = LoggerFactory.getLogger(ThermalPostProcessor.class);
   private static final ThermalPostProcessor INSTANCE = new ThermalPostProcessor();
   private RenderTarget ping;
   private RenderTarget pong;
   private RenderTarget snapshot;
   private final List<ThermalPass> passes = new ArrayList<>();
   private final PostProcessContext context = new PostProcessContext();
   private boolean initialized = false;

   ThermalPostProcessor() {
   }

   public static ThermalPostProcessor getInstance() {
      return INSTANCE;
   }

   public void init() {
      if (!this.initialized) {
         this.passes.clear();
         this.passes.add(new FallbackThermalPass());
         this.passes.add(new AtmosphericFogPass());
         this.passes.add(new AGCPass());
         this.passes.add(new DDEPass());
         this.passes.add(DepthOfFieldPass.getInstance());
         this.passes.add(new NETDNoisePass());
         this.passes.add(new FPNPass());
         this.passes.add(new PaletteLUTPass());
         this.passes.add(new VignetteDistortionPass());
         this.initialized = true;
      }
   }

   public List<ThermalPass> getPasses() {
      return Collections.unmodifiableList(this.passes);
   }

   public PostProcessContext getContext() {
      return this.context;
   }

   private RenderTarget sized(RenderTarget target, String name, int width, int height) {
      if (target == null) {
         return new TextureTarget("fpvdrone thermal " + name, width, height, GpuFormat.RGBA8_UNORM, null);
      }
      if (target.width != width || target.height != height) {
         target.resize(width, height);
      }
      return target;
   }

   /**
    * Processes the main picture in place. {@code depth} is the scene depth (null if unusable) and
    * {@code projection} the level's projection matrix.
    */
   public void process(RenderTarget main, GpuTextureView depth, Matrix4f projection, boolean depthUsable) {
      if (!this.initialized || this.passes.isEmpty() || main.width <= 0 || main.height <= 0) {
         return;
      }
      this.ping = this.sized(this.ping, "ping", main.width, main.height);
      this.pong = this.sized(this.pong, "pong", main.width, main.height);
      this.snapshot = this.sized(this.snapshot, "snapshot", main.width, main.height);

      UniformManager uniforms = UniformManager.getInstance();
      this.context.setAmbientTemp(uniforms.getAmbientTemp());
      this.context.setAtmosphericAbsorption(uniforms.getAtmosphericAbsorption());
      this.context.setWeatherMod(uniforms.getWeatherMod());
      this.context.setFrameCount(uniforms.getFrameCount());
      this.context.setWidth(main.width);
      this.context.setHeight(main.height);
      this.context.setPalette(ThermalCameraState.getInstance().getPalette());
      this.context.setFpnReduction(NUCManager.getInstance().getFpnReduction());
      this.context.setDepth(depth);
      this.context.setUseDepth(depthUsable);
      projection.invert(this.context.getInvProjection());

      // 1.1.8 copied the main picture into a snapshot first, so the first pass never reads what it writes.
      FullscreenQuad.blit(main, this.snapshot);
      RenderTarget in = this.snapshot;
      RenderTarget out = this.ping;
      for (int i = 0; i < this.passes.size(); i++) {
         boolean last = i == this.passes.size() - 1;
         RenderTarget target = last ? main : out;
         try {
            this.passes.get(i).apply(in, target, this.context);
         } catch (RuntimeException e) {
            LOGGER.error("[thermal] pass {} failed", this.passes.get(i).getName(), e);
            FullscreenQuad.blit(in, target);
         }
         if (!last) {
            in = target;
            out = target == this.ping ? this.pong : this.ping;
         }
      }
   }

   public void cleanup() {
      for (ThermalPass pass : this.passes) {
         pass.cleanup();
      }
      this.passes.clear();
      for (RenderTarget t : new RenderTarget[]{this.ping, this.pong, this.snapshot}) {
         if (t != null) {
            t.destroyBuffers();
         }
      }
      this.ping = null;
      this.pong = null;
      this.snapshot = null;
      this.initialized = false;
   }

   public boolean isReady() {
      return this.initialized;
   }
}
