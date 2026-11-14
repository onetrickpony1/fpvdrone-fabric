package com.fpvdrone.gui.list;

import com.fpvdrone.gui.entry.FPVEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;

public class FPVList extends ContainerObjectSelectionList<FPVEntry> {
    public FPVEntry activeEntry = null;
    public Screen parentScreen;

    public FPVList(Screen parentScreen) {
        // The list spans from y = 43 down to 32 px above the bottom of the screen.
        super(Minecraft.getInstance(), parentScreen.width, parentScreen.height - 32 - 43, 43, 20);
        this.parentScreen = parentScreen;
    }

    @Override
    public int addEntry(FPVEntry entry) {
        return super.addEntry(entry);
    }

    public int getLeftPadding() {
        return 30;
    }

    public int getRightPadding() {
        return 30;
    }

    @Override
    protected int scrollBarX() {
        return this.getX() + this.width - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }
}
