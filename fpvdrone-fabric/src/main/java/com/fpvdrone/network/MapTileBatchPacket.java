package com.fpvdrone.network;

import com.fpvdrone.client.map.ReconStore;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.fpvdrone.network.compat.NetworkContext;

public class MapTileBatchPacket {
   private final Identifier v5suI;
   private final List<MapTileBatchPacket.KKeMvpuBgEY> ibzfxic;
   private static final int wuhB = 4096;

   public MapTileBatchPacket(Identifier var1, List<MapTileBatchPacket.KKeMvpuBgEY> var2) {
      this.v5suI = var1;
      this.ibzfxic = var2;
   }

   public static void encode(MapTileBatchPacket var0, FriendlyByteBuf var1) {
      var1.writeIdentifier(var0.v5suI);
      var1.writeVarInt(var0.ibzfxic.size());

      for (MapTileBatchPacket.KKeMvpuBgEY var3 : var0.ibzfxic) {
         var1.writeVarInt(var3.v5suI);
         var1.writeVarInt(var3.ibzfxic);

         for (int var4 = 0; var4 < 289; var4++) {
            var1.writeShort(var3.wuhB[var4]);
         }

         for (int var5 = 0; var5 < 256; var5++) {
            var1.writeVarInt(var3.dOfFwvo[var5]);
         }

         for (int var6 = 0; var6 < 256; var6++) {
            var1.writeVarInt(var3.rsDd50tfV[var6]);
         }
      }
   }

   public static MapTileBatchPacket decode(FriendlyByteBuf var0) {
      Identifier var1 = var0.readIdentifier();
      int var2 = var0.readVarInt();
      if (var2 >= 0 && var2 <= 4096) {
         var var3 = new ArrayList(var2);

         for (int var4 = 0; var4 < var2; var4++) {
            int var5 = var0.readVarInt();
            int var6 = var0.readVarInt();
            short[] var7 = new short[289];

            for (int var8 = 0; var8 < var7.length; var8++) {
               var7[var8] = var0.readShort();
            }

            int[] var11 = new int[256];

            for (int var9 = 0; var9 < var11.length; var9++) {
               var11[var9] = var0.readVarInt();
            }

            int[] var12 = new int[256];

            for (int var10 = 0; var10 < var12.length; var10++) {
               var12[var10] = var0.readVarInt();
            }

            var3.add(new MapTileBatchPacket.KKeMvpuBgEY(var5, var6, var7, var11, var12));
         }

         return new MapTileBatchPacket(var1, var3);
      } else {
         throw new IllegalArgumentException("Recon tile batch with an implausible entry count: " + var2);
      }
   }

   public static void handle(MapTileBatchPacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         ReconStore var1x = ReconStore.v5suI();

         for (MapTileBatchPacket.KKeMvpuBgEY var3 : var0.ibzfxic) {
            var1x.v5suI(var0.v5suI, var3.v5suI, var3.ibzfxic, var3.wuhB, var3.dOfFwvo, var3.rsDd50tfV);
         }
      });
      var1.get().setPacketHandled(true);
   }

   public static final class KKeMvpuBgEY {
      public final int v5suI;
      public final int ibzfxic;
      public final short[] wuhB;
      public final int[] dOfFwvo;
      public final int[] rsDd50tfV;

      public KKeMvpuBgEY(int var1, int var2, short[] var3, int[] var4, int[] var5) {
         this.v5suI = var1;
         this.ibzfxic = var2;
         this.wuhB = var3;
         this.dOfFwvo = var4;
         this.rsDd50tfV = var5;
      }
   }
}
