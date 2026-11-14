package com.fpvdrone.gui.screen.wizard;

import com.fpvdrone.client.ControllerConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.RangeListener;
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

public class CalibrateControllerStickScreen extends EmptyListScreen {
    private static final int STICK_SIZE = 80;
    private static final int STICK_OFFSET_X = 60;
    private CalibrateControllerStickScreen.Step step = CalibrateControllerStickScreen.Step.FIRST_CENTERING;
    private boolean showRecenter = false;
    private Button retryButton;
    private long lastProceedTime;
    private long detectedTime = 0L;

    public CalibrateControllerStickScreen(Screen previousScreen) {
        super(previousScreen, new WizardHeader(Component.translatable("fpvdrone.wizard.calibrateStick.title"), true), new BackProceedFooter());
        BackProceedFooter footer = (BackProceedFooter)this.footer;
        footer.completeConstructor(this::onProceed, this::getProceedLabel, this::isProceedVisible);
        footer.overrideOnBack(this::onBack);
        this.lastProceedTime = System.currentTimeMillis();
    }

    @Override
    protected void init() {
        super.init();
        this.retryButton = Button.builder(Component.translatable("fpvdrone.wizard.calibrateStick.retry"), btn -> this.retryCalibration())
            .pos(this.width / 2 - 40, this.getStickBottomY() + 28)
            .size(80, 20)
            .build();
        this.retryButton.visible = false;
        this.addRenderableWidget(this.retryButton);
    }

    private int getStickCenterY() {
        return 142;
    }

    private int getStickBottomY() {
        return this.getStickCenterY() + 40;
    }

    public void tick() {
        super.tick();
        ControllerReader.getInstance().poll();
        ControllerReader.tickListeners();
    }

    public void startListening() {
        ControllerReader.startInputListening(channel -> {
            ControllerConfig config = ControllerReader.getConfig();
            float value = ControllerReader.getAxisDiffOnChannel(channel);
            boolean invert = value < 0.0F;
            switch (this.step) {
                case FIRST_CENTERING:
                case RANGE:
                case VERIFY:
                default:
                    break;
                case LEFT_UP:
                    config.setThrottleChannel(channel);
                    config.setInvertThrottle(invert);
                    break;
                case LEFT_RIGHT:
                    config.setYawChannel(channel);
                    config.setInvertYaw(invert);
                    break;
                case RIGHT_UP:
                    config.setPitchChannel(channel);
                    config.setInvertPitch(invert);
                    break;
                case RIGHT_RIGHT:
                    config.setRollChannel(channel);
                    config.setInvertRoll(invert);
            }

            this.playClickSound();
            SettingsLoader.saveControllerConfig(config);
            this.detectedTime = System.currentTimeMillis();
            this.showRecenter = true;
        });
    }

    public void startRangeListening() {
        ControllerReader.startRangeListening(range -> {
            ControllerConfig config = ControllerReader.getConfig();
            config.setAxisRange(range);
            SettingsLoader.saveControllerConfig(config);
            this.playClickSound();
            this.step = CalibrateControllerStickScreen.Step.VERIFY;
            this.lastProceedTime = System.currentTimeMillis();
        });
    }

    private void onProceed() {
        this.lastProceedTime = System.currentTimeMillis();
        this.detectedTime = 0L;
        switch (this.step) {
            case FIRST_CENTERING:
                this.step = CalibrateControllerStickScreen.Step.LEFT_UP;
                this.startListening();
                break;
            case LEFT_UP:
                if (this.showRecenter) {
                    this.showRecenter = false;
                    this.step = CalibrateControllerStickScreen.Step.LEFT_RIGHT;
                    this.startListening();
                }
                break;
            case LEFT_RIGHT:
                if (this.showRecenter) {
                    this.showRecenter = false;
                    this.step = CalibrateControllerStickScreen.Step.RIGHT_UP;
                    this.startListening();
                }
                break;
            case RIGHT_UP:
                if (this.showRecenter) {
                    this.showRecenter = false;
                    this.step = CalibrateControllerStickScreen.Step.RIGHT_RIGHT;
                    this.startListening();
                }
                break;
            case RIGHT_RIGHT:
                if (this.showRecenter) {
                    this.showRecenter = false;
                    this.step = CalibrateControllerStickScreen.Step.RANGE;
                    this.startRangeListening();
                }
                break;
            case RANGE:
                this.step = CalibrateControllerStickScreen.Step.VERIFY;
                break;
            case VERIFY:
                this.openArmCalibration();
        }
    }

    private void onBack() {
        ControllerReader.stopAllListening();
        if (this.step == CalibrateControllerStickScreen.Step.FIRST_CENTERING) {
            this.onClose();
        } else if (this.showRecenter) {
            this.showRecenter = false;
        } else {
            this.step = CalibrateControllerStickScreen.Step.values()[this.step.ordinal() - 1];
            if (this.step != CalibrateControllerStickScreen.Step.FIRST_CENTERING
                && this.step != CalibrateControllerStickScreen.Step.RANGE
                && this.step != CalibrateControllerStickScreen.Step.VERIFY) {
                this.startListening();
            }
        }
    }

    private void retryCalibration() {
        this.step = CalibrateControllerStickScreen.Step.FIRST_CENTERING;
        this.showRecenter = false;
        this.lastProceedTime = System.currentTimeMillis();
    }

    private String getProceedLabel() {
        if (this.step == CalibrateControllerStickScreen.Step.FIRST_CENTERING) {
            return I18n.get("fpvdrone.wizard.calibrateStick.ready", new Object[0]);
        } else if (this.showRecenter) {
            return I18n.get("fpvdrone.wizard.calibrateStick.centered", new Object[0]);
        } else {
            return this.step == CalibrateControllerStickScreen.Step.VERIFY ? I18n.get("fpvdrone.wizard.calibrateStick.next", new Object[0]) : "";
        }
    }

    private boolean isProceedVisible() {
        return this.step == CalibrateControllerStickScreen.Step.FIRST_CENTERING || this.showRecenter || this.step == CalibrateControllerStickScreen.Step.VERIFY;
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

        this.renderSticks(graphics, partialTicks);
        this.retryButton.visible = this.step == CalibrateControllerStickScreen.Step.VERIFY;
        this.retryButton.setY(this.getStickBottomY() + 28);
        int belowSticksY = this.getStickBottomY() + 12;
        if (this.step == CalibrateControllerStickScreen.Step.RANGE) {
            float progress = RangeListener.getInstance().getProgress();
            int barWidth = 200;
            int barX = this.width / 2 - barWidth / 2;
            StickOverlayRenderer.renderProgressBar(graphics, barX, belowSticksY, barWidth, 10, progress);
        }

        if (this.isListeningForInput()) {
            StickOverlayRenderer.renderListeningDots(graphics, this.width / 2 - 30, belowSticksY, this.lastProceedTime);
        }

        if (this.detectedTime > 0L && System.currentTimeMillis() - this.detectedTime < 500L) {
            StickOverlayRenderer.renderDetected(graphics, this.width / 2 - 25, belowSticksY);
        }
    }

    private String getContentTitle() {
        if (this.showRecenter) {
            return I18n.get("fpvdrone.wizard.calibrateStick.recenter", new Object[0]);
        } else {
            return switch (this.step) {
                case FIRST_CENTERING -> I18n.get("fpvdrone.wizard.calibrateStick.centerSticks", new Object[0]);
                case LEFT_UP -> I18n.get("fpvdrone.wizard.calibrateStick.leftUp", new Object[0]);
                case LEFT_RIGHT -> I18n.get("fpvdrone.wizard.calibrateStick.leftRight", new Object[0]);
                case RIGHT_UP -> I18n.get("fpvdrone.wizard.calibrateStick.rightUp", new Object[0]);
                case RIGHT_RIGHT -> I18n.get("fpvdrone.wizard.calibrateStick.rightRight", new Object[0]);
                case RANGE -> I18n.get("fpvdrone.wizard.calibrateStick.range", new Object[0]);
                case VERIFY -> I18n.get("fpvdrone.wizard.calibrateStick.verify", new Object[0]);
            };
        }
    }

    private String getContentSubtitle() {
        if (this.showRecenter) {
            return I18n.get("fpvdrone.wizard.calibrateStick.recenterHint", new Object[0]);
        } else {
            return switch (this.step) {
                case FIRST_CENTERING -> I18n.get("fpvdrone.wizard.calibrateStick.centerHint", new Object[0]);
                case RANGE -> I18n.get("fpvdrone.wizard.calibrateStick.rangeHint", new Object[0]);
                case VERIFY -> I18n.get("fpvdrone.wizard.calibrateStick.verifyHint", new Object[0]);
                default -> null;
            };
        }
    }

    private void renderSticks(GuiGraphicsExtractor graphics, float partialTicks) {
        float t = this.getT();
        float leftX = this.getYawAnim(t);
        float leftY = this.getThrottleAnim(t);
        float rightX = this.getRollAnim(t);
        float rightY = this.getPitchAnim(t);
        int centerY = this.getStickCenterY();
        int leftStickX = this.width / 2 - 60;
        int rightStickX = this.width / 2 + 60;
        boolean leftSelected = this.showRecenter
            || this.step == CalibrateControllerStickScreen.Step.FIRST_CENTERING
            || this.step == CalibrateControllerStickScreen.Step.LEFT_UP
            || this.step == CalibrateControllerStickScreen.Step.LEFT_RIGHT
            || this.step == CalibrateControllerStickScreen.Step.RANGE
            || this.step == CalibrateControllerStickScreen.Step.VERIFY;
        boolean rightSelected = this.showRecenter
            || this.step == CalibrateControllerStickScreen.Step.FIRST_CENTERING
            || this.step == CalibrateControllerStickScreen.Step.RIGHT_UP
            || this.step == CalibrateControllerStickScreen.Step.RIGHT_RIGHT
            || this.step == CalibrateControllerStickScreen.Step.RANGE
            || this.step == CalibrateControllerStickScreen.Step.VERIFY;
        StickOverlayRenderer.renderStickGrid(graphics, leftStickX, centerY, 80, leftX, leftY, leftSelected);
        StickOverlayRenderer.renderStickGrid(graphics, rightStickX, centerY, 80, rightX, rightY, rightSelected);
        if (!this.showRecenter && this.step != CalibrateControllerStickScreen.Step.VERIFY) {
            this.renderDirectionArrows(graphics, leftStickX, rightStickX, centerY, t);
        }
    }

    private void renderDirectionArrows(GuiGraphicsExtractor graphics, int leftX, int rightX, int centerY, float t) {
        switch (this.step) {
            case FIRST_CENTERING:
            case RANGE:
            case VERIFY:
            default:
                break;
            case LEFT_UP:
                StickOverlayRenderer.renderAnimatedArrow(graphics, leftX, centerY, StickOverlayRenderer.Direction.UP, t);
                break;
            case LEFT_RIGHT:
                StickOverlayRenderer.renderAnimatedArrow(graphics, leftX, centerY, StickOverlayRenderer.Direction.RIGHT, t);
                break;
            case RIGHT_UP:
                StickOverlayRenderer.renderAnimatedArrow(graphics, rightX, centerY, StickOverlayRenderer.Direction.UP, t);
                break;
            case RIGHT_RIGHT:
                StickOverlayRenderer.renderAnimatedArrow(graphics, rightX, centerY, StickOverlayRenderer.Direction.RIGHT, t);
        }
    }

    private float getT() {
        int loopLength = this.step != CalibrateControllerStickScreen.Step.FIRST_CENTERING
                && !this.showRecenter
                && this.step != CalibrateControllerStickScreen.Step.RANGE
            ? 1200
            : 2000;
        return (float)((System.currentTimeMillis() - this.lastProceedTime) % loopLength) / loopLength;
    }

    private float getThrottleAnim(float t) {
        if (this.step == CalibrateControllerStickScreen.Step.FIRST_CENTERING || this.showRecenter) {
            return StickOverlayRenderer.getSpiralY(t);
        } else if (this.step == CalibrateControllerStickScreen.Step.RANGE) {
            return StickOverlayRenderer.getBoxY(t) * 2.0F - 1.0F;
        } else if (this.step == CalibrateControllerStickScreen.Step.LEFT_UP) {
            return this.easeInOut(t);
        } else {
            return this.step == CalibrateControllerStickScreen.Step.VERIFY ? ControllerReader.getThrottle() : 0.0F;
        }
    }

    private float getYawAnim(float t) {
        if (this.step == CalibrateControllerStickScreen.Step.FIRST_CENTERING || this.showRecenter) {
            return StickOverlayRenderer.getSpiralX(t);
        } else if (this.step == CalibrateControllerStickScreen.Step.RANGE) {
            return StickOverlayRenderer.getBoxX(t) * 2.0F - 1.0F;
        } else if (this.step == CalibrateControllerStickScreen.Step.LEFT_RIGHT) {
            return this.easeInOut(t);
        } else {
            return this.step == CalibrateControllerStickScreen.Step.VERIFY ? ControllerReader.getYaw() : 0.0F;
        }
    }

    private float getPitchAnim(float t) {
        if (this.step == CalibrateControllerStickScreen.Step.FIRST_CENTERING || this.showRecenter) {
            return StickOverlayRenderer.getSpiralY(t);
        } else if (this.step == CalibrateControllerStickScreen.Step.RANGE) {
            return StickOverlayRenderer.getBoxY(t) * 2.0F - 1.0F;
        } else if (this.step == CalibrateControllerStickScreen.Step.RIGHT_UP) {
            return this.easeInOut(t);
        } else {
            return this.step == CalibrateControllerStickScreen.Step.VERIFY ? ControllerReader.getPitch() : 0.0F;
        }
    }

    private float getRollAnim(float t) {
        if (this.step == CalibrateControllerStickScreen.Step.FIRST_CENTERING || this.showRecenter) {
            return StickOverlayRenderer.getSpiralX(t);
        } else if (this.step == CalibrateControllerStickScreen.Step.RANGE) {
            return StickOverlayRenderer.getBoxX(t) * 2.0F - 1.0F;
        } else if (this.step == CalibrateControllerStickScreen.Step.RIGHT_RIGHT) {
            return this.easeInOut(t);
        } else {
            return this.step == CalibrateControllerStickScreen.Step.VERIFY ? ControllerReader.getRoll() : 0.0F;
        }
    }

    private float easeInOut(float t) {
        float cycle = t * 2.0F;
        return cycle < 1.0F ? cycle : 2.0F - cycle;
    }

    private boolean isListeningForInput() {
        return ControllerReader.isInputListening() && !this.showRecenter;
    }

    private void playClickSound() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void openArmCalibration() {
        ControllerReader.stopAllListening();
        Minecraft.getInstance().gui.setScreen(new CalibrateControllerArmScreen(this));
    }

    public static enum Step {
        FIRST_CENTERING,
        LEFT_UP,
        LEFT_RIGHT,
        RIGHT_UP,
        RIGHT_RIGHT,
        RANGE,
        VERIFY;
    }
}
