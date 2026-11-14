package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;

public class DronePositionResponsePacket {
    private final UUID droneId;
    private final double x;
    private final double y;
    private final double z;
    private final boolean found;

    public DronePositionResponsePacket(UUID droneId, double x, double y, double z, boolean found) {
        this.droneId = droneId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.found = found;
    }

    public static void encode(DronePositionResponsePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeBoolean(msg.found);
    }

    public static DronePositionResponsePacket decode(FriendlyByteBuf buf) {
        return new DronePositionResponsePacket(buf.readUUID(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readBoolean());
    }

    public static void handle(DronePositionResponsePacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.found) {
                FPVMod.onDronePositionReceived(msg.droneId, msg.x, msg.y, msg.z);
            } else {
                FPVMod.onDroneNotFound(msg.droneId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
