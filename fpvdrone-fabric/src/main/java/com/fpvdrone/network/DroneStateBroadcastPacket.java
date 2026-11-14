package com.fpvdrone.network;

import com.fpvdrone.FPVClientMod;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;

/** 1.1.8: server to the pilot, where their standing body is now (it can be pushed or fall while they fly). */
public class DroneStateBroadcastPacket {
   private final double v5suI;
   private final double ibzfxic;
   private final double z;
   private final float dOfFwvo;
   private final float pitch;

   public DroneStateBroadcastPacket(double var1, double var3, double var5, float var7, float var8) {
      this.v5suI = var1;
      this.ibzfxic = var3;
      this.z = var5;
      this.dOfFwvo = var7;
      this.pitch = var8;
   }

   public static void encode(DroneStateBroadcastPacket var0, FriendlyByteBuf var1) {
      var1.writeDouble(var0.v5suI);
      var1.writeDouble(var0.ibzfxic);
      var1.writeDouble(var0.z);
      var1.writeFloat(var0.dOfFwvo);
      var1.writeFloat(var0.pitch);
   }

   public static DroneStateBroadcastPacket decode(FriendlyByteBuf var0) {
      return new DroneStateBroadcastPacket(var0.readDouble(), var0.readDouble(), var0.readDouble(), var0.readFloat(), var0.readFloat());
   }

   public static void handle(DroneStateBroadcastPacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         Minecraft var1x = Minecraft.getInstance();
         if (var1x.player != null) {
            FPVClientMod.updatePilotBodyPosition(var1x.player.getUUID(), var0.v5suI, var0.ibzfxic, var0.z, var0.dOfFwvo, var0.pitch);
         }
      });
      var1.get().setPacketHandled(true);
   }
}
