package com.fpvdrone.network;

import com.fpvdrone.item.FpvGogglesItem;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.fpvdrone.network.compat.NetworkContext;

public class SaveDronePositionPacket {
    private final UUID droneId;
    private final double x;
    private final double y;
    private final double z;

    public SaveDronePositionPacket(UUID droneId, double x, double y, double z) {
        this.droneId = droneId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static void encode(SaveDronePositionPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
    }

    public static SaveDronePositionPacket decode(FriendlyByteBuf buf) {
        return new SaveDronePositionPacket(buf.readUUID(), buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public static void handle(SaveDronePositionPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
                    if (stack.getItem() instanceof FpvGogglesItem) {
                        FpvGogglesItem.getLinkedDroneId(stack).ifPresent(linkedId -> {
                            if (linkedId.equals(msg.droneId)) {
                                FpvGogglesItem.setLastDronePosition(stack, msg.x, msg.y, msg.z);
                            }
                        });
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
