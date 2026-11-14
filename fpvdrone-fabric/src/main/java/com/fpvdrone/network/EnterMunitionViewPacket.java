package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.LoiteringMunitionEntity;
import com.fpvdrone.entity.PilotBodyManager;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.NetworkContext;

public class EnterMunitionViewPacket {
   private final int v5suI;
   private final double ibzfxic;
   private final double wuhB;
   private final double dOfFwvo;
   private final float rsDd50tfV;
   private final float uSUb;

   public EnterMunitionViewPacket(int var1, double var2, double var4, double var6, float var8, float var9) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var4;
      this.dOfFwvo = var6;
      this.rsDd50tfV = var8;
      this.uSUb = var9;
   }

   public static void encode(EnterMunitionViewPacket var0, FriendlyByteBuf var1) {
      var1.writeVarInt(var0.v5suI);
      var1.writeDouble(var0.ibzfxic);
      var1.writeDouble(var0.wuhB);
      var1.writeDouble(var0.dOfFwvo);
      var1.writeFloat(var0.rsDd50tfV);
      var1.writeFloat(var0.uSUb);
   }

   public static EnterMunitionViewPacket decode(FriendlyByteBuf var0) {
      return new EnterMunitionViewPacket(var0.readVarInt(), var0.readDouble(), var0.readDouble(), var0.readDouble(), var0.readFloat(), var0.readFloat());
   }

   public static void handle(EnterMunitionViewPacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         ServerPlayer var2 = var1.get().getSender();
         if (var2 != null) {
            if (var2.level().getEntity(var0.v5suI) instanceof LoiteringMunitionEntity var4) {
               if (var4.v5suI(var2.getUUID()) && var4.uQdlyiwB().v8ZN()) {
                  if (!FPVMod.isServerPiloting(var2.getUUID())) {
                     if (var2.isPassenger()) {
                        var2.stopRiding();
                     }

                     var2.snapTo(var0.ibzfxic, var0.wuhB, var0.dOfFwvo, var0.rsDd50tfV, var0.uSUb);
                     var2.setOldPosAndRot();
                     FPVMod.saveFpvReturnPosition(var2, var0.ibzfxic, var0.wuhB, var0.dOfFwvo);
                     PilotBodyManager.onEnterFPV(var2);
                     FPVMod.setMunitionPilot(var2.getUUID(), var0.v5suI);
                     var2.refreshDimensions();
                     ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> var2), new FpvStatePacket(var2.getUUID(), true));
                     var2.noPhysics = true;
                     var2.getAbilities().flying = true;
                     var2.getAbilities().mayfly = true;
                     var2.fallDistance = 0.0F;
                     var2.snapTo(var4.getX(), var4.getY(), var4.getZ(), var2.getYRot(), var2.getXRot());
                     var2.setOldPosAndRot();
                  }
               }
            }
         }
      });
      var1.get().setPacketHandled(true);
   }
}
