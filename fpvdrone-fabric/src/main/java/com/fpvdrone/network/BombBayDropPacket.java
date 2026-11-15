package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.data.DroneDefinition;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroppedBombEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.network.compat.NetworkContext;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 Packet_L8NbAxp3X8, client to server: the pilot released one station of the drone's bomb bay (the definition's
 * "BombBay": one projectile type, several stations each with its own attach point). Like {@link DropPayloadPacket}, the
 * client has already spawned a predicted bomb; the server spawns the real one if that station is still loaded and empties
 * it.
 */
public class BombBayDropPacket {
   private static final double MAX_OFFSET = 32.0;
   private static final double SPEED_TICKS = 20.0;
   private final UUID droneId;
   private final int station;
   private final double x;
   private final double y;
   private final double z;
   private final double velX;
   private final double velY;
   private final double velZ;
   private final float noseX;
   private final float noseY;
   private final float noseZ;
   private final long releaseTick;

   public BombBayDropPacket(
      UUID droneId, int station, double x, double y, double z, double velX, double velY, double velZ, float noseX, float noseY, float noseZ, long releaseTick
   ) {
      this.droneId = droneId;
      this.station = station;
      this.x = x;
      this.y = y;
      this.z = z;
      this.velX = velX;
      this.velY = velY;
      this.velZ = velZ;
      this.noseX = noseX;
      this.noseY = noseY;
      this.noseZ = noseZ;
      this.releaseTick = releaseTick;
   }

   public static void encode(BombBayDropPacket msg, FriendlyByteBuf buf) {
      buf.writeUUID(msg.droneId);
      buf.writeVarInt(msg.station);
      buf.writeDouble(msg.x);
      buf.writeDouble(msg.y);
      buf.writeDouble(msg.z);
      buf.writeDouble(msg.velX);
      buf.writeDouble(msg.velY);
      buf.writeDouble(msg.velZ);
      buf.writeFloat(msg.noseX);
      buf.writeFloat(msg.noseY);
      buf.writeFloat(msg.noseZ);
      buf.writeLong(msg.releaseTick);
   }

   public static BombBayDropPacket decode(FriendlyByteBuf buf) {
      return new BombBayDropPacket(
         buf.readUUID(),
         buf.readVarInt(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readFloat(),
         buf.readFloat(),
         buf.readFloat(),
         buf.readLong()
      );
   }

   public static void handle(BombBayDropPacket msg, Supplier<NetworkContext> ctx) {
      ctx.get().enqueueWork(() -> {
         ServerPlayer player = ctx.get().getSender();
         if (player != null && mayControl(player, msg.droneId)) {
            if (FPVMod.getServerActiveDroneId(player.getUUID()) == null) {
               FPVMod.setActiveDrone(player.getUUID(), msg.droneId);
            }

            ServerLevel level = player.level();
            if (level.getEntity(msg.droneId) instanceof DroneEntity drone) {
               DroneDefinition def = definition(drone);
               if (def != null && def.jGhI2() && msg.station >= 0 && msg.station < def.uKpZYj().stations().size() && isStationLoaded(drone, msg.station)) {
                  Vec3 pos = clampToDrone(new Vec3(msg.x, msg.y, msg.z), drone);
                  DroppedBombEntity bomb = DroppedBombEntity.v5suI(
                     level,
                     pos,
                     new Vec3(msg.velX, msg.velY, msg.velZ),
                     new Vec3(msg.noseX, msg.noseY, msg.noseZ),
                     def.uKpZYj().projectile(),
                     player.getUUID(),
                     drone.getId(),
                     0.0F,
                     0.0F,
                     0.0F,
                     1.0F,
                     msg.station,
                     msg.releaseTick
                  );
                  level.addFreshEntity(bomb);
                  emptyStation(drone, msg.station);
               }
            }
         }
      });
      ctx.get().setPacketHandled(true);
   }

   /** The client's release point, pulled back to at most 32 blocks + 20 ticks of the drone's speed from the drone. */
   static Vec3 clampToDrone(Vec3 requested, DroneEntity drone) {
      Vec3 dronePos = drone.position();
      double max = MAX_OFFSET + drone.getDeltaMovement().length() * SPEED_TICKS;
      Vec3 offset = requested.subtract(dronePos);
      double distSq = offset.lengthSqr();
      if (distSq > max * max) {
         double dist = Math.sqrt(distSq);
         return dist > 1.0E-6 ? dronePos.add(offset.scale(max / dist)) : dronePos;
      }

      return requested;
   }

   /**
    * 1.1.8 EnterFpvPacket.v5suI(ServerPlayer, UUID): may this player fly (and so drop from) this drone? Their active
    * drone, a drone linked to any player, their own drone, one linked to the goggles they wear, or one bound to a DJI
    * controller or remote control in their inventory.
    */
   static boolean mayControl(ServerPlayer player, UUID droneId) {
      if (droneId == null) {
         return false;
      }

      if (droneId.equals(FPVMod.getServerActiveDroneId(player.getUUID()))) {
         return true;
      }

      if (com.fpvdrone.entity.DroneLinkManager.isDroneLinked(droneId)) {
         return true;
      }

      if (player.level().getEntity(droneId) instanceof com.fpvdrone.entity.DroneEntity drone
         && drone.getOwner().isPresent() && drone.getOwner().get().equals(player.getUUID())) {
         return true;
      }

      ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
      if (helmet.getItem() instanceof FpvGogglesItem) {
         if (FpvGogglesItem.getLinkedDrones(helmet).containsValue(droneId)) {
            return true;
         }

         Optional<UUID> legacy = FpvGogglesItem.getLinkedDroneId(helmet);
         if (legacy.isPresent() && legacy.get().equals(droneId)) {
            return true;
         }
      }

      net.minecraft.world.entity.player.Inventory inventory = player.getInventory();
      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack stack = inventory.getItem(i);
         if (stack.getItem() instanceof com.fpvdrone.item.DjiControllerItem
            && com.fpvdrone.item.DjiControllerItem.getLinkedDroneId(stack).filter(droneId::equals).isPresent()) {
            return true;
         }

         if (stack.getItem() instanceof com.fpvdrone.item.RemoteControlItem
            && com.fpvdrone.item.RemoteControlItem.getLinkedDroneId(stack).filter(droneId::equals).isPresent()) {
            return true;
         }
      }

      return false;
   }

   private static @Nullable DroneDefinition definition(DroneEntity drone) {
      return drone.oEuCBB();
   }

   private static boolean isStationLoaded(DroneEntity drone, int station) {
      return drone.ibzfxic(station);
   }

   private static void emptyStation(DroneEntity drone, int station) {
      drone.wuhB(station);
   }
}
