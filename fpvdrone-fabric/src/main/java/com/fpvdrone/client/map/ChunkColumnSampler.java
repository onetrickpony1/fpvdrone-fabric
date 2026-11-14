package com.fpvdrone.client.map;

import com.fpvdrone.entity.TerrainProbe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class ChunkColumnSampler {
   private static final int v5suI = 24;
   private static final int ibzfxic = Block.getId(Blocks.AIR.defaultBlockState());
   private static final int wuhB = 8;
   private static final ChunkColumnSampler.Ol3TEDSog dOfFwvo = new ChunkColumnSampler.Ol3TEDSog(null, null, false);
   private static final ChunkColumnSampler.Ol3TEDSog rsDd50tfV = new ChunkColumnSampler.Ol3TEDSog(null, null, true);

   private ChunkColumnSampler() {
   }

   public static ChunkColumnSampler.Ol3TEDSog v5suI(Level var0, ChunkAccess var1, int var2, int var3) {
      if (!var1.hasPrimedHeightmap(Types.WORLD_SURFACE)) {
         return rsDd50tfV;
      }

      int var4 = var2 << 4;
      int var5 = var3 << 4;
      int var6 = var1.getMinY();
      int var7 = BlockMapColors.v5suI();
      int[] var8 = new int[256];
      short[] var9 = new short[256];
      int[] var10 = new int[256];
      int[] var11 = new int[256];
      Registry var12 = var0.registryAccess().lookupOrThrow(Registries.BIOME);
      MutableBlockPos var13 = new MutableBlockPos();
      var var14 = new ArrayList(0);
      boolean var15 = false;

      for (int var16 = 0; var16 < 16; var16++) {
         for (int var17 = 0; var17 < 16; var17++) {
            int var18 = var16 * 16 + var17;
            int var19 = var4 + var17;
            int var20 = var5 + var16;
            int var21 = TerrainProbe.v5suI(var1, var19, var20, var13);
            BlockState var22 = var1.getBlockState(var13.set(var19, var21, var20));
            var9[var18] = (short)(TerrainProbe.v5suI(var22) ? var21 - 1 : var21);
            if (var21 > var6) {
               var15 = true;
            }

            var11[var18] = var12.getId((Biome)var1.getNoiseBiome(var19 >> 2, var21 >> 2, var20 >> 2).value());
            ChunkColumnSampler.KKeMvpuBgEY var23 = v5suI(var0, var1, var19, var20, var21, var6, var13, var14);
            if (var23 == null) {
               if (var14.size() >= 8) {
                  return new ChunkColumnSampler.Ol3TEDSog(null, var14, true);
               }
            } else {
               var8[var18] = var23.v5suI();
               var10[var18] = var23.ibzfxic();
            }
         }
      }

      if (!var14.isEmpty()) {
         return new ChunkColumnSampler.Ol3TEDSog(null, var14, true);
      } else {
         return var15 ? new ChunkColumnSampler.Ol3TEDSog(new ReconStore.Rdl0X5jeod(var8, var9, var10, var11, true, true, var7), null, false) : dOfFwvo;
      }
   }

   private static ChunkColumnSampler.KKeMvpuBgEY v5suI(
      Level var0, ChunkAccess var1, int var2, int var3, int var4, int var5, MutableBlockPos var6, List<BlockState> var7
   ) {
      int var8 = 0;
      int var9 = ibzfxic;
      int var10 = 0;

      while (true) {
         label40: {
            if (var10 < 16 && var4 - var10 >= var5) {
               int var11 = var4 - var10;
               BlockState var12 = var1.getBlockState(var6.set(var2, var11, var3));
               if (var12.isAir()) {
                  break label40;
               }

               if (!BlockMapColors.ibzfxic(var12)) {
                  var7.add(var12);
                  return null;
               }

               int var13 = BlockMapColors.v5suI(var12, tint(var0), var6);
               if ((var13 >>> 24 & 0xFF) < 24) {
                  var13 = BlockMapColors.ibzfxic(var12, tint(var0), var6);
               }

               if ((var13 >>> 24 & 0xFF) < 24) {
                  break label40;
               }

               var8 = var13;
               var9 = Block.getId(var12);
            }

            if ((var8 >>> 24 & 0xFF) < 24) {
               return new ChunkColumnSampler.KKeMvpuBgEY(-16777216, ibzfxic);
            }

            return new ChunkColumnSampler.KKeMvpuBgEY(var8, var9);
         }

         var10++;
      }
   }

   public static int v5suI(int var0, int var1, int var2, int var3, float var4, float var5, float var6) {
      return MapShading.v5suI(var0, var1, var2, var3, var4, var5, var6);
   }

   private static final class KKeMvpuBgEY {
      private final int v5suI;
      private final int ibzfxic;

      private KKeMvpuBgEY(int var1, int var2) {
         this.v5suI = var1;
         this.ibzfxic = var2;
      }


      public int v5suI() {

         return this.v5suI;

      }

   
      public int ibzfxic() {
         return this.ibzfxic;
      }
   }

   public static final class Ol3TEDSog {
      private final ReconStore.Rdl0X5jeod v5suI;
      private final List<BlockState> ibzfxic;
      private final boolean wuhB;

      public Ol3TEDSog(ReconStore.Rdl0X5jeod var1, List<BlockState> var2, boolean var3) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
      }


      public ReconStore.Rdl0X5jeod v5suI() {

         return this.v5suI;

      }

   
      public List<BlockState> ibzfxic() {
         return this.ibzfxic;
      }

      public boolean wuhB() {
         return this.wuhB;
      }
   }

   /** 26.3: only the client level is a BlockAndTintGetter; the sampler only ever runs on it. */
   private static net.minecraft.client.renderer.block.BlockAndTintGetter tint(net.minecraft.world.level.Level level) {
      return level instanceof net.minecraft.client.renderer.block.BlockAndTintGetter t ? t : net.minecraft.client.renderer.block.BlockAndTintGetter.EMPTY;
   }
}
