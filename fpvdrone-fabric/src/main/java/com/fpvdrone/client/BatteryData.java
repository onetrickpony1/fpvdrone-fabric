package com.fpvdrone.client;

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;

public class BatteryData {
    private static final float VOLTAGE_PER_CELL = 3.7F;
    private static final float WEIGHT_PER_MAH = 0.1429F;
    private int cells = 6;
    private int capacityMah = 3300;
    private int cRating = 75;
    private int remainingMah = -1;

    public BatteryData() {
    }

    public BatteryData(int cells, int capacityMah, int cRating) {
        this.setCells(cells);
        this.setCapacityMah(capacityMah);
        this.setCRating(cRating);
    }

    public static BatteryData defaults() {
        return new BatteryData();
    }

    public int getCells() {
        return this.cells;
    }

    public void setCells(int value) {
        this.cells = clamp(value, 1, 8);
    }

    public int getCapacityMah() {
        return this.capacityMah;
    }

    public void setCapacityMah(int value) {
        this.capacityMah = clamp(value, 300, 8000);
    }

    public int getCRating() {
        return this.cRating;
    }

    public void setCRating(int value) {
        this.cRating = clamp(value, 45, 150);
    }

    public int getRemainingMah() {
        return this.remainingMah < 0 ? this.capacityMah : this.remainingMah;
    }

    public void setRemainingMah(int value) {
        this.remainingMah = Math.max(0, Math.min(value, this.capacityMah));
    }

    public boolean isFullCharge() {
        return this.remainingMah < 0;
    }

    public float getVoltage() {
        return this.cells * 3.7F;
    }

    public float getWeightGrams() {
        return this.capacityMah * 0.1429F;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("cells", this.cells);
        tag.putInt("capacityMah", this.capacityMah);
        tag.putInt("cRating", this.cRating);
        if (this.remainingMah >= 0) {
            tag.putInt("remainingMah", this.remainingMah);
        }

        return tag;
    }

    public static BatteryData fromNbt(CompoundTag tag) {
        BatteryData data = new BatteryData();
        if (tag == null) {
            return data;
        } else {
            if (tag.contains("cells")) {
                data.setCells(tag.getIntOr("cells", 0));
            }

            if (tag.contains("capacityMah")) {
                data.setCapacityMah(tag.getIntOr("capacityMah", 0));
            }

            if (tag.contains("cRating")) {
                data.setCRating(tag.getIntOr("cRating", 0));
            }

            if (tag.contains("remainingMah")) {
                data.setRemainingMah(tag.getIntOr("remainingMah", 0));
            }

            return data;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            BatteryData that = (BatteryData)o;
            return this.cells == that.cells && this.capacityMah == that.capacityMah && this.cRating == that.cRating && this.remainingMah == that.remainingMah;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.cells, this.capacityMah, this.cRating, this.remainingMah);
    }
}
