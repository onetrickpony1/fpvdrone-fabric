package com.fpvdrone.gui.list;

import com.fpvdrone.gui.entry.CategoryEntry;
import com.fpvdrone.gui.entry.SingleButtonEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ModelSettingsList extends FPVList {
    private final Runnable onCalibrationWizard;

    public ModelSettingsList(Screen parentScreen, Runnable onCalibrationWizard) {
        super(parentScreen);
        this.onCalibrationWizard = onCalibrationWizard;
        this.buildEntries();
    }

    private void buildEntries() {
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.settings.controller", new Object[0])));
        this.addEntry(
            new SingleButtonEntry(this, "", I18n.get("fpvdrone.settings.calibrationWizard", new Object[0]), btn -> this.onCalibrationWizard.run())
        );
    }
}
