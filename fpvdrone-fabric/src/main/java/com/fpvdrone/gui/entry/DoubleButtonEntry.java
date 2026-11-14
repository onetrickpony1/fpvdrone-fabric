package com.fpvdrone.gui.entry;

import com.fpvdrone.gui.list.FPVList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public class DoubleButtonEntry extends FPVEntry {
    private final Button button1;
    private final Button button2;
    private final Supplier<String> label1Supplier;
    private final Supplier<String> label2Supplier;

    public DoubleButtonEntry(FPVList list, String name, Supplier<String> label1Supplier, OnPress onPress1, Supplier<String> label2Supplier, OnPress onPress2) {
        super(list, name);
        this.label1Supplier = label1Supplier;
        this.label2Supplier = label2Supplier;
        this.button1 = Button.builder(Component.literal(label1Supplier.get()), onPress1).pos(0, 0).size(100, 20).build();
        this.button2 = Button.builder(Component.literal(label2Supplier.get()), onPress2).pos(0, 0).size(100, 20).build();
    }

    public DoubleButtonEntry(FPVList list, String name, String label1, OnPress onPress1, String label2, OnPress onPress2) {
        this(list, name, () -> label1, onPress1, () -> label2, onPress2);
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
        int labelWidth = font.width(this.name);
        int labelY = rowTop + (rowHeight - 9) / 2;
        graphics.text(font, this.name, rowLeft, labelY, -1);
        int buttonWidth = 100;
        int spacing = 12;
        int button2X = rowLeft + rowWidth - buttonWidth;
        int button1X = button2X - spacing - buttonWidth;
        this.button1.setMessage(Component.literal(this.label1Supplier.get()));
        this.button1.setX(button1X);
        this.button1.setY(rowTop);
        this.button1.setWidth(buttonWidth);
        this.button1.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.button2.setMessage(Component.literal(this.label2Supplier.get()));
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
