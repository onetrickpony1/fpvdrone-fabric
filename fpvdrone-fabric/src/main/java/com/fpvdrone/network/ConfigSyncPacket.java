package com.fpvdrone.network;

import com.fpvdrone.FpvConfig;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import com.fpvdrone.network.compat.NetworkContext;

public class ConfigSyncPacket {
    private final int maxRange;
    private final int freezeStartDistance;
    private final float obstaclePenaltyMultiplier;
    private final float groundProximityPenaltyMax;
    private final int groundProximityDistanceStart;
    private final int groundProximityHeightThreshold;
    private final float heightBonusPerBlock;
    private final int heightBaseline;
    private final float signalSmoothing;
    private final int zeroSignalFramesForDisconnect;
    private final float freezeIntensity;
    private final float failsafeDescentRate;
    private final float failsafeHorizontalDamping;
    private final float explosionBlockPower;
    private final float explosionBlastRadius;
    private final float explosionBlastDamage;
    private final float explosionKillZone;
    private final boolean explosionOpensDoors;
    private final float kamikazeExplosionPower;
    private final int shrapnelCount;
    private final boolean droneCollisionDamage;

    public ConfigSyncPacket() {
        this.maxRange = FpvConfig.getMaxRange();
        this.freezeStartDistance = FpvConfig.getFreezeStartDistance();
        this.obstaclePenaltyMultiplier = FpvConfig.getObstaclePenaltyMultiplier();
        this.groundProximityPenaltyMax = FpvConfig.getGroundProximityPenaltyMax();
        this.groundProximityDistanceStart = FpvConfig.getGroundProximityDistanceStart();
        this.groundProximityHeightThreshold = FpvConfig.getGroundProximityHeightThreshold();
        this.heightBonusPerBlock = FpvConfig.getHeightBonusPerBlock();
        this.heightBaseline = FpvConfig.getHeightBaseline();
        this.signalSmoothing = FpvConfig.getSignalSmoothing();
        this.zeroSignalFramesForDisconnect = FpvConfig.getZeroSignalFramesForDisconnect();
        this.freezeIntensity = FpvConfig.getFreezeIntensity();
        this.failsafeDescentRate = FpvConfig.getFailsafeDescentRate();
        this.failsafeHorizontalDamping = FpvConfig.getFailsafeHorizontalDamping();
        this.explosionBlockPower = FpvConfig.getExplosionBlockPower();
        this.explosionBlastRadius = FpvConfig.getExplosionBlastRadius();
        this.explosionBlastDamage = FpvConfig.getExplosionBlastDamage();
        this.explosionKillZone = FpvConfig.getExplosionKillZone();
        this.explosionOpensDoors = FpvConfig.getExplosionOpensDoors();
        this.kamikazeExplosionPower = FpvConfig.getKamikazeExplosionPower();
        this.shrapnelCount = FpvConfig.getShrapnelCount();
        this.droneCollisionDamage = FpvConfig.getDroneCollisionDamage();
    }

    private ConfigSyncPacket(
        int maxRange,
        int freezeStartDistance,
        float obstaclePenaltyMultiplier,
        float groundProximityPenaltyMax,
        int groundProximityDistanceStart,
        int groundProximityHeightThreshold,
        float heightBonusPerBlock,
        int heightBaseline,
        float signalSmoothing,
        int zeroSignalFramesForDisconnect,
        float freezeIntensity,
        float failsafeDescentRate,
        float failsafeHorizontalDamping,
        float explosionBlockPower,
        float explosionBlastRadius,
        float explosionBlastDamage,
        float explosionKillZone,
        boolean explosionOpensDoors,
        float kamikazeExplosionPower,
        int shrapnelCount,
        boolean droneCollisionDamage
    ) {
        this.maxRange = maxRange;
        this.freezeStartDistance = freezeStartDistance;
        this.obstaclePenaltyMultiplier = obstaclePenaltyMultiplier;
        this.groundProximityPenaltyMax = groundProximityPenaltyMax;
        this.groundProximityDistanceStart = groundProximityDistanceStart;
        this.groundProximityHeightThreshold = groundProximityHeightThreshold;
        this.heightBonusPerBlock = heightBonusPerBlock;
        this.heightBaseline = heightBaseline;
        this.signalSmoothing = signalSmoothing;
        this.zeroSignalFramesForDisconnect = zeroSignalFramesForDisconnect;
        this.freezeIntensity = freezeIntensity;
        this.failsafeDescentRate = failsafeDescentRate;
        this.failsafeHorizontalDamping = failsafeHorizontalDamping;
        this.explosionBlockPower = explosionBlockPower;
        this.explosionBlastRadius = explosionBlastRadius;
        this.explosionBlastDamage = explosionBlastDamage;
        this.explosionKillZone = explosionKillZone;
        this.explosionOpensDoors = explosionOpensDoors;
        this.kamikazeExplosionPower = kamikazeExplosionPower;
        this.shrapnelCount = shrapnelCount;
        this.droneCollisionDamage = droneCollisionDamage;
    }

    public static void encode(ConfigSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.maxRange);
        buf.writeInt(msg.freezeStartDistance);
        buf.writeFloat(msg.obstaclePenaltyMultiplier);
        buf.writeFloat(msg.groundProximityPenaltyMax);
        buf.writeInt(msg.groundProximityDistanceStart);
        buf.writeInt(msg.groundProximityHeightThreshold);
        buf.writeFloat(msg.heightBonusPerBlock);
        buf.writeInt(msg.heightBaseline);
        buf.writeFloat(msg.signalSmoothing);
        buf.writeInt(msg.zeroSignalFramesForDisconnect);
        buf.writeFloat(msg.freezeIntensity);
        buf.writeFloat(msg.failsafeDescentRate);
        buf.writeFloat(msg.failsafeHorizontalDamping);
        buf.writeFloat(msg.explosionBlockPower);
        buf.writeFloat(msg.explosionBlastRadius);
        buf.writeFloat(msg.explosionBlastDamage);
        buf.writeFloat(msg.explosionKillZone);
        buf.writeBoolean(msg.explosionOpensDoors);
        buf.writeFloat(msg.kamikazeExplosionPower);
        buf.writeVarInt(msg.shrapnelCount);
        buf.writeBoolean(msg.droneCollisionDamage);
    }

    public static ConfigSyncPacket decode(FriendlyByteBuf buf) {
        return new ConfigSyncPacket(
            buf.readInt(),
            buf.readInt(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readInt(),
            buf.readInt(),
            buf.readFloat(),
            buf.readInt(),
            buf.readFloat(),
            buf.readInt(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readBoolean(),
            buf.readFloat(),
            buf.readVarInt(),
            buf.readBoolean()
        );
    }

    public static void handle(ConfigSyncPacket msg, Supplier<NetworkContext> ctx) {
        ctx.get()
            .enqueueWork(
                () -> FpvConfig.applyServerConfig(
                    msg.maxRange,
                    msg.freezeStartDistance,
                    msg.obstaclePenaltyMultiplier,
                    msg.groundProximityPenaltyMax,
                    msg.groundProximityDistanceStart,
                    msg.groundProximityHeightThreshold,
                    msg.heightBonusPerBlock,
                    msg.heightBaseline,
                    msg.signalSmoothing,
                    msg.zeroSignalFramesForDisconnect,
                    msg.freezeIntensity,
                    msg.failsafeDescentRate,
                    msg.failsafeHorizontalDamping,
                    msg.explosionBlockPower,
                    msg.explosionBlastRadius,
                    msg.explosionBlastDamage,
                    msg.explosionKillZone,
                    msg.explosionOpensDoors,
                    msg.kamikazeExplosionPower,
                    msg.shrapnelCount,
                    msg.droneCollisionDamage
                )
            );
        ctx.get().setPacketHandled(true);
    }
}
