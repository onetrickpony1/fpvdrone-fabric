package com.fpvdrone.gui.screen.wizard;

import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.gui.addon.BackProceedFooter;
import com.fpvdrone.gui.addon.WizardHeader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class CalibrateKeyboardScreen extends EmptyListScreen {
    public CalibrateKeyboardScreen(Screen previousScreen) {
        super(previousScreen, new WizardHeader(Component.translatable("fpvdrone.wizard.calibrateKeyboard.title"), true), new BackProceedFooter());
        BackProceedFooter footer = (BackProceedFooter)this.footer;
        footer.completeConstructor(this::onProceed, () -> I18n.get("fpvdrone.wizard.calibrateKeyboard.proceed", new Object[0]), () -> true);
    }

    @Override
    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderCustom(graphics, mouseX, mouseY, partialTicks);
        int centerY = this.height / 2 - 40;
        int lineHeight = 12;
        graphics.centeredText(this.font, Component.translatable("fpvdrone.wizard.calibrateKeyboard.info1"), this.width / 2, centerY, -1);
        graphics.centeredText(this.font, Component.translatable("fpvdrone.wizard.calibrateKeyboard.info2"), this.width / 2, centerY + lineHeight * 2, -1);
        graphics.centeredText(this.font, Component.translatable("fpvdrone.wizard.calibrateKeyboard.wasd"), this.width / 2, centerY + lineHeight * 4, -1);
        graphics.centeredText(
            this.font, Component.translatable("fpvdrone.wizard.calibrateKeyboard.arrows"), this.width / 2, centerY + lineHeight * 5, -1
        );
        graphics.centeredText(
            this.font,
            Component.translatable("fpvdrone.wizard.calibrateKeyboard.armDisarm", new Object[]{KeyManager.FPV_TOGGLE_KEY.getTranslatedKeyMessage().getString().toUpperCase()}),
            this.width / 2,
            centerY + lineHeight * 6,
            -1
        );
    }

    private void onProceed() {
        ControllerReader.getInstance().clearPreferredDevice();
        Minecraft.getInstance().gui.setScreen(new CompleteScreen(this, CompleteScreen.Mode.KEYBOARD));
    }
}
