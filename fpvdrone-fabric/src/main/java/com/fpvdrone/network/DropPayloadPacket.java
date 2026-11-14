package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroppedBombEntity;
import com.fpvdrone.network.compat.NetworkContext;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 Packet_HM8bbhcY, client to server: the pilot dropped the single payload hung on the drone's payload_mount (an
 * RGD-5, 40 mm grenade, TM-62...). The client has already spawned its own predicted bomb at the spot where the payload
 * was drawn; this asks the server for the real one at the same spot, velocity, nose, hang orientation and release tick.
 * <p>
 * The server trusts the client's position only within 32 blocks plus 20 ticks of the drone's own speed (clamped toward
 * the drone otherwise), so a modified client cannot drop a bomb anywhere on the map.
 */
public class DropPayloadPacket {
   private final UUID droneId;
   private final double x;
   private final double y;
   private final double z;
   private final double velX;
   private final double velY;
   private final double velZ;
   private final float noseX;
   private final float noseY;
   private final float noseZ;
   private final float qx;
   private final float qy;
   private final float qz;
   private final float qw;
   private final long releaseTick;

   public DropPayloadPacket(
      UUID droneId,
      double x,
      double y,
      double z,
      double velX,
      double velY,
      double velZ,
      float noseX,
      float noseY,
      float noseZ,
      float qx,
      float qy,
      float qz,
      float qw,
      long releaseTick
   ) {
      this.droneId = droneId;
      this.x = x;
      this.y = y;
      this.z = z;
      this.velX = velX;
      this.velY = velY;
      this.velZ = velZ;
      this.noseX = noseX;
      this.noseY = noseY;
      this.noseZ = noseZ;
      this.qx = qx;
      this.qy = qy;
      this.qz = qz;
      this.qw = qw;
      this.releaseTick = releaseTick;
   }

   public static void encode(DropPayloadPacket msg, FriendlyByteBuf buf) {
      buf.writeUUID(msg.droneId);
      buf.writeDouble(msg.x);
      buf.writeDouble(msg.y);
      buf.writeDouble(msg.z);
      buf.writeDouble(msg.velX);
      buf.writeDouble(msg.velY);
      buf.writeDouble(msg.velZ);
      buf.writeFloat(msg.noseX);
      buf.writeFloat(msg.noseY);
      buf.writeFloat(msg.noseZ);
      buf.writeFloat(msg.qx);
      buf.writeFloat(msg.qy);
      buf.writeFloat(msg.qz);
      buf.writeFloat(msg.qw);
      buf.writeLong(msg.releaseTick);
   }

   public static DropPayloadPacket decode(FriendlyByteBuf buf) {
      return new DropPayloadPacket(
         buf.readUUID(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readFloat(),
         buf.readFloat(),
         buf.readFloat(),
         buf.readFloat(),
         buf.readFloat(),
         buf.readFloat(),
         buf.readFloat(),
         buf.readLong()
      );
   }

   public static void handle(DropPayloadPacket msg, Supplier<NetworkContext> ctx) {
      ctx.get().enqueueWork(() -> {
         ServerPlayer player = ctx.get().getSender();
         if (player != null && BombBayDropPacket.mayControl(player, msg.droneId)) {
            if (FPVMod.getServerActiveDroneId(player.getUUID()) == null) {
               FPVMod.setActiveDrone(player.getUUID(), msg.droneId);
            }

            ServerLevel level = player.level();
            if (level.getEntity(msg.droneId) instanceof DroneEntity drone) {
               String projectileId = payloadId(drone);
               if (projectileId != null && !projectileId.isEmpty()) {
                  ProjectileDefinition def = com.fpvdrone.data.ProjectileDefinitions.ibzfxic(projectileId).orElse(null);
                  if (def != null && def.rsDd50tfV()) {
                     Vec3 pos = BombBayDropPacket.clampToDrone(new Vec3(msg.x, msg.y, msg.z), drone);
                     DroppedBombEntity bomb = DroppedBombEntity.v5suI(
                        level,
                        pos,
                        new Vec3(msg.velX, msg.velY, msg.velZ),
                        new Vec3(msg.noseX, msg.noseY, msg.noseZ),
                        projectileId,
                        player.getUUID(),
                        drone.getId(),
                        msg.qx,
                        msg.qy,
                        msg.qz,
                        msg.qw,
                        -1,
                        msg.releaseTick
                     );
                     level.addFreshEntity(bomb);
                     clearPayload(drone);
                  }
               }
            }
         }
      });
      ctx.get().setPacketHandled(true);
   }

   private static String payloadId(DroneEntity drone) {
      return drone.i263D9();
   }

   private static void clearPayload(DroneEntity drone) {
      drone.v5suI("");
   }
}
