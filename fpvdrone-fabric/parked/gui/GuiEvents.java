package com.fpvdrone.gui;

import com.fpvdrone.gui.screen.ModelSettingsScreen;
import com.fpvdrone.gui.screen.wizard.DecideControllerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent.Init.Post;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(
    value = {Dist.CLIENT},
    modid = "fpvdrone",
    bus = Bus.FORGE
)
public class GuiEvents {
    private static final int VANILLA_ROW_PITCH = 24;

    @SubscribeEvent
    public static void onScreenInit(Post event) {
        if (event.getScreen() instanceof ControlsScreen screen) {
            Button doneBtn = findDoneButton(screen);
            int fpvY;
            if (doneBtn != null) {
                fpvY = doneBtn.getY();
                doneBtn.setY(fpvY + 24);
            } else {
                fpvY = screen.height - 27 - 24;
            }

            Button fpvButton = Button.builder(
                    Component.translatable("fpvdrone.settings.title"), btn -> Minecraft.getInstance().setScreen(new DecideControllerScreen(screen))
                )
                .pos(screen.width / 2 - 155, fpvY)
                .size(310, 20)
                .build();
            event.addListener(fpvButton);
        }
    }

    private static Button findDoneButton(Screen screen) {
        for (GuiEventListener child : screen.children()) {
            if (child instanceof Button btn && btn.getMessage().equals(CommonComponents.GUI_DONE)) {
                return btn;
            }
        }

        return null;
    }

    public static void openModelSettingsScreen(Screen parent) {
        Minecraft.getInstance().setScreen(new ModelSettingsScreen(parent));
    }
}
