package com.fpvdrone.network;

import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroneLinkManager;
import com.fpvdrone.item.FpvGogglesItem;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.NetworkContext;

public class ChangeGogglesChannelPacket {
    private final int direction;

    public ChangeGogglesChannelPacket(int direction) {
        this.direction = direction;
    }

    public static void encode(ChangeGogglesChannelPacket msg, FriendlyByteBuf buf) {
        buf.writeByte(msg.direction);
    }

    public static ChangeGogglesChannelPacket decode(FriendlyByteBuf buf) {
        return new ChangeGogglesChannelPacket(buf.readByte());
    }

    public static void handle(ChangeGogglesChannelPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                ItemStack goggles;
                if (helmet.getItem() instanceof FpvGogglesItem) {
                    goggles = helmet;
                } else {
                    goggles = player.getMainHandItem();
                    if (!(goggles.getItem() instanceof FpvGogglesItem)) {
                        return;
                    }
                }

                int currentChannel = FpvGogglesItem.getChannel(goggles);
                Map<Integer, UUID> linkedDrones = FpvGogglesItem.getLinkedDrones(goggles);
                if (linkedDrones.size() > 1) {
                    ServerLevel level = player.level();
                    TreeSet<Integer> channels = new TreeSet<>();

                    for (Entry<Integer, UUID> e : linkedDrones.entrySet()) {
                        int ch = e.getKey();
                        if (ch == currentChannel) {
                            channels.add(ch);
                        } else if (level.getEntity(e.getValue()) instanceof DroneEntity drone && drone.hasBattery()) {
                            channels.add(ch);
                        }
                    }

                    if (channels.size() > 1) {
                        int dir = msg.direction > 0 ? 1 : -1;
                        int var14;
                        if (dir > 0) {
                            Integer next = channels.higher(currentChannel);
                            var14 = next != null ? next : channels.first();
                        } else {
                            Integer prev = channels.lower(currentChannel);
                            var14 = prev != null ? prev : channels.last();
                        }

                        if (var14 != currentChannel) {
                            if (var14 != currentChannel) {
                                UUID newDroneId = linkedDrones.get(var14);
                                if (newDroneId == null) {
                                    return;
                                }

                                FpvGogglesItem.setChannel(goggles, var14);
                                FpvGogglesItem.setLinkedDroneId(goggles, newDroneId);
                                DroneLinkManager.linkDrone(newDroneId, player.getUUID());
                                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ChannelChangedResponsePacket(var14));
                            }
                        }
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
