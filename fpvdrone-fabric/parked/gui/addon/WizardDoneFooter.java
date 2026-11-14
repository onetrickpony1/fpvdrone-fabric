package com.fpvdrone.gui.addon;

import com.fpvdrone.gui.screen.FpvScreen;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class WizardDoneFooter extends ScreenAddon {
    private Button doneButton;
    private Button moreSettingsButton;
    @Nullable
    private Runnable onMoreSettings;

    public void setOnMoreSettings(Runnable onMoreSettings) {
        this.onMoreSettings = onMoreSettings;
    }

    @Override
    public void init(FpvScreen screen) {
        int y = screen.height - 6 - 20;
        int spacing = 12;
        int buttonWidth = 100;
        int totalWidth = buttonWidth * 2 + spacing;
        int startX = screen.width / 2 - totalWidth / 2;
        this.doneButton = Button.builder(Component.translatable("gui.done"), btn -> screen.onClose())
            .pos(startX, y)
            .size(buttonWidth, 20)
            .build();
        screen.addFpvWidget(this.doneButton);
        this.moreSettingsButton = Button.builder(Component.translatable("fpvdrone.settings.moreSettings"), btn -> {
            if (this.onMoreSettings != null) {
                this.onMoreSettings.run();
            }
        }).pos(startX + buttonWidth + spacing, y).size(buttonWidth, 20).build();
        screen.addFpvWidget(this.moreSettingsButton);
    }

    @Override
    public void render(FpvScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    }
}
