package com.fpvdrone.gui.screen.wizard;

import com.fpvdrone.gui.addon.ScreenAddon;
import com.fpvdrone.gui.screen.FpvScreen;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class EmptyListScreen extends FpvScreen {
    public EmptyListScreen(Screen previousScreen, @Nullable ScreenAddon header, @Nullable ScreenAddon footer) {
        super(previousScreen, header, footer);
    }

    @Override
    public void renderCustom(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
    }
}
