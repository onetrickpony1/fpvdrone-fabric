package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.PilotBodyManager;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import com.fpvdrone.network.compat.NetworkContext;

public class ExitFpvPacket {
    private final UUID droneId;

    public ExitFpvPacket(UUID droneId) {
        this.droneId = droneId;
    }

    public static void encode(ExitFpvPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
    }

    public static ExitFpvPacket decode(FriendlyByteBuf buf) {
        return new ExitFpvPacket(buf.readUUID());
    }

    public static void handle(ExitFpvPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.level();
                Entity entity = level.getEntity(msg.droneId);
                PilotBodyManager.onExitFPV(player);
                player.noPhysics = false;
                player.getAbilities().flying = false;
                player.getAbilities().mayfly = player.isCreative() || player.isSpectator();
                player.onUpdateAbilities();
                FPVMod.removeActiveDrone(player.getUUID());
                FPVMod.releaseServerDroneTicket(msg.droneId, level);
                player.refreshDimensions();
                if (entity instanceof DroneEntity drone) {
                    drone.setInFpv(false);
                    drone.setBeingControlled(false);
                } else {
                    FPVMod.scheduleDroneCleanup(msg.droneId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
