package com.fpvdrone.gui.addon;

import com.fpvdrone.gui.screen.FpvScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class BackFooter extends ScreenAddon {
    private Button backButton;

    @Override
    public void init(FpvScreen screen) {
        int y = screen.height - 6 - 20;
        this.backButton = Button.builder(Component.translatable("gui.back"), btn -> screen.onClose()).pos(12, y).size(60, 20).build();
        screen.addFpvWidget(this.backButton);
    }

    @Override
    public void render(FpvScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    }
}
