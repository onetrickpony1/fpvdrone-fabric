package com.fpvdrone.network;

import com.fpvdrone.network.compat.NetworkContext;
import com.fpvdrone.render.ShrapnelDebugRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;

/**
 * 1.1.8 (ShrapnelDebugPacket), server to client: every shrapnel ray of an explosion (where it stopped and whether it hit
 * something), for the debug view drawn with entity hitboxes on (F3+B). Sent to players within 64 blocks.
 */
public class ShrapnelDebugPacket {
   private final Vec3 center;
   private final List<Vec3> rayEnds;
   private final List<Boolean> rayHits;

   public ShrapnelDebugPacket(Vec3 center, List<Vec3> rayEnds, List<Boolean> rayHits) {
      this.center = center;
      this.rayEnds = rayEnds;
      this.rayHits = rayHits;
   }

   public static void encode(ShrapnelDebugPacket msg, FriendlyByteBuf buf) {
      buf.writeDouble(msg.center.x);
      buf.writeDouble(msg.center.y);
      buf.writeDouble(msg.center.z);
      buf.writeVarInt(msg.rayEnds.size());

      for (int i = 0; i < msg.rayEnds.size(); i++) {
         Vec3 end = msg.rayEnds.get(i);
         buf.writeDouble(end.x);
         buf.writeDouble(end.y);
         buf.writeDouble(end.z);
         buf.writeBoolean(msg.rayHits.get(i));
      }
   }

   public static ShrapnelDebugPacket decode(FriendlyByteBuf buf) {
      Vec3 center = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
      int count = buf.readVarInt();
      List<Vec3> ends = new ArrayList<>(count);
      List<Boolean> hits = new ArrayList<>(count);

      for (int i = 0; i < count; i++) {
         ends.add(new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
         hits.add(buf.readBoolean());
      }

      return new ShrapnelDebugPacket(center, ends, hits);
   }

   public static void handle(ShrapnelDebugPacket msg, Supplier<NetworkContext> ctx) {
      ctx.get().enqueueWork(() -> ShrapnelDebugRenderer.onShrapnelReceived(msg.center, msg.rayEnds, msg.rayHits));
      ctx.get().setPacketHandled(true);
   }
}
