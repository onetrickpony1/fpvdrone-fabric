package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.PilotBodyManager;
import com.fpvdrone.item.FpvGogglesItem;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.fpvdrone.network.compat.NetworkContext;

public class RemoveGogglesPacket {
    private final UUID droneId;

    public RemoveGogglesPacket(UUID droneId) {
        this.droneId = droneId;
    }

    public static void encode(RemoveGogglesPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
    }

    public static RemoveGogglesPacket decode(FriendlyByteBuf buf) {
        return new RemoveGogglesPacket(buf.readUUID());
    }

    public static void handle(RemoveGogglesPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.level();
                Entity entity = level.getEntity(msg.droneId);
                ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
                if (helmet.getItem() instanceof FpvGogglesItem) {
                    player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    if (!player.getInventory().add(helmet)) {
                        player.drop(helmet, false, net.minecraft.util.Prediction.SERVER_ONLY);
                    }
                }

                player.noPhysics = false;
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = player.isCreative() || player.isSpectator();
                player.onUpdateAbilities();
                PilotBodyManager.onExitFPV(player);
                FPVMod.clearFpvReturnPosition(player);
                FPVMod.removeActiveDrone(player.getUUID());
                FPVMod.releaseServerDroneTicket(msg.droneId, player.level());
                if (entity instanceof DroneEntity drone) {
                    drone.setInFpv(false);
                    drone.setOwner(null);
                    drone.setBeingControlled(false);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
