package com.fpvdrone.gui.list;

import com.fpvdrone.client.ControllerConfig;
import com.fpvdrone.client.SettingsLoader;
import com.fpvdrone.gui.entry.CategoryEntry;
import com.fpvdrone.gui.entry.DoubleNavEntry;
import com.fpvdrone.gui.entry.FloatEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class RatesList extends FPVList {
    private final ControllerConfig config;

    public RatesList(Screen parentScreen, ControllerConfig config) {
        super(parentScreen);
        this.config = config;
        this.buildEntries();
    }

    private void buildEntries() {
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.rates.yaw", new Object[0])));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.rates.rate", new Object[0]), () -> this.config.getRcRate(1), v -> {
            this.config.setRcRate(1, v);
            this.save();
        }, 0.0F, 2.55F));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.rates.super", new Object[0]), () -> this.config.getSuperRate(1), v -> {
            this.config.setSuperRate(1, v);
            this.save();
        }, 0.0F, 1.0F));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.rates.expo", new Object[0]), () -> this.config.getExpo(1), v -> {
            this.config.setExpo(1, v);
            this.save();
        }, 0.0F, 1.0F));
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.rates.pitch", new Object[0])));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.rates.rate", new Object[0]), () -> this.config.getRcRate(2), v -> {
            this.config.setRcRate(2, v);
            this.save();
        }, 0.0F, 2.55F));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.rates.super", new Object[0]), () -> this.config.getSuperRate(2), v -> {
            this.config.setSuperRate(2, v);
            this.save();
        }, 0.0F, 1.0F));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.rates.expo", new Object[0]), () -> this.config.getExpo(2), v -> {
            this.config.setExpo(2, v);
            this.save();
        }, 0.0F, 1.0F));
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.rates.roll", new Object[0])));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.rates.rate", new Object[0]), () -> this.config.getRcRate(3), v -> {
            this.config.setRcRate(3, v);
            this.save();
        }, 0.0F, 2.55F));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.rates.super", new Object[0]), () -> this.config.getSuperRate(3), v -> {
            this.config.setSuperRate(3, v);
            this.save();
        }, 0.0F, 1.0F));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.rates.expo", new Object[0]), () -> this.config.getExpo(3), v -> {
            this.config.setExpo(3, v);
            this.save();
        }, 0.0F, 1.0F));
        this.addEntry(new CategoryEntry(this, ""));
        this.addEntry(
            new DoubleNavEntry(
                this,
                I18n.get("fpvdrone.rates.slowReset", new Object[0]),
                btn -> this.applySlowPreset(),
                I18n.get("fpvdrone.rates.fastReset", new Object[0]),
                btn -> this.applyFastPreset()
            )
        );
    }

    private void applySlowPreset() {
        this.config.setRcRate(1, 0.7F);
        this.config.setRcRate(2, 0.7F);
        this.config.setRcRate(3, 0.7F);
        this.config.setSuperRate(1, 0.5F);
        this.config.setSuperRate(2, 0.5F);
        this.config.setSuperRate(3, 0.5F);
        this.config.setExpo(1, 0.2F);
        this.config.setExpo(2, 0.2F);
        this.config.setExpo(3, 0.2F);
        this.save();
        this.rebuildEntries();
    }

    private void applyFastPreset() {
        this.config.setRcRate(1, 1.0F);
        this.config.setRcRate(2, 1.0F);
        this.config.setRcRate(3, 1.0F);
        this.config.setSuperRate(1, 0.7F);
        this.config.setSuperRate(2, 0.7F);
        this.config.setSuperRate(3, 0.7F);
        this.config.setExpo(1, 0.0F);
        this.config.setExpo(2, 0.0F);
        this.config.setExpo(3, 0.0F);
        this.save();
        this.rebuildEntries();
    }

    private void rebuildEntries() {
        this.clearEntries();
        this.buildEntries();
    }

    private void save() {
        SettingsLoader.saveRateProfile(this.config);
    }
}
