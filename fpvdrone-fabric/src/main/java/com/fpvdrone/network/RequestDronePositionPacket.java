package com.fpvdrone.network;

import com.fpvdrone.entity.DroneEntity;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.NetworkContext;

public class RequestDronePositionPacket {
    private final UUID droneId;

    public RequestDronePositionPacket(UUID droneId) {
        this.droneId = droneId;
    }

    public static void encode(RequestDronePositionPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
    }

    public static RequestDronePositionPacket decode(FriendlyByteBuf buf) {
        return new RequestDronePositionPacket(buf.readUUID());
    }

    public static void handle(RequestDronePositionPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get()
            .enqueueWork(
                () -> {
                    ServerPlayer player = ctx.get().getSender();
                    if (player != null) {
                        ServerLevel level = player.level();
                        Entity entity = level.getEntity(msg.droneId);
                        if (entity != null) {
                            if (entity instanceof DroneEntity drone) {
                                drone.ensureAboveGround();
                            }

                            Vec3 pos = entity.position();
                            ModNetwork.CHANNEL
                                .send(
                                    PacketDistributor.PLAYER.with(() -> player),
                                    new DronePositionResponsePacket(msg.droneId, pos.x, pos.y, pos.z, true)
                                );
                        } else {
                            ModNetwork.CHANNEL
                                .send(PacketDistributor.PLAYER.with(() -> player), new DronePositionResponsePacket(msg.droneId, 0.0, 0.0, 0.0, false));
                        }
                    }
                }
            );
        ctx.get().setPacketHandled(true);
    }
}
