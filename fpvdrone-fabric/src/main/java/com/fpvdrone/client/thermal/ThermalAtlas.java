package com.fpvdrone.client.thermal;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1.1.8 ThermalAtlasGenerator: a texture the size of the block atlas in which every block sprite is filled with that
 * block's {@link ThermalProperties}. The thermal chunk shader samples it instead of the block atlas, so each block
 * shows its own temperature. Texels that are transparent in the real texture stay empty (so cut-out blocks keep their
 * shape), except for blocks drawn solid, doors and glass, which are filled whole.
 * <p>
 * 1.1.8 read the atlas alpha back from the GPU; 26.3 keeps each sprite's original image, which is used instead.
 */
public final class ThermalAtlas {
   private static final Logger LOGGER = LoggerFactory.getLogger(ThermalAtlas.class);
   private static DynamicTexture thermalTexture;
   private static GpuTexture sourceAtlasTexture;

   private ThermalAtlas() {
   }

   /** The thermal atlas view, made (again) when the block atlas changed. Null if it could not be made. */
   public static GpuTextureView ensure() {
      Minecraft mc = Minecraft.getInstance();
      AbstractTexture blocks = mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
      if (!(blocks instanceof TextureAtlas atlas)) {
         return null;
      }
      GpuTexture blockTexture;
      try {
         blockTexture = atlas.getTexture();
      } catch (IllegalStateException e) {
         return null;
      }
      if (thermalTexture == null || sourceAtlasTexture != blockTexture || blockTexture.isClosed()) {
         generate(atlas, blockTexture);
      }
      return thermalTexture != null ? thermalTexture.getTextureView() : null;
   }

   public static DynamicTexture texture() {
      return thermalTexture;
   }

   public static void cleanup() {
      if (thermalTexture != null) {
         thermalTexture.close();
         thermalTexture = null;
      }
      sourceAtlasTexture = null;
   }

   private static void generate(TextureAtlas atlas, GpuTexture blockTexture) {
      cleanup();
      int width = blockTexture.getWidth(0);
      int height = blockTexture.getHeight(0);
      if (width <= 0 || height <= 0) {
         FallbackManager.forceFallback("Invalid block atlas dimensions");
         return;
      }
      long start = System.nanoTime();
      ThermalRegistry.registerAll();
      NativeImage image = new NativeImage(NativeImage.Format.RGBA, width, height, false);
      image.fillRect(0, 0, width, height, 0); // ThermalRegistry.ATLAS_DEFAULT is all zero
      RandomSource random = RandomSource.create(42L);
      BlockStateModelSet models = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
      int sprites = 0;
      for (Block block : BuiltInRegistries.BLOCK) {
         Set<TextureAtlasSprite> blockSprites = new HashSet<>();
         boolean drawnSolid = false;
         for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            drawnSolid |= collectSprites(models.get(state), random, blockSprites);
         }
         if (blockSprites.isEmpty()) {
            continue;
         }
         ThermalProperties props = ThermalRegistry.get(block);
         boolean glass = props.materialType() >= 0.24F && props.materialType() <= 0.26F;
         boolean door = block instanceof DoorBlock || block instanceof TrapDoorBlock;
         boolean fillWhole = glass || door || drawnSolid;
         int color = props.encodeABGR();
         for (TextureAtlasSprite sprite : blockSprites) {
            fillSprite(image, sprite, color, fillWhole);
            sprites++;
         }
      }
      fillFluid(image, Fluids.LAVA, ThermalRegistry.get(Blocks.LAVA));
      fillFluid(image, Fluids.WATER, ThermalRegistry.get(Blocks.WATER));
      fillFluid(image, Fluids.FLOWING_LAVA, ThermalRegistry.get(Blocks.LAVA));
      fillFluid(image, Fluids.FLOWING_WATER, ThermalRegistry.get(Blocks.WATER));
      thermalTexture = new DynamicTexture(() -> "fpvdrone thermal atlas", image);
      sourceAtlasTexture = blockTexture;
      LOGGER.info("[thermal] thermal atlas {}x{}: {} sprites in {} ms", width, height, sprites, (System.nanoTime() - start) / 1000000L);
   }

   /** Adds the block-atlas sprites of a model; true if any quad is drawn in the solid layer. */
   private static boolean collectSprites(BlockStateModel model, RandomSource random, Set<TextureAtlasSprite> out) {
      boolean solid = false;
      try {
         addSprite(model.particleMaterial().sprite(), out);
         List<BlockStateModelPart> parts = new ArrayList<>();
         random.setSeed(42L);
         model.collectParts(random, parts);
         for (BlockStateModelPart part : parts) {
            for (Direction dir : Direction.values()) {
               solid |= addQuads(part.getQuads(dir), out);
            }
            solid |= addQuads(part.getQuads(null), out);
         }
      } catch (Exception e) {
         // 1.1.8 skipped models that fail the same way.
      }
      return solid;
   }

   private static boolean addQuads(List<BakedQuad> quads, Set<TextureAtlasSprite> out) {
      boolean solid = false;
      if (quads != null) {
         for (BakedQuad quad : quads) {
            if (addSprite(quad.materialInfo().sprite(), out) && quad.materialInfo().layer() == ChunkSectionLayer.SOLID) {
               solid = true;
            }
         }
      }
      return solid;
   }

   private static boolean addSprite(TextureAtlasSprite sprite, Set<TextureAtlasSprite> out) {
      if (sprite != null && TextureAtlas.LOCATION_BLOCKS.equals(sprite.atlasLocation())) {
         out.add(sprite);
         return true;
      }
      return false;
   }

   private static void fillFluid(NativeImage image, Fluid fluid, ThermalProperties props) {
      try {
         FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
         int color = props.encodeABGR();
         for (var material : new net.minecraft.client.resources.model.sprite.Material.Baked[]{model.stillMaterial(), model.flowingMaterial()}) {
            if (material != null && TextureAtlas.LOCATION_BLOCKS.equals(material.sprite().atlasLocation())) {
               fillSprite(image, material.sprite(), color, true);
            }
         }
      } catch (Exception e) {
         LOGGER.debug("[thermal] no fluid model for {}", fluid, e);
      }
   }

   private static void fillSprite(NativeImage image, TextureAtlasSprite sprite, int abgr, boolean fillWhole) {
      int w = sprite.contents().width();
      int h = sprite.contents().height();
      // The sprite's padding (edge texels repeated around it, for mipmaps) is filled like the nearest edge texel.
      int pad = Math.max(0, Math.round(sprite.getU0() * image.getWidth()) - sprite.getX());
      for (int y = -pad; y < h + pad; y++) {
         int py = sprite.getY() + pad + y;
         if (py < 0 || py >= image.getHeight()) {
            continue;
         }
         for (int x = -pad; x < w + pad; x++) {
            int px = sprite.getX() + pad + x;
            if (px < 0 || px >= image.getWidth()) {
               continue;
            }
            if (!fillWhole) {
               boolean transparent;
               try {
                  transparent = sprite.contents().isTransparent(0, Math.clamp(x, 0, w - 1), Math.clamp(y, 0, h - 1));
               } catch (RuntimeException e) {
                  transparent = false;
               }
               if (transparent) {
                  continue;
               }
            }
            image.setPixelABGR(px, py, abgr);
         }
      }
   }
}
