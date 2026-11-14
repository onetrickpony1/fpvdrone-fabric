package com.fpvdrone.gui.addon;

import com.fpvdrone.gui.screen.FpvScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class DoneFooter extends ScreenAddon {
    private Button doneButton;

    @Override
    public void init(FpvScreen screen) {
        int y = screen.height - 6 - 20;
        int buttonWidth = 100;
        this.doneButton = Button.builder(Component.translatable("gui.done"), btn -> screen.onClose())
            .pos(screen.width / 2 - buttonWidth / 2, y)
            .size(buttonWidth, 20)
            .build();
        screen.addFpvWidget(this.doneButton);
    }

    @Override
    public void render(FpvScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
    }
}
