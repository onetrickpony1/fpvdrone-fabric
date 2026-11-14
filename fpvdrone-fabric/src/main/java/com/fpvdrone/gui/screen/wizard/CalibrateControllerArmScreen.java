package com.fpvdrone.gui.screen.wizard;

import com.fpvdrone.client.ControllerConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.client.SettingsLoader;
import com.fpvdrone.gui.addon.BackProceedFooter;
import com.fpvdrone.gui.addon.WizardHeader;
import com.fpvdrone.gui.widget.StickOverlayRenderer;
import com.fpvdrone.render.GimbalState;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Function;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

/**
 * Gimbal tilt setup, the last wizard step before Setup Complete. In 1.1.8 this class keeps its old name but no longer
 * sets up the arm switch (that moved into {@link BindActionsScreen}): move the tilt wheel to pick it (and flip it if
 * reversed), or press keys to bind Camera Up / Camera Down, or skip. The mouse wheel tilts the preview.
 */
public class CalibrateControllerArmScreen extends EmptyListScreen {
    private static final int GRID_SIZE = 56;
    private static final int GRID_Y = 116;
    private CalibrateControllerArmScreen.Step step = CalibrateControllerArmScreen.Step.BEGIN;
    private CalibrateControllerArmScreen.KeySlot keySlot = CalibrateControllerArmScreen.KeySlot.UP;
    private Button backwardsButton;
    private final Function<Screen, Screen> next;
    private long startTime;
    private long detectedTime = 0L;

    public CalibrateControllerArmScreen(Screen previousScreen) {
        this(previousScreen, screen -> new CompleteScreen(screen, CompleteScreen.Mode.CONTROLLER));
    }

    public CalibrateControllerArmScreen(Screen previousScreen, Function<Screen, Screen> next) {
        super(previousScreen, new WizardHeader(Component.translatable("fpvdrone.wizard.calibrateGimbal.title"), true), new BackProceedFooter());
        this.next = next;
        BackProceedFooter footer = (BackProceedFooter)this.footer;
        footer.completeConstructor(this::onProceed, this::getProceedLabel, () -> true);
        footer.overrideOnBack(this::onBack);
        this.startTime = System.currentTimeMillis();
    }

    private static int detectedY() {
        return GRID_Y + 28;
    }

    private static int keysY() {
        return detectedY() + 24;
    }

    private static int backwardsY() {
        return keysY() + 40;
    }

    @Override
    protected void init() {
        super.init();
        this.backwardsButton = Button.builder(Component.translatable("fpvdrone.wizard.calibrateGimbal.reversed"), btn -> this.toggleInvert())
            .pos(this.width / 2 - 60, backwardsY())
            .size(120, 20)
            .build();
        this.backwardsButton.visible = false;
        this.addRenderableWidget(this.backwardsButton);
        this.startListening();
    }

    public void tick() {
        super.tick();
        ControllerReader.getInstance().poll();
        ControllerReader.tickListeners();
    }

    private void startListening() {
        ControllerReader.startInputListeningFull(result -> {
            if (this.step == CalibrateControllerArmScreen.Step.BEGIN) {
                if (result.isButton) {
                    // Only an axis (a tilt wheel) can drive the gimbal.
                    this.startListening();
                } else {
                    ControllerConfig config = ControllerReader.getConfig();
                    config.setGimbalChannel(result.channel);
                    config.setInvertGimbal(result.diff < 0.0F);
                    SettingsLoader.saveControllerConfig(config);
                    this.detectedTime = System.currentTimeMillis();
                    BindActionsScreen.playClickSound();
                    this.step = CalibrateControllerArmScreen.Step.VERIFY;
                    this.startTime = System.currentTimeMillis();
                }
            }
        });
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            return super.keyPressed(event);
        }

        KeyMapping mapping = this.keySlot == CalibrateControllerArmScreen.KeySlot.UP ? KeyManager.GIMBAL_UP_KEY : KeyManager.GIMBAL_DOWN_KEY;
        KeyManager.bindKey(mapping, InputConstants.getKey(event));
        this.keySlot = this.keySlot == CalibrateControllerArmScreen.KeySlot.UP
            ? CalibrateControllerArmScreen.KeySlot.DOWN
            : CalibrateControllerArmScreen.KeySlot.UP;
        this.detectedTime = System.currentTimeMillis();
        BindActionsScreen.playClickSound();
        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (scrollY == 0.0) {
            return true;
        }

        GimbalState.getInstance().nudge((float)(-scrollY) * GimbalState.SCROLL_STEP);
        this.detectedTime = System.currentTimeMillis();
        BindActionsScreen.playClickSound();
        return true;
    }

    private void toggleInvert() {
        ControllerConfig config = ControllerReader.getConfig();
        config.setInvertGimbal(!config.isInvertGimbal());
        SettingsLoader.saveControllerConfig(config);
    }

    private void onProceed() {
        if (this.step == CalibrateControllerArmScreen.Step.BEGIN) {
            // Skip: no tilt wheel.
            ControllerConfig config = ControllerReader.getConfig();
            config.setGimbalChannel(-1);
            SettingsLoader.saveControllerConfig(config);
        }

        ControllerReader.stopAllListening();
        Minecraft.getInstance().gui.setScreen(this.next.apply(this));
    }

    private void onBack() {
        ControllerReader.stopAllListening();
        if (this.step == CalibrateControllerArmScreen.Step.BEGIN) {
            this.onClose();
        } else {
            this.step = CalibrateControllerArmScreen.Step.BEGIN;
            this.startTime = System.currentTimeMillis();
            this.startListening();
        }
    }

    private String getProceedLabel() {
        return this.step == CalibrateControllerArmScreen.Step.BEGIN
            ? I18n.get("fpvdrone.wizard.calibrateGimbal.skip")
            : I18n.get("fpvdrone.wizard.calibrateGimbal.done");
    }

    /** Preview without a tilt wheel: the gimbal pitch as a stick position (up = looking up). */
    private static float previewFromGimbal() {
        float pitch = GimbalState.getInstance().getPitch();
        float range = pitch < 0.0F ? -GimbalState.MIN_PITCH : GimbalState.MAX_PITCH;
        return Mth.clamp(-pitch / range, -1.0F, 1.0F);
    }

    @Override
    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderCustom(graphics, mouseX, mouseY, partialTicks);
        int titleY = 53;
        graphics.centeredText(this.font, this.getContentTitle(), this.width / 2, titleY, -1);
        graphics.centeredText(this.font, this.getContentSubtitle(), this.width / 2, titleY + 15, -1);
        float stickY = ControllerReader.getConfig().getGimbalChannel() >= 0 ? ControllerReader.readGimbalAxis() : previewFromGimbal();
        StickOverlayRenderer.renderStickGrid(graphics, this.width / 2, GRID_Y, GRID_SIZE, 0.0F, stickY, this.step == CalibrateControllerArmScreen.Step.VERIFY);
        if (this.detectedTime > 0L && System.currentTimeMillis() - this.detectedTime < 500L) {
            StickOverlayRenderer.renderDetected(graphics, this.width / 2 - 25, detectedY() + 8);
        } else if (this.step == CalibrateControllerArmScreen.Step.BEGIN && ControllerReader.isInputListening()) {
            StickOverlayRenderer.renderListeningDots(graphics, this.width / 2 - 30, detectedY() + 8, this.startTime);
        }

        boolean upSelected = this.keySlot == CalibrateControllerArmScreen.KeySlot.UP;
        graphics.centeredText(
            this.font,
            I18n.get("fpvdrone.wizard.calibrateGimbal.upKey", KeyManager.keyName(KeyManager.GIMBAL_UP_KEY)) + (upSelected ? "  <" : ""),
            this.width / 2,
            keysY(),
            upSelected ? 0xFFFF9636 : 0xFFCCCCCC
        );
        graphics.centeredText(
            this.font,
            I18n.get("fpvdrone.wizard.calibrateGimbal.downKey", KeyManager.keyName(KeyManager.GIMBAL_DOWN_KEY)) + (!upSelected ? "  <" : ""),
            this.width / 2,
            keysY() + 12,
            !upSelected ? 0xFFFF9636 : 0xFFCCCCCC
        );
        graphics.centeredText(this.font, I18n.get("fpvdrone.wizard.calibrateGimbal.scrollNote"), this.width / 2, keysY() + 26, 0xFF999999);
        this.backwardsButton.visible = this.step == CalibrateControllerArmScreen.Step.VERIFY;
        this.backwardsButton.setY(backwardsY());
    }

    private String getContentTitle() {
        return switch (this.step) {
            case BEGIN -> I18n.get("fpvdrone.wizard.calibrateGimbal.begin");
            case VERIFY -> I18n.get("fpvdrone.wizard.calibrateGimbal.verify");
        };
    }

    private String getContentSubtitle() {
        return switch (this.step) {
            case BEGIN -> I18n.get("fpvdrone.wizard.calibrateGimbal.beginHint");
            case VERIFY -> I18n.get("fpvdrone.wizard.calibrateGimbal.verifyHint");
        };
    }

    private enum KeySlot {
        UP,
        DOWN;
    }

    public enum Step {
        BEGIN,
        VERIFY;
    }
}
