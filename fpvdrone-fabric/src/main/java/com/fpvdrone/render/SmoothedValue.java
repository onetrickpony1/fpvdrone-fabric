package com.fpvdrone.render;

import net.minecraft.util.Mth;

/**
 * 1.1.8: a clamped value that eases towards its target at a fixed rate per second, timed by wall clock
 * so it is smooth at any frame rate. Used by the gimbal tilt, camera zoom and free look.
 */
public final class SmoothedValue {
    private final float rate;
    private float min;
    private float max;
    private float current;
    private float target;
    private long lastNanos = 0L;
    private float lastDt = 0.0F;

    public SmoothedValue(float initial, float rate, float min, float max) {
        this.min = min;
        this.max = max;
        this.rate = rate;
        this.current = Mth.clamp(initial, min, max);
        this.target = this.current;
    }

    /** Advances towards the target and returns the eased value. Call once per frame. */
    public float update() {
        long now = System.nanoTime();
        this.lastDt = this.lastNanos == 0L ? 0.0F : Math.min((float)(now - this.lastNanos) / 1.0E9F, 0.1F);
        this.lastNanos = now;
        if (this.lastDt > 0.0F) {
            this.current = this.current + (this.target - this.current) * Math.min(1.0F, this.rate * this.lastDt);
        }

        return this.current;
    }

    /** Seconds covered by the last {@link #update()}. */
    public float lastDt() {
        return this.lastDt;
    }

    public float target() {
        return this.target;
    }

    public void addToTarget(float delta) {
        this.target = Mth.clamp(this.target + delta, this.min, this.max);
    }

    public void setTarget(float value) {
        this.target = Mth.clamp(value, this.min, this.max);
    }

    public void setRange(float min, float max) {
        this.min = min;
        this.max = max;
        this.current = Mth.clamp(this.current, min, max);
        this.target = Mth.clamp(this.target, min, max);
    }

    /** Jumps straight to a value (no easing). */
    public void snapTo(float value) {
        this.current = Mth.clamp(value, this.min, this.max);
        this.target = this.current;
    }

    public void reset(float value) {
        this.snapTo(value);
        this.lastNanos = 0L;
    }
}
