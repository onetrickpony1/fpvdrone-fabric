package com.fpvdrone.client;

import com.fpvdrone.entity.DroneEntity;
import java.util.UUID;

public class BatteryManager {
    private static final BatteryManager instance = new BatteryManager();
    private int batteryCells = 6;
    private float cellVoltage = 4.2F;
    private float openCircuitVoltage = 4.2F;
    private int capacityMah = 3300;
    private float usedMahFloat = 0.0F;
    private float currentAmps = 0.0F;
    /** Extra draw of the HD camera's clean feed in survival (1.0 = none). */
    private float currentMultiplier = 1.0F;
    private float wattage = 0.0F;
    private long lastTickTime = 0L;
    private boolean hasBattery = false;
    private boolean motorsArmed = false;
    private UUID initializedForDrone = null;
    private static final float CELL_VOLTAGE_FULL = 4.2F;
    private static final float CELL_VOLTAGE_EMPTY = 3.3F;
    private static final float IR_PER_CELL_FRESH = 0.008F;
    private static final float IR_PER_CELL_EMPTY = 0.02F;
    private static final float VOLTAGE_SMOOTHING = 0.008F;
    private static final float REFERENCE_WEIGHT = 1050.0F;
    private static final float OPTIMAL_AUW = 1500.0F;
    private static final float MAX_AUW = 3500.0F;
    private static final float HOVER_CURRENT_AT_REF = 12.0F;
    private static final float HOVER_CURRENT_AT_OPTIMAL = 25.0F;
    private static final float HOVER_CURRENT_AT_MAX = 80.0F;
    private static final float PEAK_CURRENT = 120.0F;
    private static final float IDLE_CURRENT = 1.5F;

    private BatteryManager() {
    }

    public static BatteryManager getInstance() {
        return instance;
    }

    public void initFromDrone(DroneEntity drone) {
        if (drone != null && drone.hasBattery()) {
            if (drone.getUUID().equals(this.initializedForDrone)) {
                return;
            }

            BatteryData data = drone.getBattery();
            this.batteryCells = data.getCells();
            this.capacityMah = data.getCapacityMah();
            this.hasBattery = true;
            this.initializedForDrone = drone.getUUID();
            this.reset();
            float syncedUsed = drone.getSyncedUsedMah();
            if (syncedUsed > 0.0F) {
                this.usedMahFloat = syncedUsed;
                float percentUsed = this.usedMahFloat / this.capacityMah;
                this.cellVoltage = 4.2F - percentUsed * 0.89999986F;
                this.cellVoltage = Math.max(3.3F, this.cellVoltage);
                this.openCircuitVoltage = this.cellVoltage;
            }
        } else {
            this.hasBattery = false;
        }
    }

    public boolean hasBattery() {
        return this.hasBattery;
    }

    public void setHasBattery(boolean value) {
        this.hasBattery = value;
    }

    public void setMotorsArmed(boolean value) {
        this.motorsArmed = value;
    }

    public void tick(float throttle, float totalWeight) {
        if (this.hasBattery) {
            long now = System.currentTimeMillis();
            if (this.lastTickTime == 0L) {
                this.lastTickTime = now;
            } else {
                float dt = (float)(now - this.lastTickTime) / 1000.0F;
                this.lastTickTime = now;
                float throttleNorm = (throttle + 1.0F) / 2.0F;
                float hoverCurrent = calculateHoverCurrent(totalWeight);
                float maxCurrentAtWeight = calculateMaxCurrent(totalWeight);
                if (!(throttleNorm < 0.01F) && this.motorsArmed) {
                    float throttleFactor = throttleNorm * throttleNorm;
                    this.currentAmps = hoverCurrent + throttleFactor * (maxCurrentAtWeight - hoverCurrent);
                } else {
                    this.currentAmps = 1.5F;
                }

                this.currentAmps *= this.currentMultiplier;
                float mahUsed = this.currentAmps * dt * 1000.0F / 3600.0F;
                this.usedMahFloat += mahUsed;
                float percentUsed = this.usedMahFloat / this.capacityMah;
                this.openCircuitVoltage = 4.2F - percentUsed * 0.89999986F;
                this.openCircuitVoltage = Math.max(3.3F, this.openCircuitVoltage);
                float depletionFactor = Math.min(1.0F, percentUsed);
                float irPerCell = 0.008F + depletionFactor * 0.011999999F;
                float sagVolts = this.currentAmps * irPerCell;
                float targetVoltage = Math.max(3.3F, this.openCircuitVoltage - sagVolts);
                this.cellVoltage = this.cellVoltage + 0.008F * (targetVoltage - this.cellVoltage);
                this.cellVoltage = Math.max(3.3F, this.cellVoltage);
                float totalVoltage = this.cellVoltage * this.batteryCells;
                this.wattage = this.currentAmps * totalVoltage;
            }
        }
    }

    public static float calculateWeightMultiplier(float totalWeight) {
        float ratio = totalWeight / 580.0F;
        return ratio * ratio;
    }

    public static float calculateHoverCurrent(float totalWeight) {
        if (totalWeight <= 1050.0F) {
            float ratio = totalWeight / 1050.0F;
            return 12.0F * (float)Math.pow(ratio, 1.5);
        } else if (totalWeight <= 1500.0F) {
            float t = (totalWeight - 1050.0F) / 450.0F;
            return 12.0F + t * 13.0F;
        } else {
            float t = (totalWeight - 1500.0F) / 2000.0F;
            t = Math.min(t, 1.0F);
            return 25.0F + t * 55.0F;
        }
    }

    public static float calculateMaxCurrent(float totalWeight) {
        if (totalWeight <= 1050.0F) {
            return 45.0F;
        } else if (totalWeight <= 1500.0F) {
            float t = (totalWeight - 1050.0F) / 450.0F;
            return 45.0F + t * 30.0F;
        } else {
            float t = (totalWeight - 1500.0F) / 2000.0F;
            t = Math.min(t, 1.0F);
            return 75.0F + t * 45.0F;
        }
    }

    public void reset() {
        this.cellVoltage = 4.2F;
        this.openCircuitVoltage = 4.2F;
        this.usedMahFloat = 0.0F;
        this.currentAmps = 0.0F;
        this.wattage = 0.0F;
        this.lastTickTime = 0L;
    }

    public void fullReset() {
        this.reset();
        this.initializedForDrone = null;
    }

    public float getTotalVoltage() {
        return this.cellVoltage * this.batteryCells;
    }

    public float getCellVoltage() {
        return this.cellVoltage;
    }

    public int getUsedMah() {
        return (int)this.usedMahFloat;
    }

    public float getUsedMahFloat() {
        return this.usedMahFloat;
    }

    public void setCurrentMultiplier(float multiplier) {
        this.currentMultiplier = multiplier;
    }

    public float getCurrentMultiplier() {
        return this.currentMultiplier;
    }

    public float getCurrentAmps() {
        return this.currentAmps;
    }

    public int getBatteryPercent() {
        if (this.capacityMah <= 0) {
            return 0;
        } else {
            float remaining = 1.0F - this.usedMahFloat / this.capacityMah;
            return Math.max(0, Math.min(100, (int)(remaining * 100.0F)));
        }
    }

    public void setBatteryCells(int cells) {
        this.batteryCells = cells;
    }

    public int getCapacityMah() {
        return this.capacityMah;
    }

    public boolean isDead() {
        return this.hasBattery && this.usedMahFloat >= this.capacityMah;
    }
}
