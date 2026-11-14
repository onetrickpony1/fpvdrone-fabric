package com.fpvdrone.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

/** Non-persistent chunk tickets that keep a flying drone's chunk loaded and ticking (replaces ForgeChunkManager). */
public final class DroneChunkTickets {
    public static final TicketType DRONE = Registry.register(
        BuiltInRegistries.TICKET_TYPE,
        Identifier.fromNamespaceAndPath("fpvdrone", "drone"),
        new TicketType(0L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION | TicketType.FLAG_KEEP_DIMENSION_ACTIVE)
    );
    private static final int RADIUS = 2;

    private DroneChunkTickets() {
    }

    public static void init() {
    }

    public static void forceChunk(ServerLevel level, int chunkX, int chunkZ, boolean add) {
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        if (add) {
            level.getChunkSource().addTicketWithRadius(DRONE, pos, RADIUS);
        } else {
            level.getChunkSource().removeTicketWithRadius(DRONE, pos, RADIUS);
        }
    }
}
