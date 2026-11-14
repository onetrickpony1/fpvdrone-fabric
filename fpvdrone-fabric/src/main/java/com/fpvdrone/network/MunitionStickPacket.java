package com.fpvdrone.network;

import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.entity.LoiteringMunitionEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

public class MunitionStickPacket {
   private final int v5suI;
   private final boolean ibzfxic;
   private final boolean wuhB;
   private final double dOfFwvo;
   private final double rsDd50tfV;
   private final double uSUb;
   private final int jGhI2;

   public MunitionStickPacket(int var1, boolean var2, boolean var3, double var4, double var6, double var8, int var10) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
      this.dOfFwvo = var4;
      this.rsDd50tfV = var6;
      this.uSUb = var8;
      this.jGhI2 = var10;
   }

   public static void encode(MunitionStickPacket var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.v5suI);
      var1.writeBoolean(var0.ibzfxic);
      var1.writeBoolean(var0.wuhB);
      var1.writeDouble(var0.dOfFwvo);
      var1.writeDouble(var0.rsDd50tfV);
      var1.writeDouble(var0.uSUb);
      var1.writeVarInt(var0.jGhI2);
   }

   public static MunitionStickPacket decode(FriendlyByteBuf var0) {
      return new MunitionStickPacket(
         var0.readVarInt(), var0.readBoolean(), var0.readBoolean(), var0.readDouble(), var0.readDouble(), var0.readDouble(), var0.readVarInt()
      );
   }

   public static void handle(MunitionStickPacket var0, Supplier<NetworkContext> var1) {
      var1.get()
         .enqueueWork(
            () -> {
               ServerPlayer var2 = var1.get().getSender();
               if (var2 != null) {
                  if (var2.level().getEntity(var0.v5suI) instanceof LoiteringMunitionEntity var4
                     && var4.isAlive()
                     && var4.v5suI(var2.getUUID())
                     && var4.uQdlyiwB().v8ZN()
                     && var4.uQdlyiwB().qlv6NKWnH().rsDd50tfV().controlScheme() == DroneDefinition.ControlScheme.ibzfxic) {
                     var4.v5suI(var0.dOfFwvo, var0.rsDd50tfV, var0.uSUb, var0.ibzfxic, var0.wuhB, var0.jGhI2);
                  }
               }
            }
         );
      var1.get().setPacketHandled(true);
   }
}
