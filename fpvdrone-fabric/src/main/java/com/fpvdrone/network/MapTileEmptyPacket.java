package com.fpvdrone.network;

import com.fpvdrone.client.map.ReconStore;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import com.fpvdrone.network.compat.NetworkContext;

public class MapTileEmptyPacket {
   private final Identifier v5suI;
   private final int ibzfxic;
   private final int wuhB;

   public MapTileEmptyPacket(Identifier var1, int var2, int var3) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
   }

   public static void encode(MapTileEmptyPacket var0, FriendlyByteBuf var1) {
      var1.writeIdentifier(var0.v5suI);
      var1.writeVarInt(var0.ibzfxic);
      var1.writeVarInt(var0.wuhB);
   }

   public static MapTileEmptyPacket decode(FriendlyByteBuf var0) {
      return new MapTileEmptyPacket(var0.readIdentifier(), var0.readVarInt(), var0.readVarInt());
   }

   public static void handle(MapTileEmptyPacket var0, Supplier<NetworkContext> var1) {
      var1.get()
         .enqueueWork(() -> ReconStore.v5suI().rsDd50tfV(var0.v5suI, var0.ibzfxic, var0.wuhB));
      var1.get().setPacketHandled(true);
   }
}
