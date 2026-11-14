package com.fpvdrone.network;

import com.fpvdrone.item.OpenerCookOff;
import com.fpvdrone.network.compat.NetworkContext;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;

/** 1.1.8 (Packet_Vvqb4Q5l), client to server: the drop key with the "Can Opener" vest worn sets its mine off. */
public class OpenerDetonatePacket {
   public static void encode(OpenerDetonatePacket msg, FriendlyByteBuf buf) {
   }

   public static OpenerDetonatePacket decode(FriendlyByteBuf buf) {
      return new OpenerDetonatePacket();
   }

   public static void handle(OpenerDetonatePacket msg, Supplier<NetworkContext> ctx) {
      ctx.get().enqueueWork(() -> OpenerCookOff.detonate(ctx.get().getSender()));
      ctx.get().setPacketHandled(true);
   }
}
