package com.fpvdrone.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

final class EntityChunkForcer {
   private EntityChunkForcer() {
   }

   static ChunkPos v5suI(Entity var0, @Nullable ChunkPos var1) {
      if (var0.level() instanceof ServerLevel var2) {
         ChunkPos var4 = ChunkPos.containing(var0.blockPosition());
         if (var1 != null && var1.equals(var4)) {
            return var1;
         }

         if (var1 != null) {
            DroneChunkTickets.forceChunk(var2, var1.x(), var1.z(), false, true);
         }

         DroneChunkTickets.forceChunk(var2, var4.x(), var4.z(), true, true);
         return var4;
      } else {
         return var1;
      }
   }

   @Nullable
   static ChunkPos ibzfxic(Entity var0, @Nullable ChunkPos var1) {
      if (var1 != null && var0.level() instanceof ServerLevel var2) {
         DroneChunkTickets.forceChunk(var2, var1.x(), var1.z(), false, true);
      }

      return null;
   }
}
