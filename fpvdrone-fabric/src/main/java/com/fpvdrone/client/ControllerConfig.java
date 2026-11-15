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
    // 1.1.8: controller channel (axis index, or button index when *IsButton) for each bindable action; -1 = none.
    private int exitChannel = -1;
    private boolean exitIsButton = true;
    private int bombChannel = -1;
    private boolean bombIsButton = true;
    private int zoomChannel = -1;
    private boolean zoomIsButton = true;
    private int rthChannel = -1;
    private boolean rthIsButton = true;
    private int hoverChannel = -1;
    private boolean hoverIsButton = true;
    private int fireRocketChannel = -1;
    private boolean fireRocketIsButton = true;
    private int focusChannel = -1;
    private boolean focusIsButton = true;
    private int interactChannel = -1;
    private boolean interactIsButton = true;
    private int turtleChannel = -1;
    private boolean turtleIsButton = true;
    // 1.1.8: gimbal tilt axis (-1 = none: keys / mouse wheel tilt the camera instead).
    private int gimbalChannel = -1;
    private boolean invertGimbal = false;
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

    public int getExitChannel() { return this.exitChannel; }
    public void setExitChannel(int channel) { this.exitChannel = channel; }
    public boolean isExitIsButton() { return this.exitIsButton; }
    public void setExitIsButton(boolean isButton) { this.exitIsButton = isButton; }

    public int getBombChannel() { return this.bombChannel; }
    public void setBombChannel(int channel) { this.bombChannel = channel; }
    public boolean isBombIsButton() { return this.bombIsButton; }
    public void setBombIsButton(boolean isButton) { this.bombIsButton = isButton; }

    public int getZoomChannel() { return this.zoomChannel; }
    public void setZoomChannel(int channel) { this.zoomChannel = channel; }
    public boolean isZoomIsButton() { return this.zoomIsButton; }
    public void setZoomIsButton(boolean isButton) { this.zoomIsButton = isButton; }

    public int getRthChannel() { return this.rthChannel; }
    public void setRthChannel(int channel) { this.rthChannel = channel; }
    public boolean isRthIsButton() { return this.rthIsButton; }
    public void setRthIsButton(boolean isButton) { this.rthIsButton = isButton; }

    public int getTurtleChannel() { return this.turtleChannel; }
    public void setTurtleChannel(int channel) { this.turtleChannel = channel; }
    public boolean isTurtleIsButton() { return this.turtleIsButton; }
    public void setTurtleIsButton(boolean isButton) { this.turtleIsButton = isButton; }
    public int getHoverChannel() { return this.hoverChannel; }
    public void setHoverChannel(int channel) { this.hoverChannel = channel; }
    public boolean isHoverIsButton() { return this.hoverIsButton; }
    public void setHoverIsButton(boolean isButton) { this.hoverIsButton = isButton; }

    public int getFireRocketChannel() { return this.fireRocketChannel; }
    public void setFireRocketChannel(int channel) { this.fireRocketChannel = channel; }
    public boolean isFireRocketIsButton() { return this.fireRocketIsButton; }
    public void setFireRocketIsButton(boolean isButton) { this.fireRocketIsButton = isButton; }

    public int getFocusChannel() { return this.focusChannel; }
    public void setFocusChannel(int channel) { this.focusChannel = channel; }
    public boolean isFocusIsButton() { return this.focusIsButton; }
    public void setFocusIsButton(boolean isButton) { this.focusIsButton = isButton; }

    public int getInteractChannel() { return this.interactChannel; }
    public void setInteractChannel(int channel) { this.interactChannel = channel; }
    public boolean isInteractIsButton() { return this.interactIsButton; }
    public void setInteractIsButton(boolean isButton) { this.interactIsButton = isButton; }

    public int getGimbalChannel() { return this.gimbalChannel; }
    public void setGimbalChannel(int channel) { this.gimbalChannel = channel; }
    public boolean isInvertGimbal() { return this.invertGimbal; }
    public void setInvertGimbal(boolean invert) { this.invertGimbal = invert; }

    /** 1.1.8 additions to controller.json: gimbal channel/inversion and one {channel, isButton} per bindable action. */
    public void writeBindings(com.google.gson.JsonObject json) {
        if (json.has("channels")) {
            json.getAsJsonObject("channels").addProperty("gimbal", this.gimbalChannel);
        }

        if (json.has("inversion")) {
            json.getAsJsonObject("inversion").addProperty("gimbal", this.invertGimbal);
        }

        com.google.gson.JsonObject bindings = new com.google.gson.JsonObject();
        for (DroneAction action : DroneAction.values()) {
            com.google.gson.JsonObject entry = new com.google.gson.JsonObject();
            entry.addProperty("channel", action.getChannel(this));
            entry.addProperty("isButton", action.isButton(this));
            bindings.add(action.id(), entry);
        }

        json.add("bindings", bindings);
    }

    public void readBindings(com.google.gson.JsonObject json) {
        if (json.has("channels")) {
            com.google.gson.JsonObject channels = json.getAsJsonObject("channels");
            if (channels.has("gimbal")) {
                this.gimbalChannel = channels.get("gimbal").getAsInt();
            }
        }

        if (json.has("inversion")) {
            com.google.gson.JsonObject inversion = json.getAsJsonObject("inversion");
            if (inversion.has("gimbal")) {
                this.invertGimbal = inversion.get("gimbal").getAsBoolean();
            }
        }

        if (json.has("bindings")) {
            com.google.gson.JsonObject bindings = json.getAsJsonObject("bindings");
            for (DroneAction action : DroneAction.values()) {
                if (bindings.has(action.id())) {
                    com.google.gson.JsonObject entry = bindings.getAsJsonObject(action.id());
                    if (entry.has("channel")) {
                        action.setChannel(this, entry.get("channel").getAsInt());
                    }

                    if (entry.has("isButton")) {
                        action.setButton(this, entry.get("isButton").getAsBoolean());
                    }
                }
            }
        }
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
