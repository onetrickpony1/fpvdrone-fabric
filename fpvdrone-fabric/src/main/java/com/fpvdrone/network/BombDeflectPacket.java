package com.fpvdrone.network;

import com.fpvdrone.entity.DroppedBombEntity;
import com.fpvdrone.network.compat.NetworkContext;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 Packet_TwUF2U, server to client: a player batted a dropped bomb away ("Deflect"). Carries the bomb's new velocity,
 * nose direction and spin so every tracking client continues the same flight at once instead of waiting for position
 * updates (the bomb is simulated client-side between them).
 */
public class BombDeflectPacket {
   private final int entityId;
   private final double velX;
   private final double velY;
   private final double velZ;
   private final double noseX;
   private final double noseY;
   private final double noseZ;
   private final double spinX;
   private final double spinY;
   private final double spinZ;

   public BombDeflectPacket(int entityId, Vec3 velocity, Vec3 nose, Vec3 spin) {
      this(entityId, velocity.x, velocity.y, velocity.z, nose.x, nose.y, nose.z, spin.x, spin.y, spin.z);
   }

   private BombDeflectPacket(
      int entityId, double velX, double velY, double velZ, double noseX, double noseY, double noseZ, double spinX, double spinY, double spinZ
   ) {
      this.entityId = entityId;
      this.velX = velX;
      this.velY = velY;
      this.velZ = velZ;
      this.noseX = noseX;
      this.noseY = noseY;
      this.noseZ = noseZ;
      this.spinX = spinX;
      this.spinY = spinY;
      this.spinZ = spinZ;
   }

   public static void encode(BombDeflectPacket msg, FriendlyByteBuf buf) {
      buf.writeVarInt(msg.entityId);
      buf.writeDouble(msg.velX);
      buf.writeDouble(msg.velY);
      buf.writeDouble(msg.velZ);
      buf.writeDouble(msg.noseX);
      buf.writeDouble(msg.noseY);
      buf.writeDouble(msg.noseZ);
      buf.writeDouble(msg.spinX);
      buf.writeDouble(msg.spinY);
      buf.writeDouble(msg.spinZ);
   }

   public static BombDeflectPacket decode(FriendlyByteBuf buf) {
      return new BombDeflectPacket(
         buf.readVarInt(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble(),
         buf.readDouble()
      );
   }

   public static void handle(BombDeflectPacket msg, Supplier<NetworkContext> ctx) {
      ctx.get().enqueueWork(() -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.level != null && mc.level.getEntity(msg.entityId) instanceof DroppedBombEntity bomb) {
            bomb.v5suI(new Vec3(msg.velX, msg.velY, msg.velZ), new Vec3(msg.noseX, msg.noseY, msg.noseZ), new Vec3(msg.spinX, msg.spinY, msg.spinZ));
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
