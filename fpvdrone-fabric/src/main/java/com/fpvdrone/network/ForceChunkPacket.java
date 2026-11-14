package com.fpvdrone.network;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.entity.DroneChunkTickets;
import com.fpvdrone.network.compat.NetworkContext;

public class ForceChunkPacket {
    private final int chunkX;
    private final int chunkZ;
    private final boolean force;
    private static final int MAX_CHUNK_DISTANCE = 32;

    public ForceChunkPacket(int chunkX, int chunkZ, boolean force) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.force = force;
    }

    public static void encode(ForceChunkPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.chunkX);
        buf.writeInt(msg.chunkZ);
        buf.writeBoolean(msg.force);
    }

    public static ForceChunkPacket decode(FriendlyByteBuf buf) {
        return new ForceChunkPacket(buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public static void handle(ForceChunkPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                int playerChunkX = player.blockPosition().getX() >> 4;
                int playerChunkZ = player.blockPosition().getZ() >> 4;
                if (Math.abs(msg.chunkX - playerChunkX) <= 32 && Math.abs(msg.chunkZ - playerChunkZ) <= 32) {
                    ServerLevel level = player.level();
                    UUID playerId = player.getUUID();
                    DroneChunkTickets.forceChunk(level, msg.chunkX, msg.chunkZ, msg.force);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
