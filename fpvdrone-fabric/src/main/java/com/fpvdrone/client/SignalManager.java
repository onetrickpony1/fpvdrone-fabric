package com.fpvdrone.client;

import com.fpvdrone.FpvConfig;
import com.fpvdrone.entity.DroneEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SignalManager {
    private static final SignalManager instance = new SignalManager();
    private double distance = 0.0;
    private float signalStrength = 1.0F;
    private float freezeSignal = 1.0F;
    private boolean controlBlocked = false;
    private long lastUpdateTick = 0L;
    private boolean forcedNoSignal = false;
    private Level level = null;
    private int graceTicksRemaining = 0;
    private static final int RESET_GRACE_TICKS = 60;
    private Vec3 origin = null;
    private boolean djiLink = false;

    private SignalManager() {
    }

    public static SignalManager getInstance() {
        return instance;
    }

    public void setOrigin(Vec3 origin) {
        this.origin = origin;
    }

    public void setLevel(Level level) {
        this.level = level;
        SignalCalculator.setLevel(level);
    }

    public void tick(DroneEntity drone) {
        if (drone != null) {
            this.setLevel(drone.level());
            this.tick(drone.position(), drone.level().getGameTime());
        }
    }

    public void tick(Vec3 dronePos, long gameTime) {
        if (this.forcedNoSignal) {
            this.signalStrength = 0.0F;
            this.freezeSignal = 0.0F;
            this.controlBlocked = true;
        } else if (this.graceTicksRemaining > 0) {
            this.graceTicksRemaining--;
            this.distance = 0.0;
            this.signalStrength = 1.0F;
            this.freezeSignal = 1.0F;
            this.controlBlocked = false;
        } else if (this.origin == null) {
            this.distance = 0.0;
            this.signalStrength = 1.0F;
            this.freezeSignal = 1.0F;
            this.controlBlocked = false;
        } else {
            this.distance = SignalCalculator.calculateDistance(dronePos, this.origin);
            double maxRange = FpvConfig.getMaxRange();
            if (this.distance >= maxRange) {
                this.signalStrength = 0.0F;
                this.freezeSignal = 0.0F;
                this.controlBlocked = true;
                this.lastUpdateTick = gameTime;
            } else {
                if (this.level != null) {
                    this.signalStrength = SignalCalculator.calculateSignalFull(dronePos, this.origin, this.level);
                } else {
                    this.signalStrength = 1.0F;
                }

                this.freezeSignal = SignalCalculator.calculateFreezeSignal(this.distance);
                this.controlBlocked = this.signalStrength <= 0.01F;
                this.lastUpdateTick = gameTime;
            }
        }
    }

    public float getSignalStrength() {
        return this.signalStrength;
    }

    public float getFreezeSignal() {
        return this.freezeSignal;
    }

    public double getDistance() {
        return this.distance;
    }

    public boolean isControlBlocked() {
        return this.controlBlocked;
    }

    public void forceNoSignal() {
        this.forcedNoSignal = true;
        this.signalStrength = 0.0F;
        this.freezeSignal = 0.0F;
        this.controlBlocked = true;
        this.graceTicksRemaining = 0;
    }

    public boolean isForcedNoSignal() {
        return this.forcedNoSignal;
    }

    public boolean isInGracePeriod() {
        return this.graceTicksRemaining > 0;
    }

    /** 1.1.8: where the signal is measured from (the pilot, or the DJI controller). */
    public Vec3 getOrigin() {
        return this.origin;
    }

    /**
     * 1.1.8: DJI link mode (Mavic flown from its controller). 1.1.8 then computes signal from the controller with its own
     * model (Record_TRHK7MZuHe). 1.1.8-PENDING: that model; for now the flag only resets the signal to full.
     */
    public void setDjiLink(boolean on) {
        this.djiLink = on;
        if (on) {
            this.forcedNoSignal = false;
            this.signalStrength = 1.0F;
            this.controlBlocked = false;
        }
    }

    public boolean isDjiLink() {
        return this.djiLink;
    }

    public void reset() {
        this.djiLink = false;
        this.origin = null;
        this.distance = 0.0;
        this.signalStrength = 1.0F;
        this.freezeSignal = 1.0F;
        this.controlBlocked = false;
        this.lastUpdateTick = 0L;
        this.forcedNoSignal = false;
        this.graceTicksRemaining = 60;
        this.level = null;
        SignalCalculator.invalidateCache();
    }
}
