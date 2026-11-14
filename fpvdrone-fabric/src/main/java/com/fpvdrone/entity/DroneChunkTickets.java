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

    /** Live ticket holders per (dimension, chunk, radius), so one owner releasing a chunk can't unload it for another. */
    private static final java.util.Map<String, Integer> HOLDERS = new java.util.HashMap<>();

    public static void forceChunk(ServerLevel level, int chunkX, int chunkZ, boolean add) {
        forceChunk(level, chunkX, chunkZ, add, true);
    }

    /**
     * Forge's forceChunk(level, mod, owner, x, z, add, ticking): ticking chunks get radius 2 (entities tick there),
     * the others radius 0 (loaded only).
     */
    public static synchronized void forceChunk(ServerLevel level, int chunkX, int chunkZ, boolean add, boolean ticking) {
        int radius = ticking ? RADIUS : 0;
        ChunkPos pos = new ChunkPos(chunkX, chunkZ);
        String key = level.dimension().identifier() + "|" + chunkX + "|" + chunkZ + "|" + radius;
        int held = HOLDERS.getOrDefault(key, 0);
        if (add) {
            if (held == 0) level.getChunkSource().addTicketWithRadius(DRONE, pos, radius);
            HOLDERS.put(key, held + 1);
        } else if (held > 0) {
            if (held == 1) {
                level.getChunkSource().removeTicketWithRadius(DRONE, pos, radius);
                HOLDERS.remove(key);
            } else {
                HOLDERS.put(key, held - 1);
            }
        }
    }
}
