package com.fpvdrone.gui.list;

import com.fpvdrone.FpvConfig;
import com.fpvdrone.gui.entry.BooleanEntry;
import com.fpvdrone.gui.entry.CategoryEntry;
import com.fpvdrone.gui.entry.FloatEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class OtherSettingsList extends FPVList {
    public OtherSettingsList(Screen parentScreen) {
        super(parentScreen);
        this.buildEntries();
    }

    private void buildEntries() {
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.other.display", new Object[0])));
        this.addEntry(
            new BooleanEntry(this, I18n.get("fpvdrone.other.stickOverlay", new Object[0]), FpvConfig::isStickOverlay, FpvConfig::setStickOverlay)
        );
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.other.camera", new Object[0])));
        this.addEntry(
            new FloatEntry(
                this, I18n.get("fpvdrone.other.defaultAngle", new Object[0]), FpvConfig::getDefaultAngle, FpvConfig::setDefaultAngle, 0.0F, 90.0F
            )
        );
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.other.fov", new Object[0]), FpvConfig::getFov, FpvConfig::setFov, 60.0F, 180.0F));
    }
}
