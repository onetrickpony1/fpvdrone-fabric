package com.fpvdrone.gui.screen;

import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.gui.addon.BackFooter;
import com.fpvdrone.gui.addon.WizardHeader;
import com.fpvdrone.gui.list.ChannelMappingList;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/** 1.1.8 (the original's sxez6mi/MU939wEYZ): the channel mapping list on its own screen, opened from Remote-Controller Settings. */
public class ChannelMappingScreen extends FpvScreen {
    private ChannelMappingList list;

    public ChannelMappingScreen(Screen previousScreen) {
        super(previousScreen, new WizardHeader(Component.translatable("fpvdrone.settings.channelMapping"), false), new BackFooter());
    }

    @Override
    protected void init() {
        super.init();
        this.list = new ChannelMappingList(this, ControllerReader.getConfig());
        this.addWidget(this.list);
    }

    public void tick() {
        super.tick();
        // The list's listeners need fresh controller readings while the screen is open.
        ControllerReader.getInstance().poll();
        ControllerReader.tickListeners();
    }

    @Override
    public void renderCustom(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        this.list.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return this.list.onKeyPressed(event) || super.keyPressed(event);
    }

    @Override
    public void removed() {
        this.list.stopListening();
        super.removed();
    }
}
