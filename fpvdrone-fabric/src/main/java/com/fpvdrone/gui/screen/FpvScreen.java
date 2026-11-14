package com.fpvdrone.gui.screen;

import com.fpvdrone.gui.addon.ScreenAddon;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class FpvScreen extends Screen {
    @Nullable
    protected Screen previousScreen;
    @Nullable
    protected ScreenAddon header;
    @Nullable
    protected ScreenAddon footer;

    public FpvScreen(Screen previousScreen, @Nullable ScreenAddon header, @Nullable ScreenAddon footer) {
        super(Component.empty());
        this.previousScreen = previousScreen;
        this.header = header;
        this.footer = footer;
    }

    protected void init() {
        if (this.header != null) {
            this.header.init(this);
        }

        if (this.footer != null) {
            this.footer.init(this);
        }
    }

    // The screen background is drawn by Screen before this method is called.
    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderCustom(graphics, mouseX, mouseY, partialTicks);
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        if (this.header != null) {
            this.header.render(this, graphics, mouseX, mouseY, partialTicks);
        }

        if (this.footer != null) {
            this.footer.render(this, graphics, mouseX, mouseY, partialTicks);
        }
    }

    public abstract void renderCustom(GuiGraphicsExtractor var1, int var2, int var3, float var4);

    public void onClose() {
        this.minecraft.gui.setScreen(this.previousScreen);
    }

    public <T extends GuiEventListener & Renderable & NarratableEntry> T addFpvWidget(T widget) {
        return (T)this.addRenderableWidget(widget);
    }

    @Nullable
    public Screen getPreviousScreen() {
        return this.previousScreen;
    }
}
