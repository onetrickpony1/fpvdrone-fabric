package com.fpvdrone.gui.entry;

import com.fpvdrone.gui.list.FPVList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class FloatEntry extends FPVEntry {
    private final FloatEntry.FloatSlider slider;
    private final EditBox editBox;
    private final Supplier<Float> valueSupplier;
    private final Consumer<Float> valueSetter;
    private final float minValue;
    private final float maxValue;
    private boolean updatingFromSlider = false;
    private boolean updatingFromEditBox = false;

    public FloatEntry(FPVList list, String name, Supplier<Float> valueSupplier, Consumer<Float> valueSetter, float minValue, float maxValue) {
        super(list, name);
        this.valueSupplier = valueSupplier;
        this.valueSetter = valueSetter;
        this.minValue = minValue;
        this.maxValue = maxValue;
        float normalizedValue = (valueSupplier.get() - minValue) / (maxValue - minValue);
        this.slider = new FloatEntry.FloatSlider(0, 0, 100, 20, normalizedValue);
        this.editBox = new EditBox(list.parentScreen.getMinecraft().font, 0, 0, 50, 20, Component.literal(""));
        this.editBox.setValue(String.format("%.2f", valueSupplier.get()));
        this.editBox.setResponder(this::onEditBoxChanged);
    }

    private void onEditBoxChanged(String text) {
        if (!this.updatingFromSlider) {
            try {
                float value = Float.parseFloat(text);
                value = Mth.clamp(value, this.minValue, this.maxValue);
                this.valueSetter.accept(value);
                this.updatingFromEditBox = true;
                this.slider.setValue((value - this.minValue) / (this.maxValue - this.minValue));
                this.updatingFromEditBox = false;
            } catch (NumberFormatException var3) {
            }
        }
    }

    private void onSliderChanged(double normalizedValue) {
        if (!this.updatingFromEditBox) {
            float value = (float)(this.minValue + normalizedValue * (this.maxValue - this.minValue));
            this.valueSetter.accept(value);
            this.updatingFromSlider = true;
            this.editBox.setValue(String.format("%.2f", value));
            this.updatingFromSlider = false;
        }
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
        int editBoxWidth = 50;
        int sliderWidth = 100;
        int spacing = 4;
        int editBoxX = rowLeft + rowWidth - editBoxWidth;
        int sliderX = editBoxX - spacing - sliderWidth;
        this.slider.setX(sliderX);
        this.slider.setY(rowTop);
        this.slider.setWidth(sliderWidth);
        this.slider.render(graphics, mouseX, mouseY, partialTicks);
        this.editBox.setX(editBoxX);
        this.editBox.setY(rowTop);
        this.editBox.setWidth(editBoxWidth);
        this.editBox.render(graphics, mouseX, mouseY, partialTicks);
    }

    public List<? extends GuiEventListener> children() {
        return List.of(this.slider, this.editBox);
    }

    public List<? extends NarratableEntry> narratables() {
        return List.of(this.slider, this.editBox);
    }

    private class FloatSlider extends AbstractSliderButton {
        public FloatSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
        }

        public void setValue(double value) {
            this.value = Mth.clamp(value, 0.0, 1.0);
            this.updateMessage();
        }

        protected void updateMessage() {
        }

        protected void applyValue() {
            FloatEntry.this.onSliderChanged(this.value);
        }
    }
}
