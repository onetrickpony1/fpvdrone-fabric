package com.fpvdrone.physics.wind;

import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

/**
 * Dryden low-altitude turbulence (docs/WIND.md), one filter per drone, run in that drone's physics. Local noise:
 * nothing is synced. Exact first-order update per step: {@code a = exp(-V dt / L)},
 * {@code x <- a x + sigma sqrt(1 - a^2) eta}.
 *
 * <ul>
 *   <li>{@code L_u = L_v = h / (0.177 + 0.0027 h)^1.2}, {@code L_w = h} (h = height above ground, at least 3 m)</li>
 *   <li>{@code sigma_w = 0.1 W6}, {@code sigma_u = sigma_v = sigma_w / (0.177 + 0.0027 h)^0.4}</li>
 *   <li>at the edge of a wake (shelter between 0 and 1) sigma is raised by {@code 1 + 6 s (1 - s)}</li>
 * </ul>
 * u is along the mean wind, v across it, w vertical.
 */
public final class Turbulence {
    private final RandomSource random = RandomSource.create();
    private float u;
    private float v;
    private float w;

    /**
     * Advances the filter and writes the turbulent wind (world axes, m/s) into {@code out}. {@code mean} is the
     * local mean wind, {@code w6} the open-ground wind at 6 m, {@code airSpeed} the drone's speed through the air.
     */
    public Vector3f step(float dt, float heightAboveGround, Vector3f mean, float w6, float airSpeed, float shelter, float scale, Vector3f out) {
        if (scale <= 0.0F || w6 < 0.05F) {
            this.u = this.v = this.w = 0.0F;
            return out.set(0.0F, 0.0F, 0.0F);
        }
        float h = Math.max(3.0F, heightAboveGround);
        float k = 0.177F + 0.0027F * h;
        float lu = h / (float)Math.pow(k, 1.2);
        float lw = h;
        float wake = 1.0F + 6.0F * shelter * (1.0F - shelter);
        float sw = 0.1F * w6 * wake * scale;
        float su = sw / (float)Math.pow(k, 0.4);
        float speed = Math.max(Math.max(airSpeed, mean.length()), 1.0F);
        this.u = next(this.u, speed, dt, lu, su);
        this.v = next(this.v, speed, dt, lu, su);
        this.w = next(this.w, speed, dt, lw, sw);
        float mx = mean.x;
        float mz = mean.z;
        float len = (float)Math.sqrt(mx * mx + mz * mz);
        float ax = len > 1.0E-3F ? mx / len : 1.0F;
        float az = len > 1.0E-3F ? mz / len : 0.0F;
        // u along the wind, v across it (to its left), w up
        return out.set(ax * this.u - az * this.v, this.w, az * this.u + ax * this.v);
    }

    private float next(float x, float speed, float dt, float length, float sigma) {
        float a = (float)Math.exp(-speed * dt / length);
        return a * x + sigma * (float)Math.sqrt(Math.max(0.0F, 1.0F - a * a)) * (float)this.random.nextGaussian();
    }
}
