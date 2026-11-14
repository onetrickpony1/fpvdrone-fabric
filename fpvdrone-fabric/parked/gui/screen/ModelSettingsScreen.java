package com.fpvdrone.gui.screen;

import com.fpvdrone.gui.addon.BackFooter;
import com.fpvdrone.gui.addon.ServerTitleWikiHeader;
import com.fpvdrone.gui.list.ModelSettingsList;
import com.fpvdrone.gui.screen.wizard.DecideControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class ModelSettingsScreen extends FpvScreen {
    private ModelSettingsList list;

    public ModelSettingsScreen(Screen previousScreen) {
        super(previousScreen, new ServerTitleWikiHeader(), new BackFooter());
    }

    @Override
    protected void init() {
        super.init();
        this.list = new ModelSettingsList(this, this::openCalibrationWizard);
        this.addWidget(this.list);
    }

    @Override
    public void renderCustom(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.list.render(graphics, mouseX, mouseY, partialTicks);
    }

    private void openCalibrationWizard() {
        Minecraft.getInstance().setScreen(new DecideControllerScreen(this.previousScreen));
    }
}
