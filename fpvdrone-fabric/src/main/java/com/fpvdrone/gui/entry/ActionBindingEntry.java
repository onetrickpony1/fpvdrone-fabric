package com.fpvdrone.gui.entry;

import com.fpvdrone.client.ControllerConfig;
import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.client.DroneAction;
import com.fpvdrone.client.KeyManager;
import com.fpvdrone.gui.list.FPVList;
import com.fpvdrone.gui.screen.wizard.BindActionsScreen;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

/** 1.1.8 channel-mapping row (the original's sxez6mi/UkynoVINANr): an action with a controller button and a key button. */
public class ActionBindingEntry extends FPVEntry {
    private static final int BUTTON_WIDTH = 80;
    private final DroneAction action;
    private final Button controllerButton;
    private final Button keyButton;
    private ActionBindingEntry.Listening listening = ActionBindingEntry.Listening.NONE;

    public ActionBindingEntry(FPVList list, DroneAction action, ActionBindingEntry.Listener listener) {
        super(list, I18n.get(action.langKey()));
        this.action = action;
        this.controllerButton = Button.builder(Component.literal(this.controllerLabel()), btn -> {
            this.listening = ActionBindingEntry.Listening.CONTROLLER;
            listener.onListenController(this);
        }).pos(0, 0).size(BUTTON_WIDTH, 20).build();
        this.keyButton = Button.builder(Component.literal(this.keyLabel()), btn -> {
            this.listening = ActionBindingEntry.Listening.KEY;
            listener.onListenKey(this);
        }).pos(0, 0).size(BUTTON_WIDTH, 20).build();
    }

    public DroneAction getAction() {
        return this.action;
    }

    public ActionBindingEntry.Listening getListening() {
        return this.listening;
    }

    public void setController(int channel, boolean isButton) {
        ControllerConfig config = ControllerReader.getConfig();
        this.action.setChannel(config, channel);
        this.action.setButton(config, isButton);
        this.listening = ActionBindingEntry.Listening.NONE;
    }

    public void setKey(InputConstants.Key key) {
        KeyMapping mapping = KeyManager.getKey(this.action);
        if (mapping != null) {
            KeyManager.bindKey(mapping, key);
        }

        this.listening = ActionBindingEntry.Listening.NONE;
    }

    public void stopListening() {
        this.listening = ActionBindingEntry.Listening.NONE;
    }

    private String controllerLabel() {
        return this.listening == ActionBindingEntry.Listening.CONTROLLER ? "..." : BindActionsScreen.controllerLabel(this.action);
    }

    private String keyLabel() {
        if (this.listening == ActionBindingEntry.Listening.KEY) {
            return "...";
        }

        KeyMapping mapping = KeyManager.getKey(this.action);
        return mapping != null && !mapping.isUnbound() ? I18n.get("fpvdrone.bind.keyLabel", KeyManager.keyName(mapping)) : I18n.get("fpvdrone.bind.none");
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
        graphics.text(font, this.name, rowLeft, rowTop + (rowHeight - 9) / 2, -1);
        int spacing = 4;
        int keyX = rowLeft + rowWidth - BUTTON_WIDTH;
        int controllerX = keyX - spacing - BUTTON_WIDTH;
        this.controllerButton.setMessage(Component.literal(this.controllerLabel()));
        this.controllerButton.setX(controllerX);
        this.controllerButton.setY(rowTop);
        this.controllerButton.setWidth(BUTTON_WIDTH);
        this.controllerButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        this.keyButton.setMessage(Component.literal(this.keyLabel()));
        this.keyButton.setX(keyX);
        this.keyButton.setY(rowTop);
        this.keyButton.setWidth(BUTTON_WIDTH);
        this.keyButton.extractRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    public List<? extends GuiEventListener> children() {
        return List.of(this.controllerButton, this.keyButton);
    }

    public List<? extends NarratableEntry> narratables() {
        return List.of(this.controllerButton, this.keyButton);
    }

    public interface Listener {
        void onListenController(ActionBindingEntry entry);

        void onListenKey(ActionBindingEntry entry);
    }

    public enum Listening {
        NONE,
        CONTROLLER,
        KEY;
    }
}
