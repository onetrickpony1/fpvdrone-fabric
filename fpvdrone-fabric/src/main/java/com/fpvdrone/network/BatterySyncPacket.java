package com.fpvdrone.network;

import com.fpvdrone.entity.DroneEntity;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

public class BatterySyncPacket {
    private final UUID droneId;
    private final float usedMah;

    public BatterySyncPacket(UUID droneId, float usedMah) {
        this.droneId = droneId;
        this.usedMah = usedMah;
    }

    public static void encode(BatterySyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeFloat(msg.usedMah);
    }

    public static BatterySyncPacket decode(FriendlyByteBuf buf) {
        return new BatterySyncPacket(buf.readUUID(), buf.readFloat());
    }

    public static void handle(BatterySyncPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.level();
                if (level.getEntity(msg.droneId) instanceof DroneEntity drone && drone.getOwner().map(id -> id.equals(player.getUUID())).orElse(false)) {
                    drone.setServerUsedMah(msg.usedMah);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
