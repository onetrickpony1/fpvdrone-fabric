package com.fpvdrone.gui.list;

import com.fpvdrone.gui.entry.SingleButtonEntry;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;

public class ModelChoicesList extends FPVList {
    private final Consumer<String> onSelect;
    private static final String[] MODELS = new String[]{"Default", "Toothpick", "Cinewhoop", "Racing"};

    public ModelChoicesList(Screen parentScreen, Consumer<String> onSelect) {
        super(parentScreen);
        this.onSelect = onSelect;
        this.buildEntries();
    }

    private void buildEntries() {
        for (String model : MODELS) {
            this.addEntry(new SingleButtonEntry(this, "", model, btn -> this.onSelect.accept(model)));
        }
    }
}
