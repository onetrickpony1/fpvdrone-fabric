package com.fpvdrone.gui.screen.wizard;

import com.fpvdrone.client.ControllerConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.SettingsLoader;
import com.fpvdrone.gui.addon.BackProceedFooter;
import com.fpvdrone.gui.addon.WizardHeader;
import com.fpvdrone.gui.widget.StickOverlayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class CalibrateControllerArmScreen extends EmptyListScreen {
    private CalibrateControllerArmScreen.Step step = CalibrateControllerArmScreen.Step.BEGIN;
    private int detectedChannel = -1;
    private boolean detectedIsButton = false;
    private Button backwardsButton;
    private long lastProceedTime;
    private long detectedTime = 0L;

    public CalibrateControllerArmScreen(Screen previousScreen) {
        super(previousScreen, new WizardHeader(Component.translatable("fpvdrone.wizard.calibrateArm.title"), true), new BackProceedFooter());
        BackProceedFooter footer = (BackProceedFooter)this.footer;
        footer.completeConstructor(this::onProceed, this::getProceedLabel, this::isProceedVisible);
        footer.overrideOnBack(this::onBack);
        this.lastProceedTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        this.backwardsButton = Button.builder(Component.translatable("fpvdrone.wizard.calibrateArm.backwards"), btn -> this.toggleInvert())
            .pos(this.width / 2 - 60, this.height / 2 + 40)
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
                this.detectedChannel = result.channel;
                this.detectedIsButton = result.isButton;
                this.playClickSound();
                this.detectedTime = System.currentTimeMillis();
                this.step = CalibrateControllerArmScreen.Step.PING;
                this.lastProceedTime = System.currentTimeMillis();
                this.startListening();
            } else if (this.step == CalibrateControllerArmScreen.Step.PING) {
                if (result.channel == this.detectedChannel) {
                    this.playClickSound();
                    this.detectedTime = System.currentTimeMillis();
                    this.saveArmConfig();
                    this.step = CalibrateControllerArmScreen.Step.FLIP;
                    this.lastProceedTime = System.currentTimeMillis();
                } else {
                    this.step = CalibrateControllerArmScreen.Step.BEGIN;
                    this.detectedChannel = -1;
                    this.startListening();
                }
            }
        });
    }

    private void saveArmConfig() {
        ControllerConfig config = ControllerReader.getConfig();
        if (this.detectedIsButton) {
            int buttonIndex = this.detectedChannel - ControllerReader.getInstance().getAxisCount();
            config.setArmChannel(buttonIndex);
            config.setArmIsButton(true);
        } else {
            config.setArmChannel(this.detectedChannel);
            config.setArmIsButton(false);
        }

        config.setInvertArm(false);
        SettingsLoader.saveControllerConfig(config);
    }

    private void toggleInvert() {
        ControllerConfig config = ControllerReader.getConfig();
        config.setInvertArm(!config.isInvertArm());
        SettingsLoader.saveControllerConfig(config);
    }

    private void onProceed() {
        if (this.step == CalibrateControllerArmScreen.Step.FLIP) {
            this.openCompleteScreen();
        }
    }

    private void onBack() {
        ControllerReader.stopAllListening();
        if (this.step == CalibrateControllerArmScreen.Step.BEGIN) {
            this.onClose();
        } else {
            this.step = CalibrateControllerArmScreen.Step.BEGIN;
            this.detectedChannel = -1;
            this.startListening();
        }
    }

    private String getProceedLabel() {
        return this.step == CalibrateControllerArmScreen.Step.FLIP ? I18n.get("fpvdrone.wizard.calibrateArm.done", new Object[0]) : "";
    }

    private boolean isProceedVisible() {
        return this.step == CalibrateControllerArmScreen.Step.FLIP;
    }

    @Override
    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderCustom(graphics, mouseX, mouseY, partialTicks);
        String title = this.getContentTitle();
        int titleY = 53;
        graphics.centeredText(this.font, title, this.width / 2, titleY, -1);
        String subtitle = this.getContentSubtitle();
        if (subtitle != null) {
            graphics.centeredText(this.font, subtitle, this.width / 2, titleY + 15, -1);
        }

        this.backwardsButton.visible = this.step == CalibrateControllerArmScreen.Step.FLIP;
        if (this.step == CalibrateControllerArmScreen.Step.BEGIN || this.step == CalibrateControllerArmScreen.Step.PING) {
            StickOverlayRenderer.renderListeningDots(graphics, this.width / 2 - 30, this.height / 2, this.lastProceedTime);
        }

        if (this.detectedTime > 0L && System.currentTimeMillis() - this.detectedTime < 500L) {
            StickOverlayRenderer.renderDetected(graphics, this.width / 2 - 25, this.height / 2 + 20);
        }
    }

    private String getContentTitle() {
        return switch (this.step) {
            case BEGIN -> I18n.get("fpvdrone.wizard.calibrateArm.begin", new Object[0]);
            case PING -> I18n.get("fpvdrone.wizard.calibrateArm.ping", new Object[0]);
            case FLIP -> I18n.get("fpvdrone.wizard.calibrateArm.flip", new Object[0]);
        };
    }

    private String getContentSubtitle() {
        return switch (this.step) {
            case BEGIN -> I18n.get("fpvdrone.wizard.calibrateArm.beginHint", new Object[0]);
            case PING -> I18n.get("fpvdrone.wizard.calibrateArm.pingHint", new Object[0]);
            case FLIP -> I18n.get("fpvdrone.wizard.calibrateArm.flipHint", new Object[0]);
        };
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void openCompleteScreen() {
        ControllerReader.stopAllListening();
        Minecraft.getInstance().gui.setScreen(new CompleteScreen(this, CompleteScreen.Mode.CONTROLLER));
    }

    public static enum Step {
        BEGIN,
        PING,
        FLIP;
    }
}
