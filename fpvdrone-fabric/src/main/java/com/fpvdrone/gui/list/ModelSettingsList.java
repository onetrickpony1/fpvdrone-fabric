package com.fpvdrone.gui.list;

import com.fpvdrone.gui.entry.CategoryEntry;
import com.fpvdrone.gui.entry.SingleButtonEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class ModelSettingsList extends FPVList {
    private final Runnable onCalibrationWizard;
    private final Runnable onChannelMapping;

    public ModelSettingsList(Screen parentScreen, Runnable onCalibrationWizard, Runnable onChannelMapping) {
        super(parentScreen);
        this.onCalibrationWizard = onCalibrationWizard;
        this.onChannelMapping = onChannelMapping;
        this.buildEntries();
    }

    private void buildEntries() {
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.settings.controller", new Object[0])));
        this.addEntry(
            new SingleButtonEntry(this, "", I18n.get("fpvdrone.settings.calibrationWizard", new Object[0]), btn -> this.onCalibrationWizard.run())
        );
        // 1.1.8: channel mapping (axes, gimbal tilt, arm switch, action switches and keys).
        this.addEntry(
            new SingleButtonEntry(this, "", I18n.get("fpvdrone.settings.channelMapping", new Object[0]), btn -> this.onChannelMapping.run())
        );
    }
}
