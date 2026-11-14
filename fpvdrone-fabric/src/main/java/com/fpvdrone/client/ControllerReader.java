package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.osd.BootStateManager;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.physics.PhysicsState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.InputConstants;

public class ControllerReader {
    private static final ControllerReader instance = new ControllerReader();
    private static ControllerConfig config = new ControllerConfig();
    private int connectedJoystick = -1;
    private float[] axes = new float[16];
    private boolean[] buttons = new boolean[32];
    private boolean lastArmState = false;
    private int actualAxisCount = 0;
    private int actualButtonCount = 0;
    private float throttle = 0.0F;
    private float yaw = 0.0F;
    private float pitch = 0.0F;
    private float roll = 0.0F;
    private float gimbal = 0.0F;
    /** Throttle held while a screen is open or the window is in the background (1.1.8). */
    private float heldThrottle = -1.0F;
    private float keyboardThrottle = -1.0F;
    private float keyboardYaw = 0.0F;
    private float keyboardPitch = 0.0F;
    private float keyboardRoll = 0.0F;
    /** 1.1.8: GPS-style keyboard flying (DJI): climb/descend keys, W/S pitch, A/D roll, throttle centred when let go. */
    private boolean gpsMode = false;
    private final boolean[] lastActionStates = new boolean[DroneAction.values().length];
    private static final float THROTTLE_RAMP_SPEED = 0.4F;
    private static final float YAW_RAMP_SPEED = 2.0F;
    private static final float YAW_DECAY_SPEED = 3.5F;
    private long lastKbmNanos = 0L;

    private ControllerReader() {
    }

    public static ControllerReader getInstance() {
        return instance;
    }

    public static ControllerConfig getConfig() {
        return config;
    }

    public static void setConfig(ControllerConfig newConfig) {
        config = newConfig;
    }

    public void init(long windowHandle) {
        SettingsLoader.loadAll(config);
        SdlJoysticks.init();
        this.detectJoystick();
    }

    public void saveConfig() {
        SettingsLoader.saveAll(config);
    }

    private void detectJoystick() {
        this.connectedJoystick = this.resolvePreferredJid();
    }

    private int resolvePreferredJid() {
        String preferred = config.getPreferredJoystickGuid();
        if (preferred.isEmpty()) {
            return -1;
        } else {
            SdlJoysticks.refresh(false);

            for (int jid = 0; jid < SdlJoysticks.MAX_JOYSTICKS; jid++) {
                if (SdlJoysticks.isPresent(jid)) {
                    String guid = SdlJoysticks.getGuid(jid);
                    if (preferred.equals(guid)) {
                        return jid;
                    }
                }
            }

            return -1;
        }
    }

    public void bindToJid(int jid) {
        String guid = SdlJoysticks.getGuid(jid);
        String name = SdlJoysticks.getName(jid);
        config.setPreferredJoystickGuid(guid != null ? guid : "");
        config.setPreferredJoystickName(name != null ? name : "");
        SettingsLoader.saveControllerConfig(config);
        this.connectedJoystick = jid;
    }

    public void clearPreferredDevice() {
        config.setPreferredJoystickGuid("");
        config.setPreferredJoystickName("");
        SettingsLoader.saveControllerConfig(config);
        this.connectedJoystick = -1;
    }

    public static float[] readAxes(int jid) {
        float[] result = new float[16];
        if (SdlJoysticks.readGamepadAxes(jid, result) == 0) {
            SdlJoysticks.readJoystickAxes(jid, result);
        }

        return result;
    }

    public static boolean[] readButtons(int jid) {
        boolean[] result = new boolean[32];
        if (SdlJoysticks.readGamepadButtons(jid, result) == 0) {
            SdlJoysticks.readJoystickButtons(jid, result);
        }

        return result;
    }

    public void poll() {
        SdlJoysticks.refresh(false);
        if (this.connectedJoystick == -1) {
            this.detectJoystick();
        }

        if (this.connectedJoystick != -1 && !SdlJoysticks.isPresent(this.connectedJoystick)) {
            // Device unplugged: hand over to keyboard with the current stick positions.
            this.keyboardThrottle = this.throttle;
            this.keyboardYaw = this.yaw;
            this.lastKbmNanos = 0L;
            this.connectedJoystick = -1;
        }

        if (this.connectedJoystick != -1 && !FpvConfig.isForceKbm()) {
            int axisCount = SdlJoysticks.readGamepadAxes(this.connectedJoystick, this.axes);
            if (axisCount > 0) {
                this.actualAxisCount = axisCount;
                this.actualButtonCount = SdlJoysticks.readGamepadButtons(this.connectedJoystick, this.buttons);
            } else {
                this.actualAxisCount = SdlJoysticks.readJoystickAxes(this.connectedJoystick, this.axes);
                this.actualButtonCount = SdlJoysticks.readJoystickButtons(this.connectedJoystick, this.buttons);
            }

            this.throttle = this.processCalibratedAxis(config.getThrottleChannel(), config.isInvertThrottle());
            this.yaw = this.processCalibratedAxis(config.getYawChannel(), config.isInvertYaw());
            this.pitch = this.processCalibratedAxis(config.getPitchChannel(), config.isInvertPitch());
            this.roll = this.processCalibratedAxis(config.getRollChannel(), config.isInvertRoll());
            this.gimbal = config.getGimbalChannel() >= 0 ? this.processCalibratedAxis(config.getGimbalChannel(), config.isInvertGimbal()) : 0.0F;
            if (isInputBlocked()) {
                // 1.1.8: a screen or an unfocused window centres the sticks but keeps the throttle where it was.
                this.yaw = 0.0F;
                this.pitch = 0.0F;
                this.roll = 0.0F;
                this.gimbal = 0.0F;
                this.throttle = this.heldThrottle;
            } else {
                this.heldThrottle = this.throttle;
            }
        } else {
            long now = System.nanoTime();
            float dt = this.lastKbmNanos == 0L ? 0.016F : Math.min((float)(now - this.lastKbmNanos) / 1.0E9F, 0.05F);
            this.lastKbmNanos = now;
            if (isInputBlocked()) {
                this.throttle = this.heldThrottle;
                this.yaw = 0.0F;
                this.pitch = 0.0F;
                this.roll = 0.0F;
                this.gimbal = 0.0F;
                this.keyboardYaw = 0.0F;
                this.keyboardPitch = 0.0F;
                this.keyboardRoll = 0.0F;
            } else {
                this.updateKeyboardInput(dt);
                this.throttle = this.keyboardThrottle;
                this.yaw = this.keyboardYaw;
                this.pitch = this.keyboardPitch;
                this.roll = this.keyboardRoll;
                this.gimbal = 0.0F;
                this.heldThrottle = this.throttle;
            }
        }
    }

    /** A screen is open or the game window is in the background. */
    private static boolean isInputBlocked() {
        Minecraft mc = Minecraft.getInstance();
        return mc.gui.screen() != null || !mc.isWindowActive();
    }

    private void updateKeyboardInput(float dt) {
        if (this.gpsMode) {
            // GPS-style (DJI): climb/descend keys drive the throttle stick; W/S and A/D are pitch and roll; all spring back.
            boolean climb = KeyManager.isKeyHeld(KeyManager.THROTTLE_CLIMB_KEY, InputConstants.KEY_SPACE);
            boolean descend = KeyManager.isKeyHeld(KeyManager.THROTTLE_DESCEND_KEY, InputConstants.KEY_LALT);
            this.keyboardThrottle = springStick(this.keyboardThrottle, climb, descend, dt);
            boolean forward = InputConstants.isKeyDown(InputConstants.KEY_W);
            boolean back = InputConstants.isKeyDown(InputConstants.KEY_S);
            this.keyboardPitch = springStick(this.keyboardPitch, forward, back, dt);
            boolean right = InputConstants.isKeyDown(InputConstants.KEY_D);
            boolean left = InputConstants.isKeyDown(InputConstants.KEY_A);
            this.keyboardRoll = springStick(this.keyboardRoll, right, left, dt);
            this.keyboardYaw = 0.0F;
            return;
        }

        boolean upDown = KeyManager.isKeyHeld(KeyManager.THROTTLE_UP_KEY, InputConstants.KEY_W);
        boolean downDown = KeyManager.isKeyHeld(KeyManager.THROTTLE_DOWN_KEY, InputConstants.KEY_S);
        if (upDown && !downDown) {
            this.keyboardThrottle += 0.4F * dt;
        } else if (downDown && !upDown) {
            this.keyboardThrottle -= 0.4F * dt;
        }

        this.keyboardThrottle = clamp(this.keyboardThrottle, -1.0F, 1.0F);
        boolean aDown = KeyManager.isKeyHeld(KeyManager.YAW_LEFT_KEY, InputConstants.KEY_A);
        boolean dDown = KeyManager.isKeyHeld(KeyManager.YAW_RIGHT_KEY, InputConstants.KEY_D);
        if (aDown && !dDown) {
            this.keyboardYaw = moveToward(this.keyboardYaw, -1.0F, 2.0F * dt);
        } else if (dDown && !aDown) {
            this.keyboardYaw = moveToward(this.keyboardYaw, 1.0F, 2.0F * dt);
        } else {
            this.keyboardYaw = decayToZero(this.keyboardYaw, 3.5F * dt);
        }

        this.keyboardYaw = clamp(this.keyboardYaw, -1.0F, 1.0F);
        this.keyboardPitch = 0.0F;
        this.keyboardRoll = 0.0F;
    }

    /** A self-centring stick: ramps towards +1/-1 while a key is held, back to 0 when let go. */
    private static float springStick(float value, boolean positive, boolean negative, float dt) {
        if (positive && !negative) {
            return clamp(moveToward(value, 1.0F, 2.0F * dt), -1.0F, 1.0F);
        } else {
            return negative && !positive ? clamp(moveToward(value, -1.0F, 2.0F * dt), -1.0F, 1.0F) : decayToZero(value, 3.5F * dt);
        }
    }

    /**
     * 1.1.8: GPS-style keyboard flying for DJI drones. Switching it on centres the sticks. FPVClientMod sets it from
     * the drone's kind when the flight state is set up.
     */
    public static void setGpsMode(boolean gps) {
        ControllerReader reader = getInstance();
        if (gps && !reader.gpsMode) {
            reader.keyboardThrottle = 0.0F;
            reader.keyboardPitch = 0.0F;
            reader.keyboardRoll = 0.0F;
        }

        reader.gpsMode = gps;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float decayToZero(float value, float amount) {
        if (value > 0.0F) {
            return Math.max(0.0F, value - amount);
        } else {
            return value < 0.0F ? Math.min(0.0F, value + amount) : 0.0F;
        }
    }

    private static float moveToward(float current, float target, float maxDelta) {
        return Math.abs(target - current) <= maxDelta ? target : current + Math.signum(target - current) * maxDelta;
    }

    private float getRawAxis(int channel) {
        return channel >= 0 && channel < this.axes.length ? this.axes[channel] : 0.0F;
    }

    private float processCalibratedAxis(int channel, boolean invert) {
        float raw = this.getRawAxis(channel);
        float min = config.getAxisMin(channel);
        float max = config.getAxisMax(channel);
        if (Math.abs(max - min) < 0.001F) {
            min = -1.0F;
            max = 1.0F;
        }

        if (raw < min) {
            return invert ? 1.0F : -1.0F;
        } else if (raw > max) {
            return invert ? -1.0F : 1.0F;
        } else {
            float d = raw - min;
            float p = d / (max - min);
            float normalized = p * 2.0F - 1.0F;
            if (invert) {
                normalized = -normalized;
            }

            normalized = Math.max(-1.0F, Math.min(1.0F, normalized));
            float deadzone = config.getDeadzone();
            if (Math.abs(normalized) < deadzone) {
                return 0.0F;
            } else {
                float sign = Math.signum(normalized);
                float absValue = Math.abs(normalized);
                return sign * (absValue - deadzone) / (1.0F - deadzone);
            }
        }
    }

    public static float processAxis(float value, float deadzone, boolean invert) {
        value = Math.max(-1.0F, Math.min(1.0F, value));
        if (Math.abs(value) < deadzone) {
            return 0.0F;
        } else {
            float sign = Math.signum(value);
            float absValue = Math.abs(value);
            float normalized = (absValue - deadzone) / (1.0F - deadzone);
            float result = sign * normalized;
            if (invert) {
                result = -result;
            }

            return Math.max(-1.0F, Math.min(1.0F, result));
        }
    }

    public boolean readArmState() {
        if (config.isArmIsButton()) {
            int button = config.getArmChannel();
            if (button >= 0 && button < this.buttons.length) {
                boolean state = this.buttons[button];
                return config.isInvertArm() ? !state : state;
            } else {
                return false;
            }
        } else {
            int channel = config.getArmChannel();
            if (channel >= 0 && channel < this.axes.length) {
                float value = this.axes[channel];
                boolean state = value > 0.5F;
                return config.isInvertArm() ? !state : state;
            } else {
                return false;
            }
        }
    }

    public boolean checkArmToggle() {
        boolean currentState = this.readArmState();
        boolean toggled = currentState && !this.lastArmState;
        this.lastArmState = currentState;
        return toggled;
    }

    /**
     * Arm switch edge -> arm/disarm, then the controller-bound actions and the gimbal tilt.
     * 1.1.8 runs all three in the render tick (screen closed) right after poll(); the port's FPVClientMod calls this method there.
     */
    public void handleArmToggle(UUID playerId, Consumer<Boolean> onArmStateChanged) {
        boolean toggled = this.checkArmToggle();
        Minecraft mc = Minecraft.getInstance();
        if (!isInputBlocked() && toggled && mc.player != null) {
            boolean wasArmed = FPVMod.isArmed(playerId);
            KeyManager.handleMotorToggle(mc);
            boolean nowArmed = FPVMod.isArmed(playerId);
            if (onArmStateChanged != null && wasArmed != nowArmed) {
                onArmStateChanged.accept(nowArmed);
            }
        }

        this.pollActions(mc);
        com.fpvdrone.render.CameraManager.tickGimbal(mc);
    }

    /** 1.1.8: runs each action whose controller switch/button was just pressed (arm is handled above). */
    public void pollActions(Minecraft mc) {
        if (mc.player == null) {
            return;
        }

        boolean blocked = isInputBlocked();
        for (DroneAction action : DroneAction.values()) {
            if (action != DroneAction.ARM) {
                boolean down = this.isActionDown(action);
                boolean pressed = down && !this.lastActionStates[action.ordinal()];
                this.lastActionStates[action.ordinal()] = down;
                if (pressed && !blocked) {
                    action.run(mc);
                }
            }
        }
    }

    private boolean isActionDown(DroneAction action) {
        int channel = action.getChannel(config);
        if (channel < 0) {
            return false;
        } else if (action.isButton(config)) {
            return channel < this.buttons.length && this.buttons[channel];
        } else {
            return channel < this.axes.length && this.axes[channel] > 0.5F;
        }
    }

    public boolean isConnected() {
        return this.connectedJoystick != -1 || !FpvConfig.isForceKbm();
    }

    public boolean isPhysicalControllerConnected() {
        return this.connectedJoystick != -1;
    }

    public boolean isGamepadController() {
        return this.connectedJoystick != -1 && SdlJoysticks.isGamepad(this.connectedJoystick);
    }

    public String getControllerName() {
        return this.connectedJoystick == -1 ? null : SdlJoysticks.getName(this.connectedJoystick);
    }

    public List<String> getAvailableControllers() {
        List<String> controllers = new ArrayList<>();

        SdlJoysticks.refresh(true);

        for (int jid = 0; jid < SdlJoysticks.MAX_JOYSTICKS; jid++) {
            if (SdlJoysticks.isPresent(jid)) {
                String name = SdlJoysticks.getName(jid);
                controllers.add(name != null ? name : "Controller " + jid);
            }
        }

        return controllers;
    }

    public void selectController(int index) {
        int count = 0;

        for (int jid = 0; jid < SdlJoysticks.MAX_JOYSTICKS; jid++) {
            if (SdlJoysticks.isPresent(jid)) {
                if (count == index) {
                    this.bindToJid(jid);
                    return;
                }

                count++;
            }
        }
    }

    public static float getThrottle() {
        return getInstance().throttle;
    }

    public static float getYaw() {
        return getInstance().yaw;
    }

    public static float getPitch() {
        return getInstance().pitch;
    }

    public static float getRoll() {
        return getInstance().roll;
    }

    /** Gimbal tilt wheel, -1..1 (0 with no tilt channel or while a screen is open). */
    public static float getGimbal() {
        return getInstance().gimbal;
    }

    /**
     * 1.1.8: stick axes read straight from the device. The setup wizard shows these, because the values above are
     * centred (and the throttle held) while a screen is open.
     */
    public static float readThrottleAxis() {
        return getInstance().processCalibratedAxis(config.getThrottleChannel(), config.isInvertThrottle());
    }

    public static float readYawAxis() {
        return getInstance().processCalibratedAxis(config.getYawChannel(), config.isInvertYaw());
    }

    public static float readPitchAxis() {
        return getInstance().processCalibratedAxis(config.getPitchChannel(), config.isInvertPitch());
    }

    public static float readRollAxis() {
        return getInstance().processCalibratedAxis(config.getRollChannel(), config.isInvertRoll());
    }

    /** Gimbal tilt wheel read straight from the device (for the setup wizard, which runs with a screen open). */
    public static float readGimbalAxis() {
        int channel = config.getGimbalChannel();
        return channel >= 0 ? getInstance().processCalibratedAxis(channel, config.isInvertGimbal()) : 0.0F;
    }

    public float getRawAxisValue(int axis) {
        return axis >= 0 && axis < this.axes.length ? this.axes[axis] : 0.0F;
    }

    public boolean getButton(int button) {
        return button >= 0 && button < this.buttons.length ? this.buttons[button] : false;
    }

    public int getAxisCount() {
        return this.actualAxisCount > 0 ? this.actualAxisCount : this.axes.length;
    }

    public int getButtonCount() {
        return this.actualButtonCount > 0 ? this.actualButtonCount : this.buttons.length;
    }

    public void cleanup() {
    }

    public static void startInputListening(Consumer<Integer> onChannelDetected) {
        InputListener.getInstance().startListening(result -> {
            if (onChannelDetected != null) {
                onChannelDetected.accept(result.channel);
            }
        });
    }

    public static void startInputListeningFull(Consumer<InputListener.DetectionResult> onDetected) {
        InputListener.getInstance().startListening(onDetected);
    }

    public static void startRangeListening(Consumer<float[]> onRangeComplete) {
        RangeListener.getInstance().startListening(onRangeComplete);
    }

    public static void startRangeListening(Consumer<float[]> onRangeComplete, long durationMs) {
        RangeListener.getInstance().startListening(onRangeComplete, durationMs);
    }

    public static void stopAllListening() {
        InputListener.getInstance().stopListening();
        RangeListener.getInstance().stopListening();
    }

    public static float getAxisDiffOnChannel(int channel) {
        return InputListener.getInstance().getAxisDiff(channel);
    }

    public static boolean isInputListening() {
        return InputListener.getInstance().isListening();
    }

    public static boolean isRangeListening() {
        return RangeListener.getInstance().isListening();
    }

    public static void tickListeners() {
        InputListener.getInstance().tick();
        RangeListener.getInstance().tick();
    }
}
