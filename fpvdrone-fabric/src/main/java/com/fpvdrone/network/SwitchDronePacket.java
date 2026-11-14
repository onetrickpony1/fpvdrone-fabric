package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.NetworkContext;

public class SwitchDronePacket {
    private final UUID oldDroneId;
    private final UUID newDroneId;

    public SwitchDronePacket(UUID oldDroneId, UUID newDroneId) {
        this.oldDroneId = oldDroneId;
        this.newDroneId = newDroneId;
    }

    public static void encode(SwitchDronePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.oldDroneId);
        buf.writeUUID(msg.newDroneId);
    }

    public static SwitchDronePacket decode(FriendlyByteBuf buf) {
        return new SwitchDronePacket(buf.readUUID(), buf.readUUID());
    }

    public static void handle(SwitchDronePacket msg, Supplier<NetworkContext> ctx) {
        ctx.get()
            .enqueueWork(
                () -> {
                    ServerPlayer player = ctx.get().getSender();
                    if (player != null) {
                        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                        if (helmet.getItem() instanceof FpvGogglesItem) {
                            Map<Integer, UUID> linkedDrones = FpvGogglesItem.getLinkedDrones(helmet);
                            if (linkedDrones.containsValue(msg.newDroneId)) {
                                ServerLevel level = player.level();
                                if (!(level.getEntity(msg.newDroneId) instanceof DroneEntity newDroneCheck && !newDroneCheck.hasBattery())) {
                                    if (level.getEntity(msg.oldDroneId) instanceof DroneEntity oldDrone) {
                                        oldDrone.setMotorsArmed(false);
                                        oldDrone.setInFpv(false);
                                        oldDrone.setBeingControlled(false);
                                    }

                                    FPVMod.releaseServerDroneTicket(msg.oldDroneId, player.level());
                                    FPVMod.setActiveDrone(player.getUUID(), msg.newDroneId);
                                    if (level.getEntity(msg.newDroneId) instanceof DroneEntity newDrone) {
                                        newDrone.setInFpv(true);
                                        newDrone.ensureAboveGround();
                                        player.snapTo(newDrone.getX(), newDrone.getY(), newDrone.getZ(), player.getYRot(), player.getXRot());
                                        player.setOldPosAndRot();
                                        ModNetwork.CHANNEL
                                            .send(
                                                PacketDistributor.PLAYER.with(() -> player),
                                                new DronePositionResponsePacket(
                                                    msg.newDroneId, newDrone.getX(), newDrone.getY(), newDrone.getZ(), true
                                                )
                                            );
                                    } else {
                                        ModNetwork.CHANNEL
                                            .send(
                                                PacketDistributor.PLAYER.with(() -> player),
                                                new DronePositionResponsePacket(msg.newDroneId, 0.0, 0.0, 0.0, false)
                                            );
                                    }

                                    ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new FpvStatePacket(player.getUUID(), true));
                                }
                            }
                        }
                    }
                }
            );
        ctx.get().setPacketHandled(true);
    }
}
