package com.fpvdrone.gui.entry;

import com.fpvdrone.gui.list.FPVList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public class BooleanEntry extends FPVEntry {
    private final Button toggleButton;
    private final Supplier<Boolean> valueSupplier;
    private final Consumer<Boolean> valueSetter;

    public BooleanEntry(FPVList list, String name, Supplier<Boolean> valueSupplier, Consumer<Boolean> valueSetter) {
        super(list, name);
        this.valueSupplier = valueSupplier;
        this.valueSetter = valueSetter;
        this.toggleButton = Button.builder(Component.literal(this.getToggleLabel()), btn -> valueSetter.accept(!valueSupplier.get()))
            .pos(0, 0)
            .size(50, 20)
            .build();
    }

    private String getToggleLabel() {
        return this.valueSupplier.get() ? "ON" : "OFF";
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
        int buttonWidth = 50;
        int buttonX = rowLeft + rowWidth - buttonWidth;
        this.toggleButton.setMessage(Component.literal(this.getToggleLabel()));
        this.toggleButton.setX(buttonX);
        this.toggleButton.setY(rowTop);
        this.toggleButton.setWidth(buttonWidth);
        this.toggleButton.render(graphics, mouseX, mouseY, partialTicks);
    }

    public List<? extends GuiEventListener> children() {
        return List.of(this.toggleButton);
    }

    public List<? extends NarratableEntry> narratables() {
        return List.of(this.toggleButton);
    }
}
