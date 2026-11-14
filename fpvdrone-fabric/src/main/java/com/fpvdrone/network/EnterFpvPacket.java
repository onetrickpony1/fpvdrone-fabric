package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.PilotBodyManager;
import com.fpvdrone.item.FpvGogglesItem;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.NetworkContext;

public class EnterFpvPacket {
    private final UUID droneId;

    public EnterFpvPacket(UUID droneId) {
        this.droneId = droneId;
    }

    public static void encode(EnterFpvPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
    }

    public static EnterFpvPacket decode(FriendlyByteBuf buf) {
        return new EnterFpvPacket(buf.readUUID());
    }

    public static void handle(EnterFpvPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get()
            .enqueueWork(
                () -> {
                    ServerPlayer player = ctx.get().getSender();
                    if (player != null) {
                        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                        if (helmet.getItem() instanceof FpvGogglesItem) {
                            Map<Integer, UUID> linkedDrones = FpvGogglesItem.getLinkedDrones(helmet);
                            if (!linkedDrones.containsValue(msg.droneId)) {
                                Optional<UUID> legacyLink = FpvGogglesItem.getLinkedDroneId(helmet);
                                if (legacyLink.isEmpty() || !legacyLink.get().equals(msg.droneId)) {
                                    return;
                                }
                            }

                            ServerLevel level = player.level();
                            if (!(level.getEntity(msg.droneId) instanceof DroneEntity drone)) {
                                ModNetwork.CHANNEL
                                    .send(
                                        PacketDistributor.PLAYER.with(() -> player), new FpvEntryRejectedPacket(msg.droneId, "fpvdrone.status.drone_not_found")
                                    );
                            } else if (!drone.hasBattery()) {
                                ModNetwork.CHANNEL
                                    .send(PacketDistributor.PLAYER.with(() -> player), new FpvEntryRejectedPacket(msg.droneId, "fpvdrone.status.no_battery"));
                            } else {
                                PilotBodyManager.onEnterFPV(player);
                                drone.setInFpv(true);
                                drone.ensureAboveGround();
                                FPVMod.setActiveDrone(player.getUUID(), msg.droneId);
                                player.refreshDimensions();
                                ModNetwork.CHANNEL
                                    .send(
                                        PacketDistributor.PLAYER.with(() -> player),
                                        new DronePositionResponsePacket(msg.droneId, drone.getX(), drone.getY(), drone.getZ(), true)
                                    );
                                ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), new FpvStatePacket(player.getUUID(), true));
                                player.noPhysics = true;
                                player.getAbilities().flying = true;
                                player.getAbilities().mayfly = true;
                                player.fallDistance = 0.0F;
                                player.snapTo(drone.getX(), drone.getY(), drone.getZ(), player.getYRot(), player.getXRot());
                                player.setOldPosAndRot();
                                player.setYHeadRot(player.getYRot());
                                player.setYBodyRot(player.getYRot());
                            }
                        }
                    }
                }
            );
        ctx.get().setPacketHandled(true);
    }
}
