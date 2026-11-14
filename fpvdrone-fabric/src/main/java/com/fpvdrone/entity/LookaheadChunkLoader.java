package com.fpvdrone.entity;

import com.fpvdrone.data.Record_IukVkAm;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public final class LookaheadChunkLoader {
   public static final int v5suI = 2;
   public static final int ibzfxic = 6;
   private static final Map<UUID, Map<Long, Boolean>> wuhB = new ConcurrentHashMap<>();

   private LookaheadChunkLoader() {
   }

   public static void v5suI(ServerLevel var0, UUID var1, Vec3 var2, Vec3 var3) {
      v5suI(var0, var1, var2, var3, Record_IukVkAm.v5suI);
   }

   public static void v5suI(ServerLevel var0, UUID var1, Vec3 var2, Vec3 var3, Record_IukVkAm var4) {
      Map<Long, Boolean> var5 = v5suI(var2, var3, var4);
      Map<Long, Boolean> var6 = wuhB.getOrDefault(var1, Collections.emptyMap());

      for (Entry<?, ?> var8 : var6.entrySet()) {
         long var9 = (Long)var8.getKey();
         if (!var5.containsKey(var9)) {
            DroneChunkTickets.forceChunk(var0, ChunkPos.getX(var9), ChunkPos.getZ(var9), false, (Boolean)var8.getValue());
         }
      }

      for (Entry<?, ?> var16 : var5.entrySet()) {
         long var17 = (Long)var16.getKey();
         boolean var11 = (Boolean)var16.getValue();
         int var12 = ChunkPos.getX(var17);
         int var13 = ChunkPos.getZ(var17);
         Boolean var14 = (Boolean)var6.get(var17);
         if (var14 == null) {
            DroneChunkTickets.forceChunk(var0, var12, var13, true, var11);
         } else if (var14 != var11) {
            DroneChunkTickets.forceChunk(var0, var12, var13, false, var14);
            DroneChunkTickets.forceChunk(var0, var12, var13, true, var11);
         }
      }

      wuhB.put(var1, var5);
   }

   public static Map<Long, Boolean> v5suI(Vec3 var0, Vec3 var1, Record_IukVkAm var2) {
      Map<Long, Boolean> var3 = new HashMap<>();
      int var4 = var2.v5suI();
      int var5 = var2.ibzfxic();
      int var6 = SectionPos.blockToSectionCoord(var0.x());
      int var7 = SectionPos.blockToSectionCoord(var0.z());
      double var8 = var1.x();
      double var10 = var1.z();
      double var12 = Math.sqrt(var8 * var8 + var10 * var10);
      double var14 = 0.0;
      double var16 = 0.0;
      if (var12 > 1.0E-4) {
         var14 = var8 / var12;
         var16 = var10 / var12;
      }

      int var18;
      int var19;
      if (var12 > 1.0E-4) {
         double var20 = Math.max(0, var5 - var4 - 1) * 16.0;
         double var22 = v5suI(var12 * 40.0, 0.0, var20);
         var18 = SectionPos.blockToSectionCoord(var0.x() + var14 * var22);
         var19 = SectionPos.blockToSectionCoord(var0.z() + var16 * var22);
      } else {
         var18 = var6;
         var19 = var7;
      }

      for (int var26 = -var5; var26 <= var5; var26++) {
         for (int var21 = -var5; var21 <= var5; var21++) {
            var3.put(ChunkPos.pack(var18 + var26, var19 + var21), Boolean.FALSE);
         }
      }

      for (int var27 = -var4; var27 <= var4; var27++) {
         for (int var29 = -var4; var29 <= var4; var29++) {
            var3.put(ChunkPos.pack(var6 + var27, var7 + var29), Boolean.TRUE);
         }
      }

      if (var2.wuhB() > 0.0 && var12 > 1.0E-4) {
         double var28 = var12 * 20.0 * var2.wuhB();
         int var30 = SectionPos.blockToSectionCoord(var0.x() + var14 * var28);
         int var23 = SectionPos.blockToSectionCoord(var0.z() + var16 * var28);

         for (int var24 = -var4; var24 <= var4; var24++) {
            for (int var25 = -var4; var25 <= var4; var25++) {
               var3.put(ChunkPos.pack(var30 + var24, var23 + var25), Boolean.TRUE);
            }
         }
      }

      return var3;
   }

   public static void v5suI(ServerLevel var0, UUID var1) {
      Map<Long, Boolean> var2 = wuhB.remove(var1);
      if (var2 != null) {
         for (Entry<?, ?> var4 : var2.entrySet()) {
            long var5 = (Long)var4.getKey();
            DroneChunkTickets.forceChunk(var0, ChunkPos.getX(var5), ChunkPos.getZ(var5), false, (Boolean)var4.getValue());
         }
      }
   }

   private static double v5suI(double var0, double var2, double var4) {
      return var0 < var2 ? var2 : (var0 > var4 ? var4 : var0);
   }
}
