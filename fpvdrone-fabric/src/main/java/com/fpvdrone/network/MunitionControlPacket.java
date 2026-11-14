package com.fpvdrone.network;

import com.fpvdrone.entity.LoiteringMunitionEntity;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

public class MunitionControlPacket {
   private final int v5suI;
   private final float ibzfxic;
   private final float wuhB;
   private final float dOfFwvo;
   private final float rsDd50tfV;

   public MunitionControlPacket(int var1, float var2, float var3, float var4, float var5) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
      this.dOfFwvo = var4;
      this.rsDd50tfV = var5;
   }

   public static void encode(MunitionControlPacket var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.v5suI);
      var1.writeFloat(var0.ibzfxic);
      var1.writeFloat(var0.wuhB);
      var1.writeFloat(var0.dOfFwvo);
      var1.writeFloat(var0.rsDd50tfV);
   }

   public static MunitionControlPacket decode(FriendlyByteBuf var0) {
      return new MunitionControlPacket(var0.readVarInt(), var0.readFloat(), var0.readFloat(), var0.readFloat(), var0.readFloat());
   }

   public static void handle(MunitionControlPacket var0, Supplier<NetworkContext> var1) {
      var1.get()
         .enqueueWork(
            () -> {
               ServerPlayer var2 = var1.get().getSender();
               if (var2 != null) {
                  if (var2.level().getEntity(var0.v5suI) instanceof LoiteringMunitionEntity var4
                     && var4.isAlive()
                     && var4.v5suI(var2.getUUID())
                     && var4.uQdlyiwB().v8ZN()) {
                     var4.v5suI(var0.ibzfxic, var0.wuhB, var0.dOfFwvo, var0.rsDd50tfV);
                  }
               }
            }
         );
      var1.get().setPacketHandled(true);
   }
}
