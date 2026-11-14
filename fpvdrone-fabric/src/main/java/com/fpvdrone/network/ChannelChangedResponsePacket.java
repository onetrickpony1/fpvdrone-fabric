package com.fpvdrone.network;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import com.fpvdrone.network.compat.NetworkContext;

public class ChannelChangedResponsePacket {
    private final int channel;

    public ChannelChangedResponsePacket(int channel) {
        this.channel = channel;
    }

    public static void encode(ChannelChangedResponsePacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.channel);
    }

    public static ChannelChangedResponsePacket decode(FriendlyByteBuf buf) {
        return new ChannelChangedResponsePacket(buf.readByte());
    }

    public static void handle(ChannelChangedResponsePacket msg, Supplier<NetworkContext> ctx) {
        ctx.get()
            .enqueueWork(
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.player != null) {
                        mc.player
                            .sendOverlayMessage(
                                Component.translatable("item.fpvdrone.fpv_goggles.channel_changed", new Object[]{"R" + msg.channel})
                                    .withStyle(Style.EMPTY.withColor(16749622))
                            );
                    }
                }
            );
        ctx.get().setPacketHandled(true);
    }
}
