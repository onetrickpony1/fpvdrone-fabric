package com.fpvdrone.gui.screen;

import com.fpvdrone.gui.addon.BackFooter;
import com.fpvdrone.gui.addon.ServerTitleWikiHeader;
import com.fpvdrone.gui.list.ModelSettingsList;
import com.fpvdrone.gui.screen.wizard.DecideControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

public class ModelSettingsScreen extends FpvScreen {
    private ModelSettingsList list;

    public ModelSettingsScreen(Screen previousScreen) {
        super(previousScreen, new ServerTitleWikiHeader(), new BackFooter());
    }

    @Override
    protected void init() {
        super.init();
        this.list = new ModelSettingsList(this, this::openCalibrationWizard, this::openChannelMapping);
        this.addWidget(this.list);
    }

    @Override
    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        this.list.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    private void openCalibrationWizard() {
        Minecraft.getInstance().gui.setScreen(new DecideControllerScreen(this.previousScreen));
    }

    private void openChannelMapping() {
        Minecraft.getInstance().gui.setScreen(new ChannelMappingScreen(this));
    }
}
