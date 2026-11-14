package com.fpvdrone.network;

import com.fpvdrone.client.MunitionClientState;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;

public class MunitionViewEndedPacket {
   private final int v5suI;

   public MunitionViewEndedPacket(int var1) {
      this.v5suI = var1;
   }

   public static void encode(MunitionViewEndedPacket var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.v5suI);
   }

   public static MunitionViewEndedPacket decode(FriendlyByteBuf var0) {
      return new MunitionViewEndedPacket(var0.readVarInt());
   }

   public static void handle(MunitionViewEndedPacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> MunitionClientState.pyHY5());
      var1.get().setPacketHandled(true);
   }
}
