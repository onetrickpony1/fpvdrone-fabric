package com.fpvdrone.client;

import com.fpvdrone.FpvConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SignalCalculator {
    private static final double SAMPLE_STEP = 4.0;
    private static final float UNLOADED_CHUNK_PENALTY = 2.0F;
    private static Level cachedLevel = null;
    private static Vec3 cachedDronePos = null;
    private static Vec3 cachedPilotPos = null;
    private static float cachedSignal = 1.0F;
    private static float smoothedSignal = 1.0F;
    private static long cachedTick = -1L;
    private static final int CACHE_DURATION = 20;
    private static final float MIN_SIGNAL_THRESHOLD = 0.01F;
    private static int zeroSignalCounter = 0;
    private static float cachedObstaclePenalty = 0.0F;
    private static long obstacleCacheTick = -1L;
    private static final int OBSTACLE_CACHE_DURATION = 40;
    private static int cachedGroundY = -1;
    private static BlockPos cachedGroundPos = null;
    private static long groundCacheTick = -1L;
    private static final int GROUND_CACHE_DURATION = 60;

    public static void setLevel(Level level) {
        cachedLevel = level;
    }

    public static double calculateDistance(Vec3 dronePos, Vec3 pilotPos) {
        if (dronePos != null && pilotPos != null) {
            double dx = dronePos.x - pilotPos.x;
            double dy = dronePos.y - pilotPos.y;
            double dz = dronePos.z - pilotPos.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            return Double.isNaN(dist) ? 0.0 : dist;
        } else {
            return 0.0;
        }
    }

    public static float calculateSignalFull(Vec3 dronePos, Vec3 pilotPos, Level level) {
        if (dronePos != null && pilotPos != null && level != null) {
            long currentTick = level.getGameTime();
            if (cachedTick >= 0L && currentTick - cachedTick < 20L && cachedDronePos != null && cachedPilotPos != null) {
                boolean positionChanged = cachedDronePos.distanceToSqr(dronePos) >= 9.0 || cachedPilotPos.distanceToSqr(pilotPos) >= 9.0;
                if (!positionChanged && cachedSignal > 0.01F) {
                    return cachedSignal;
                }
            }

            cachedLevel = level;
            cachedDronePos = dronePos;
            cachedPilotPos = pilotPos;
            cachedTick = currentTick;
            double distance = calculateDistance(dronePos, pilotPos);
            double maxRange = FpvConfig.getMaxRange();
            float distanceFactor = 1.0F - (float)Math.min(1.0, distance / maxRange);
            float obstaclePenalty;
            if (obstacleCacheTick >= 0L && currentTick - obstacleCacheTick < 40L) {
                obstaclePenalty = cachedObstaclePenalty;
            } else {
                obstaclePenalty = calculateObstaclePenalty(dronePos, pilotPos, level);
                cachedObstaclePenalty = obstaclePenalty;
                obstacleCacheTick = currentTick;
            }

            float obstacleFactor = 1.0F / (1.0F + obstaclePenalty * 0.03F * FpvConfig.getObstaclePenaltyMultiplier());
            float minHeight = (float)Math.min(dronePos.y, pilotPos.y);
            float heightBonus = Math.max(0.0F, (minHeight - FpvConfig.getHeightBaseline()) * FpvConfig.getHeightBonusPerBlock() * 0.01F);
            float heightFactor = 1.0F + heightBonus;
            boolean hasLOS = obstaclePenalty < 1.0F;
            float losFactor = hasLOS ? 1.1F : 1.0F;
            float groundProximityPenalty = calculateGroundProximityPenalty(dronePos, level, distance, currentTick);
            float groundFactor = 1.0F - groundProximityPenalty / 100.0F;
            float rawSignal = distanceFactor * obstacleFactor * heightFactor * losFactor * groundFactor;
            rawSignal = Math.max(0.0F, Math.min(1.0F, rawSignal));
            smoothedSignal = smoothedSignal + (rawSignal - smoothedSignal) * FpvConfig.getSignalSmoothing();
            if (rawSignal <= 0.01F) {
                zeroSignalCounter++;
            } else {
                zeroSignalCounter = 0;
            }

            if (zeroSignalCounter >= FpvConfig.getZeroSignalFramesForDisconnect()) {
                cachedSignal = 0.0F;
            } else {
                cachedSignal = Math.max(0.01F, smoothedSignal);
            }

            return cachedSignal;
        } else {
            return 1.0F;
        }
    }

    private static float calculateGroundProximityPenalty(Vec3 dronePos, Level level, double distance, long currentTick) {
        float gpDistStart = FpvConfig.getGroundProximityDistanceStart();
        if (distance < gpDistStart) {
            return 0.0F;
        } else {
            BlockPos droneBlockPos = new BlockPos((int)dronePos.x, (int)dronePos.y, (int)dronePos.z);
            int groundY;
            if (cachedGroundPos != null
                && groundCacheTick >= 0L
                && currentTick - groundCacheTick < 60L
                && Math.abs(cachedGroundPos.getX() - droneBlockPos.getX()) < 8
                && Math.abs(cachedGroundPos.getZ() - droneBlockPos.getZ()) < 8) {
                groundY = cachedGroundY;
            } else {
                groundY = findGroundLevel(level, droneBlockPos);
                cachedGroundY = groundY;
                cachedGroundPos = droneBlockPos;
                groundCacheTick = currentTick;
            }

            float heightAboveGround = (float)(dronePos.y - groundY);
            float gpHeightThreshold = FpvConfig.getGroundProximityHeightThreshold();
            if (heightAboveGround >= gpHeightThreshold) {
                return 0.0F;
            } else {
                float distanceFactor = (float)Math.min(1.0, (distance - gpDistStart) / 150.0);
                float heightFactor = 1.0F - heightAboveGround / gpHeightThreshold;
                heightFactor *= heightFactor;
                return FpvConfig.getGroundProximityPenaltyMax() * distanceFactor * heightFactor;
            }
        }
    }

    private static int findGroundLevel(Level level, BlockPos pos) {
        MutableBlockPos mutable = new MutableBlockPos(pos.getX(), pos.getY(), pos.getZ());
        int minY = Math.max(level.getMinY(), pos.getY() - 64);

        for (int y = pos.getY(); y > minY; y -= 2) {
            mutable.setY(y);
            if (!level.isLoaded(mutable)) {
                return pos.getY() - 5;
            }

            BlockState state = level.getBlockState(mutable);
            if (state.isSolidRender()) {
                return y;
            }
        }

        return minY;
    }

    private static float calculateObstaclePenalty(Vec3 from, Vec3 to, Level level) {
        float penalty = 0.0F;
        Vec3 direction = to.subtract(from).normalize();
        double distance = from.distanceTo(to);
        int samples = (int)(distance / 4.0);
        int maxSamples = 50;
        int step = samples > maxSamples ? samples / maxSamples : 1;

        for (int i = 1; i < samples; i += step) {
            Vec3 point = from.add(direction.scale(i * 4.0));
            BlockPos blockPos = new BlockPos((int)point.x, (int)point.y, (int)point.z);
            if (!level.isLoaded(blockPos)) {
                penalty += 2.0F;
            } else {
                BlockState state = level.getBlockState(blockPos);
                float blockPenalty = BlockAttenuationRegistry.getAttenuation(state);
                if (blockPenalty > 0.0F) {
                    penalty += blockPenalty;
                }
            }
        }

        return penalty;
    }

    public static float calculateFreezeSignal(double distance) {
        double freezeStart = FpvConfig.getFreezeStartDistance();
        double maxRange = FpvConfig.getMaxRange();
        if (distance < freezeStart) {
            return 1.0F;
        } else if (distance >= maxRange) {
            return 0.0F;
        } else {
            double freezeRange = maxRange - freezeStart;
            float signal = (float)(1.0 - (distance - freezeStart) / freezeRange);
            return Math.max(0.0F, Math.min(1.0F, signal));
        }
    }

    public static void invalidateCache() {
        cachedTick = -1L;
        cachedDronePos = null;
        cachedPilotPos = null;
        cachedSignal = 1.0F;
        smoothedSignal = 1.0F;
        zeroSignalCounter = 0;
        obstacleCacheTick = -1L;
        cachedObstaclePenalty = 0.0F;
        groundCacheTick = -1L;
        cachedGroundPos = null;
        cachedGroundY = -1;
    }
}
