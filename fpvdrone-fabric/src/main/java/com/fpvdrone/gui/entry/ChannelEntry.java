package com.fpvdrone.gui.entry;

import com.fpvdrone.gui.list.FPVList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public class ChannelEntry extends FPVEntry {
    private final Button channelButton;
    private final Button invertButton;
    private final Supplier<Integer> channelSupplier;
    private final Consumer<Integer> channelSetter;
    private final Supplier<Boolean> invertSupplier;
    private final Consumer<Boolean> invertSetter;
    private final Runnable onChannelClick;
    private boolean listening = false;

    public ChannelEntry(
        FPVList list,
        String name,
        Supplier<Integer> channelSupplier,
        Consumer<Integer> channelSetter,
        Supplier<Boolean> invertSupplier,
        Consumer<Boolean> invertSetter,
        Runnable onChannelClick
    ) {
        super(list, name);
        this.channelSupplier = channelSupplier;
        this.channelSetter = channelSetter;
        this.invertSupplier = invertSupplier;
        this.invertSetter = invertSetter;
        this.onChannelClick = onChannelClick;
        this.channelButton = Button.builder(Component.literal(this.getChannelLabel()), btn -> {
            if (onChannelClick != null) {
                this.listening = true;
                onChannelClick.run();
            }
        }).pos(0, 0).size(60, 20).build();
        this.invertButton = Button.builder(Component.literal(this.getInvertLabel()), btn -> invertSetter.accept(!invertSupplier.get()))
            .pos(0, 0)
            .size(60, 20)
            .build();
    }

    private String getChannelLabel() {
        if (this.listening) {
            return "...";
        } else {
            int channel = this.channelSupplier.get();
            return channel >= 0 ? "CH " + channel : "None";
        }
    }

    private String getInvertLabel() {
        return this.invertSupplier.get() ? "Flipped" : "Normal";
    }

    public void setChannel(int channel) {
        this.channelSetter.accept(channel);
        this.listening = false;
    }

    public void stopListening() {
        this.listening = false;
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
        int buttonWidth = 60;
        int spacing = 4;
        int invertX = rowLeft + rowWidth - buttonWidth;
        int channelX = invertX - spacing - buttonWidth;
        this.channelButton.setMessage(Component.literal(this.getChannelLabel()));
        this.channelButton.setX(channelX);
        this.channelButton.setY(rowTop);
        this.channelButton.setWidth(buttonWidth);
        this.channelButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.invertButton.setMessage(Component.literal(this.getInvertLabel()));
        this.invertButton.setX(invertX);
        this.invertButton.setY(rowTop);
        this.invertButton.setWidth(buttonWidth);
        this.invertButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    public List<? extends GuiEventListener> children() {
        return List.of(this.channelButton, this.invertButton);
    }

    public List<? extends NarratableEntry> narratables() {
        return List.of(this.channelButton, this.invertButton);
    }
}
