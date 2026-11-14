package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

public class DroneSignalLostPacket {
    private final UUID droneId;

    public DroneSignalLostPacket(UUID droneId) {
        this.droneId = droneId;
    }

    public static void encode(DroneSignalLostPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
    }

    public static DroneSignalLostPacket decode(FriendlyByteBuf buf) {
        return new DroneSignalLostPacket(buf.readUUID());
    }

    public static void handle(DroneSignalLostPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.level();
                if (level.getEntity(msg.droneId) instanceof DroneEntity drone) {
                    UUID serverDroneId = FPVMod.getServerActiveDroneId(player.getUUID());
                    if (serverDroneId != null && serverDroneId.equals(msg.droneId)) {
                        if (!drone.isFalling()) {
                            drone.setSignalLossFall(true);
                            drone.setBeingControlled(false);
                            if (drone.hasRpg()) {
                                drone.detonateRpg();
                            } else {
                                drone.dropAsItem();
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
