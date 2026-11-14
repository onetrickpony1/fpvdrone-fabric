package com.fpvdrone.gui.list;

import com.fpvdrone.gui.entry.FPVEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;

public class FPVList extends ContainerObjectSelectionList<FPVEntry> {
    public FPVEntry activeEntry = null;
    public Screen parentScreen;

    public FPVList(Screen parentScreen) {
        super(Minecraft.getInstance(), parentScreen.width, parentScreen.height, 43, parentScreen.height - 32, 20);
        this.parentScreen = parentScreen;
    }

    public int addEntry(FPVEntry entry) {
        return super.addEntry(entry);
    }

    public int getLeftPadding() {
        return 30;
    }

    public int getRightPadding() {
        return 30;
    }

    protected int getScrollbarPosition() {
        return this.width - 6;
    }

    public int getRowWidth() {
        return this.width;
    }
}
