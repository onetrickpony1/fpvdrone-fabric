package com.fpvdrone.network;

import com.fpvdrone.entity.LoiteringMunitionEntity;
import com.fpvdrone.entity.RampEntity;
import com.fpvdrone.data.FlightRoute;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import com.fpvdrone.network.compat.NetworkContext;

public class RouteAssignPacket {
   private final int v5suI;
   private final FlightRoute ibzfxic;
   private final boolean wuhB;
   private static final double dOfFwvo = 9216.0;

   public RouteAssignPacket(int var1, FlightRoute var2, boolean var3) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
   }

   public static void encode(RouteAssignPacket var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.v5suI);
      var1.writeBoolean(var0.wuhB);
      var0.ibzfxic.v5suI(var1);
   }

   public static RouteAssignPacket decode(FriendlyByteBuf var0) {
      int var1 = var0.readVarInt();
      boolean var2 = var0.readBoolean();
      FlightRoute var3 = FlightRoute.ibzfxic(var0);
      return new RouteAssignPacket(var1, var3, var2);
   }

   public static void handle(RouteAssignPacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         ServerPlayer var2 = var1.get().getSender();
         if (var2 != null) {
            Entity var3 = var2.level().getEntity(var0.v5suI);
            if (var3 != null) {
               if (var3 instanceof LoiteringMunitionEntity var4) {
                  boolean var6 = var4.v5suI(var2.getUUID());
                  if (!var6 && var3.distanceToSqr(var2) > 9216.0) {
                     return;
                  }

                  if (!var4.uQdlyiwB().rOIp0p7().ibzfxic()) {
                     return;
                  }

                  var4.v5suI(var0.ibzfxic);
                  var2.sendOverlayMessage(Component.translatable("message.fpvdrone.route_assigned", new Object[]{var0.ibzfxic.wuhB()}));
               } else if (var3 instanceof RampEntity var5) {
                  if (var3.distanceToSqr(var2) > 9216.0) {
                     return;
                  }

                  var5.v5suI(var0.ibzfxic);
                  if (var0.wuhB) {
                     var5.wuhB(var2);
                  } else {
                     var2.sendOverlayMessage(Component.translatable("message.fpvdrone.route_assigned", new Object[]{var0.ibzfxic.wuhB()}));
                  }
               }
            }
         }
      });
      var1.get().setPacketHandled(true);
   }
}
