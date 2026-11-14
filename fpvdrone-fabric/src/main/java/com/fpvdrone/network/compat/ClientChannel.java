package com.fpvdrone.network.compat;

import com.fpvdrone.network.ModNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Client-only half of the channel; only touched from client code paths. */
public final class ClientChannel {
    private ClientChannel() {
    }

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(FpvPayload.TYPE, (payload, ctx) -> ModNetwork.CHANNEL.handle(payload, null));
    }

    static void send(FpvPayload payload) {
        if (ClientPlayNetworking.canSend(FpvPayload.TYPE)) {
            ClientPlayNetworking.send(payload);
        }
    }
}
