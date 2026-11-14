package com.fpvdrone.client;

import com.fpvdrone.data.BatteryDefinition;
import com.fpvdrone.data.BatteryDefinitions;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;

public class BatteryData {
    private int cells = 6;
    private int capacityMah = 3300;
    private int cRating = 75;
    private int remainingMah = -1;
    /** 1.1.8: which battery definition (data/fpvdrone/batteries) this pack is; sets voltage per cell and weight. */
    private String typeId = "default";

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

    /** 1.1.8: a full pack of the given battery type, with that definition's cells, capacity and C rating. */
    public static BatteryData forType(String typeId) {
        BatteryDefinition def = BatteryDefinitions.ibzfxic(typeId);
        if (def == null) {
            return new BatteryData();
        }

        BatteryData data = new BatteryData(def.rsDd50tfV(), def.uSUb(), def.jGhI2());
        data.typeId = def.wuhB();
        return data;
    }

    public String getTypeId() {
        return this.typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId != null && !typeId.isEmpty() ? typeId : "default";
    }

    /** Null only before the definitions have loaded. */
    private BatteryDefinition definition() {
        return BatteryDefinitions.ibzfxic(this.typeId);
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

    /** 1.1.8: volts per cell come from the battery definition (3.7 V for the default pack). */
    public float getVoltage() {
        BatteryDefinition def = this.definition();
        return this.cells * (def != null ? def.wEJx3Itb() : 3.7F);
    }

    /** 1.1.8: the weight is the definition's fixed weight (471.57 g for the default pack, = 3300 mAh x 0.1429). */
    public float getWeightGrams() {
        BatteryDefinition def = this.definition();
        return def != null ? def.bAcdvq9() : this.capacityMah * 0.1429F;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("cells", this.cells);
        tag.putInt("capacityMah", this.capacityMah);
        tag.putInt("cRating", this.cRating);
        tag.putString("typeId", this.typeId);
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
            if (tag.contains("typeId")) {
                data.setTypeId(tag.getStringOr("typeId", "default"));
            }

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
            return this.cells == that.cells && this.capacityMah == that.capacityMah && this.cRating == that.cRating && this.remainingMah == that.remainingMah
                && Objects.equals(this.typeId, that.typeId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.cells, this.capacityMah, this.cRating, this.remainingMah, this.typeId);
    }
}
