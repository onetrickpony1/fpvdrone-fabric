package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.entity.FallType;
import com.fpvdrone.physics.PhysicsState;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;
import org.joml.Vector3f;

public class DroneDamagePacket {
    private final UUID droneId;
    private final byte fallTypeOrdinal;
    private final float impulseX;
    private final float impulseY;
    private final float impulseZ;
    private final float yawRate;
    private final float pitchRate;
    private final float rollRate;
    private final float thrustFactor;
    private final float dragMultiplier;
    private static final float MAX_IMPULSE_MAGNITUDE = 30.0F;

    public DroneDamagePacket(UUID droneId, FallType type, Vector3f impulse, float[] angularVelocity) {
        this.droneId = droneId;
        this.fallTypeOrdinal = (byte)type.ordinal();
        this.impulseX = impulse.x();
        this.impulseY = impulse.y();
        this.impulseZ = impulse.z();
        this.yawRate = angularVelocity[0];
        this.pitchRate = angularVelocity[1];
        this.rollRate = angularVelocity[2];
        this.thrustFactor = type.getThrustFactor();
        this.dragMultiplier = type.getDragMultiplier();
    }

    private DroneDamagePacket(
        UUID droneId,
        byte fallTypeOrdinal,
        float impulseX,
        float impulseY,
        float impulseZ,
        float yawRate,
        float pitchRate,
        float rollRate,
        float thrustFactor,
        float dragMultiplier
    ) {
        this.droneId = droneId;
        this.fallTypeOrdinal = fallTypeOrdinal;
        this.impulseX = impulseX;
        this.impulseY = impulseY;
        this.impulseZ = impulseZ;
        this.yawRate = yawRate;
        this.pitchRate = pitchRate;
        this.rollRate = rollRate;
        this.thrustFactor = thrustFactor;
        this.dragMultiplier = dragMultiplier;
    }

    public static void encode(DroneDamagePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeByte(msg.fallTypeOrdinal);
        buf.writeFloat(msg.impulseX);
        buf.writeFloat(msg.impulseY);
        buf.writeFloat(msg.impulseZ);
        buf.writeFloat(msg.yawRate);
        buf.writeFloat(msg.pitchRate);
        buf.writeFloat(msg.rollRate);
        buf.writeFloat(msg.thrustFactor);
        buf.writeFloat(msg.dragMultiplier);
    }

    public static DroneDamagePacket decode(FriendlyByteBuf buf) {
        return new DroneDamagePacket(
            buf.readUUID(),
            buf.readByte(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat()
        );
    }

    public static void handle(DroneDamagePacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                PhysicsState physicsState = FPVMod.getPhysicsState(mc.player.getUUID());
                if (physicsState != null) {
                    FallType[] values = FallType.values();
                    byte ordinal = msg.fallTypeOrdinal;
                    if (ordinal < 0 || ordinal >= values.length) {
                        ordinal = 0;
                    }

                    FallType fallType = values[ordinal];
                    float yaw = clamp(msg.yawRate, fallType.getMinYawRate(), fallType.getMaxYawRate());
                    float pitch = clamp(msg.pitchRate, fallType.getMinPitchRate(), fallType.getMaxPitchRate());
                    float roll = clamp(msg.rollRate, fallType.getMinRollRate(), fallType.getMaxRollRate());
                    float ix = msg.impulseX;
                    float iy = msg.impulseY;
                    float iz = msg.impulseZ;
                    float mag = (float)Math.sqrt(ix * ix + iy * iy + iz * iz);
                    if (mag > 30.0F) {
                        float scale = 30.0F / mag;
                        ix *= scale;
                        iy *= scale;
                        iz *= scale;
                    }

                    physicsState.setFalling(true);
                    physicsState.setFallingParams(msg.thrustFactor, msg.dragMultiplier);
                    physicsState.applyImpulse(new Vector3f(ix, iy, iz));
                    physicsState.setAngularVelocity(yaw, pitch, roll);
                    FpvStatusChecker.markDroneFalling(msg.droneId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(value, max));
    }
}
