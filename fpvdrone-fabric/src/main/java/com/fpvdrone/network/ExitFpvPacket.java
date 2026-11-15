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
    /** 1.1.8: the pilot left during return-to-home; the server keeps flying the drone home. */
    private final boolean returningHome;

    public ExitFpvPacket(UUID droneId) {
        this(droneId, false);
    }

    public ExitFpvPacket(UUID droneId, boolean returningHome) {
        this.droneId = droneId;
        this.returningHome = returningHome;
    }

    public static void encode(ExitFpvPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeBoolean(msg.returningHome);
    }

    public static ExitFpvPacket decode(FriendlyByteBuf buf) {
        return new ExitFpvPacket(buf.readUUID(), buf.readBoolean());
    }

    public static void handle(ExitFpvPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                ServerLevel level = player.level();
                Entity entity = level.getEntity(msg.droneId);
                PilotBodyManager.onExitFPV(player);
                FPVMod.clearFpvReturnPosition(player);
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
                    // 1.1.8: a Mavic still in the air (0.75+ blocks) with a battery finishes its return home on its own.
                    if (msg.returningHome && drone.xogV5KBhg() && drone.isAirborneAbove(0.75) && drone.hasBattery()) {
                        drone.startServerReturnHome(player.getX(), player.getY(), player.getZ());
                    }
                } else {
                    FPVMod.scheduleDroneCleanup(msg.droneId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
