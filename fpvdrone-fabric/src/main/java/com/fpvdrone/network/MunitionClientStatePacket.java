package com.fpvdrone.network;

import com.fpvdrone.entity.LoiteringMunitionEntity;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;

public class MunitionClientStatePacket {
   private final int v5suI;
   private final String ibzfxic;

   public MunitionClientStatePacket(int var1, String var2) {
      this.v5suI = var1;
      this.ibzfxic = var2 == null ? "" : var2;
   }

   public static void encode(MunitionClientStatePacket var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.v5suI);
      var1.writeUtf(var0.ibzfxic);
   }

   public static MunitionClientStatePacket decode(FriendlyByteBuf var0) {
      return new MunitionClientStatePacket(var0.readVarInt(), var0.readUtf());
   }

   public static void handle(MunitionClientStatePacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         ClientLevel var1x = Minecraft.getInstance().level;
         if (var1x != null) {
            if (var1x.getEntity(var0.v5suI) instanceof LoiteringMunitionEntity var3) {
               var3.v5suI(var0.ibzfxic);
            }
         }
      });
      var1.get().setPacketHandled(true);
   }
}
