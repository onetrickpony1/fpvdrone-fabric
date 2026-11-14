package com.fpvdrone.gui.list;

import com.fpvdrone.client.ControllerReader;
import com.fpvdrone.gui.entry.SingleButtonEntry;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ControllerChoicesList extends FPVList {
    private final Consumer<Integer> onSelect;

    public ControllerChoicesList(Screen parentScreen, Consumer<Integer> onSelect) {
        super(parentScreen);
        this.onSelect = onSelect;
        this.buildEntries();
    }

    private void buildEntries() {
        List<String> controllers = ControllerReader.getInstance().getAvailableControllers();
        if (controllers.isEmpty()) {
            this.addEntry(new SingleButtonEntry(this, "", I18n.get("fpvdrone.controller.noControllers", new Object[0]), btn -> {}));
        } else {
            for (int i = 0; i < controllers.size(); i++) {
                int index = i;
                String name = controllers.get(i);
                this.addEntry(new SingleButtonEntry(this, "", name, btn -> this.onSelect.accept(index)));
            }
        }
    }

    public void refresh() {
        this.clearEntries();
        this.buildEntries();
    }
}
