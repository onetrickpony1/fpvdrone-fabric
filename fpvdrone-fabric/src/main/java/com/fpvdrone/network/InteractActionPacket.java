package com.fpvdrone.network;

import com.fpvdrone.data.InteractZone;
import com.fpvdrone.interact.InteractContents;
import com.fpvdrone.interact.InteractExecutor;
import com.fpvdrone.network.compat.NetworkContext;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/**
 * 1.1.8 (Packet_ZrfXHAQ1T), client to server: the player chose an option in the interact menu of an aircraft or ramp.
 * The server looks the zone up in the entity's own definition, checks the player is within 16 blocks of it, and lets
 * the zone's content run the option.
 */
public class InteractActionPacket {
   private static final double MAX_DISTANCE_SQR = 256.0;
   private final int entityId;
   private final String zoneId;
   private final String optionId;

   public InteractActionPacket(int entityId, String zoneId, String optionId) {
      this.entityId = entityId;
      this.zoneId = zoneId;
      this.optionId = optionId;
   }

   public static void encode(InteractActionPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.entityId);
      buf.writeUtf(msg.zoneId);
      buf.writeUtf(msg.optionId);
   }

   public static InteractActionPacket decode(FriendlyByteBuf buf) {
      return new InteractActionPacket(buf.readVarInt(), buf.readUtf(), buf.readUtf());
   }

   public static void handle(InteractActionPacket msg, Supplier<NetworkContext> ctx) {
      ctx.get().enqueueWork(() -> {
         ServerPlayer player = ctx.get().getSender();
         if (player == null) {
            return;
         }

         Entity entity = player.level().getEntity(msg.entityId);
         if (entity == null || !entity.isAlive() || !InteractContents.hasZones(entity)) {
            return;
         }

         if (entity.getBoundingBox().distanceToSqr(player.getEyePosition()) > MAX_DISTANCE_SQR) {
            return;
         }

         InteractZone zone = InteractContents.zoneOf(entity, msg.zoneId);
         if (zone != null) {
            InteractExecutor executor = InteractContents.executor(zone.dOfFwvo());
            if (executor != null) {
               executor.execute(player, entity, zone, msg.optionId);
            }
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
