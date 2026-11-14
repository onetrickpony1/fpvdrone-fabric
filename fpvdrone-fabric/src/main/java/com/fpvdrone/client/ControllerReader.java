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
    private float keyboardThrottle = -1.0F;
    private float keyboardYaw = 0.0F;
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
        } else {
            long now = System.nanoTime();
            float dt = this.lastKbmNanos == 0L ? 0.016F : Math.min((float)(now - this.lastKbmNanos) / 1.0E9F, 0.05F);
            this.lastKbmNanos = now;
            this.updateKeyboardInput(dt);
            this.throttle = this.keyboardThrottle;
            this.yaw = this.keyboardYaw;
            this.pitch = 0.0F;
            this.roll = 0.0F;
        }
    }

    private void updateKeyboardInput(float dt) {
        boolean wDown = InputConstants.isKeyDown(InputConstants.KEY_W);
        boolean sDown = InputConstants.isKeyDown(InputConstants.KEY_S);
        if (wDown && !sDown) {
            this.keyboardThrottle += 0.4F * dt;
        } else if (sDown && !wDown) {
            this.keyboardThrottle -= 0.4F * dt;
        }

        this.keyboardThrottle = clamp(this.keyboardThrottle, -1.0F, 1.0F);
        boolean aDown = InputConstants.isKeyDown(InputConstants.KEY_A);
        boolean dDown = InputConstants.isKeyDown(InputConstants.KEY_D);
        if (aDown && !dDown) {
            this.keyboardYaw = moveToward(this.keyboardYaw, -1.0F, 2.0F * dt);
        } else if (dDown && !aDown) {
            this.keyboardYaw = moveToward(this.keyboardYaw, 1.0F, 2.0F * dt);
        } else {
            this.keyboardYaw = decayToZero(this.keyboardYaw, 3.5F * dt);
        }

        this.keyboardYaw = clamp(this.keyboardYaw, -1.0F, 1.0F);
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

    public void handleArmToggle(UUID playerId, Consumer<Boolean> onArmStateChanged) {
        if (this.checkArmToggle()) {
            if (!BootStateManager.getInstance().isBootActive()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
                    if (helmet.getItem() instanceof FpvGogglesItem) {
                        Optional<UUID> linkedDroneId = FpvGogglesItem.getLinkedDroneId(helmet);
                        if (!linkedDroneId.isEmpty()) {
                            UUID droneId = linkedDroneId.get();
                            if (!RemoteStateChecker.hasLinkedRemote(mc.player, droneId)) {
                                mc.player.sendOverlayMessage(Component.translatable("fpvdrone.status.connect_remote").withStyle(Style.EMPTY.withColor(16749622)));
                            } else {
                                Optional<DroneEntity> droneOpt = FPVMod.getActiveDrone(playerId);
                                if (!droneOpt.isEmpty()) {
                                    DroneEntity drone = droneOpt.get();
                                    boolean newState = !drone.areMotorsArmed();
                                    if (newState && !drone.hasBattery()) {
                                        mc.player
                                            .sendOverlayMessage(Component.translatable("fpvdrone.status.no_battery").withStyle(Style.EMPTY.withColor(16749622)));
                                    } else {
                                        drone.setMotorsArmed(newState);
                                        if (newState) {
                                            mc.player.setPos(drone.getX(), drone.getY(), drone.getZ());
                                            mc.player.setOldPosAndRot();
                                            FPVMod.enterFPVMode(mc.player, drone);
                                            PhysicsState physics = FPVMod.getPhysicsState(playerId);
                                            if (physics != null) {
                                                physics.setMotorsArmed(true);
                                            }
                                        } else {
                                            PhysicsState physics = FPVMod.getPhysicsState(playerId);
                                            if (physics != null) {
                                                physics.setMotorsArmed(false);
                                            }

                                            FPVMod.finalizeDroneState(mc.player);
                                        }

                                        if (onArmStateChanged != null) {
                                            onArmStateChanged.accept(newState);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
