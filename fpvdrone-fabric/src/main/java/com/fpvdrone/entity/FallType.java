package com.fpvdrone.entity;

import java.util.Random;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

public enum FallType {
    DEAD_DROP(0.0F, 5.0F, 0.0F, 5.0F, 0.0F, 5.0F, 0.0F, 1.0F),
    SPIN_OUT(200.0F, 480.0F, 0.0F, 15.0F, 5.0F, 30.0F, 0.0F, 1.2F), // 1.1.8: was 300-720 deg/s yaw, 0.15 thrust
    TUMBLE(90.0F, 360.0F, 90.0F, 360.0F, 90.0F, 360.0F, 0.0F, 0.9F),
    GLIDE_DOWN(0.0F, 10.0F, 15.0F, 45.0F, 0.0F, 10.0F, 0.0F, 1.8F),
    FLIP_AND_DROP(0.0F, 5.0F, 180.0F, 360.0F, 0.0F, 5.0F, 0.0F, 1.1F); // 1.1.8: now actually flips (was 0-5 deg/s pitch)

    private final float minYawRate;
    private final float maxYawRate;
    private final float minPitchRate;
    private final float maxPitchRate;
    private final float minRollRate;
    private final float maxRollRate;
    private final float thrustFactor;
    private final float dragMultiplier;

    private FallType(
        float minYawRate,
        float maxYawRate,
        float minPitchRate,
        float maxPitchRate,
        float minRollRate,
        float maxRollRate,
        float thrustFactor,
        float dragMultiplier
    ) {
        this.minYawRate = minYawRate;
        this.maxYawRate = maxYawRate;
        this.minPitchRate = minPitchRate;
        this.maxPitchRate = maxPitchRate;
        this.minRollRate = minRollRate;
        this.maxRollRate = maxRollRate;
        this.thrustFactor = thrustFactor;
        this.dragMultiplier = dragMultiplier;
    }

    public float getMinYawRate() {
        return this.minYawRate;
    }

    public float getMaxYawRate() {
        return this.maxYawRate;
    }

    public float getMinPitchRate() {
        return this.minPitchRate;
    }

    public float getMaxPitchRate() {
        return this.maxPitchRate;
    }

    public float getMinRollRate() {
        return this.minRollRate;
    }

    public float getMaxRollRate() {
        return this.maxRollRate;
    }

    public float getThrustFactor() {
        return this.thrustFactor;
    }

    public float getDragMultiplier() {
        return this.dragMultiplier;
    }

    public float[] randomizeAngularVelocity(Random random) {
        float yaw = this.minYawRate + random.nextFloat() * (this.maxYawRate - this.minYawRate);
        float pitch = this.minPitchRate + random.nextFloat() * (this.maxPitchRate - this.minPitchRate);
        float roll = this.minRollRate + random.nextFloat() * (this.maxRollRate - this.minRollRate);
        return new float[]{yaw, pitch, roll};
    }

    public static FallType determine(DamageSource source, float amount, float droneSpeed, boolean hitFromAbove) {
        if (source.is(DamageTypeTags.IS_FIRE)
            || source.is(DamageTypes.MAGIC)
            || source.is(DamageTypes.INDIRECT_MAGIC)
            || source.is(DamageTypes.IN_WALL)) {
            return DEAD_DROP;
        } else if (source.is(DamageTypeTags.IS_EXPLOSION) || amount > 7.0F) {
            return TUMBLE;
        } else if (droneSpeed > 15.0F && amount < 5.0F) {
            return GLIDE_DOWN;
        } else {
            return hitFromAbove ? FLIP_AND_DROP : SPIN_OUT;
        }
    }
}
