package com.fpvdrone.network;

import com.fpvdrone.entity.LoiteringMunitionEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

public class MunitionPacketB {
   private final int v5suI;

   public MunitionPacketB(int var1) {
      this.v5suI = var1;
   }

   public static void encode(MunitionPacketB var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.v5suI);
   }

   public static MunitionPacketB decode(FriendlyByteBuf var0) {
      return new MunitionPacketB(var0.readVarInt());
   }

   public static void handle(MunitionPacketB var0, Supplier<NetworkContext> var1) {
      var1.get()
         .enqueueWork(
            () -> {
               ServerPlayer var2 = var1.get().getSender();
               if (var2 != null) {
                  if (var2.level().getEntity(var0.v5suI) instanceof LoiteringMunitionEntity var4
                     && var4.isAlive()
                     && var4.v5suI(var2.getUUID())
                     && var4.uQdlyiwB().v8ZN()) {
                     var4.v5suI(var2);
                  }
               }
            }
         );
      var1.get().setPacketHandled(true);
   }
}
