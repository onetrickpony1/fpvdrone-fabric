package com.fpvdrone.network.compat;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/** Tracks the running server, replacing Forge's ServerHolder.server(). */
public final class ServerHolder {
    private static volatile MinecraftServer server;

    private ServerHolder() {
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);
    }

    public static MinecraftServer server() {
        return server;
    }
}
