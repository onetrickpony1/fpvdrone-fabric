package com.fpvdrone.client;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;

public class DroneConfig {
    public static final int CHANNEL_THROTTLE = 0;
    public static final int CHANNEL_YAW = 1;
    public static final int CHANNEL_PITCH = 2;
    public static final int CHANNEL_ROLL = 3;
    private float[] rcRate = new float[]{1.0F, 1.15F, 1.15F, 1.15F};
    private float[] superRate = new float[]{0.7F, 0.67F, 0.67F, 0.67F};
    private float[] expo = new float[]{0.0F, 0.0F, 0.0F, 0.0F};
    private float motorKv = 1300.0F;
    private float propDiameter = 9.0F;
    private float propPitch = 4.5F;
    private float dragCoefficient = 1.1F;
    private float thrustMultiplier = 1.0F;
    private boolean flightMode3d = false;
    private String droneName = "KINDER";

    public static DroneConfig defaults() {
        return new DroneConfig();
    }

    public float getRcRate(int channel) {
        return channel >= 0 && channel < 4 ? this.rcRate[channel] : 1.0F;
    }

    public void setRcRate(int channel, float value) {
        if (channel >= 0 && channel < 4) {
            this.rcRate[channel] = clamp(value, 0.0F, 2.55F);
        }
    }

    public float getSuperRate(int channel) {
        return channel >= 0 && channel < 4 ? this.superRate[channel] : 0.7F;
    }

    public void setSuperRate(int channel, float value) {
        if (channel >= 0 && channel < 4) {
            this.superRate[channel] = clamp(value, 0.0F, 1.0F);
        }
    }

    public float getExpo(int channel) {
        return channel >= 0 && channel < 4 ? this.expo[channel] : 0.0F;
    }

    public void setExpo(int channel, float value) {
        if (channel >= 0 && channel < 4) {
            this.expo[channel] = clamp(value, 0.0F, 1.0F);
        }
    }

    public float getMotorKv() {
        return this.motorKv;
    }

    public void setMotorKv(float value) {
        this.motorKv = clamp(value, 800.0F, 3000.0F);
    }

    public float getPropDiameter() {
        return this.propDiameter;
    }

    public void setPropDiameter(float value) {
        this.propDiameter = clamp(value, 3.0F, 12.0F);
    }

    public float getPropPitch() {
        return this.propPitch;
    }

    public void setPropPitch(float value) {
        this.propPitch = clamp(value, 2.0F, 8.0F);
    }

    public float getDragCoefficient() {
        return this.dragCoefficient;
    }

    public void setDragCoefficient(float value) {
        this.dragCoefficient = clamp(value, 0.5F, 2.0F);
    }

    public float getThrustMultiplier() {
        return this.thrustMultiplier;
    }

    public void setThrustMultiplier(float value) {
        this.thrustMultiplier = clamp(value, 0.5F, 2.0F);
    }

    public boolean isFlightMode3d() {
        return this.flightMode3d;
    }

    public void setFlightMode3d(boolean value) {
        this.flightMode3d = value;
    }

    public String getDroneName() {
        return this.droneName;
    }

    public void setDroneName(String value) {
        if (value != null && value.length() <= 20) {
            this.droneName = value.isEmpty() ? "KINDER" : value;
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        CompoundTag rates = new CompoundTag();
        rates.put("rcRate", floatArrayToList(this.rcRate));
        rates.put("superRate", floatArrayToList(this.superRate));
        rates.put("expo", floatArrayToList(this.expo));
        tag.put("rates", rates);
        CompoundTag physics = new CompoundTag();
        physics.putFloat("motorKv", this.motorKv);
        physics.putFloat("propDiameter", this.propDiameter);
        physics.putFloat("propPitch", this.propPitch);
        physics.putFloat("dragCoefficient", this.dragCoefficient);
        physics.putFloat("thrustMultiplier", this.thrustMultiplier);
        physics.putBoolean("flightMode3d", this.flightMode3d);
        physics.putString("droneName", this.droneName);
        tag.put("physics", physics);
        return tag;
    }

    public static DroneConfig fromNbt(CompoundTag tag) {
        DroneConfig config = new DroneConfig();
        if (tag == null) {
            return config;
        } else {
            if (tag.contains("rates")) {
                CompoundTag rates = tag.getCompoundOrEmpty("rates");
                if (rates.contains("rcRate")) {
                    float[] arr = listToFloatArray(rates.getListOrEmpty("rcRate"), 4);

                    for (int i = 0; i < 4; i++) {
                        config.setRcRate(i, arr[i]);
                    }
                }

                if (rates.contains("superRate")) {
                    float[] arr = listToFloatArray(rates.getListOrEmpty("superRate"), 4);

                    for (int i = 0; i < 4; i++) {
                        config.setSuperRate(i, arr[i]);
                    }
                }

                if (rates.contains("expo")) {
                    float[] arr = listToFloatArray(rates.getListOrEmpty("expo"), 4);

                    for (int i = 0; i < 4; i++) {
                        config.setExpo(i, arr[i]);
                    }
                }
            }

            if (tag.contains("physics")) {
                CompoundTag physics = tag.getCompoundOrEmpty("physics");
                if (physics.contains("motorKv")) {
                    config.setMotorKv(physics.getFloatOr("motorKv", 0.0F));
                }

                if (physics.contains("propDiameter")) {
                    config.setPropDiameter(physics.getFloatOr("propDiameter", 0.0F));
                }

                if (physics.contains("propPitch")) {
                    config.setPropPitch(physics.getFloatOr("propPitch", 0.0F));
                }

                if (physics.contains("dragCoefficient")) {
                    config.setDragCoefficient(physics.getFloatOr("dragCoefficient", 0.0F));
                }

                if (physics.contains("thrustMultiplier")) {
                    config.setThrustMultiplier(physics.getFloatOr("thrustMultiplier", 0.0F));
                }

                if (physics.contains("flightMode3d")) {
                    config.setFlightMode3d(physics.getBooleanOr("flightMode3d", false));
                }

                if (physics.contains("droneName")) {
                    config.setDroneName(physics.getStringOr("droneName", ""));
                }
            }

            return config;
        }
    }

    private static ListTag floatArrayToList(float[] arr) {
        ListTag list = new ListTag();

        for (float f : arr) {
            list.add(FloatTag.valueOf(f));
        }

        return list;
    }

    private static float[] listToFloatArray(ListTag list, int size) {
        float[] arr = new float[size];

        for (int i = 0; i < Math.min(list.size(), size); i++) {
            arr[i] = list.getFloatOr(i, 0.0F);
        }

        return arr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            DroneConfig that = (DroneConfig)o;
            return Float.compare(that.motorKv, this.motorKv) == 0
                && Float.compare(that.propDiameter, this.propDiameter) == 0
                && Float.compare(that.propPitch, this.propPitch) == 0
                && Float.compare(that.dragCoefficient, this.dragCoefficient) == 0
                && Float.compare(that.thrustMultiplier, this.thrustMultiplier) == 0
                && this.flightMode3d == that.flightMode3d
                && Objects.equals(this.droneName, that.droneName)
                && Arrays.equals(this.rcRate, that.rcRate)
                && Arrays.equals(this.superRate, that.superRate)
                && Arrays.equals(this.expo, that.expo);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
            this.motorKv, this.propDiameter, this.propPitch, this.dragCoefficient, this.thrustMultiplier, this.flightMode3d, this.droneName
        );
        result = 31 * result + Arrays.hashCode(this.rcRate);
        result = 31 * result + Arrays.hashCode(this.superRate);
        return 31 * result + Arrays.hashCode(this.expo);
    }
}
