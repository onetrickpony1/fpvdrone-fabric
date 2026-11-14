package com.fpvdrone.render;

/**
 * 1.1.8: tilt of a stabilised camera gimbal (the DJI Mavic's), in degrees; positive looks down.
 * Range -35 (up) to 90 (straight down). Recoil from firing kicks it briefly and decays.
 */
public final class GimbalState {
    public static final float MIN_PITCH = -35.0F;
    public static final float MAX_PITCH = 90.0F;
    public static final float SCROLL_STEP = 5.0F;
    public static final float RATE_DEG_PER_SEC = 60.0F;
    public static final float EASE_RATE = 12.0F;
    public static final float KICK_DECAY_SECONDS = 0.22F;
    private static final GimbalState INSTANCE = new GimbalState();
    private final SmoothedValue pitch = new SmoothedValue(0.0F, EASE_RATE, MIN_PITCH, MAX_PITCH);
    private float pitchKick = 0.0F;
    private float rollKick = 0.0F;
    private long lastRateNanos = 0L;

    private GimbalState() {
    }

    public static GimbalState getInstance() {
        return INSTANCE;
    }

    /** Current pitch including any recoil kick. Call once per frame. */
    public float getPitch() {
        float base = this.pitch.update();
        float decay = (float)Math.exp(-this.pitch.lastDt() / KICK_DECAY_SECONDS);
        this.pitchKick *= decay;
        this.rollKick *= decay;
        return base + this.pitchKick;
    }

    public float getRollKick() {
        return this.rollKick;
    }

    public void kick(float pitch, float roll) {
        this.pitchKick += pitch;
        this.rollKick += roll;
    }

    /** Scroll wheel / wizard step: positive tilts down. */
    public void nudge(float degrees) {
        this.pitch.addToTarget(degrees);
    }

    public void setPitch(float degrees) {
        this.pitch.snapTo(degrees);
    }

    /** Continuous tilt from keys or a tilt wheel: input in -1..1, positive tilts up. */
    public void applyRate(float input) {
        long now = System.nanoTime();
        float dt = this.lastRateNanos == 0L ? 0.0F : Math.min((float)(now - this.lastRateNanos) / 1.0E9F, 0.1F);
        this.lastRateNanos = now;
        if (dt != 0.0F && input != 0.0F) {
            this.pitch.addToTarget(-input * RATE_DEG_PER_SEC * dt);
        }
    }

    public void reset() {
        this.pitch.reset(0.0F);
        this.pitchKick = 0.0F;
        this.rollKick = 0.0F;
        this.lastRateNanos = 0L;
    }
}
