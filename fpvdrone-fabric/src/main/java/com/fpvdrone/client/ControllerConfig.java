package com.fpvdrone.client;

public class ControllerConfig {
    public static final int CHANNEL_THROTTLE = 0;
    public static final int CHANNEL_YAW = 1;
    public static final int CHANNEL_PITCH = 2;
    public static final int CHANNEL_ROLL = 3;
    private int throttleChannel = 1;
    private int yawChannel = 0;
    private int pitchChannel = 3;
    private int rollChannel = 2;
    private int armButton = 0;
    private int armChannel = 0;
    private boolean armIsButton = true;
    private boolean invertArm = false;
    private float[] axisRange = new float[]{-1.0F, 1.0F, -1.0F, 1.0F, -1.0F, 1.0F, -1.0F, 1.0F, -1.0F, 1.0F, -1.0F, 1.0F, -1.0F, 1.0F, -1.0F, 1.0F};
    private boolean invertThrottle = false;
    private boolean invertYaw = false;
    private boolean invertPitch = false;
    private boolean invertRoll = false;
    private float deadzone = 0.05F;
    private String preferredJoystickGuid = "";
    private String preferredJoystickName = "";
    private float throttleRcRate = 1.0F;
    private float yawRcRate = 0.6F;
    private float pitchRcRate = 0.6F;
    private float rollRcRate = 0.6F;
    private float throttleSuperRate = 0.7F;
    private float yawSuperRate = 0.67F;
    private float pitchSuperRate = 0.67F;
    private float rollSuperRate = 0.67F;
    private float throttleExpo = 0.0F;
    private float yawExpo = 0.15F;
    private float pitchExpo = 0.15F;
    private float rollExpo = 0.15F;

    public int getThrottleChannel() {
        return this.throttleChannel;
    }

    public void setThrottleChannel(int channel) {
        this.throttleChannel = channel;
    }

    public int getYawChannel() {
        return this.yawChannel;
    }

    public void setYawChannel(int channel) {
        this.yawChannel = channel;
    }

    public int getPitchChannel() {
        return this.pitchChannel;
    }

    public void setPitchChannel(int channel) {
        this.pitchChannel = channel;
    }

    public int getRollChannel() {
        return this.rollChannel;
    }

    public void setRollChannel(int channel) {
        this.rollChannel = channel;
    }

    public int getArmButton() {
        return this.armButton;
    }

    public void setArmButton(int button) {
        this.armButton = button;
    }

    public int getArmChannel() {
        return this.armChannel;
    }

    public void setArmChannel(int channel) {
        this.armChannel = channel;
    }

    public boolean isArmIsButton() {
        return this.armIsButton;
    }

    public void setArmIsButton(boolean isButton) {
        this.armIsButton = isButton;
    }

    public boolean isInvertArm() {
        return this.invertArm;
    }

    public void setInvertArm(boolean invert) {
        this.invertArm = invert;
    }

    public float[] getAxisRange() {
        return this.axisRange;
    }

    public void setAxisRange(float[] range) {
        if (range != null && range.length == 16) {
            this.axisRange = range;
        }
    }

    public float getAxisMin(int axis) {
        return axis >= 0 && axis < 8 ? this.axisRange[axis * 2] : -1.0F;
    }

    public float getAxisMax(int axis) {
        return axis >= 0 && axis < 8 ? this.axisRange[axis * 2 + 1] : 1.0F;
    }

    public void setAxisMin(int axis, float min) {
        if (axis >= 0 && axis < 8) {
            this.axisRange[axis * 2] = min;
        }
    }

    public void setAxisMax(int axis, float max) {
        if (axis >= 0 && axis < 8) {
            this.axisRange[axis * 2 + 1] = max;
        }
    }

    public boolean isInvertThrottle() {
        return this.invertThrottle;
    }

    public void setInvertThrottle(boolean invert) {
        this.invertThrottle = invert;
    }

    public boolean isInvertYaw() {
        return this.invertYaw;
    }

    public void setInvertYaw(boolean invert) {
        this.invertYaw = invert;
    }

    public boolean isInvertPitch() {
        return this.invertPitch;
    }

    public void setInvertPitch(boolean invert) {
        this.invertPitch = invert;
    }

    public boolean isInvertRoll() {
        return this.invertRoll;
    }

    public void setInvertRoll(boolean invert) {
        this.invertRoll = invert;
    }

    public float getDeadzone() {
        return this.deadzone;
    }

    public void setDeadzone(float deadzone) {
        this.deadzone = Math.max(0.0F, Math.min(1.0F, deadzone));
    }

    public String getPreferredJoystickGuid() {
        return this.preferredJoystickGuid;
    }

    public void setPreferredJoystickGuid(String guid) {
        this.preferredJoystickGuid = guid != null ? guid : "";
    }

    public String getPreferredJoystickName() {
        return this.preferredJoystickName;
    }

    public void setPreferredJoystickName(String name) {
        this.preferredJoystickName = name != null ? name : "";
    }

    public float getRcRate(int channel) {
        return switch (channel) {
            case 0 -> this.throttleRcRate;
            case 1 -> this.yawRcRate;
            case 2 -> this.pitchRcRate;
            case 3 -> this.rollRcRate;
            default -> 1.0F;
        };
    }

    public void setRcRate(int channel, float rate) {
        rate = Math.max(0.0F, Math.min(2.55F, rate));
        switch (channel) {
            case 0:
                this.throttleRcRate = rate;
                break;
            case 1:
                this.yawRcRate = rate;
                break;
            case 2:
                this.pitchRcRate = rate;
                break;
            case 3:
                this.rollRcRate = rate;
        }
    }

    public float getSuperRate(int channel) {
        return switch (channel) {
            case 0 -> this.throttleSuperRate;
            case 1 -> this.yawSuperRate;
            case 2 -> this.pitchSuperRate;
            case 3 -> this.rollSuperRate;
            default -> 0.7F;
        };
    }

    public void setSuperRate(int channel, float rate) {
        rate = Math.max(0.0F, Math.min(1.0F, rate));
        switch (channel) {
            case 0:
                this.throttleSuperRate = rate;
                break;
            case 1:
                this.yawSuperRate = rate;
                break;
            case 2:
                this.pitchSuperRate = rate;
                break;
            case 3:
                this.rollSuperRate = rate;
        }
    }

    public float getExpo(int channel) {
        return switch (channel) {
            case 0 -> this.throttleExpo;
            case 1 -> this.yawExpo;
            case 2 -> this.pitchExpo;
            case 3 -> this.rollExpo;
            default -> 0.0F;
        };
    }

    public void setExpo(int channel, float expo) {
        expo = Math.max(0.0F, Math.min(1.0F, expo));
        switch (channel) {
            case 0:
                this.throttleExpo = expo;
                break;
            case 1:
                this.yawExpo = expo;
                break;
            case 2:
                this.pitchExpo = expo;
                break;
            case 3:
                this.rollExpo = expo;
        }
    }

    public boolean isInverted(int channel) {
        return switch (channel) {
            case 0 -> this.invertThrottle;
            case 1 -> this.invertYaw;
            case 2 -> this.invertPitch;
            case 3 -> this.invertRoll;
            default -> false;
        };
    }

    public void setInverted(int channel, boolean invert) {
        switch (channel) {
            case 0:
                this.invertThrottle = invert;
                break;
            case 1:
                this.invertYaw = invert;
                break;
            case 2:
                this.invertPitch = invert;
                break;
            case 3:
                this.invertRoll = invert;
        }
    }
}
