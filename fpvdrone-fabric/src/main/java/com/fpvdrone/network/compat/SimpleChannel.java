package com.fpvdrone.network.compat;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/** Forge-SimpleChannel-shaped facade over a single Fabric payload type, so the packet classes keep their encode/decode/handle methods. */
public final class SimpleChannel {
    private final List<Message<?>> byId = new ArrayList<>();
    private final Map<Class<?>, Message<?>> byClass = new HashMap<>();

    public SimpleChannel() {
        PayloadTypeRegistry.serverboundPlay().register(FpvPayload.TYPE, FpvPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FpvPayload.TYPE, FpvPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FpvPayload.TYPE, (payload, ctx) -> this.handle(payload, ctx.player()));
    }

    public <M> void registerMessage(
        int id, Class<M> type, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<NetworkContext>> handler, boolean serverbound
    ) {
        Message<M> msg = new Message<>(id, encoder, decoder, handler, serverbound);
        while (this.byId.size() <= id) {
            this.byId.add(null);
        }
        this.byId.set(id, msg);
        this.byClass.put(type, msg);
    }

    @SuppressWarnings("unchecked")
    private <M> FpvPayload encode(M message) {
        Message<M> msg = (Message<M>) this.byClass.get(message.getClass());
        if (msg == null) {
            throw new IllegalArgumentException("Unregistered message " + message.getClass().getName());
        }
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            msg.encoder.accept(message, buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return new FpvPayload(msg.id, bytes);
        } finally {
            buf.release();
        }
    }

    /** Dispatches a received payload; sender is null on the client. */
    public void handle(FpvPayload payload, ServerPlayer sender) {
        Message<?> msg = payload.id() >= 0 && payload.id() < this.byId.size() ? this.byId.get(payload.id()) : null;
        if (msg != null && msg.serverbound == (sender != null)) {
            FriendlyByteBuf buf = FpvPayload.wrap(payload.data());
            try {
                msg.dispatch(buf, new NetworkContext(sender));
            } finally {
                buf.release();
            }
        }
    }

    public <M> void send(PacketDistributor.PacketTarget target, M message) {
        FpvPayload payload = this.encode(message);
        for (ServerPlayer player : target.players()) {
            if (ServerPlayNetworking.canSend(player, FpvPayload.TYPE)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    public <M> void sendToServer(M message) {
        ClientChannel.send(this.encode(message));
    }

    private record Message<M>(
        int id, BiConsumer<M, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<NetworkContext>> handler, boolean serverbound
    ) {
        void dispatch(FriendlyByteBuf buf, NetworkContext ctx) {
            M decoded = this.decoder.apply(buf);
            this.handler.accept(decoded, () -> ctx);
        }
    }
}
