package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.ShaderType;
import com.mojang.renderpearl.api.pipeline.UniformType;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The 26.3 stand-ins for 1.1.8's thermal ShaderInstances (ThermalRenderHook.registerShaders and the mixins that
 * returned them from GameRenderer's shader getters): pipelines for thermal chunks, thermal entities and the post
 * passes. 1.1.8's sky_thermal and cloud_thermal shaders were registered but never used, so they are not ported.
 */
public final class ThermalPipelines {
   private static final Logger LOGGER = LoggerFactory.getLogger(ThermalPipelines.class);
   public static final String TERRAIN_UBO = "ThermalTerrain";
   public static final String POST_UBO = "ThermalPost";
   public static final BindGroupLayout TERRAIN_INFO = BindGroupLayout.builder().withUniform(TERRAIN_UBO, UniformType.UNIFORM_BUFFER).build();

   // 1.1.8 rendertype_solid_thermal / rendertype_cutout_thermal / rendertype_translucent_thermal
   private static final RenderPipeline SOLID = terrain("solid", false, null, null);
   private static final RenderPipeline SOLID_MULTIDRAW = terrain("solid", true, null, null);
   private static final RenderPipeline CUTOUT = terrain("cutout", false, "THERMAL_CUTOUT", null);
   private static final RenderPipeline CUTOUT_MULTIDRAW = terrain("cutout", true, "THERMAL_CUTOUT", null);
   private static final RenderPipeline TRANSLUCENT = terrain("translucent", false, "THERMAL_TRANSLUCENT", new ColorTargetState(BlendFunction.TRANSLUCENT));
   private static final RenderPipeline TRANSLUCENT_MULTIDRAW = terrain("translucent", true, "THERMAL_TRANSLUCENT", new ColorTargetState(BlendFunction.TRANSLUCENT));

   // Post passes (1.1.8 shaders/core/post/*).
   public static final RenderPipeline FALLBACK_THERMAL = post("fallback_thermal", true, true, false);
   public static final RenderPipeline ATMOSPHERIC_FOG = post("atmospheric_fog", false, true, false);
   public static final RenderPipeline DEPTH_OF_FIELD = post("depth_of_field", true, true, false);
   public static final RenderPipeline NETD_NOISE = post("netd_noise", false, true, false);
   public static final RenderPipeline FPN = post("fpn", false, true, false);
   public static final RenderPipeline PALETTE_LUT = post("palette_lut", false, false, true);
   public static final RenderPipeline VIGNETTE_DISTORTION = post("vignette_distortion", false, false, false);

   private static final Identifier ENTITY_SHADER = Identifier.withDefaultNamespace("core/entity");
   private static final Identifier THERMAL_ENTITY_SHADER = Identifier.fromNamespaceAndPath("fpvdrone", "core/thermal_entity");
   private static final Map<RenderPipeline, RenderPipeline> ENTITY_VARIANTS = new IdentityHashMap<>();

   private ThermalPipelines() {
   }

   /** Loading this class registers the pipelines (so resource reloads compile them); call at client setup. */
   public static void init() {
   }

   private static RenderPipeline terrain(String name, boolean multidraw, String define, ColorTargetState blend) {
      RenderPipeline.Builder builder = RenderPipeline.builder(multidraw ? RenderPipelines.MULTIDRAW_TERRAIN_SNIPPET : RenderPipelines.TERRAIN_SNIPPET)
         .withLocation(Identifier.fromNamespaceAndPath("fpvdrone", "pipeline/thermal_" + name + "_terrain" + (multidraw ? "_multidraw" : "")))
         .withVertexShader(Identifier.fromNamespaceAndPath("fpvdrone", "core/thermal_terrain"))
         .withFragmentShader(Identifier.fromNamespaceAndPath("fpvdrone", "core/thermal_terrain"))
         .withBindGroupLayout(TERRAIN_INFO)
         .withColorTargetState(blend != null ? blend : ColorTargetState.DEFAULT);
      if (define != null) {
         builder.withShaderDefine(define);
      }
      return RenderPipelines.register(builder.build());
   }

   private static RenderPipeline post(String name, boolean depth, boolean ubo, boolean lut) {
      BindGroupLayout.Builder layout = BindGroupLayout.builder().withUniform("InSampler", UniformType.COMBINED_IMAGE_SAMPLER);
      if (depth) {
         layout.withUniform("DepthSampler", UniformType.COMBINED_IMAGE_SAMPLER);
      }
      if (lut) {
         layout.withUniform("LutSampler", UniformType.COMBINED_IMAGE_SAMPLER);
      }
      if (ubo) {
         layout.withUniform(POST_UBO, UniformType.UNIFORM_BUFFER);
      }
      return RenderPipelines.register(
         RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("fpvdrone", "pipeline/thermal_" + name))
            .withVertexShader("core/screenquad")
            .withFragmentShader(Identifier.fromNamespaceAndPath("fpvdrone", "post/" + name))
            .withBindGroupLayout(layout.build())
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build()
      );
   }

   /** True if the pipeline compiled (a broken shader must not take the game down). */
   public static boolean usable(RenderPipeline pipeline) {
      try {
         CompiledRenderPipeline compiled = RenderSystem.getCompiledPipelineNullable(pipeline);
         return compiled != null && !compiled.isClosed();
      } catch (RuntimeException e) {
         return false;
      }
   }

   /** 1.1.8 RenderTypeShaderMixin (getRendertypeSolid/Cutout/TranslucentShader): the thermal pipeline of a chunk layer. */
   public static RenderPipeline terrainFor(ChunkSectionLayer layer, boolean multidraw) {
      RenderPipeline pipeline = switch (layer) {
         case SOLID -> multidraw ? SOLID_MULTIDRAW : SOLID;
         case CUTOUT -> multidraw ? CUTOUT_MULTIDRAW : CUTOUT;
         case TRANSLUCENT -> multidraw ? TRANSLUCENT_MULTIDRAW : TRANSLUCENT;
      };
      if (usable(pipeline)) {
         return pipeline;
      }
      FallbackManager.forceFallback("thermal chunk pipeline " + pipeline.getLocation() + " did not compile");
      return layer.pipeline(multidraw);
   }

   /**
    * 1.1.8 RenderTypeShaderMixin (getRendertypeEntity*Shader): entities draw with the thermal entity shader. In 26.3
    * blending, culling and depth belong to the pipeline, so each vanilla entity pipeline gets a thermal twin with the
    * same states and only the fragment shader changed. Other pipelines are returned unchanged.
    */
   public static RenderPipeline entityFor(RenderPipeline pipeline) {
      RenderPipeline variant = ENTITY_VARIANTS.get(pipeline);
      if (variant == null) {
         variant = makeEntityVariant(pipeline);
         ENTITY_VARIANTS.put(pipeline, variant);
      }
      if (variant != pipeline && !usable(variant)) {
         LOGGER.warn("[thermal] entity pipeline {} did not compile; entities keep the normal look", variant.getLocation());
         ENTITY_VARIANTS.put(pipeline, pipeline);
         return pipeline;
      }
      return variant;
   }

   private static RenderPipeline makeEntityVariant(RenderPipeline pipeline) {
      Map<ShaderType, Identifier> shaders = pipeline.getShaders();
      if (pipeline.getVertexFormatBinding(0) != DefaultVertexFormat.ENTITY
         || !ENTITY_SHADER.equals(shaders.get(ShaderType.VERTEX))
         || !ENTITY_SHADER.equals(shaders.get(ShaderType.FRAGMENT))) {
         return pipeline;
      }
      ShaderDefines defines = pipeline.getShaderDefines();
      for (String key : defines.values().keySet()) {
         if (key.startsWith("OIT")) {
            return pipeline;
         }
      }
      for (String flag : defines.flags()) {
         if (flag.startsWith("OIT")) {
            return pipeline;
         }
      }
      try {
         List<ColorTargetState> targets = pipeline.getColorTargetStates();
         ColorTargetState[] targetArray = new ColorTargetState[8];
         for (int i = 0; i < targets.size() && i < 8; i++) {
            targetArray[i] = targets.get(i);
         }
         List<VertexFormat> formats = pipeline.getVertexFormatBindings();
         VertexFormat[] formatArray = new VertexFormat[16];
         for (int i = 0; i < formats.size() && i < 16; i++) {
            formatArray[i] = formats.get(i);
         }
         RenderPipeline.Snippet copy = new RenderPipeline.Snippet(
            shaders,
            Optional.of(defines),
            Optional.of(pipeline.getBindGroupLayouts()),
            targetArray,
            Math.min(targets.size(), 8),
            Optional.ofNullable(pipeline.getDepthStencilState()),
            Optional.of(pipeline.getPolygonMode()),
            Optional.of(pipeline.isCull()),
            formatArray,
            Optional.of(pipeline.getPrimitiveTopology()),
            pipeline.pushConstantSize()
         );
         Identifier location = pipeline.getLocation();
         return RenderPipeline.builder(copy)
            .withLocation(Identifier.fromNamespaceAndPath("fpvdrone", "thermal/" + location.getNamespace() + "/" + location.getPath()))
            .withFragmentShader(THERMAL_ENTITY_SHADER)
            .build();
      } catch (RuntimeException e) {
         LOGGER.warn("[thermal] could not make a thermal twin of {}", pipeline.getLocation(), e);
         return pipeline;
      }
   }
}
