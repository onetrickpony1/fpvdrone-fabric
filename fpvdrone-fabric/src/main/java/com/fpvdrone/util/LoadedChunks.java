package com.fpvdrone.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public final class LoadedChunks {
   private LoadedChunks() {
   }

   private static ChunkAccess wuhB(Level var0, int var1, int var2) {
      LevelChunk var3 = var0.getChunkSource().getChunk(var1 >> 4, var2 >> 4, false);
      return var3 != null && var3.getPersistedStatus() == ChunkStatus.FULL ? var3 : null;
   }

   public static boolean v5suI(Level var0, int var1, int var2) {
      return wuhB(var0, var1, var2) != null;
   }

   public static boolean v5suI(Level var0, ChunkPos var1) {
      return v5suI(var0, var1.getMinBlockX(), var1.getMinBlockZ());
   }

   public static int ibzfxic(Level var0, int var1, int var2) {
      return v5suI(var0, Types.MOTION_BLOCKING, var1, var2);
   }

   public static int v5suI(Level var0, Types var1, int var2, int var3) {
      return wuhB(var0, var2, var3) == null ? Integer.MIN_VALUE : var0.getHeight(var1, var2, var3);
   }

   public static BlockState v5suI(Level var0, BlockPos var1) {
      ChunkAccess var2 = wuhB(var0, var1.getX(), var1.getZ());
      return var2 == null ? null : var2.getBlockState(var1);
   }
}
