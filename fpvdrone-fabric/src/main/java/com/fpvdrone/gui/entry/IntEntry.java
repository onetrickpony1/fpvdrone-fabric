package com.fpvdrone.gui.entry;

import com.fpvdrone.gui.list.FPVList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class IntEntry extends FPVEntry {
    private final EditBox editBox;
    private final Button minusButton;
    private final Button plusButton;
    private final Supplier<Integer> valueSupplier;
    private final Consumer<Integer> valueSetter;
    private final int minValue;
    private final int maxValue;
    private final int step;

    public IntEntry(FPVList list, String name, Supplier<Integer> valueSupplier, Consumer<Integer> valueSetter, int minValue, int maxValue, int step) {
        super(list, name);
        this.valueSupplier = valueSupplier;
        this.valueSetter = valueSetter;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.editBox = new EditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 50, 20, Component.literal(""));
        this.editBox.setValue(String.valueOf(valueSupplier.get()));
        this.editBox.setResponder(this::onEditBoxChanged);
        this.minusButton = Button.builder(Component.literal("-"), btn -> {
            int newValue = Mth.clamp(valueSupplier.get() - step, minValue, maxValue);
            valueSetter.accept(newValue);
            this.editBox.setValue(String.valueOf(newValue));
        }).pos(0, 0).size(20, 20).build();
        this.plusButton = Button.builder(Component.literal("+"), btn -> {
            int newValue = Mth.clamp(valueSupplier.get() + step, minValue, maxValue);
            valueSetter.accept(newValue);
            this.editBox.setValue(String.valueOf(newValue));
        }).pos(0, 0).size(20, 20).build();
    }

    public IntEntry(FPVList list, String name, Supplier<Integer> valueSupplier, Consumer<Integer> valueSetter, int minValue, int maxValue) {
        this(list, name, valueSupplier, valueSetter, minValue, maxValue, 1);
    }

    private void onEditBoxChanged(String text) {
        try {
            int value = Integer.parseInt(text);
            value = Mth.clamp(value, this.minValue, this.maxValue);
            this.valueSetter.accept(value);
        } catch (NumberFormatException var3) {
        }
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
        int labelY = rowTop + (rowHeight - 9) / 2;
        graphics.text(font, this.name, rowLeft, labelY, -1);
        int buttonSize = 20;
        int editBoxWidth = 50;
        int spacing = 2;
        int plusX = rowLeft + rowWidth - buttonSize;
        int editBoxX = plusX - spacing - editBoxWidth;
        int minusX = editBoxX - spacing - buttonSize;
        this.minusButton.setX(minusX);
        this.minusButton.setY(rowTop);
        this.minusButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.editBox.setX(editBoxX);
        this.editBox.setY(rowTop);
        this.editBox.setWidth(editBoxWidth);
        this.editBox.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.plusButton.setX(plusX);
        this.plusButton.setY(rowTop);
        this.plusButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    public List<? extends GuiEventListener> children() {
        return List.of(this.minusButton, this.editBox, this.plusButton);
    }

    public List<? extends NarratableEntry> narratables() {
        return List.of(this.minusButton, this.editBox, this.plusButton);
    }
}
