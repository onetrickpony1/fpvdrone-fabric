package com.fpvdrone.gui.entry;

import com.fpvdrone.gui.list.FPVList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public class CategoryEntry extends FPVEntry {
    public CategoryEntry(FPVList list, String name) {
        super(list, name);
    }

    @Override
    public void betterRender(
        GuiGraphics graphics,
        Font font,
        int rowIndex,
        int rowTop,
        int rowLeft,
        int rowWidth,
        int rowHeight,
        int mouseX,
        int mouseY,
        boolean isMouseOver,
        float partialTicks
    ) {
        int centerX = rowLeft + rowWidth / 2;
        int textY = rowTop + (rowHeight - 9) / 2;
        graphics.drawCenteredString(font, this.name, centerX, textY, 16777215);
    }

    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    public List<? extends NarratableEntry> narratables() {
        return Collections.emptyList();
    }
}
