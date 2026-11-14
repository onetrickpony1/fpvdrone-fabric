package com.fpvdrone.network;

import com.fpvdrone.client.DroneConfig;
import com.fpvdrone.entity.DroneEntity;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.network.compat.NetworkContext;

public class DroneConfigPacket {
    private final int entityId;
    private final CompoundTag configData;

    public DroneConfigPacket(int entityId, DroneConfig config) {
        this.entityId = entityId;
        this.configData = config.toNbt();
    }

    private DroneConfigPacket(int entityId, CompoundTag configData) {
        this.entityId = entityId;
        this.configData = configData;
    }

    public static void encode(DroneConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeNbt(msg.configData);
    }

    public static DroneConfigPacket decode(FriendlyByteBuf buf) {
        return new DroneConfigPacket(buf.readInt(), buf.readNbt());
    }

    public static void handle(DroneConfigPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.level();
                if (level.getEntity(msg.entityId) instanceof DroneEntity drone) {
                    DroneConfig config = DroneConfig.fromNbt(msg.configData);
                    drone.setConfig(config);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
