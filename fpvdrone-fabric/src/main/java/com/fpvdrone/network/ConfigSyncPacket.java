package com.fpvdrone.network;

import com.fpvdrone.FpvConfig;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;

/**
 * Server config pushed to clients (1.1.8: only the global values; per-drone numbers travel with the drone
 * definitions).
 */
public class ConfigSyncPacket {
    private final float signalRangeMultiplier;
    private final float signalSmoothing;
    private final int zeroSignalFramesForDisconnect;
    private final float freezeIntensity;
    private final float failsafeDescentRate;
    private final float failsafeHorizontalDamping;
    private final boolean explosionBreaksBlocks;
    private final boolean droneCollisionDamage;
    private final boolean turtleMode;
    // port addition: wind (docs/WIND.md)
    private boolean windEnabled = FpvConfig.isWindEnabled();
    private float windStrength = FpvConfig.getWindStrength();
    private boolean windSheltering = FpvConfig.isWindSheltering();

    public ConfigSyncPacket() {
        this(
            FpvConfig.getSignalRangeMultiplier(),
            FpvConfig.getSignalSmoothing(),
            FpvConfig.getZeroSignalFramesForDisconnect(),
            FpvConfig.getFreezeIntensity(),
            FpvConfig.getFailsafeDescentRate(),
            FpvConfig.getFailsafeHorizontalDamping(),
            FpvConfig.isExplosionBreaksBlocks(),
            FpvConfig.getDroneCollisionDamage(),
            FpvConfig.isTurtleMode()
        );
    }

    private ConfigSyncPacket(
        float signalRangeMultiplier,
        float signalSmoothing,
        int zeroSignalFramesForDisconnect,
        float freezeIntensity,
        float failsafeDescentRate,
        float failsafeHorizontalDamping,
        boolean explosionBreaksBlocks,
        boolean droneCollisionDamage,
        boolean turtleMode
    ) {
        this.signalRangeMultiplier = signalRangeMultiplier;
        this.signalSmoothing = signalSmoothing;
        this.zeroSignalFramesForDisconnect = zeroSignalFramesForDisconnect;
        this.freezeIntensity = freezeIntensity;
        this.failsafeDescentRate = failsafeDescentRate;
        this.failsafeHorizontalDamping = failsafeHorizontalDamping;
        this.explosionBreaksBlocks = explosionBreaksBlocks;
        this.droneCollisionDamage = droneCollisionDamage;
        this.turtleMode = turtleMode;
    }

    public static void encode(ConfigSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.signalRangeMultiplier);
        buf.writeFloat(msg.signalSmoothing);
        buf.writeInt(msg.zeroSignalFramesForDisconnect);
        buf.writeFloat(msg.freezeIntensity);
        buf.writeFloat(msg.failsafeDescentRate);
        buf.writeFloat(msg.failsafeHorizontalDamping);
        buf.writeBoolean(msg.explosionBreaksBlocks);
        buf.writeBoolean(msg.droneCollisionDamage);
        buf.writeBoolean(msg.turtleMode);
        buf.writeBoolean(msg.windEnabled);
        buf.writeFloat(msg.windStrength);
        buf.writeBoolean(msg.windSheltering);
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
        ConfigSyncPacket msg = new ConfigSyncPacket(
            buf.readFloat(), buf.readFloat(), buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readBoolean(), buf.readBoolean(),
            buf.readBoolean()
        );
        msg.windEnabled = buf.readBoolean();
        msg.windStrength = buf.readFloat();
        msg.windSheltering = buf.readBoolean();
        return msg;
    }

    public static void handle(ConfigSyncPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get()
            .enqueueWork(
                () -> {
                    FpvConfig.applyServerWind(msg.windEnabled, msg.windStrength, msg.windSheltering);
                    FpvConfig.applyServerConfig(
                    msg.signalRangeMultiplier,
                    msg.signalSmoothing,
                    msg.zeroSignalFramesForDisconnect,
                    msg.freezeIntensity,
                    msg.failsafeDescentRate,
                    msg.failsafeHorizontalDamping,
                    msg.explosionBreaksBlocks,
                    msg.droneCollisionDamage,
                    msg.turtleMode
                    );
                }
            );
        ctx.get().setPacketHandled(true);
    }
}
