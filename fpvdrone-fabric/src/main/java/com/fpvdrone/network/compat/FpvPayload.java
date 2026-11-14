package com.fpvdrone.network.compat;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** One wire payload carrying a message id plus its encoded body; stands in for Forge's SimpleChannel framing. */
public record FpvPayload(int id, byte[] data) implements CustomPacketPayload {
    public static final Type<FpvPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("fpvdrone", "main"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FpvPayload> CODEC = StreamCodec.of(
        (buf, p) -> {
            buf.writeVarInt(p.id);
            buf.writeByteArray(p.data);
        },
        buf -> new FpvPayload(buf.readVarInt(), buf.readByteArray())
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    static FriendlyByteBuf wrap(byte[] data) {
        return new FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(data));
    }
}
