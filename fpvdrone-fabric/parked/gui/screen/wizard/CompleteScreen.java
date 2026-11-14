package com.fpvdrone.gui.screen.wizard;

import com.fpvdrone.client.KeyManager;
import com.fpvdrone.gui.addon.WizardDoneFooter;
import com.fpvdrone.gui.addon.WizardHeader;
import com.fpvdrone.gui.screen.FpvScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CompleteScreen extends EmptyListScreen {
    private final CompleteScreen.Mode mode;

    public CompleteScreen(Screen previousScreen, CompleteScreen.Mode mode) {
        super(previousScreen, new WizardHeader(Component.translatable("fpvdrone.wizard.complete.title"), false), new WizardDoneFooter());
        this.mode = mode;
    }

    @Override
    protected void init() {
        super.init();
        WizardDoneFooter footer = (WizardDoneFooter)this.footer;
        footer.setOnMoreSettings(this::openModelSettings);
    }

    @Override
    public void renderCustom(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderCustom(graphics, mouseX, mouseY, partialTicks);
        int centerY = this.height / 2 - 30;
        int lineHeight = 14;
        graphics.drawCenteredString(this.font, Component.translatable("fpvdrone.wizard.complete.ready"), this.width / 2, centerY, 16777215);
        Component instruction1;
        Component instruction2;
        if (this.mode == CompleteScreen.Mode.CONTROLLER) {
            instruction1 = Component.translatable("fpvdrone.wizard.complete.controller.instruction1");
            instruction2 = Component.translatable("fpvdrone.wizard.complete.controller.instruction2");
        } else {
            instruction1 = Component.translatable(
                "fpvdrone.wizard.complete.keyboard.instruction1", new Object[]{KeyManager.FPV_TOGGLE_KEY.getTranslatedKeyMessage().getString().toUpperCase()}
            );
            instruction2 = Component.translatable("fpvdrone.wizard.complete.keyboard.instruction2");
        }

        graphics.drawCenteredString(this.font, instruction1, this.width / 2, centerY + lineHeight * 2, 16777215);
        graphics.drawCenteredString(this.font, instruction2, this.width / 2, centerY + lineHeight * 3, 16777215);
    }

    private void openModelSettings() {
        this.closeToRoot();
    }

    @Override
    public void onClose() {
        this.closeToRoot();
    }

    private void closeToRoot() {
        Screen target = this.previousScreen;

        while (target instanceof FpvScreen) {
            FpvScreen fpvScreen = (FpvScreen)target;
            Screen parent = fpvScreen.getPreviousScreen();
            if (parent == null) {
                break;
            }

            target = parent;
        }

        this.minecraft.setScreen(target);
    }

    public static enum Mode {
        CONTROLLER,
        KEYBOARD;
    }
}
