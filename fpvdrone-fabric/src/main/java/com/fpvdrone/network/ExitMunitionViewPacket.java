package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

public class ExitMunitionViewPacket {
   private final int v5suI;

   public ExitMunitionViewPacket(int var1) {
      this.v5suI = var1;
   }

   public static void encode(ExitMunitionViewPacket var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.v5suI);
   }

   public static ExitMunitionViewPacket decode(FriendlyByteBuf var0) {
      return new ExitMunitionViewPacket(var0.readVarInt());
   }

   public static void handle(ExitMunitionViewPacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         ServerPlayer var1x = var1.get().getSender();
         if (var1x != null) {
            FPVMod.exitMunitionView(var1x, false);
         }
      });
      var1.get().setPacketHandled(true);
   }
}
