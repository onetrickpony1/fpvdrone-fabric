package com.fpvdrone.map;

import com.fpvdrone.entity.TerrainProbe;
import com.fpvdrone.physics.PhysicsState;

import com.fpvdrone.network.ModNetwork;
import com.fpvdrone.network.MapTileBatchPacket;
import com.fpvdrone.network.MapTileEmptyPacket;
import com.fpvdrone.mixin.ChunkMapReadInvoker;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.fpvdrone.network.compat.PacketDistributor;

public final class ServerMapTiles {
   private static final int v5suI = 32768;
   private static final int ibzfxic = 4;
   private static final int wuhB = 192;
   private static final int dOfFwvo = 1024;
   private static final int rsDd50tfV = 64;
   private static final long uSUb = 1500000L;
   private static final Map<UUID, ArrayDeque<ServerMapTiles.HrdCkfoU9x>> jGhI2 = new LinkedHashMap<>();
   private static int wEJx3Itb;
   private static final ConcurrentLinkedQueue<ServerMapTiles.Ol3TEDSog> bAcdvq9 = new ConcurrentLinkedQueue<>();
   private static final AtomicInteger c1jHYPb4 = new AtomicInteger();
   private static final Map<ServerMapTiles.Rdl0X5jeod, Integer> v8ZN = new HashMap<>();
   private static final ConcurrentLinkedQueue<ServerMapTiles.Rdl0X5jeod> zTT3nw6 = new ConcurrentLinkedQueue<>();
   private static final int vmpmU = 58593;

   private ServerMapTiles() {
   }

   public static void v5suI(ServerPlayer var0, Identifier var1, int var2, int var3) {
      UUID var4 = var0.getUUID();
      ServerMapTiles.Rdl0X5jeod var5 = new ServerMapTiles.Rdl0X5jeod(var4, var1, var2, var3);
      if (Math.abs(var2) > 58593 || Math.abs(var3) > 58593) {
         v5suI(var0, var5);
      } else if (!var0.level().dimension().identifier().equals(var1)) {
         v5suI(var0, var5);
      } else {
         int var6 = var2 * 32;
         int var7 = var3 * 32;
         if (!v8ZN.containsKey(var5)) {
            if (v5suI(var4) < 4 && wEJx3Itb + 1024 <= 32768) {
               ArrayDeque var8 = jGhI2.computeIfAbsent(var4, var0x -> new ArrayDeque<>());

               for (int var9 = 0; var9 < 32; var9++) {
                  for (int var10 = 0; var10 < 32; var10++) {
                     var8.add(new ServerMapTiles.HrdCkfoU9x(var4, var1, var6 + var10, var7 + var9, var5));
                  }
               }

               wEJx3Itb += 1024;
               v8ZN.put(var5, 1024);
            } else {
               v5suI(var0, var5);
            }
         }
      }
   }

   private static int v5suI(UUID var0) {
      int var1 = 0;

      for (ServerMapTiles.Rdl0X5jeod var3 : v8ZN.keySet()) {
         if (var3.v5suI().equals(var0)) {
            var1++;
         }
      }

      return var1;
   }

   private static void v5suI(MinecraftServer var0, ServerMapTiles.Rdl0X5jeod var1) {
      Integer var2 = v8ZN.get(var1);
      if (var2 != null) {
         if (var2 > 1) {
            v8ZN.put(var1, var2 - 1);
         } else {
            v8ZN.remove(var1);
            ServerPlayer var3 = var0.getPlayerList().getPlayer(var1.v5suI());
            if (var3 != null) {
               v5suI(var3, var1);
            }
         }
      }
   }

   private static void v5suI(ServerPlayer var0, ServerMapTiles.Rdl0X5jeod var1) {
      ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> var0), new MapTileEmptyPacket(var1.ibzfxic(), var1.wuhB(), var1.dOfFwvo()));
   }

   /** Fabric replacements for the 1.1.8 Forge subscribers (server tick end, player logged out). */
   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(ServerMapTiles::onServerTickEnd);
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((h, server) -> onLoggedOut(h.player.getUUID()));
   }

   private static void onServerTickEnd(MinecraftServer var1) {
      {

         ServerMapTiles.Rdl0X5jeod var2;
         while ((var2 = zTT3nw6.poll()) != null) {
            v5suI(var1, var2);
         }

         v5suI(var1);
         ibzfxic(var1);
      }
   }

   private static void onLoggedOut(UUID var1) {
      v8ZN.keySet().removeIf(var1x -> var1x.v5suI().equals(var1));
      ArrayDeque var2 = jGhI2.remove(var1);
      if (var2 != null) {
         wEJx3Itb = wEJx3Itb - var2.size();
      }
   }

   private static void v5suI(MinecraftServer var0) {
      while (wEJx3Itb > 0 && c1jHYPb4.get() < 64) {
         boolean var1 = false;
         var var2 = jGhI2.entrySet().iterator();

         while (var2.hasNext() && c1jHYPb4.get() < 64) {
            ArrayDeque var3 = (ArrayDeque)((Entry)var2.next()).getValue();
            ServerMapTiles.HrdCkfoU9x var4 = (ServerMapTiles.HrdCkfoU9x)var3.poll();
            if (var4 == null) {
               var2.remove();
            } else {
               wEJx3Itb--;
               var1 = true;
               v5suI(var0, var4);
            }
         }

         if (!var1) {
            break;
         }
      }
   }

   private static void v5suI(MinecraftServer var0, ServerMapTiles.HrdCkfoU9x var1) {
      ServerPlayer var2 = var0.getPlayerList().getPlayer(var1.v5suI());
      if (var2 == null) {
         v5suI(var0, var1.rsDd50tfV());
      } else {
         ServerLevel var3 = var2.level();
         if (!var3.dimension().identifier().equals(var1.ibzfxic())) {
            v5suI(var0, var1.rsDd50tfV());
         } else {
            c1jHYPb4.incrementAndGet();
            ServerChunkCache var4 = var3.getChunkSource();
            ChunkMapReadInvoker var5 = (ChunkMapReadInvoker)var4.chunkMap;
            int var6 = var1.wuhB();
            int var7 = var1.dOfFwvo();
            var5.fpvdrone$readChunk(new ChunkPos(var6, var7)).whenComplete((var3x, var4x) -> {
               if (var4x == null && var3x != null && var3x.isPresent()) {
                  bAcdvq9.add(new ServerMapTiles.Ol3TEDSog(var1.v5suI(), var1.ibzfxic(), var6, var7, var3x.get(), var1.rsDd50tfV()));
               } else {
                  c1jHYPb4.decrementAndGet();
                  zTT3nw6.add(var1.rsDd50tfV());
               }
            });
         }
      }
   }

   private static void ibzfxic(MinecraftServer var0) {
      if (!bAcdvq9.isEmpty()) {
         var var1 = new HashMap<UUID, ServerMapTiles.KKeMvpuBgEY>();
         var var2 = new ArrayList<ServerMapTiles.Rdl0X5jeod>();
         long var3 = System.nanoTime() + 1500000L;

         ServerMapTiles.Ol3TEDSog var5;
         while (System.nanoTime() < var3 && (var5 = bAcdvq9.poll()) != null) {
            c1jHYPb4.decrementAndGet();
            var2.add(var5.uSUb());
            ServerPlayer var6 = var0.getPlayerList().getPlayer(var5.v5suI());
            if (var6 != null) {
               ServerLevel var7 = var6.level();
               if (var7.dimension().identifier().equals(var5.ibzfxic())) {
                  ServerMapTiles.Sy7j08t var8 = v5suI(var7, var5.wuhB(), var5.dOfFwvo(), var5.rsDd50tfV());
                  if (var8 != null) {
                     Identifier var9 = var5.ibzfxic();
                     ServerMapTiles.KKeMvpuBgEY var10 = var1.computeIfAbsent(var5.v5suI(), var1x -> new ServerMapTiles.KKeMvpuBgEY(var9));
                     if (var10.v5suI.equals(var9)) {
                        var10.ibzfxic.add(new MapTileBatchPacket.KKeMvpuBgEY(var5.wuhB(), var5.dOfFwvo(), var8.v5suI(), var8.ibzfxic(), var8.wuhB()));
                        if (var10.ibzfxic.size() >= 192) {
                           break;
                        }
                     }
                  }
               }
            }
         }

         for (Entry<?, ?> var13 : var1.entrySet()) {
            ServerPlayer var15 = var0.getPlayerList().getPlayer((UUID)var13.getKey());
            if (var15 != null && !((ServerMapTiles.KKeMvpuBgEY)var13.getValue()).ibzfxic.isEmpty()) {
               ModNetwork.CHANNEL
                  .send(
                     PacketDistributor.PLAYER.with(() -> var15),
                     new MapTileBatchPacket(((ServerMapTiles.KKeMvpuBgEY)var13.getValue()).v5suI, ((ServerMapTiles.KKeMvpuBgEY)var13.getValue()).ibzfxic)
                  );
            }
         }

         for (ServerMapTiles.Rdl0X5jeod var14 : var2) {
            v5suI(var0, var14);
         }
      }
   }

   private static ServerMapTiles.Sy7j08t v5suI(ServerLevel var0, int var1, int var2, CompoundTag var3) {
      RegionChunkReader var4 = RegionChunkReader.v5suI(var0, var3);
      return var4 == null ? null : v5suI(var4, var1, var2, var0);
   }

   private static ServerMapTiles.Sy7j08t v5suI(RegionChunkReader var0, int var1, int var2, ServerLevel var3) {
      int var4 = var1 << 4;
      int var5 = var2 << 4;
      byte var6 = 17;
      short[] var7 = new short[289];
      int[] var8 = new int[289];
      TerrainProbe.KKeMvpuBgEY var9 = var0::v5suI;
      int var10 = var0.v5suI();
      int var11 = var0.ibzfxic();

      for (int var12 = -1; var12 < 16; var12++) {
         for (int var13 = -1; var13 < 16; var13++) {
            int var14 = Math.max(0, Math.min(15, var13));
            int var15 = Math.max(0, Math.min(15, var12));
            int var16 = var4 + var14;
            int var17 = var5 + var15;
            int var18 = TerrainProbe.v5suI(var9, var10, var11, var16, var17);
            BlockState var19 = var0.v5suI(var16, var18, var17);
            int var20 = TerrainProbe.v5suI(var19) ? var18 - 1 : var18;
            int var21 = (var12 + 1) * var6 + var13 + 1;
            var7[var21] = (short)var20;
            var8[var21] = var18;
         }
      }

      int[] var22 = new int[256];
      int[] var23 = new int[256];

      for (int var24 = 0; var24 < 16; var24++) {
         for (int var25 = 0; var25 < 16; var25++) {
            int var26 = var8[(var24 + 1) * var6 + var25 + 1];
            int var27 = var4 + var25;
            int var28 = var5 + var24;
            BlockState var29 = var0.v5suI(var27, var26, var28);
            var22[var24 * 16 + var25] = Block.getId(var29);
            Holder var30 = var0.ibzfxic(var27 >> 2, var26 >> 2, var28 >> 2);
            var23[var24 * 16 + var25] = var3.registryAccess().lookupOrThrow(Registries.BIOME).getId((Biome)var30.value());
         }
      }

      return new ServerMapTiles.Sy7j08t(var7, var22, var23);
   }

   private static final class HrdCkfoU9x {
      private final UUID v5suI;
      private final Identifier ibzfxic;
      private final int wuhB;
      private final int dOfFwvo;
      private final ServerMapTiles.Rdl0X5jeod rsDd50tfV;

      private HrdCkfoU9x(UUID var1, Identifier var2, int var3, int var4, ServerMapTiles.Rdl0X5jeod var5) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var5;
      }


      public UUID v5suI() {

         return this.v5suI;

      }

   
      public Identifier ibzfxic() {
         return this.ibzfxic;
      }

      public int wuhB() {
         return this.wuhB;
      }

      public int dOfFwvo() {
         return this.dOfFwvo;
      }

      public ServerMapTiles.Rdl0X5jeod rsDd50tfV() {
         return this.rsDd50tfV;
      }
   }

   private static final class KKeMvpuBgEY {
      final Identifier v5suI;
      final List<MapTileBatchPacket.KKeMvpuBgEY> ibzfxic = new ArrayList<>();

      KKeMvpuBgEY(Identifier var1) {
         this.v5suI = var1;
      }
   }

   private static final class Ol3TEDSog {
      private final UUID v5suI;
      private final Identifier ibzfxic;
      private final int wuhB;
      private final int dOfFwvo;
      private final CompoundTag rsDd50tfV;
      private final ServerMapTiles.Rdl0X5jeod uSUb;

      private Ol3TEDSog(UUID var1, Identifier var2, int var3, int var4, CompoundTag var5, ServerMapTiles.Rdl0X5jeod var6) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var5;
         this.uSUb = var6;
      }


      public UUID v5suI() {

         return this.v5suI;

      }

   
      public Identifier ibzfxic() {
         return this.ibzfxic;
      }

      public int wuhB() {
         return this.wuhB;
      }

      public int dOfFwvo() {
         return this.dOfFwvo;
      }

      public CompoundTag rsDd50tfV() {
         return this.rsDd50tfV;
      }

      public ServerMapTiles.Rdl0X5jeod uSUb() {
         return this.uSUb;
      }
   }

   private static final class Rdl0X5jeod {
      private final UUID v5suI;
      private final Identifier ibzfxic;
      private final int wuhB;
      private final int dOfFwvo;

      private Rdl0X5jeod(UUID var1, Identifier var2, int var3, int var4) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
      }


      public UUID v5suI() {

         return this.v5suI;

      }

   
      public Identifier ibzfxic() {
         return this.ibzfxic;
      }

      public int wuhB() {
         return this.wuhB;
      }

      public int dOfFwvo() {
         return this.dOfFwvo;
      }
   }

   private static final class Sy7j08t {
      private final short[] v5suI;
      private final int[] ibzfxic;
      private final int[] wuhB;

      private Sy7j08t(short[] var1, int[] var2, int[] var3) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
      }


      public short[] v5suI() {

         return this.v5suI;

      }

   
      public int[] ibzfxic() {
         return this.ibzfxic;
      }

      public int[] wuhB() {
         return this.wuhB;
      }
   }
}
