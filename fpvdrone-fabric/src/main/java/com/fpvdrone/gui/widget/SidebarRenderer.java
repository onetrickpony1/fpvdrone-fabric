package com.fpvdrone.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class SidebarRenderer {
    private static final String[] ICONS = new String[]{"═", "⚡", "▶", "■", "★", ">"};
    private static final String[] TABS = new String[]{"Ports", "Powers", "Modes", "OSD", "Options", "CLI"};
    private static final int OSD_TAB_INDEX = 3;

    private SidebarRenderer() {
    }

    public static void render(
        GuiGraphicsExtractor graphics,
        Font font,
        int headerH,
        int sidebarW,
        int screenH,
        int logBarH,
        int colorSurface100,
        int colorSurface200,
        int colorBorder,
        int colorPrimary,
        int colorPrimaryT2,
        int colorText,
        int colorTextDim
    ) {
        int sidebarBottom = screenH - logBarH;
        graphics.fill(0, headerH, sidebarW, sidebarBottom, colorSurface100);
        graphics.fill(sidebarW - 1, headerH, sidebarW, sidebarBottom, colorBorder);
        int tabsTop = headerH + 8;
        int availableH = sidebarBottom - tabsTop - 8;
        int idealTabStep = 16;
        int minTabStep = 12;
        int neededH = TABS.length * idealTabStep + 8;
        int tabStep = neededH > availableH && availableH > 0 ? Math.max(minTabStep, (availableH - 8) / TABS.length) : idealTabStep;
        int tabsBottom = tabsTop + TABS.length * tabStep + 8;
        graphics.fill(4, tabsTop, sidebarW - 5, Math.min(tabsBottom, sidebarBottom), colorSurface200);
        graphics.enableScissor(0, headerH, sidebarW, sidebarBottom);
        int y = tabsTop + 4;
        int tabH = Math.min(14, tabStep - 2);

        for (int i = 0; i < TABS.length; i++) {
            boolean isOsd = i == 3;
            if (isOsd) {
                graphics.fill(6, y - 1, sidebarW - 7, y + tabH + 1, colorPrimaryT2);
            }

            int iconColor = isOsd ? colorPrimary : colorTextDim;
            int textYPos = y + (tabH - 9) / 2 + 1;
            int iconX = 12;
            int textX = iconX + 10;
            graphics.text(font, ICONS[i], iconX, textYPos, iconColor, true);
            int textClipRight = sidebarW - 7;
            graphics.enableScissor(textX, y - 1, textClipRight, y + tabH + 1);
            graphics.text(font, TABS[i], textX, textYPos, colorText, true);
            graphics.disableScissor();
            y += tabStep;
        }

        graphics.disableScissor();
    }
}
