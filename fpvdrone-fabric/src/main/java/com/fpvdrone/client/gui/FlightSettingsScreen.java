package com.fpvdrone.client.gui;

import com.fpvdrone.FpvConfig;
import com.fpvdrone.client.ControllerConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.SdlJoysticks;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Small flight-focused replacement for the original controller wizard. */
public final class FlightSettingsScreen extends Screen {
    private final ControllerConfig config = ControllerReader.getConfig();
    private int device = -1;
    private boolean calibrating;
    private final float[] minimum = new float[16];
    private final float[] maximum = new float[16];
    private String status = "Keyboard: W/S throttle, A/D yaw, mouse pitch/roll, N arm.";

    public FlightSettingsScreen() {
        super(Component.literal("FPV flight controls"));
        SdlJoysticks.refresh(true);
        for (int i = 0; i < SdlJoysticks.MAX_JOYSTICKS; i++) {
            if (SdlJoysticks.isPresent(i) && config.getPreferredJoystickGuid().equals(SdlJoysticks.getGuid(i))) device = i;
        }
    }

    @Override
    protected void init() {
        int x = width / 2 - 150;
        int y = 36;
        addRenderableWidget(Button.builder(deviceLabel(), button -> {
            if (calibrating) return;
            do { device++; } while (device < SdlJoysticks.MAX_JOYSTICKS && !SdlJoysticks.isPresent(device));
            if (device >= SdlJoysticks.MAX_JOYSTICKS) device = -1;
            if (device < 0) ControllerReader.getInstance().clearPreferredDevice();
            else ControllerReader.getInstance().bindToJid(device);
            FpvConfig.setForceKbm(device < 0);
            button.setMessage(deviceLabel());
        }).bounds(x, y, 300, 20).build());
        String[] names = {"Throttle", "Yaw", "Pitch", "Roll"};
        for (int axis = 0; axis < 4; axis++) {
            final int channel = axis;
            final String name = names[axis];
            addRenderableWidget(Button.builder(axisLabel(name, channel), button -> {
                setAxis(channel, (getAxis(channel) + 1) % 8);
                button.setMessage(axisLabel(name, channel));
            }).bounds(x, y + 28 + axis * 24, 190, 20).build());
            addRenderableWidget(Button.builder(invertLabel(channel), button -> {
                config.setInverted(channel, !config.isInverted(channel));
                button.setMessage(invertLabel(channel));
            }).bounds(x + 195, y + 28 + axis * 24, 105, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Calibrate stick ranges"), button -> {
            if (device < 0 || !SdlJoysticks.isPresent(device)) {
                status = "Choose a connected controller first.";
                return;
            }
            if (!calibrating) {
                Arrays.fill(minimum, Float.POSITIVE_INFINITY);
                Arrays.fill(maximum, Float.NEGATIVE_INFINITY);
                calibrating = true;
                status = "Move all sticks fully in every direction, then finish.";
                button.setMessage(Component.literal("Finish calibration"));
            } else {
                for (int i = 0; i < 16; i++) {
                    if (maximum[i] - minimum[i] > 0.1F) {
                        config.setAxisMin(i, minimum[i]);
                        config.setAxisMax(i, maximum[i]);
                    }
                }
                calibrating = false;
                status = "Calibration saved. Check the live axes before flying.";
                ControllerReader.getInstance().saveConfig();
                button.setMessage(Component.literal("Calibrate stick ranges"));
            }
        }).bounds(x, y + 128, 300, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), button -> onClose())
            .bounds(x + 75, height - 27, 150, 20).build());
    }

    private Component deviceLabel() {
        return Component.literal(device < 0 ? "Input: keyboard and mouse" : "Input: " + SdlJoysticks.getName(device));
    }
    private Component axisLabel(String name, int channel) {
        return Component.literal(name + ": axis " + (getAxis(channel) + 1));
    }
    private Component invertLabel(int channel) {
        return Component.literal(config.isInverted(channel) ? "Reversed" : "Normal");
    }
    private int getAxis(int channel) {
        return switch (channel) {
            case 0 -> config.getThrottleChannel();
            case 1 -> config.getYawChannel();
            case 2 -> config.getPitchChannel();
            default -> config.getRollChannel();
        };
    }
    private void setAxis(int channel, int axis) {
        switch (channel) {
            case 0 -> config.setThrottleChannel(axis);
            case 1 -> config.setYawChannel(axis);
            case 2 -> config.setPitchChannel(axis);
            case 3 -> config.setRollChannel(axis);
        }
    }

    @Override
    public void tick() {
        SdlJoysticks.refresh(true);
        if (calibrating && device >= 0 && SdlJoysticks.isPresent(device)) {
            float[] axes = ControllerReader.readAxes(device);
            for (int i = 0; i < axes.length; i++) {
                minimum[i] = Math.min(minimum[i], axes[i]);
                maximum[i] = Math.max(maximum[i], axes[i]);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 16, 0xffffffff);
        graphics.centeredText(font, status, width / 2, 192, 0xffcccccc);
        if (device >= 0 && SdlJoysticks.isPresent(device)) {
            float[] axes = ControllerReader.readAxes(device);
            String values = String.format(Locale.ROOT, "Live axes 1–4: %.2f  %.2f  %.2f  %.2f", axes[0], axes[1], axes[2], axes[3]);
            graphics.centeredText(font, values, width / 2, 207, 0xff88ddff);
        }
    }

    @Override
    public void onClose() {
        ControllerReader.getInstance().saveConfig();
        FpvConfig.save();
        super.onClose();
    }
}
