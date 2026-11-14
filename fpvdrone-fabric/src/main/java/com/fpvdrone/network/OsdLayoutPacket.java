package com.fpvdrone.network;

import com.fpvdrone.client.osd.OsdLayout;
import com.fpvdrone.client.osd.OsdLogger;
import com.fpvdrone.entity.DroneEntity;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

public class OsdLayoutPacket {
    private final int entityId;
    private final CompoundTag layoutData;

    public OsdLayoutPacket(int entityId, OsdLayout layout) {
        this.entityId = entityId;
        this.layoutData = layout.toNbt();
    }

    private OsdLayoutPacket(int entityId, CompoundTag layoutData) {
        this.entityId = entityId;
        this.layoutData = layoutData;
    }

    public int getEntityId() {
        return this.entityId;
    }

    public CompoundTag getLayoutData() {
        return this.layoutData;
    }

    public static void encode(OsdLayoutPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeNbt(msg.layoutData);
    }

    public static OsdLayoutPacket decode(FriendlyByteBuf buf) {
        return new OsdLayoutPacket(buf.readInt(), buf.readNbt());
    }

    public static void handle(OsdLayoutPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.level();
                if (level.getEntity(msg.entityId) instanceof DroneEntity drone) {
                    OsdLayout layout = OsdLayout.fromNbt(msg.layoutData);
                    drone.setOsdLayout(layout);
                    OsdLogger.networkInfo("Received OSD layout for drone {} from player {}", msg.entityId, player.getName().getString());
                } else {
                    OsdLogger.networkWarn("Packet for non-existent or non-drone entity {}", msg.entityId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
