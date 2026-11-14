package com.fpvdrone.entity;

public final class PivotOffset {
    public static final float PIVOT_Y = 0.21875F;

    private PivotOffset() {
    }

    public static float[] compute(float yawDeg, float pitchDeg, float rollDeg) {
        float pitch = (float)Math.toRadians(-pitchDeg);
        float yaw = (float)Math.toRadians(yawDeg);
        float roll = (float)Math.toRadians(rollDeg);
        float cosP = (float)Math.cos(pitch);
        float sinP = (float)Math.sin(pitch);
        float cosY = (float)Math.cos(yaw);
        float sinY = (float)Math.sin(yaw);
        float cosR = (float)Math.cos(roll);
        float sinR = (float)Math.sin(roll);
        float rx = -0.21875F * sinR;
        float ry = 0.21875F * cosR;
        float rz = 0.0F;
        float py = ry * cosP - rz * sinP;
        float pz = ry * sinP + rz * cosP;
        float wx = rx * cosY + pz * sinY;
        float wz = -rx * sinY + pz * cosY;
        return new float[]{wx, py - 0.21875F, wz};
    }
}
