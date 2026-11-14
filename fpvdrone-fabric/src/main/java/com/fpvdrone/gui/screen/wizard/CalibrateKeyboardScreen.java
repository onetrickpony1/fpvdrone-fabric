package com.fpvdrone.gui.screen.wizard;

import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.DroneAction;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.gui.addon.BackProceedFooter;
import com.fpvdrone.gui.addon.WizardHeader;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

/**
 * Keyboard setup. 1.1.8: press keys to rebind throttle up/down and yaw left/right (the highlighted one is next),
 * then the action key-binding steps and the gimbal step follow before Setup Complete.
 */
public class CalibrateKeyboardScreen extends EmptyListScreen {
    private static final CalibrateKeyboardScreen.Slot[] SLOTS = CalibrateKeyboardScreen.Slot.values();
    private CalibrateKeyboardScreen.Slot current = CalibrateKeyboardScreen.Slot.THROTTLE_UP;

    public CalibrateKeyboardScreen(Screen previousScreen) {
        super(previousScreen, new WizardHeader(Component.translatable("fpvdrone.wizard.calibrateKeyboard.title"), true), new BackProceedFooter());
        BackProceedFooter footer = (BackProceedFooter)this.footer;
        footer.completeConstructor(this::onProceed, () -> I18n.get("fpvdrone.wizard.calibrateKeyboard.proceed", new Object[0]), () -> true);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isEscape()) {
            return super.keyPressed(event);
        }

        KeyManager.bindKey(this.current.mapping(), InputConstants.getKey(event));
        this.current = SLOTS[(this.current.ordinal() + 1) % SLOTS.length];
        BindActionsScreen.playClickSound();
        return true;
    }

    @Override
    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderCustom(graphics, mouseX, mouseY, partialTicks);
        int centerY = this.height / 2 - 50;
        int lineHeight = 12;
        graphics.centeredText(this.font, Component.translatable("fpvdrone.wizard.calibrateKeyboard.info1"), this.width / 2, centerY, -1);
        graphics.centeredText(this.font, Component.translatable("fpvdrone.wizard.calibrateKeyboard.info2"), this.width / 2, centerY + lineHeight * 2, -1);
        int slotsY = centerY + lineHeight * 4;

        for (CalibrateKeyboardScreen.Slot slot : SLOTS) {
            boolean selected = slot == this.current;
            String text = I18n.get(slot.langKey, KeyManager.keyName(slot.mapping())) + (selected ? "  <" : "");
            graphics.centeredText(this.font, text, this.width / 2, slotsY + lineHeight * slot.ordinal(), selected ? 0xFFFF9636 : 0xFFCCCCCC);
        }

        graphics.centeredText(
            this.font, Component.translatable("fpvdrone.wizard.calibrateKeyboard.arrows"), this.width / 2, slotsY + lineHeight * 5, -1
        );
        graphics.centeredText(
            this.font,
            Component.translatable("fpvdrone.wizard.calibrateKeyboard.armDisarm", new Object[]{KeyManager.bindingLabel(DroneAction.ARM).toUpperCase()}),
            this.width / 2,
            slotsY + lineHeight * 7,
            -1
        );
    }

    private void onProceed() {
        ControllerReader.getInstance().clearPreferredDevice();
        Minecraft.getInstance()
            .gui
            .setScreen(
                new BindActionsScreen(
                    this,
                    BindActionsScreen.allActions(),
                    bindScreen -> new CalibrateControllerArmScreen(bindScreen, gimbalScreen -> new CompleteScreen(gimbalScreen, CompleteScreen.Mode.KEYBOARD))
                )
            );
    }

    private enum Slot {
        THROTTLE_UP("fpvdrone.wizard.calibrateKeyboard.throttleUpKey"),
        THROTTLE_DOWN("fpvdrone.wizard.calibrateKeyboard.throttleDownKey"),
        YAW_LEFT("fpvdrone.wizard.calibrateKeyboard.yawLeftKey"),
        YAW_RIGHT("fpvdrone.wizard.calibrateKeyboard.yawRightKey");

        private final String langKey;

        Slot(String langKey) {
            this.langKey = langKey;
        }

        KeyMapping mapping() {
            return switch (this) {
                case THROTTLE_UP -> KeyManager.THROTTLE_UP_KEY;
                case THROTTLE_DOWN -> KeyManager.THROTTLE_DOWN_KEY;
                case YAW_LEFT -> KeyManager.YAW_LEFT_KEY;
                case YAW_RIGHT -> KeyManager.YAW_RIGHT_KEY;
            };
        }
    }
}
