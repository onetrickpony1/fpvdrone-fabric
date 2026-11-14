package com.fpvdrone.client.map;

import com.fpvdrone.entity.TerrainProbe;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.MapColor.Brightness;

public final class BlockMapColors {
   private static final Map<BlockState, Integer> v5suI = new ConcurrentHashMap<>();
   private static final Map<BlockState, Integer> ibzfxic = new ConcurrentHashMap<>();
   private static final Map<Identifier, Integer> wuhB = new ConcurrentHashMap<>();
   private static final ThreadLocal<RandomSource> dOfFwvo = ThreadLocal.withInitial(RandomSource::create);
   private static volatile int rsDd50tfV = 1;

   private BlockMapColors() {
   }

   public static int v5suI() {
      return rsDd50tfV;
   }

   public static void ibzfxic() {
      rsDd50tfV++;
      v5suI.clear();
      ibzfxic.clear();
      wuhB.clear();
      TerrainProbe.v5suI();
      MapTerrainSampler.ibzfxic();
   }

   public static int v5suI(BlockState var0, BlockAndTintGetter var1, BlockPos var2) {
      if (var0 == null) {
         return 0;
      }

      int var3 = wuhB(var0);
      int var4 = var3 >> 24 & 0xFF;
      if (var4 == 0) {
         return 0;
      }

      int var5 = var3 >> 16 & 0xFF;
      int var6 = var3 >> 8 & 0xFF;
      int var7 = var3 & 0xFF;
      Integer var8 = ibzfxic.get(var0);
      int var9 = var8 == null ? -1 : var8;
      if (var9 >= 0) {
         int var10 = -1;

         try {
            // 26.3: BlockColors.getColor(state, level, pos, index) became per-layer tint sources
            BlockTintSource var13 = Minecraft.getInstance().getBlockColors().getTintSource(var0, var9);
            var10 = var13 == null ? -1 : var13.colorInWorld(var0, var1, var2) & 0xFFFFFF;
         } catch (Exception var12) {
         }

         if (var10 != -1) {
            var5 = var5 * (var10 >> 16 & 0xFF) / 255;
            var6 = var6 * (var10 >> 8 & 0xFF) / 255;
            var7 = var7 * (var10 & 0xFF) / 255;
         }
      }

      return var4 << 24 | var5 << 16 | var6 << 8 | var7;
   }

   public static int ibzfxic(BlockState var0, BlockAndTintGetter var1, BlockPos var2) {
      if (var0 == null) {
         return 0;
      }

      MapColor var3;
      try {
         var3 = var0.getMapColor(var1, var2);
      } catch (Exception var5) {
         var3 = var0.getMapColor(null, null);
      }

      return var3 != null && var3 != MapColor.NONE ? 0xFF000000 | ARGB.toABGR(var3.calculateARGBColor(Brightness.NORMAL)) : 0;
   }

   public static void v5suI(BlockState var0) {
      if (var0 != null) {
         wuhB(var0);
      }
   }

   public static boolean ibzfxic(BlockState var0) {
      return var0 == null || v5suI.containsKey(var0);
   }

   private static int wuhB(BlockState var0) {
      Integer var1 = v5suI.get(var0);
      if (var1 != null) {
         return var1;
      }

      int var2 = dOfFwvo(var0);
      v5suI.put(var0, var2);
      return var2;
   }

   private static int dOfFwvo(BlockState var0) {
      Minecraft var1 = Minecraft.getInstance();
      BlockStateModel var3 = var1.getModelManager().getBlockStateModelSet().get(var0);
      TextureAtlasSprite var4 = null;
      int var5 = 0;
      RandomSource var6 = dOfFwvo.get();

      try {
         var6.setSeed(42L);
         List<BakedQuad> var7 = quads(var3, Direction.UP, var6);
         if (!var7.isEmpty()) {
            var4 = var7.get(0).materialInfo().sprite();
            var5 = var7.get(0).materialInfo().tintIndex();
         } else {
            var6.setSeed(42L);
            List<BakedQuad> var8 = quads(var3, null, var6);
            if (!var8.isEmpty()) {
               var4 = var8.get(0).materialInfo().sprite();
               var5 = var8.get(0).materialInfo().tintIndex();
            }
         }
      } catch (Exception var10) {
      }

      if (var4 == null) {
         try {
            var4 = var3.particleMaterial().sprite();
         } catch (Exception var9) {
            var4 = null;
         }

         var5 = 0;
      }

      ibzfxic.put(var0, var5);
      return var4 == null ? 0 : v5suI(var4.contents().name());
   }

   /** 26.3: a block model is a list of parts; the 1.20 getQuads(state, side, random) is the parts' quads in order. */
   private static List<BakedQuad> quads(BlockStateModel model, Direction side, RandomSource random) {
      List<BlockStateModelPart> parts = new ArrayList<>();
      model.collectParts(random, parts);
      List<BakedQuad> out = new ArrayList<>();
      for (BlockStateModelPart part : parts) {
         out.addAll(part.getQuads(side));
      }
      return out;
   }

   private static int v5suI(Identifier var0) {
      Integer var1 = wuhB.get(var0);
      if (var1 != null) {
         return var1;
      }

      int var2 = 0;
      Identifier var3 = Identifier.fromNamespaceAndPath(var0.getNamespace(), "textures/" + var0.getPath() + ".png");
      var var4 = Minecraft.getInstance().getResourceManager().getResource(var3);
      if (var4.isPresent()) {
         try (InputStream var5 = ((Resource)var4.get()).open()) {
            NativeImage var6 = NativeImage.read(var5);

            try {
               long var7 = 0L;
               long var9 = 0L;
               long var11 = 0L;
               long var13 = 0L;
               int var15 = 0;
               int var16 = var6.getWidth();
               int var17 = var6.getHeight();
               int var18 = Math.min(var16, var17);
               int var19 = Math.max(1, var18 / 16);

               for (int var20 = 0; var20 < var18; var20 += var19) {
                  for (int var21 = 0; var21 < var16; var21 += var19) {
                     int var22 = ARGB.toABGR(var6.getPixel(var21, var20));
                     int var23 = var22 >> 24 & 0xFF;
                     if (var23 >= 16) {
                        var11 += var22 >> 16 & 0xFF;
                        var9 += var22 >> 8 & 0xFF;
                        var7 += var22 & 0xFF;
                        var13 += var23;
                        var15++;
                     }
                  }
               }

               if (var15 > 0) {
                  var2 = (int)(var13 / var15) << 24 | (int)(var7 / var15) << 16 | (int)(var9 / var15) << 8 | (int)(var11 / var15);
               }
            } catch (Throwable var26) {
               if (var6 != null) {
                  try {
                     var6.close();
                  } catch (Throwable var25) {
                     var26.addSuppressed(var25);
                  }
               }

               throw var26;
            }

            if (var6 != null) {
               var6.close();
            }
         } catch (Exception var28) {
            var2 = 0;
         }
      }

      wuhB.put(var0, var2);
      return var2;
   }
}
