package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1.1.8 ThermalRenderHook: ties the thermal view into the frame. Before the world is drawn it prepares the thermal
 * atlas and shader values; while the world is drawn, chunks and entities use the thermal pipelines (see the
 * Thermal*Mixin classes); afterwards the post-processing chain turns the picture into the thermal image.
 * The sky is replaced by a near-black clear (1.1.8 cleared the colour after the sky stage).
 */
public final class ThermalRenderHook {
   private static final Logger LOGGER = LoggerFactory.getLogger(ThermalRenderHook.class);
   /** 1.1.8 AFTER_SKY clear colour. */
   public static final Vector4f SKY_CLEAR = new Vector4f(0.02F, 0.02F, 0.02F, 1.0F);
   private static final int TERRAIN_UBO_SIZE = 32;
   private static boolean renderingWorld = false;
   private static GpuTextureView terrainAtlas;
   private static GpuBuffer terrainUbo;
   private static Method irisShaderPackInUse;
   private static Object irisApi;
   private static boolean irisChecked;

   private ThermalRenderHook() {
   }

   public static void init() {
      ThermalPipelines.init();
      FootprintDecalRenderer.init();
      ThermalKeys.init();
      ClientTickEvents.END_CLIENT_TICK.register(ThermalRenderHook::onClientTick);
      // 1.1.8 ForgeEvents.LoggingIn: the thermal atlas is rebuilt for the new world.
      ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> mc.execute(ThermalAtlas::cleanup));
   }

   public static boolean isActive() {
      return ThermalCameraState.getInstance().isEnabled();
   }

   /** True between the start and end of the world drawing of a thermal frame (1.1.8 rxYnaa). */
   public static boolean isRenderingWorld() {
      return renderingWorld;
   }

   /** 1.1.8 onClientTick (END). */
   private static void onClientTick(Minecraft mc) {
      if (isActive() && mc.level != null && mc.player != null && !mc.isPaused()) {
         UniformManager.getInstance().tick(mc.level);
         FootprintSystem.getInstance().tick(mc.level);
         NUCManager.getInstance().tick();
      }
   }

   /** 1.1.8 onBeforeWorldRender. */
   public static void onBeforeWorldRender() {
      renderingWorld = false;
      terrainAtlas = null;
      if (!isActive()) {
         return;
      }
      try {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null) {
            UniformManager.getInstance().updatePerFrame(mc.level);
         }
         if (FallbackManager.isVanillaAtlas()) {
            terrainAtlas = ThermalAtlas.ensure();
            if (terrainAtlas == null) {
               FallbackManager.forceFallback("no thermal atlas");
            } else {
               writeTerrainUniforms();
            }
         }
         ThermalPostProcessor.getInstance().init();
         renderingWorld = true;
      } catch (RuntimeException e) {
         LOGGER.error("[thermal] could not prepare the thermal frame", e);
         FallbackManager.forceFallback("prepare failed: " + e);
         terrainAtlas = null;
         renderingWorld = true;
      }
   }

   /** 1.1.8 onAfterWorldRender: the post-processing chain over the finished world picture. */
   public static void onAfterWorldRender(Matrix4f projection) {
      if (!renderingWorld) {
         return;
      }
      renderingWorld = false;
      terrainAtlas = null;
      try {
         RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
         ThermalPostProcessor post = ThermalPostProcessor.getInstance();
         if (main != null && post.isReady()) {
            post.process(main, main.getDepthTextureView(), projection, !isIrisShaderPackInUse());
         }
      } catch (RuntimeException e) {
         LOGGER.error("[thermal] post-processing failed", e);
      }
   }

   /** The thermal atlas for vanilla chunks this frame, or null to draw them normally. */
   public static GpuTextureView terrainAtlas() {
      return renderingWorld ? terrainAtlas : null;
   }

   /** Binds the ThermalTerrain values for the thermal chunk pipelines. */
   public static void bindTerrainUniforms(RenderPass pass) {
      if (terrainUbo != null) {
         pass.setUniform(ThermalPipelines.TERRAIN_UBO, terrainUbo);
      }
   }

   /** Entities draw hot unless an Iris shader pack is drawing them (its own programs; left alone). */
   public static boolean overrideEntities() {
      return renderingWorld && !isIrisShaderPackInUse();
   }

   private static void writeTerrainUniforms() {
      if (terrainUbo == null) {
         terrainUbo = RenderSystem.getDevice().createBuffer(() -> "fpvdrone thermal terrain", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, TERRAIN_UBO_SIZE);
      }
      UniformManager u = UniformManager.getInstance();
      try (MemoryStack stack = MemoryStack.stackPush()) {
         ByteBuffer data = Std140Builder.onStack(stack, TERRAIN_UBO_SIZE)
            .putFloat(u.getGameTime())
            .putFloat(u.getBiomeTemp())
            .putFloat(u.getAmbientTemp())
            .putFloat(u.getWeatherMod())
            .putInt(u.getFrameCount())
            .get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(terrainUbo.slice(), data);
      }
   }

   /** Iris with a shader pack on draws the world its own way; the scene depth is then not ours to read. */
   public static boolean isIrisShaderPackInUse() {
      if (!irisChecked) {
         irisChecked = true;
         try {
            Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            irisApi = api.getMethod("getInstance").invoke(null);
            irisShaderPackInUse = api.getMethod("isShaderPackInUse");
         } catch (ReflectiveOperationException | LinkageError e) {
            irisApi = null;
         }
      }
      if (irisApi == null) {
         return false;
      }
      try {
         return (Boolean)irisShaderPackInUse.invoke(irisApi);
      } catch (ReflectiveOperationException | RuntimeException e) {
         return false;
      }
   }
}
