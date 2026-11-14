package com.fpvdrone.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import com.fpvdrone.network.compat.NetworkContext;

public class PlayerTeleportPacket {
    private final UUID playerUUID;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public PlayerTeleportPacket(UUID playerUUID, double x, double y, double z, float yaw, float pitch) {
        this.playerUUID = playerUUID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static void encode(PlayerTeleportPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUUID);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
    }

    public static PlayerTeleportPacket decode(FriendlyByteBuf buf) {
        return new PlayerTeleportPacket(buf.readUUID(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(PlayerTeleportPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                if (mc.player == null || !mc.player.getUUID().equals(msg.playerUUID)) {
                    Player player = mc.level.getPlayerByUUID(msg.playerUUID);
                    if (player != null) {
                        player.setPos(msg.x, msg.y, msg.z);
                        player.setOldPosAndRot();
                        player.setYRot(msg.yaw);
                        player.setXRot(msg.pitch);
                        player.yRotO = msg.yaw;
                        player.xRotO = msg.pitch;
                        player.setYHeadRot(msg.yaw);
                        player.yHeadRotO = msg.yaw;
                        player.setYBodyRot(msg.yaw);
                        player.yBodyRotO = msg.yaw;
                        player.snapTo(msg.x, msg.y, msg.z, msg.yaw, msg.pitch);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
