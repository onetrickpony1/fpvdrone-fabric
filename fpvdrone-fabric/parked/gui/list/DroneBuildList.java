package com.fpvdrone.gui.list;

import com.fpvdrone.gui.entry.BooleanEntry;
import com.fpvdrone.gui.entry.CategoryEntry;
import com.fpvdrone.gui.entry.FloatEntry;
import com.fpvdrone.gui.entry.IntEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;

public class DroneBuildList extends FPVList {
    private float mass = 250.0F;
    private int kv = 2400;
    private float motorWidth = 22.0F;
    private float motorHeight = 6.0F;
    private int cells = 4;
    private float diameter = 5.0F;
    private float pitch = 4.0F;
    private int blades = 3;
    private float frameWidth = 150.0F;
    private float frameHeight = 30.0F;
    private float frameLength = 150.0F;
    private boolean showProCam = true;
    private boolean isHeroCam = false;
    private boolean isToothpick = false;
    private int red = 255;
    private int green = 100;
    private int blue = 0;

    public DroneBuildList(Screen parentScreen) {
        super(parentScreen);
        this.buildEntries();
    }

    private void buildEntries() {
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.build.motors", new Object[0])));
        this.addEntry(new IntEntry(this, I18n.get("fpvdrone.build.kv", new Object[0]), () -> this.kv, v -> this.kv = v, 1000, 5000, 100));
        this.addEntry(
            new FloatEntry(this, I18n.get("fpvdrone.build.motorWidth", new Object[0]), () -> this.motorWidth, v -> this.motorWidth = v, 10.0F, 40.0F)
        );
        this.addEntry(
            new FloatEntry(this, I18n.get("fpvdrone.build.motorHeight", new Object[0]), () -> this.motorHeight, v -> this.motorHeight = v, 2.0F, 15.0F)
        );
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.build.battery", new Object[0])));
        this.addEntry(new IntEntry(this, I18n.get("fpvdrone.build.cells", new Object[0]), () -> this.cells, v -> this.cells = v, 1, 8));
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.build.props", new Object[0])));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.build.diameter", new Object[0]), () -> this.diameter, v -> this.diameter = v, 2.0F, 10.0F));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.build.pitch", new Object[0]), () -> this.pitch, v -> this.pitch = v, 1.0F, 8.0F));
        this.addEntry(new IntEntry(this, I18n.get("fpvdrone.build.blades", new Object[0]), () -> this.blades, v -> this.blades = v, 2, 6));
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.build.frame", new Object[0])));
        this.addEntry(new FloatEntry(this, I18n.get("fpvdrone.build.mass", new Object[0]), () -> this.mass, v -> this.mass = v, 50.0F, 1000.0F));
        this.addEntry(
            new FloatEntry(this, I18n.get("fpvdrone.build.frameWidth", new Object[0]), () -> this.frameWidth, v -> this.frameWidth = v, 50.0F, 400.0F)
        );
        this.addEntry(
            new FloatEntry(this, I18n.get("fpvdrone.build.frameHeight", new Object[0]), () -> this.frameHeight, v -> this.frameHeight = v, 10.0F, 100.0F)
        );
        this.addEntry(
            new FloatEntry(this, I18n.get("fpvdrone.build.frameLength", new Object[0]), () -> this.frameLength, v -> this.frameLength = v, 50.0F, 400.0F)
        );
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.build.accessories", new Object[0])));
        this.addEntry(new BooleanEntry(this, I18n.get("fpvdrone.build.showProCam", new Object[0]), () -> this.showProCam, v -> this.showProCam = v));
        this.addEntry(new BooleanEntry(this, I18n.get("fpvdrone.build.isHeroCam", new Object[0]), () -> this.isHeroCam, v -> this.isHeroCam = v));
        this.addEntry(new BooleanEntry(this, I18n.get("fpvdrone.build.isToothpick", new Object[0]), () -> this.isToothpick, v -> this.isToothpick = v));
        this.addEntry(new CategoryEntry(this, I18n.get("fpvdrone.build.colors", new Object[0])));
        this.addEntry(new IntEntry(this, I18n.get("fpvdrone.build.red", new Object[0]), () -> this.red, v -> this.red = v, 0, 255));
        this.addEntry(new IntEntry(this, I18n.get("fpvdrone.build.green", new Object[0]), () -> this.green, v -> this.green = v, 0, 255));
        this.addEntry(new IntEntry(this, I18n.get("fpvdrone.build.blue", new Object[0]), () -> this.blue, v -> this.blue = v, 0, 255));
    }
}
