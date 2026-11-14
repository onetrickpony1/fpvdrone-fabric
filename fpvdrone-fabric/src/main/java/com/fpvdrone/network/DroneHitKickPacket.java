package com.fpvdrone.network;

import com.fpvdrone.FPVMod;
import com.fpvdrone.physics.PhysicsState;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;
import org.joml.Vector3f;

public class DroneHitKickPacket {
    private final UUID droneId;
    private final float impulseX;
    private final float impulseY;
    private final float impulseZ;
    private final float kickYaw;
    private final float kickPitch;
    private final float kickRoll;
    private static final float MAX_IMPULSE = 15.0F;
    private static final float MAX_KICK = 600.0F;

    public DroneHitKickPacket(UUID droneId, Vector3f impulse, float kickYaw, float kickPitch, float kickRoll) {
        this.droneId = droneId;
        this.impulseX = impulse.x();
        this.impulseY = impulse.y();
        this.impulseZ = impulse.z();
        this.kickYaw = kickYaw;
        this.kickPitch = kickPitch;
        this.kickRoll = kickRoll;
    }

    public static void encode(DroneHitKickPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.droneId);
        buf.writeFloat(msg.impulseX);
        buf.writeFloat(msg.impulseY);
        buf.writeFloat(msg.impulseZ);
        buf.writeFloat(msg.kickYaw);
        buf.writeFloat(msg.kickPitch);
        buf.writeFloat(msg.kickRoll);
    }

    public static DroneHitKickPacket decode(FriendlyByteBuf buf) {
        return new DroneHitKickPacket(
            buf.readUUID(), new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat()), buf.readFloat(), buf.readFloat(), buf.readFloat()
        );
    }

    public static void handle(DroneHitKickPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                PhysicsState physicsState = FPVMod.getPhysicsState(mc.player.getUUID());
                if (physicsState != null) {
                    float ix = msg.impulseX;
                    float iy = msg.impulseY;
                    float iz = msg.impulseZ;
                    float mag = (float)Math.sqrt(ix * ix + iy * iy + iz * iz);
                    if (mag > 15.0F) {
                        float s = 15.0F / mag;
                        ix *= s;
                        iy *= s;
                        iz *= s;
                    }

                    float ky = clamp(msg.kickYaw, -600.0F, 600.0F);
                    float kp = clamp(msg.kickPitch, -600.0F, 600.0F);
                    float kr = clamp(msg.kickRoll, -600.0F, 600.0F);
                    physicsState.applyImpulse(new Vector3f(ix, iy, iz));
                    physicsState.applyRotationalKick(ky, kp, kr);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(v, max));
    }
}
