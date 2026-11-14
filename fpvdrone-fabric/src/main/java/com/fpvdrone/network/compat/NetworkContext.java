package com.fpvdrone.network.compat;

import net.minecraft.server.level.ServerPlayer;

/** Minimal replacement for Forge's NetworkEvent.Context. Fabric already runs play handlers on the game thread. */
public final class NetworkContext {
    private final ServerPlayer sender;

    NetworkContext(ServerPlayer sender) {
        this.sender = sender;
    }

    public void enqueueWork(Runnable work) {
        work.run();
    }

    /** The sending player for client-to-server messages, null on the client. */
    public ServerPlayer getSender() {
        return this.sender;
    }

    public void setPacketHandled(boolean handled) {
    }
}
