package com.fpvdrone.network;

import com.fpvdrone.client.FpvPlayerTracker;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import com.fpvdrone.network.compat.NetworkContext;

public class FpvStatePacket {
    private final UUID playerUUID;
    private final boolean entering;
    private final float exitYaw;
    private final float exitPitch;

    public FpvStatePacket(UUID playerUUID, boolean entering) {
        this(playerUUID, entering, 0.0F, 0.0F);
    }

    public FpvStatePacket(UUID playerUUID, boolean entering, float exitYaw, float exitPitch) {
        this.playerUUID = playerUUID;
        this.entering = entering;
        this.exitYaw = exitYaw;
        this.exitPitch = exitPitch;
    }

    public static void encode(FpvStatePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUUID);
        buf.writeBoolean(msg.entering);
        if (!msg.entering) {
            buf.writeFloat(msg.exitYaw);
            buf.writeFloat(msg.exitPitch);
        }
    }

    public static FpvStatePacket decode(FriendlyByteBuf buf) {
        UUID playerUUID = buf.readUUID();
        boolean entering = buf.readBoolean();
        float yaw = 0.0F;
        float pitch = 0.0F;
        if (!entering) {
            yaw = buf.readFloat();
            pitch = buf.readFloat();
        }

        return new FpvStatePacket(playerUUID, entering, yaw, pitch);
    }

    public static void handle(FpvStatePacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || !mc.player.getUUID().equals(msg.playerUUID)) {
                if (msg.entering) {
                    FpvPlayerTracker.freeze(msg.playerUUID);
                } else {
                    if (mc.level != null) {
                        Player player = mc.level.getPlayerByUUID(msg.playerUUID);
                        if (player != null) {
                            player.setYRot(msg.exitYaw);
                            player.setXRot(msg.exitPitch);
                            player.yRotO = msg.exitYaw;
                            player.xRotO = msg.exitPitch;
                            player.setYHeadRot(msg.exitYaw);
                            player.yHeadRotO = msg.exitYaw;
                            player.setYBodyRot(msg.exitYaw);
                            player.yBodyRotO = msg.exitYaw;
                            player.lerpHeadTo(msg.exitYaw, 0);
                            player.snapTo(player.getX(), player.getY(), player.getZ(), msg.exitYaw, msg.exitPitch);
                        }
                    }

                    FpvPlayerTracker.unfreeze(msg.playerUUID, msg.exitYaw, msg.exitPitch);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
