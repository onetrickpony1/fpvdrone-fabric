package com.fpvdrone.network;

import com.fpvdrone.data.BatteryDefinitions;
import com.fpvdrone.data.DroneDefinitions;
import com.fpvdrone.data.DroneItemIndex;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.network.compat.NetworkContext;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

/** Server to client: the drone, battery and projectile definitions the server loaded (1.1.8). */
public class DefinitionSyncPacket {
   /** Set by the client: drops the client caches built from definitions (geo models, textures, item lists). */
   public static Runnable clientRefresh = () -> {};
   private final CompoundTag data;

   public DefinitionSyncPacket() {
      this.data = new CompoundTag();
      this.data.put("batteries", BatteryDefinitions.ibzfxic());
      this.data.put("projectiles", ProjectileDefinitions.ibzfxic());
      this.data.put("drones", DroneDefinitions.ibzfxic());
   }

   private DefinitionSyncPacket(CompoundTag data) {
      this.data = data;
   }

   public static void encode(DefinitionSyncPacket msg, FriendlyByteBuf buf) {
      buf.writeNbt(msg.data);
   }

   public static DefinitionSyncPacket decode(FriendlyByteBuf buf) {
      return new DefinitionSyncPacket(buf.readNbt());
   }

   public static void handle(DefinitionSyncPacket msg, Supplier<NetworkContext> ctx) {
      ctx.get().enqueueWork(() -> {
         CompoundTag tag = msg.data;
         if (tag != null) {
            BatteryDefinitions.v5suI(tag.getListOrEmpty("batteries"));
            ProjectileDefinitions.v5suI(tag.getListOrEmpty("projectiles"));
            DroneDefinitions.v5suI(tag.getListOrEmpty("drones"));
            DroneItemIndex.v5suI();
            clientRefresh.run();
         }
      });
      ctx.get().setPacketHandled(true);
   }
}
