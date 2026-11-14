package com.fpvdrone.gui.addon;

import com.fpvdrone.gui.screen.FpvScreen;
import net.minecraft.client.gui.GuiGraphics;

public abstract class ScreenAddon {
    public abstract void init(FpvScreen var1);

    public abstract void render(FpvScreen var1, GuiGraphics var2, int var3, int var4, float var5);
}
