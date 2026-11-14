package com.fpvdrone.network;

import com.fpvdrone.FPVClientMod;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;

public class DroneDestroyedPacket {
    private final UUID droneId;

    public DroneDestroyedPacket(UUID droneId) {
        this.droneId = droneId;
    }

    public static void encode(DroneDestroyedPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
    }

    public static DroneDestroyedPacket decode(FriendlyByteBuf buf) {
        return new DroneDestroyedPacket(buf.readUUID());
    }

    public static void handle(DroneDestroyedPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> FPVClientMod.onDroneDestroyed(msg.droneId));
        ctx.get().setPacketHandled(true);
    }
}
