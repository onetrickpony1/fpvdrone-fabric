package com.fpvdrone.gui.entry;

import com.fpvdrone.gui.list.FPVList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public class DoubleNavEntry extends FPVEntry {
    private final Button button1;
    private final Button button2;

    public DoubleNavEntry(FPVList list, String label1, OnPress onPress1, String label2, OnPress onPress2) {
        super(list, "");
        this.button1 = Button.builder(Component.literal(label1), onPress1).pos(0, 0).size(100, 20).build();
        this.button2 = Button.builder(Component.literal(label2), onPress2).pos(0, 0).size(100, 20).build();
    }

    @Override
    public void betterRender(
        GuiGraphicsExtractor graphics,
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
        int buttonWidth = (rowWidth - 12) / 2;
        int button2X = rowLeft + buttonWidth + 12;
        this.button1.setX(rowLeft);
        this.button1.setY(rowTop);
        this.button1.setWidth(buttonWidth);
        this.button1.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.button2.setX(button2X);
        this.button2.setY(rowTop);
        this.button2.setWidth(buttonWidth);
        this.button2.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    public List<? extends GuiEventListener> children() {
        return List.of(this.button1, this.button2);
    }

    public List<? extends NarratableEntry> narratables() {
        return List.of(this.button1, this.button2);
    }
}
