package com.fpvdrone.network;

import com.fpvdrone.FPVClientMod;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import com.fpvdrone.network.compat.NetworkContext;

public class FpvEntryRejectedPacket {
    private final UUID droneId;
    private final String reason;

    public FpvEntryRejectedPacket(UUID droneId, String reason) {
        this.droneId = droneId;
        this.reason = reason;
    }

    public static void encode(FpvEntryRejectedPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeUtf(msg.reason);
    }

    public static FpvEntryRejectedPacket decode(FriendlyByteBuf buf) {
        return new FpvEntryRejectedPacket(buf.readUUID(), buf.readUtf());
    }

    public static void handle(FpvEntryRejectedPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.sendOverlayMessage(Component.translatable(msg.reason).withStyle(Style.EMPTY.withColor(16749622)));
                FPVClientMod.markShouldRemoveGoggles(mc.player.getUUID());
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
