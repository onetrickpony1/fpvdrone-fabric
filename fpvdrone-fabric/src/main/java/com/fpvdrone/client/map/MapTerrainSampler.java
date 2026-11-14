package com.fpvdrone.client.map;

import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.map.ReconMarks;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MapTerrainSampler {
   private static final int v5suI = 1200;
   private static int ibzfxic = 0;
   private static final int wuhB = 32;
   private static final int dOfFwvo = 128;
   private static final int rsDd50tfV = 12;
   private static final int uSUb = 256;
   private static final long jGhI2 = 1500000L;
   private static final int wEJx3Itb = 40;
   private static final long bAcdvq9 = 50000000L;
   private static long c1jHYPb4 = -1L;
   private static final LongOpenHashSet v8ZN = new LongOpenHashSet();
   private static final LongOpenHashSet zTT3nw6 = new LongOpenHashSet();
   private static final LongOpenHashSet vmpmU = new LongOpenHashSet();
   private static final ConcurrentLinkedQueue<MapTerrainSampler.KKeMvpuBgEY> b69s = new ConcurrentLinkedQueue<>();
   private static final Set<BlockState> cXHvq = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private static final Set<Long> rxYnaa = Collections.newSetFromMap(new ConcurrentHashMap<>());
   private static final LongOpenHashSet r6s7FL3 = new LongOpenHashSet();
   private static boolean iAKNPAj;
   private static int qlv6NKWnH;
   private static int hOKMYHOaC;
   private static int iS1JnZov;
   private static int lfyfLwuw;
   private static Identifier wyNDWDB;
   private static final Logger iRWc2Pi = LoggerFactory.getLogger("FPVDrone/Recon");

   private MapTerrainSampler() {
   }

   /** Fabric replacements for the 1.1.8 Forge subscribers (log in, log out, client tick end). */
   public static void register() {
      net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((h, s, c) -> c.execute(MapTerrainSampler::onLoggingIn));
      net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((h, c) -> c.execute(MapTerrainSampler::onLoggingOut));
      net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(c -> onClientTickEnd());
   }

   private static void onLoggingIn() {
      ReconStore.v5suI().v5suI(Minecraft.getInstance().getConnection(), MapTerrainSampler::wuhB);
      ReconLayer.v5suI().ibzfxic();
      ReconMarks.v5suI().ibzfxic();
      v5suI();
      ibzfxic = 0;
   }

   private static void onLoggingOut() {
      ReconStore.v5suI().jGhI2();
      ReconLayer.v5suI().ibzfxic();
      ReconMarks.v5suI().ibzfxic();
      v5suI();
   }

   private static void onClientTickEnd() {
      {
         ReconStore.v5suI().v5suI(Minecraft.getInstance().getConnection(), MapTerrainSampler::wuhB);
         ReconStore.v5suI().dOfFwvo();
         if (FpvConfig.isLiveMapObservation()) {
            dOfFwvo();
         } else {
            rxYnaa.clear();
            r6s7FL3.clear();
            if (!vmpmU.isEmpty() || !b69s.isEmpty()) {
               uSUb();
            }
         }

         if (++ibzfxic >= 1200) {
            ibzfxic = 0;
            ReconStore.v5suI().uSUb();
         }
      }
   }

   private static void dOfFwvo() {
      Minecraft var0 = Minecraft.getInstance();
      if (var0.level != null && var0.player != null) {
         Identifier var1 = var0.level.dimension().identifier();
         if (!var1.equals(wyNDWDB)) {
            c1jHYPb4();
            rxYnaa.clear();
            r6s7FL3.clear();
            wyNDWDB = var1;
         }

         rsDd50tfV();
         uSUb();
         jGhI2();
         wEJx3Itb();
         int var2 = var0.player.blockPosition().getX() >> 4;
         int var3 = var0.player.blockPosition().getZ() >> 4;
         int var4 = Math.min(32, Math.max(2, var0.options.getEffectiveRenderDistance()));
         iAKNPAj = !r6s7FL3.isEmpty();
         lfyfLwuw = v5suI(System.nanoTime());
         iS1JnZov = 0;
         if (vmpmU.size() < 256) {
            for (int var5 = 0; var5 <= var4; var5++) {
               for (int var6 = -var5; var6 <= var5; var6++) {
                  if (v5suI(var0, var1, var2 + var6, var3 - var5) && bAcdvq9() || var5 != 0 && v5suI(var0, var1, var2 + var6, var3 + var5) && bAcdvq9()) {
                     return;
                  }
               }

               for (int var7 = -var5 + 1; var7 <= var5 - 1; var7++) {
                  if (v5suI(var0, var1, var2 - var5, var3 + var7) && bAcdvq9() || v5suI(var0, var1, var2 + var5, var3 + var7) && bAcdvq9()) {
                     return;
                  }
               }
            }
         }
      }
   }

   private static void rsDd50tfV() {
      if (!rxYnaa.isEmpty()) {
         var var0 = rxYnaa.iterator();

         while (var0.hasNext()) {
            r6s7FL3.add((Long)var0.next());
            var0.remove();
         }
      }
   }

   private static void uSUb() {
      MapTerrainSampler.KKeMvpuBgEY var0;
      while ((var0 = b69s.poll()) != null) {
         if (var0.v5suI() == qlv6NKWnH) {
            vmpmU.remove(var0.ibzfxic());
            if (!var0.dOfFwvo()) {
               v8ZN.add(var0.ibzfxic());
               if (var0.wuhB()) {
                  r6s7FL3.remove(var0.ibzfxic());
               } else {
                  zTT3nw6.add(var0.ibzfxic());
               }
            }
         }
      }
   }

   private static void jGhI2() {
      if (!cXHvq.isEmpty()) {
         long var0 = System.nanoTime() + 1500000L;
         var var2 = cXHvq.iterator();

         while (var2.hasNext()) {
            BlockMapColors.v5suI((BlockState)var2.next());
            var2.remove();
            if (System.nanoTime() >= var0) {
               return;
            }
         }
      }
   }

   private static void wEJx3Itb() {
      if (++hOKMYHOaC >= 40) {
         hOKMYHOaC = 0;
         if (!zTT3nw6.isEmpty()) {
            v8ZN.removeAll(zTT3nw6);
            zTT3nw6.clear();
         }
      }
   }

   private static int v5suI(long var0) {
      int var2 = 128;
      if (c1jHYPb4 >= 0L) {
         long var3 = var0 - c1jHYPb4;
         if (var3 > 50000000L) {
            var2 = (int)Math.max(12L, 6400000000L / var3);
         }
      }

      c1jHYPb4 = var0;
      return var2;
   }

   private static boolean bAcdvq9() {
      return iS1JnZov >= lfyfLwuw || vmpmU.size() >= 256;
   }

   private static boolean v5suI(Minecraft var0, Identifier var1, int var2, int var3) {
      long var4 = ChunkPos.pack(var2, var3);
      if (!v8ZN.contains(var4) || iAKNPAj && r6s7FL3.contains(var4)) {
         if (!vmpmU.add(var4)) {
            return false;
         } else {
            LevelChunk var6 = var0.level.getChunkSource().getChunkNow(var2, var3);
            if (var6 == null) {
               vmpmU.remove(var4);
               return false;
            } else {
               v5suI(var0.level, var6, var1, var4, var2, var3);
               iS1JnZov++;
               return true;
            }
         }
      } else {
         return false;
      }
   }

   private static void v5suI(ClientLevel var0, LevelChunk var1, Identifier var2, long var3, int var5, int var6) {
      int var7 = qlv6NKWnH;
      long var8 = ReconStore.v5suI().rsDd50tfV();
      MapExecutors.v5suI().execute(() -> {
         boolean var10 = false;
         boolean var11 = false;

         try {
            ChunkColumnSampler.Ol3TEDSog var12 = ChunkColumnSampler.v5suI(var0, var1, var5, var6);
            if (var12.v5suI() != null) {
               ReconStore.v5suI().v5suI(var2, var5, var6, var12.v5suI(), var8);
               var10 = true;
            } else {
               var11 = var12.wuhB();
               if (var12.ibzfxic() != null) {
                  cXHvq.addAll(var12.ibzfxic());
               }
            }
         } catch (Throwable var16) {
         } finally {
            b69s.add(new MapTerrainSampler.KKeMvpuBgEY(var7, var3, var10, var11));
         }
      });
   }

   public static void v5suI(int var0, int var1) {
      if (FpvConfig.isLiveMapObservation()) {
         rxYnaa.add(ChunkPos.pack(var0, var1));
      }
   }

   public static void v5suI() {
      c1jHYPb4();
      rxYnaa.clear();
      r6s7FL3.clear();
      wyNDWDB = null;
      c1jHYPb4 = -1L;
   }

   public static void ibzfxic() {
      c1jHYPb4();
   }

   private static void c1jHYPb4() {
      qlv6NKWnH++;
      v8ZN.clear();
      zTT3nw6.clear();
      vmpmU.clear();
      b69s.clear();
      cXHvq.clear();
      hOKMYHOaC = 0;
   }

   public static String wuhB() {
      Minecraft var0 = Minecraft.getInstance();
      if (var0.hasSingleplayerServer() && var0.getSingleplayerServer() != null) {
         try {
            return "sp_" + var0.getSingleplayerServer().getWorldPath(LevelResource.ROOT).getFileName().toString();
         } catch (Exception var2) {
         }
      }

      ServerData var1 = var0.getCurrentServer();
      return var1 != null && var1.ip != null ? "mp_" + var1.ip : null;
   }

   private static final class KKeMvpuBgEY {
      private final int v5suI;
      private final long ibzfxic;
      private final boolean wuhB;
      private final boolean dOfFwvo;

      private KKeMvpuBgEY(int var1, long var2, boolean var4, boolean var5) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var4;
         this.dOfFwvo = var5;
      }


      public int v5suI() {

         return this.v5suI;

      }

   
      public long ibzfxic() {
         return this.ibzfxic;
      }

      public boolean wuhB() {
         return this.wuhB;
      }

      public boolean dOfFwvo() {
         return this.dOfFwvo;
      }
   }
}
