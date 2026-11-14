package com.fpvdrone.network;

import com.fpvdrone.map.ServerMapTiles;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

public class MapTileRequestPacket {
   private final Identifier v5suI;
   private final int ibzfxic;
   private final int wuhB;

   public MapTileRequestPacket(Identifier var1, int var2, int var3) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
   }

   public static void encode(MapTileRequestPacket var0, FriendlyByteBuf var1) {
      var1.writeIdentifier(var0.v5suI);
      var1.writeVarInt(var0.ibzfxic);
      var1.writeVarInt(var0.wuhB);
   }

   public static MapTileRequestPacket decode(FriendlyByteBuf var0) {
      return new MapTileRequestPacket(var0.readIdentifier(), var0.readVarInt(), var0.readVarInt());
   }

   public static void handle(MapTileRequestPacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         ServerPlayer var2 = var1.get().getSender();
         if (var2 != null) {
            ServerMapTiles.v5suI(var2, var0.v5suI, var0.ibzfxic, var0.wuhB);
         }
      });
      var1.get().setPacketHandled(true);
   }
}
