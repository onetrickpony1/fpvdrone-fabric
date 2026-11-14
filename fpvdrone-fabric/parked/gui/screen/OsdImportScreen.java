package com.fpvdrone.gui.screen;

import com.fpvdrone.client.osd.OsdLayout;
import com.fpvdrone.client.osd.OsdLayoutIO;
import com.fpvdrone.client.osd.OsdLogger;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class OsdImportScreen extends FpvScreen {
    private static final int ROW_HEIGHT = 22;
    private static final int LIST_TOP = 40;
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private final Consumer<OsdLayout> onImport;
    private List<Path> layoutFiles;
    private int scrollOffset;
    private int selectedIndex = -1;

    public OsdImportScreen(Screen previous, Consumer<OsdLayout> onImport) {
        super(previous, null, null);
        this.onImport = onImport;
    }

    @Override
    protected void init() {
        super.init();
        this.layoutFiles = OsdLayoutIO.listLayouts();
        this.scrollOffset = 0;
        this.selectedIndex = -1;
        this.addFpvWidget(
            Button.builder(Component.literal("Import Selected"), btn -> this.importSelected())
                .bounds(this.width / 2 - 200 - 4, this.height - 30, 200, 20)
                .build()
        );
        this.addFpvWidget(
            Button.builder(Component.literal("Cancel"), btn -> this.onClose()).bounds(this.width / 2 + 4, this.height - 30, 200, 20).build()
        );
    }

    @Override
    public void renderCustom(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        Font font = this.minecraft.font;
        graphics.drawCenteredString(font, "Import OSD Layout", this.width / 2, 12, -1);
        if (this.layoutFiles.isEmpty()) {
            graphics.drawCenteredString(font, "No layout files found in cubeflight/osd_layouts/", this.width / 2, this.height / 2, -5592406);
        } else {
            graphics.drawCenteredString(font, this.layoutFiles.size() + " layout(s) available", this.width / 2, 26, -5592406);
            int listX = this.width / 2 - 150;
            int listW = 300;
            int maxVisible = (this.height - 40 - 50) / 22;

            for (int i = 0; i < maxVisible && i + this.scrollOffset < this.layoutFiles.size(); i++) {
                int idx = i + this.scrollOffset;
                int rowY = 40 + i * 22;
                String filename = this.layoutFiles.get(idx).getFileName().toString();
                boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + 22;
                boolean selected = idx == this.selectedIndex;
                int bgColor = selected ? -12961280 : (hovered ? -14013910 : -14803426);
                graphics.fill(listX, rowY, listX + listW, rowY + 22, bgColor);
                if (selected) {
                    graphics.fill(listX, rowY, listX + 2, rowY + 22, -17664);
                }

                int textColor = selected ? -17664 : (hovered ? -1 : -3355444);
                graphics.drawString(font, filename, listX + 6, rowY + (22 - 9) / 2, textColor);
            }

            if (this.scrollOffset > 0) {
                graphics.drawCenteredString(font, "▲ Scroll up", this.width / 2, 30, -7829368);
            }

            if (this.scrollOffset + maxVisible < this.layoutFiles.size()) {
                int bottomY = 40 + maxVisible * 22 + 2;
                graphics.drawCenteredString(font, "▼ Scroll down", this.width / 2, bottomY, -7829368);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int listX = this.width / 2 - 150;
            int listW = 300;
            int maxVisible = (this.height - 40 - 50) / 22;

            for (int i = 0; i < maxVisible && i + this.scrollOffset < this.layoutFiles.size(); i++) {
                int idx = i + this.scrollOffset;
                int rowY = 40 + i * 22;
                if (mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + 22) {
                    this.selectedIndex = idx;
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int maxVisible = (this.height - 40 - 50) / 22;
        int maxScroll = Math.max(0, this.layoutFiles.size() - maxVisible);
        this.scrollOffset = Math.max(0, Math.min(maxScroll, this.scrollOffset - (int)delta));
        return true;
    }

    private void importSelected() {
        if (this.selectedIndex >= 0 && this.selectedIndex < this.layoutFiles.size()) {
            Path file = this.layoutFiles.get(this.selectedIndex);

            try {
                OsdLayout layout = OsdLayoutIO.importLayout(file);
                this.onImport.accept(layout);
                this.onClose();
            } catch (Exception var3) {
                OsdLogger.layoutError("Failed to import layout from {}: {}", file.getFileName(), var3.getMessage());
            }
        } else {
            OsdLogger.builderWarn("No file selected for import");
        }
    }
}
