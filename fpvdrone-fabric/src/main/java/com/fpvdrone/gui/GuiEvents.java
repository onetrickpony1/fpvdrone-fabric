package com.fpvdrone.gui;

import com.fpvdrone.client.osd.OsdLayout;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.gui.screen.ModelSettingsScreen;
import com.fpvdrone.gui.screen.OsdBuilderScreen;
import com.fpvdrone.gui.screen.wizard.DecideControllerScreen;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.ControlsScreen;
import net.minecraft.network.chat.Component;

/**
 * Adds the "Remote-Controller Settings" button to Minecraft's Controls screen.
 *
 * The original mod placed a 310 px wide button where the Done button was and moved Done one
 * row (24 px) down. The Controls screen is now a scrolling options list with Done in a fixed
 * footer, so the button is added as a full-width row at the end of that list instead (directly
 * above Done). Done stays in the footer so it never slides off the bottom of the screen.
 */
public class GuiEvents {
    private static final int VANILLA_ROW_PITCH = 24;
    private static boolean registered = false;

    public static void init() {
        if (registered) {
            return;
        }
        registered = true;
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> onScreenInit(screen));
    }

    private static void onScreenInit(Screen screen) {
        if (screen instanceof ControlsScreen) {
            Button fpvButton = Button.builder(
                    Component.translatable("fpvdrone.settings.title"), btn -> Minecraft.getInstance().gui.setScreen(new DecideControllerScreen(screen))
                )
                .size(310, 20)
                .build();
            OptionsList list = findOptionsList(screen);
            if (list != null) {
                list.addBig(fpvButton);
            } else {
                // Fallback if the screen layout ever changes: place the button above the footer.
                fpvButton.setPosition(screen.width / 2 - 155, screen.height - 27 - VANILLA_ROW_PITCH);
                Screens.getWidgets(screen).add(fpvButton);
            }
        }
    }

    private static OptionsList findOptionsList(Screen screen) {
        for (AbstractWidget widget : Screens.getWidgets(screen)) {
            if (widget instanceof OptionsList list) {
                return list;
            }
        }

        return null;
    }

    /**
     * Opens the OSD builder for a drone. Called from shared code (DroneEntity, BetaFlightItem) only on
     * the client side; keeping the screen construction here stops those classes from referring to
     * client-only screen classes directly.
     */
    public static void openOsdBuilder(DroneEntity drone, OsdLayout layout) {
        Minecraft.getInstance().gui.setScreen(new OsdBuilderScreen(null, drone, layout));
    }

    public static void openModelSettingsScreen(Screen parent) {
        Minecraft.getInstance().gui.setScreen(new ModelSettingsScreen(parent));
    }
}
