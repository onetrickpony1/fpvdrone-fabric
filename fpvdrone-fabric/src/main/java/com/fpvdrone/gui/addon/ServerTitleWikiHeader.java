package com.fpvdrone.gui.addon;

import com.fpvdrone.gui.screen.FpvScreen;
import java.net.URI;
import net.minecraft.ChatFormatting;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ServerTitleWikiHeader extends ScreenAddon {
    private static final String WIKI_URL = "https://github.com/minecraft-fpv/mfpv-client/wiki";
    private static final String SERVER_URL = "https://discord.gg/minecraftfpv";
    private Button serverButton;
    private Button wikiButton;

    @Override
    public void init(FpvScreen screen) {
        int centerX = screen.width / 2;
        int buttonWidth = 60;
        int spacing = 4;
        this.serverButton = Button.builder(Component.translatable("fpvdrone.header.server"), btn -> this.openUrl("https://discord.gg/minecraftfpv"))
            .pos(centerX - buttonWidth - spacing, 12)
            .size(buttonWidth, 20)
            .build();
        this.wikiButton = Button.builder(
                Component.translatable("fpvdrone.header.wiki"), btn -> this.openUrl("https://github.com/minecraft-fpv/mfpv-client/wiki")
            )
            .pos(centerX + spacing, 12)
            .size(buttonWidth, 20)
            .build();
        screen.addFpvWidget(this.serverButton);
        screen.addFpvWidget(this.wikiButton);
    }

    @Override
    public void render(FpvScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.centeredText(
            Minecraft.getInstance().font, Component.translatable("fpvdrone.settings.title").withStyle(ChatFormatting.BOLD), screen.width / 2, 2, -1
        );
    }

    private void openUrl(String url) {
        try {
            Blaze3D.openUri(new URI(url));
        } catch (Exception var3) {
        }
    }
}
