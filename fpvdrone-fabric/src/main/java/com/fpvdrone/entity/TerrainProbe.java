package com.fpvdrone.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class TerrainProbe {
   private static final int v5suI = 32;
   private static final double ibzfxic = 0.25;
   private static final Map<BlockState, Boolean> wuhB = new ConcurrentHashMap<>();
   private static final Map<BlockState, Boolean> dOfFwvo = new ConcurrentHashMap<>();

   private TerrainProbe() {
   }

   public static void v5suI() {
      wuhB.clear();
      dOfFwvo.clear();
   }

   public static int v5suI(LevelReader var0, int var1, int var2) {
      int var3 = var0.getMinY();
      int var4 = var0.getHeight(Types.WORLD_SURFACE, var1, var2) - 1;
      if (var4 < var3) {
         return var3;
      }

      int var5 = Math.max(var3, var4 - 32);
      MutableBlockPos var6 = new MutableBlockPos();

      for (int var7 = var4; var7 >= var5; var7--) {
         BlockState var8 = var0.getBlockState(var6.set(var1, var7, var2));
         if (!var8.isAir() && (!var8.getFluidState().isEmpty() || !ibzfxic(var8))) {
            return var7;
         }
      }

      return var4;
   }

   public static int v5suI(ChunkAccess var0, int var1, int var2, MutableBlockPos var3) {
      int var4 = var0.getMinY();
      int var5 = Math.min(var0.getHeight(Types.WORLD_SURFACE, var1, var2), var0.getMaxY() - 1);
      if (var5 < var4) {
         return var4;
      }

      int var6 = Math.max(var4, var5 - 32);

      for (int var7 = var5; var7 >= var6; var7--) {
         BlockState var8 = var0.getBlockState(var3.set(var1, var7, var2));
         if (!var8.isAir() && (!var8.getFluidState().isEmpty() || !ibzfxic(var8))) {
            return var7;
         }
      }

      return var5;
   }

   public static int v5suI(ChunkAccess var0, int var1, int var2) {
      MutableBlockPos var3 = new MutableBlockPos();
      return v5suI((var2x, var3x, var4) -> var0.getBlockState(var3.set(var2x, var3x, var4)), var0.getMinY(), var0.getMaxY(), var1, var2);
   }

   public static int v5suI(TerrainProbe.KKeMvpuBgEY var0, int var1, int var2, int var3, int var4) {
      int var5 = var2 - 1;
      int var6 = var1 - 1;

      for (int var7 = var5; var7 >= var1; var7--) {
         if (!var0.blockAt(var3, var7, var4).isAir()) {
            var6 = var7;
            break;
         }
      }

      if (var6 < var1) {
         return var1;
      }

      int var10 = Math.max(var1, var6 - 32);

      for (int var8 = var6; var8 >= var10; var8--) {
         BlockState var9 = var0.blockAt(var3, var8, var4);
         if (!var9.isAir() && (!var9.getFluidState().isEmpty() || !ibzfxic(var9))) {
            return var8;
         }
      }

      return var6;
   }

   public static int ibzfxic(LevelReader var0, int var1, int var2) {
      int var3 = v5suI(var0, var1, var2);
      BlockState var4 = var0.getBlockState(new BlockPos(var1, var3, var2));
      return v5suI(var4) ? var3 - 1 : var3;
   }

   public static boolean v5suI(BlockState var0) {
      if (var0 != null && !var0.isAir()) {
         Boolean var1 = wuhB.get(var0);
         if (var1 != null) {
            return var1;
         }

         boolean var2;
         try {
            VoxelShape var3 = var0.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            var2 = !var3.isEmpty() && var3.max(Axis.Y) < 0.25;
         } catch (Throwable var4) {
            var2 = false;
         }

         wuhB.put(var0, var2);
         return var2;
      } else {
         return false;
      }
   }

   private static boolean ibzfxic(BlockState var0) {
      Boolean var1 = dOfFwvo.get(var0);
      if (var1 != null) {
         return var1;
      }

      boolean var2;
      try {
         var2 = var0.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
      } catch (Throwable var4) {
         var2 = false;
      }

      dOfFwvo.put(var0, var2);
      return var2;
   }

   @FunctionalInterface
   public interface KKeMvpuBgEY {
      BlockState blockAt(int var1, int var2, int var3);
   }
}
