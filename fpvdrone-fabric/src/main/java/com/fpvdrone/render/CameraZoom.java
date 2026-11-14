package com.fpvdrone.render;

import java.util.Locale;

/** 1.1.8: optical/digital camera zoom (zoom key cycles the levels, scroll zooms smoothly on munitions). */
public final class CameraZoom {
    public static final float[] DEFAULT_LEVELS = new float[]{1.0F, 3.0F, 7.0F};
    private static final float SCROLL_STEP = 0.25F;
    private static final float EASE_RATE = 6.0F;
    private static final CameraZoom INSTANCE = new CameraZoom();
    private final SmoothedValue zoom = new SmoothedValue(DEFAULT_LEVELS[0], EASE_RATE, DEFAULT_LEVELS[0], DEFAULT_LEVELS[DEFAULT_LEVELS.length - 1]);

    private CameraZoom() {
    }

    public static CameraZoom getInstance() {
        return INSTANCE;
    }

    public float getZoom() {
        return this.zoom.update();
    }

    public String getLabel() {
        return String.format(Locale.ROOT, "%.1fx", this.getZoom());
    }

    public boolean isZoomed() {
        return this.getZoom() > 1.001F;
    }

    /** Jumps the target to the level after the one nearest the current target. */
    public void cycle(float[] levels) {
        this.zoom.setRange(levels[0], levels[levels.length - 1]);
        int nearest = 0;
        float best = Float.MAX_VALUE;

        for (int i = 0; i < levels.length; i++) {
            float d = Math.abs(this.zoom.target() - levels[i]);
            if (d < best) {
                best = d;
                nearest = i;
            }
        }

        this.zoom.setTarget(levels[(nearest + 1) % levels.length]);
    }

    public void scroll(float delta, float[] levels) {
        this.zoom.setRange(levels[0], levels[levels.length - 1]);
        this.zoom.addToTarget(delta * SCROLL_STEP);
    }

    /** Narrows a vertical field of view by the zoom factor. */
    public double applyToFov(double fovDegrees) {
        float factor = this.getZoom();
        if (factor <= 1.0F) {
            return fovDegrees;
        }

        double half = Math.toRadians(fovDegrees) * 0.5;
        return Math.toDegrees(Math.atan(Math.tan(half) / factor) * 2.0);
    }

    public void reset() {
        this.zoom.setRange(DEFAULT_LEVELS[0], DEFAULT_LEVELS[DEFAULT_LEVELS.length - 1]);
        this.zoom.reset(DEFAULT_LEVELS[0]);
    }
}
