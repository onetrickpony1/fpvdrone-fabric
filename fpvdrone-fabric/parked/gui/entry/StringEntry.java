package com.fpvdrone.gui.entry;

import com.fpvdrone.gui.list.FPVList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public class StringEntry extends FPVEntry {
    private final EditBox editBox;
    private final Supplier<String> valueSupplier;
    private final Consumer<String> valueSetter;
    private final int maxLength;

    public StringEntry(FPVList list, String name, Supplier<String> valueSupplier, Consumer<String> valueSetter, int maxLength) {
        super(list, name);
        this.valueSupplier = valueSupplier;
        this.valueSetter = valueSetter;
        this.maxLength = maxLength;
        this.editBox = new EditBox(list.parentScreen.getMinecraft().font, 0, 0, 150, 20, Component.literal(""));
        this.editBox.setMaxLength(maxLength);
        this.editBox.setValue(valueSupplier.get());
        this.editBox.setResponder(valueSetter::accept);
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
        int labelY = rowTop + (rowHeight - 9) / 2;
        graphics.drawString(font, this.name, rowLeft, labelY, 16777215);
        int editBoxWidth = 150;
        int editBoxX = rowLeft + rowWidth - editBoxWidth;
        this.editBox.setX(editBoxX);
        this.editBox.setY(rowTop);
        this.editBox.setWidth(editBoxWidth);
        this.editBox.render(graphics, mouseX, mouseY, partialTicks);
    }

    public List<? extends GuiEventListener> children() {
        return List.of(this.editBox);
    }

    public List<? extends NarratableEntry> narratables() {
        return List.of(this.editBox);
    }
}
