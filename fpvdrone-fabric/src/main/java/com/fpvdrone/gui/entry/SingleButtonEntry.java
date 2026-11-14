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

public class SingleButtonEntry extends FPVEntry {
    private final Button button;
    private final Supplier<String> labelSupplier;

    public SingleButtonEntry(FPVList list, String name, Supplier<String> labelSupplier, OnPress onPress) {
        super(list, name);
        this.labelSupplier = labelSupplier;
        this.button = Button.builder(Component.literal(labelSupplier.get()), onPress).pos(0, 0).size(200, 20).build();
    }

    public SingleButtonEntry(FPVList list, String name, String label, OnPress onPress) {
        this(list, name, () -> label, onPress);
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
        this.button.setMessage(Component.literal(this.labelSupplier.get()));
        this.button.setX(rowLeft);
        this.button.setY(rowTop);
        this.button.setWidth(rowWidth);
        this.button.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    public List<? extends GuiEventListener> children() {
        return List.of(this.button);
    }

    public List<? extends NarratableEntry> narratables() {
        return List.of(this.button);
    }
}
