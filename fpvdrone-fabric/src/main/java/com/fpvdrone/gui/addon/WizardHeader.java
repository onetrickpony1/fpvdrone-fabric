package com.fpvdrone.gui.addon;

import com.fpvdrone.gui.screen.FpvScreen;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import com.mojang.datafixers.util.Pair;

public class WizardHeader extends ScreenAddon {
    @Nullable
    private final Component titleComponent;
    private final boolean showClose;
    private Button closeButton;
    private Button helpButton;
    @Nullable
    private Runnable overrideOnDone;
    private final List<Pair<String, Component>> helpQA = new ArrayList<>();

    public WizardHeader(@Nullable Component title, boolean showClose) {
        this.titleComponent = title;
        this.showClose = showClose;
    }

    public void addHelpQA(String question, Component answer) {
        this.helpQA.add(Pair.of(question, answer));
    }

    @Override
    public void init(FpvScreen screen) {
        if (this.showClose) {
            this.closeButton = Button.builder(Component.literal("×"), btn -> {
                if (this.overrideOnDone != null) {
                    this.overrideOnDone.run();
                } else {
                    screen.onClose();
                }
            }).pos(screen.width - 12 - 20, 12).size(20, 20).build();
            screen.addFpvWidget(this.closeButton);
        }

        if (!this.helpQA.isEmpty()) {
            this.helpButton = Button.builder(Component.literal("?"), btn -> {}).pos(screen.width - 12 - 44, 12).size(20, 20).build();
            screen.addFpvWidget(this.helpButton);
        }
    }

    @Override
    public void render(FpvScreen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.titleComponent != null) {
            graphics.centeredText(Minecraft.getInstance().font, this.titleComponent.copy().withStyle(ChatFormatting.BOLD), screen.width / 2, 12, -1);
        }
    }
}
