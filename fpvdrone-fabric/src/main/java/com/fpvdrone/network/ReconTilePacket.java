package com.fpvdrone.network;

import com.fpvdrone.client.map.ReconStore;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.fpvdrone.network.compat.NetworkContext;

public class ReconTilePacket {
   public static final int v5suI = 17;
   public static final int ibzfxic = 289;
   public static final int wuhB = 256;
   private final Identifier dOfFwvo;
   private final int rsDd50tfV;
   private final int uSUb;
   private final short[] jGhI2;
   private final int[] wEJx3Itb;
   private final int[] bAcdvq9;

   public ReconTilePacket(Identifier var1, int var2, int var3, short[] var4, int[] var5, int[] var6) {
      this.dOfFwvo = var1;
      this.rsDd50tfV = var2;
      this.uSUb = var3;
      this.jGhI2 = var4;
      this.wEJx3Itb = var5;
      this.bAcdvq9 = var6;
   }

   public Identifier v5suI() {
      return this.dOfFwvo;
   }

   public int ibzfxic() {
      return this.rsDd50tfV;
   }

   public int wuhB() {
      return this.uSUb;
   }

   public short[] dOfFwvo() {
      return this.jGhI2;
   }

   public int[] rsDd50tfV() {
      return this.wEJx3Itb;
   }

   public int[] uSUb() {
      return this.bAcdvq9;
   }

   public static void encode(ReconTilePacket var0, FriendlyByteBuf var1) {
      var1.writeIdentifier(var0.dOfFwvo);
      var1.writeVarInt(var0.rsDd50tfV);
      var1.writeVarInt(var0.uSUb);

      for (int var2 = 0; var2 < 289; var2++) {
         var1.writeShort(var0.jGhI2[var2]);
      }

      for (int var3 = 0; var3 < 256; var3++) {
         var1.writeVarInt(var0.wEJx3Itb[var3]);
      }

      for (int var4 = 0; var4 < 256; var4++) {
         var1.writeVarInt(var0.bAcdvq9[var4]);
      }
   }

   public static ReconTilePacket decode(FriendlyByteBuf var0) {
      Identifier var1 = var0.readIdentifier();
      int var2 = var0.readVarInt();
      int var3 = var0.readVarInt();
      short[] var4 = new short[289];

      for (int var5 = 0; var5 < 289; var5++) {
         var4[var5] = var0.readShort();
      }

      int[] var8 = new int[256];

      for (int var6 = 0; var6 < 256; var6++) {
         var8[var6] = var0.readVarInt();
      }

      int[] var9 = new int[256];

      for (int var7 = 0; var7 < 256; var7++) {
         var9[var7] = var0.readVarInt();
      }

      return new ReconTilePacket(var1, var2, var3, var4, var8, var9);
   }

   public static void handle(ReconTilePacket var0, Supplier<NetworkContext> var1) {
      var1.get()
         .enqueueWork(
            () -> ReconStore.v5suI().v5suI(var0.dOfFwvo, var0.rsDd50tfV, var0.uSUb, var0.jGhI2, var0.wEJx3Itb, var0.bAcdvq9)
         );
      var1.get().setPacketHandled(true);
   }
}
