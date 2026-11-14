package com.fpvdrone.gui.screen.wizard;

import com.fpvdrone.gui.addon.BackFooter;
import com.fpvdrone.gui.addon.WizardHeader;
import com.fpvdrone.gui.screen.FpvScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class DecideControllerScreen extends FpvScreen {
    private Button yesButton;
    private Button noButton;

    public DecideControllerScreen(Screen previousScreen) {
        super(previousScreen, new WizardHeader(Component.translatable("fpvdrone.wizard.decideController.title"), true), new BackFooter());
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int buttonWidth = 60;
        int spacing = 12;
        this.yesButton = Button.builder(Component.translatable("fpvdrone.wizard.decideController.yes"), btn -> this.openChooseControllerScreen())
            .pos(centerX - buttonWidth - spacing / 2, centerY)
            .size(buttonWidth, 20)
            .build();
        this.addRenderableWidget(this.yesButton);
        this.noButton = Button.builder(Component.translatable("fpvdrone.wizard.decideController.no"), btn -> this.openCalibrateKeyboardScreen())
            .pos(centerX + spacing / 2, centerY)
            .size(buttonWidth, 20)
            .build();
        this.addRenderableWidget(this.noButton);
    }

    @Override
    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.centeredText(this.font, Component.translatable("fpvdrone.wizard.decideController.question"), this.width / 2, this.height / 2 - 30, -1);
    }

    private void openChooseControllerScreen() {
        Minecraft.getInstance().gui.setScreen(new CalibrateControllerStickScreen(this));
    }

    private void openCalibrateKeyboardScreen() {
        Minecraft.getInstance().gui.setScreen(new CalibrateKeyboardScreen(this));
    }
}
