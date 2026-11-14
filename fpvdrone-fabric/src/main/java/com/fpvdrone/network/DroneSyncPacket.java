package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.sync.RemoteStateBroadcaster;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.entity.DroneChunkTickets;
import com.fpvdrone.network.compat.NetworkContext;

public class DroneSyncPacket {
   private final UUID droneId;
   private final double ibzfxic;
   private final double y;
   private final double dOfFwvo;
   private final float yaw;
   private final float pitch;
   private final float roll;
   private final double vx;
   private final double vy;
   private final double vz;
   private final boolean controlled;
   private final boolean motorsArmed;
   private final Optional<UUID> ownerUUID;
   private final float throttle;
   private final float qx;
   private final float rxYnaa;
   private final float qy;
   private final float qz;
   private final float qw;

   public DroneSyncPacket(
      UUID var1,
      double var2,
      double var4,
      double var6,
      float var8,
      float var9,
      float var10,
      double var11,
      double var13,
      double var15,
      boolean var17,
      boolean var18,
      Optional<UUID> var19,
      float var20,
      float var21,
      float var22,
      float var23,
      float var24,
      float var25
   ) {
      this.droneId = var1;
      this.ibzfxic = var2;
      this.y = var4;
      this.dOfFwvo = var6;
      this.yaw = var8;
      this.pitch = var9;
      this.roll = var10;
      this.vx = var11;
      this.vy = var13;
      this.vz = var15;
      this.controlled = var17;
      this.motorsArmed = var18;
      this.ownerUUID = var19;
      this.throttle = var20;
      this.qx = var21;
      this.rxYnaa = var22;
      this.qy = var23;
      this.qz = var24;
      this.qw = var25;
   }

   public DroneSyncPacket(
      UUID var1,
      double var2,
      double var4,
      double var6,
      float var8,
      float var9,
      float var10,
      double var11,
      double var13,
      double var15,
      boolean var17,
      boolean var18,
      Optional<UUID> var19,
      float var20,
      float var21,
      float var22,
      float var23,
      float var24
   ) {
      this(var1, var2, var4, var6, var8, var9, var10, var11, var13, var15, var17, var18, var19, var20, var21, var22, var23, var24, 0.0F);
   }

   public DroneSyncPacket(
      UUID var1,
      double var2,
      double var4,
      double var6,
      float var8,
      float var9,
      float var10,
      double var11,
      double var13,
      double var15,
      boolean var17,
      boolean var18,
      Optional<UUID> var19,
      float var20
   ) {
      this(var1, var2, var4, var6, var8, var9, var10, var11, var13, var15, var17, var18, var19, var20, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public DroneSyncPacket(
      UUID var1,
      double var2,
      double var4,
      double var6,
      float var8,
      float var9,
      float var10,
      double var11,
      double var13,
      double var15,
      boolean var17,
      boolean var18
   ) {
      this(var1, var2, var4, var6, var8, var9, var10, var11, var13, var15, var17, var18, Optional.empty(), 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public DroneSyncPacket(
      UUID var1,
      double var2,
      double var4,
      double var6,
      float var8,
      float var9,
      float var10,
      double var11,
      double var13,
      double var15,
      boolean var17,
      boolean var18,
      Optional<UUID> var19
   ) {
      this(var1, var2, var4, var6, var8, var9, var10, var11, var13, var15, var17, var18, var19, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
   }

   public static void encode(DroneSyncPacket var0, FriendlyByteBuf var1) {
      var1.writeUUID(var0.droneId);
      var1.writeDouble(var0.ibzfxic);
      var1.writeDouble(var0.y);
      var1.writeDouble(var0.dOfFwvo);
      var1.writeFloat(var0.yaw);
      var1.writeFloat(var0.pitch);
      var1.writeFloat(var0.roll);
      var1.writeDouble(var0.vx);
      var1.writeDouble(var0.vy);
      var1.writeDouble(var0.vz);
      var1.writeBoolean(var0.controlled);
      var1.writeBoolean(var0.motorsArmed);
      var1.writeBoolean(var0.ownerUUID.isPresent());
      var0.ownerUUID.ifPresent(var1::writeUUID);
      var1.writeFloat(var0.throttle);
      var1.writeFloat(var0.qx);
      var1.writeFloat(var0.rxYnaa);
      var1.writeFloat(var0.qy);
      var1.writeFloat(var0.qz);
      var1.writeFloat(var0.qw);
   }

   public static DroneSyncPacket decode(FriendlyByteBuf var0) {
      UUID var1 = var0.readUUID();
      double var2 = var0.readDouble();
      double var4 = var0.readDouble();
      double var6 = var0.readDouble();
      float var8 = var0.readFloat();
      float var9 = var0.readFloat();
      float var10 = var0.readFloat();
      double var11 = var0.readDouble();
      double var13 = var0.readDouble();
      double var15 = var0.readDouble();
      boolean var17 = var0.readBoolean();
      boolean var18 = var0.readBoolean();
      boolean var19 = var0.readBoolean();
      Optional var20 = var19 ? Optional.of(var0.readUUID()) : Optional.empty();
      float var21 = var0.readFloat();
      float var22 = var0.readFloat();
      float var23 = var0.readFloat();
      float var24 = var0.readFloat();
      float var25 = var0.readFloat();
      float var26 = var0.readFloat();
      return new DroneSyncPacket(var1, var2, var4, var6, var8, var9, var10, var11, var13, var15, var17, var18, var20, var21, var22, var23, var24, var25, var26);
   }

   public static void handle(DroneSyncPacket var0, Supplier<NetworkContext> var1) {
      var1.get().enqueueWork(() -> {
         ServerPlayer var2 = var1.get().getSender();
         if (var2 != null) {
            ServerLevel var3 = var2.level();
            DroneEntity var5 = var3.getEntity(var0.droneId) instanceof DroneEntity var6 ? var6 : null;
            if (var5 == null) {
               int var10 = (int)Math.floor(var0.ibzfxic) >> 4;
               int var7 = (int)Math.floor(var0.dOfFwvo) >> 4;
               DroneChunkTickets.forceChunk(var3, var10, var7, true);
               var5 = var3.getEntity(var0.droneId) instanceof DroneEntity var9 ? var9 : null;
               if (var5 == null) {
                  return;
               }
            }

            if (!var5.getOwner().isPresent() || var5.getOwner().get().equals(var2.getUUID()) || !var5.isBeingControlled()) {
               FPVMod.updateLastKnownDronePosition(var0.droneId, var0.ibzfxic, var0.dOfFwvo);
               boolean var11 = var5.isBeingControlled();
               var5.setYRot(var0.yaw);
               var5.setXRot(var0.pitch);
               var5.setDroneRoll(var0.roll);
               var5.setYBodyRot(var0.yaw);
               var5.setYHeadRot(var0.yaw);
               var5.setPos(var0.ibzfxic, var0.y, var0.dOfFwvo);
               var5.setDeltaMovement(var0.vx, var0.vy, var0.vz);
               var5.setBeingControlled(var0.controlled);
               if (!var0.controlled) {
                  var5.v5suI(false);
               }

               var5.uSUb(var0.motorsArmed);
               var5.setSyncedThrottle(var0.throttle);
               var5.c1jHYPb4(var0.qw);
               if (var0.controlled) {
                  var2.noPhysics = true;
                  var5.wuhB(false);
                  var5.wEJx3Itb();
                  var5.setOwner(var2.getUUID());
                  var2.getAbilities().flying = true;
                  var2.getAbilities().mayfly = true;
                  var2.fallDistance = 0.0F;
                  var2.snapTo(var0.ibzfxic, var0.y, var0.dOfFwvo, var0.yaw, var0.pitch);
                  var2.setOldPosAndRot();
               }

               if (var0.controlled || var11) {
                  var5.v5suI(var0.qx, var0.rxYnaa, var0.qy, var0.qz);
                  RemoteStateBroadcaster.v5suI(var5, var5);
               }
            }
         }
      });
      var1.get().setPacketHandled(true);
   }
}
