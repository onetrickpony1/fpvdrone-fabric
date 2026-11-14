package com.fpvdrone.network;

import com.fpvdrone.entity.DroneEntity;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

/**
 * 1.1.8 (Packet_CAbMHP3JTv): the pilot's client reports a hard crash (12+ m/s, no warhead) with the impact normal; the
 * server downs the drone (spin-out, or tumble from 18 m/s).
 */
public class DroneCrashPacket {
   private final UUID v5suI;
   private final float ibzfxic;
   private final float wuhB;
   private final float dOfFwvo;
   private final float rsDd50tfV;

   public DroneCrashPacket(UUID var1, float var2, float var3, float var4, float var5) {
      this.v5suI = var1;
      this.ibzfxic = var2;
      this.wuhB = var3;
      this.dOfFwvo = var4;
      this.rsDd50tfV = var5;
   }

   public static void encode(DroneCrashPacket var0, FriendlyByteBuf var1) {
      var1.writeUUID(var0.v5suI);
      var1.writeFloat(var0.ibzfxic);
      var1.writeFloat(var0.wuhB);
      var1.writeFloat(var0.dOfFwvo);
      var1.writeFloat(var0.rsDd50tfV);
   }

   public static DroneCrashPacket decode(FriendlyByteBuf var0) {
      return new DroneCrashPacket(var0.readUUID(), var0.readFloat(), var0.readFloat(), var0.readFloat(), var0.readFloat());
   }

   public static void handle(DroneCrashPacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         ServerPlayer var2 = var1.get().getSender();
         if (var2 != null) {
            ServerLevel var3 = (ServerLevel)var2.level();
            if (var3.getEntity(var0.v5suI) instanceof DroneEntity var5) {
               if (!var5.getOwner().isPresent() || var5.getOwner().get().equals(var2.getUUID()) || !var5.isBeingControlled()) {
                  var5.crashFromImpact(var0.ibzfxic, var0.wuhB, var0.dOfFwvo, var0.rsDd50tfV);
               }
            }
         }
      });
      var1.get().setPacketHandled(true);
   }
}
