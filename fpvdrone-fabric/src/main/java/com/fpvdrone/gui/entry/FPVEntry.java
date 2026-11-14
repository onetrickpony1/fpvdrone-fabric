package com.fpvdrone.gui.entry;

import com.fpvdrone.gui.list.FPVList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;

public abstract class FPVEntry extends Entry<FPVEntry> {
    protected FPVList list;
    protected String name;

    public FPVEntry(FPVList list, String name) {
        this.list = list;
        this.name = name;
    }

    public abstract void betterRender(
        GuiGraphicsExtractor var1, Font var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, boolean var10, float var11
    );

    @Override
    public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
        Font font = Minecraft.getInstance().font;
        int index = this.list.children().indexOf(this);
        // Row top and height match the old list layout (2 px inner padding on each side).
        int top = this.getContentY();
        int height = this.getContentHeight();
        int adjustedLeft = this.list.getLeftPadding();
        int adjustedWidth = this.list.parentScreen.width - this.list.getLeftPadding() - this.list.getRightPadding();
        if (this.list.maxScrollAmount() > 0) {
            adjustedWidth -= 6;
        }

        this.betterRender(graphics, font, index, top, adjustedLeft, adjustedWidth, height, mouseX, mouseY, isMouseOver, partialTicks);
    }
}
