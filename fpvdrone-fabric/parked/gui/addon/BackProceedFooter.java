package com.fpvdrone.gui.addon;

import com.fpvdrone.gui.screen.FpvScreen;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class BackProceedFooter extends ScreenAddon {
    private Button backButton;
    private Button proceedButton;
    @Nullable
    private Runnable onProceed;
    @Nullable
    private Supplier<String> proceedLabelSupplier;
    @Nullable
    private Supplier<Boolean> proceedVisibleSupplier;
    @Nullable
    private Runnable overrideOnBack;

    public void completeConstructor(Runnable onProceed, Supplier<String> proceedLabel, Supplier<Boolean> proceedVisible) {
        this.onProceed = onProceed;
        this.proceedLabelSupplier = proceedLabel;
        this.proceedVisibleSupplier = proceedVisible;
    }

    public void overrideOnBack(Runnable onBack) {
        this.overrideOnBack = onBack;
    }

    @Override
    public void init(FpvScreen screen) {
        int y = screen.height - 6 - 20;
        this.backButton = Button.builder(Component.translatable("gui.back"), btn -> {
            if (this.overrideOnBack != null) {
                this.overrideOnBack.run();
            } else {
                screen.onClose();
            }
        }).pos(12, y).size(60, 20).build();
        screen.addFpvWidget(this.backButton);
        this.proceedButton = Button.builder(Component.literal(""), btn -> {
            if (this.onProceed != null) {
                this.onProceed.run();
            }
        }).pos(screen.width - 12 - 100, y).size(100, 20).build();
        screen.addFpvWidget(this.proceedButton);
    }

    @Override
    public void render(FpvScreen screen, GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.proceedLabelSupplier != null) {
            this.proceedButton.setMessage(Component.literal(this.proceedLabelSupplier.get()));
        }

        if (this.proceedVisibleSupplier != null) {
            this.proceedButton.visible = this.proceedVisibleSupplier.get();
        }
    }
}
