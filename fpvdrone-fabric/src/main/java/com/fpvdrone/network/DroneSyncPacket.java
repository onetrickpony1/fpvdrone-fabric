package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.PilotBodyEntity;
import com.fpvdrone.entity.PilotBodyManager;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.fpvdrone.entity.DroneChunkTickets;
import com.fpvdrone.network.compat.PacketDistributor;
import com.fpvdrone.network.compat.NetworkContext;

public class DroneSyncPacket {
    private final UUID droneId;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final float roll;
    private final double vx;
    private final double vy;
    private final double vz;
    private final boolean controlled;
    private final boolean motorsArmed;
    private final Optional<UUID> ownerUUID;
    private final float throttle;
    private final float qx;
    private final float qy;
    private final float qz;
    private final float qw;

    public DroneSyncPacket(
        UUID droneId,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        float roll,
        double vx,
        double vy,
        double vz,
        boolean controlled,
        boolean motorsArmed,
        Optional<UUID> ownerUUID,
        float throttle,
        float qx,
        float qy,
        float qz,
        float qw
    ) {
        this.droneId = droneId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.controlled = controlled;
        this.motorsArmed = motorsArmed;
        this.ownerUUID = ownerUUID;
        this.throttle = throttle;
        this.qx = qx;
        this.qy = qy;
        this.qz = qz;
        this.qw = qw;
    }

    public DroneSyncPacket(
        UUID droneId,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        float roll,
        double vx,
        double vy,
        double vz,
        boolean controlled,
        boolean motorsArmed,
        Optional<UUID> ownerUUID,
        float throttle
    ) {
        this(droneId, x, y, z, yaw, pitch, roll, vx, vy, vz, controlled, motorsArmed, ownerUUID, throttle, 0.0F, 0.0F, 0.0F, 1.0F);
    }

    public DroneSyncPacket(
        UUID droneId,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        float roll,
        double vx,
        double vy,
        double vz,
        boolean controlled,
        boolean motorsArmed
    ) {
        this(droneId, x, y, z, yaw, pitch, roll, vx, vy, vz, controlled, motorsArmed, Optional.empty(), 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
    }

    public DroneSyncPacket(
        UUID droneId,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        float roll,
        double vx,
        double vy,
        double vz,
        boolean controlled,
        boolean motorsArmed,
        Optional<UUID> ownerUUID
    ) {
        this(droneId, x, y, z, yaw, pitch, roll, vx, vy, vz, controlled, motorsArmed, ownerUUID, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
    }

    public static void encode(DroneSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
        buf.writeFloat(msg.roll);
        buf.writeDouble(msg.vx);
        buf.writeDouble(msg.vy);
        buf.writeDouble(msg.vz);
        buf.writeBoolean(msg.controlled);
        buf.writeBoolean(msg.motorsArmed);
        buf.writeBoolean(msg.ownerUUID.isPresent());
        msg.ownerUUID.ifPresent(buf::writeUUID);
        buf.writeFloat(msg.throttle);
        buf.writeFloat(msg.qx);
        buf.writeFloat(msg.qy);
        buf.writeFloat(msg.qz);
        buf.writeFloat(msg.qw);
    }

    public static DroneSyncPacket decode(FriendlyByteBuf buf) {
        UUID droneId = buf.readUUID();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        float roll = buf.readFloat();
        double vx = buf.readDouble();
        double vy = buf.readDouble();
        double vz = buf.readDouble();
        boolean controlled = buf.readBoolean();
        boolean motorsArmed = buf.readBoolean();
        boolean hasOwner = buf.readBoolean();
        Optional<UUID> ownerUUID = hasOwner ? Optional.of(buf.readUUID()) : Optional.empty();
        float throttle = buf.readFloat();
        float qx = buf.readFloat();
        float qy = buf.readFloat();
        float qz = buf.readFloat();
        float qw = buf.readFloat();
        return new DroneSyncPacket(droneId, x, y, z, yaw, pitch, roll, vx, vy, vz, controlled, motorsArmed, ownerUUID, throttle, qx, qy, qz, qw);
    }

    public static void handle(DroneSyncPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get()
            .enqueueWork(
                () -> {
                    ServerPlayer player = ctx.get().getSender();
                    if (player != null) {
                        ServerLevel level = player.level();
                        DroneEntity drone = level.getEntity(msg.droneId) instanceof DroneEntity d ? d : null;
                        if (drone == null) {
                            int chunkX = (int)Math.floor(msg.x) >> 4;
                            int chunkZ = (int)Math.floor(msg.z) >> 4;
                            DroneChunkTickets.forceChunk(level, chunkX, chunkZ, true);
                            drone = level.getEntity(msg.droneId) instanceof DroneEntity retryDrone ? retryDrone : null;
                            if (drone == null) {
                                FPVMod.LOGGER.debug("[DroneSync] drone {} not loaded after force-load; will retry next sync.", msg.droneId);
                                return;
                            }
                        }

                        if (!drone.getOwner().isPresent() || drone.getOwner().get().equals(player.getUUID())) {
                            FPVMod.updateLastKnownDronePosition(msg.droneId, msg.x, msg.z);
                            drone.setYRot(msg.yaw);
                            drone.setXRot(msg.pitch);
                            drone.setDroneRoll(msg.roll);
                            drone.setYBodyRot(msg.yaw);
                            drone.setPos(msg.x, msg.y, msg.z);
                            drone.setDeltaMovement(msg.vx, msg.vy, msg.vz);
                            drone.setBeingControlled(msg.controlled);
                            if (!msg.controlled) {
                                drone.setInFpv(false);
                            }

                            drone.setMotorsArmed(msg.motorsArmed);
                            drone.setSyncedThrottle(msg.throttle);
                            if (msg.controlled) {
                                player.noPhysics = true;
                                drone.setOwner(player.getUUID());
                                player.getAbilities().flying = true;
                                player.getAbilities().mayfly = true;
                                player.fallDistance = 0.0F;
                                player.snapTo(msg.x, msg.y, msg.z, msg.yaw, msg.pitch);
                                player.setOldPosAndRot();
                            }

                            if (msg.controlled) {
                                double fpvRange = FpvConfig.getDroneObserverRange();
                                double BROADCAST_RANGE_SQ = fpvRange * fpvRange;
                                DroneStateBroadcastPacket broadcastPkt = new DroneStateBroadcastPacket(
                                    msg.droneId,
                                    msg.x,
                                    msg.y,
                                    msg.z,
                                    msg.yaw,
                                    msg.pitch,
                                    msg.roll,
                                    msg.vx,
                                    msg.vy,
                                    msg.vz,
                                    msg.controlled,
                                    msg.motorsArmed,
                                    msg.qx,
                                    msg.qy,
                                    msg.qz,
                                    msg.qw
                                );
                                UUID pilotUUID = player.getUUID();
                                PilotBodyEntity pilotBody = PilotBodyManager.getPilotBody(pilotUUID);

                                for (ServerPlayer obs : level.players()) {
                                    if (!obs.getUUID().equals(pilotUUID)) {
                                        double distSq = obs.distanceToSqr(msg.x, msg.y, msg.z);
                                        if (distSq > BROADCAST_RANGE_SQ && pilotBody != null) {
                                            distSq = Math.min(distSq, obs.distanceToSqr(pilotBody.position()));
                                        }

                                        if (distSq <= BROADCAST_RANGE_SQ) {
                                            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> obs), broadcastPkt);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            );
        ctx.get().setPacketHandled(true);
    }
}
