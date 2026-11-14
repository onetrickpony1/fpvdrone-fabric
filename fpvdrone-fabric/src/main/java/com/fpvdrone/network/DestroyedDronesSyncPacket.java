package com.fpvdrone.network;

import com.fpvdrone.client.FpvStatusChecker;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;

public class DestroyedDronesSyncPacket {
    private final List<UUID> destroyedDroneIds;

    public DestroyedDronesSyncPacket(List<UUID> destroyedDroneIds) {
        this.destroyedDroneIds = destroyedDroneIds;
    }

    public static void encode(DestroyedDronesSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.destroyedDroneIds.size());

        for (UUID id : msg.destroyedDroneIds) {
            buf.writeUUID(id);
        }
    }

    public static DestroyedDronesSyncPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<UUID> ids = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            ids.add(buf.readUUID());
        }

        return new DestroyedDronesSyncPacket(ids);
    }

    public static void handle(DestroyedDronesSyncPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            for (UUID droneId : msg.destroyedDroneIds) {
                FpvStatusChecker.markDroneDestroyed(droneId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
