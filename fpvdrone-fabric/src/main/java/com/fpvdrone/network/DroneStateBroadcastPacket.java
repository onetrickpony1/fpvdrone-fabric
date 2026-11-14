package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import com.fpvdrone.network.compat.NetworkContext;
import org.joml.Quaternionf;

public class DroneStateBroadcastPacket {
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
    private final float qx;
    private final float qy;
    private final float qz;
    private final float qw;

    public DroneStateBroadcastPacket(
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
        this.qx = qx;
        this.qy = qy;
        this.qz = qz;
        this.qw = qw;
    }

    public static void encode(DroneStateBroadcastPacket msg, FriendlyByteBuf buf) {
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
        buf.writeFloat(msg.qx);
        buf.writeFloat(msg.qy);
        buf.writeFloat(msg.qz);
        buf.writeFloat(msg.qw);
    }

    public static DroneStateBroadcastPacket decode(FriendlyByteBuf buf) {
        return new DroneStateBroadcastPacket(
            buf.readUUID(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readDouble(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat()
        );
    }

    public static void handle(DroneStateBroadcastPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                if (FPVMod.isArmed(mc.player.getUUID())) {
                    Optional<UUID> activeDrone = FPVMod.getActiveDroneId(mc.player.getUUID());
                    if (activeDrone.isPresent() && activeDrone.get().equals(msg.droneId)) {
                        return;
                    }
                }

                boolean droneFound = false;

                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof DroneEntity drone && drone.getUUID().equals(msg.droneId)) {
                        droneFound = true;
                        double distSq = drone.distanceToSqr(msg.x, msg.y, msg.z);
                        boolean isTeleport = distSq > 4096.0;
                        if (isTeleport) {
                            drone.xOld = msg.x;
                            drone.yOld = msg.y;
                            drone.zOld = msg.z;
                            drone.setPos(msg.x, msg.y, msg.z);
                            drone.lerpSteps = 0;
                        } else {
                            drone.lerpStartX = drone.getX();
                            drone.lerpStartY = drone.getY();
                            drone.lerpStartZ = drone.getZ();
                            drone.lerpTargetX = msg.x;
                            drone.lerpTargetY = msg.y;
                            drone.lerpTargetZ = msg.z;
                            drone.lerpSteps = 3;
                        }

                        drone.setYRot(msg.yaw);
                        drone.setXRot(msg.pitch);
                        drone.yRotO = drone.getYRot();
                        drone.xRotO = drone.getXRot();
                        drone.yBodyRotO = drone.yBodyRot;
                        drone.yBodyRot = msg.yaw;
                        drone.setYHeadRot(msg.yaw);
                        drone.setDroneRoll(msg.roll);
                        drone.setRemoteOrientation(new Quaternionf(msg.qx, msg.qy, msg.qz, msg.qw));
                        drone.setDeltaMovement(msg.vx, msg.vy, msg.vz);
                        drone.setBeingControlled(msg.controlled);
                        drone.setMotorsArmed(msg.motorsArmed);
                        break;
                    }
                }

                if (!droneFound) {
                    FPVMod.LOGGER.debug("[DroneStateBroadcast] entity {} not found on client; vanilla tracker will spawn it shortly.", msg.droneId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
