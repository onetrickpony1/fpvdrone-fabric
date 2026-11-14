package com.fpvdrone.gui.list;

import com.fpvdrone.client.osd.OsdElement;
import com.fpvdrone.client.osd.OsdElementRegistry;
import com.fpvdrone.client.osd.OsdElementType;
import com.fpvdrone.client.osd.OsdLayout;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

public class OsdElementList extends ContainerObjectSelectionList<OsdElementList.Entry> {
    private static final int COLOR_PRIMARY = -17664;
    private static final int COLOR_TEXT = -855310;
    private static final int COLOR_CHECK_BG = -14013910;
    private static final int COLOR_CHECK_BORDER = -11184811;
    private static final int COLOR_CHECK_MARK = -17664;
    private static final int ITEM_HEIGHT = 14;
    private static final int CATEGORY_EXTRA_TOP = 2;
    private static final int CHECK_SIZE = 8;
    private final OsdLayout layout;
    private final Runnable onVisibilityChange;

    public OsdElementList(Screen parent, int x, int y, int width, int height, OsdLayout layout, Runnable onVisibilityChange) {
        super(Minecraft.getInstance(), width, height, y, 14);
        this.setX(x);
        this.layout = layout;
        this.onVisibilityChange = onVisibilityChange;
        this.buildEntries();
    }

    private void buildEntries() {
        this.clearEntries();
        Map<String, List<OsdElementType>> byCategory = OsdElementRegistry.getByCategory();

        for (Map.Entry<String, List<OsdElementType>> cat : byCategory.entrySet()) {
            this.addEntry(new OsdElementList.CategoryHeader(cat.getKey()));

            for (OsdElementType type : cat.getValue()) {
                OsdElement elem = this.layout.getElement(type.getName());
                if (elem != null) {
                    this.addEntry(new OsdElementList.ElementEntry(type, elem));
                }
            }
        }
    }

    public void rebuild() {
        double scroll = this.scrollAmount();
        this.buildEntries();
        this.setScrollAmount(scroll);
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - 6;
    }

    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    // Rows start at the list's left edge; the content area adds the usual 2 px padding.
    @Override
    public int getRowLeft() {
        return this.getX();
    }

    // The list is drawn on top of the OSD builder panel: no menu background or separators.
    @Override
    protected void extractListBackground(GuiGraphicsExtractor graphics) {
    }

    @Override
    protected void extractListSeparators(GuiGraphicsExtractor graphics) {
    }

    private class CategoryHeader extends OsdElementList.Entry {
        private final String label;

        CategoryHeader(String category) {
            this.label = category.toUpperCase();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            int top = this.getContentY();
            int left = this.getContentX();
            int width = OsdElementList.this.getRowWidth();
            int height = this.getContentHeight();
            Font font = Minecraft.getInstance().font;
            int textY = top + (height - 9) / 2 + 2;
            graphics.text(font, this.label, left + 1, textY, -17664, true);
        }

        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
    }

    private class ElementEntry extends OsdElementList.Entry {
        private final OsdElementType type;
        private final OsdElement element;

        ElementEntry(OsdElementType type, OsdElement element) {
            this.type = type;
            this.element = element;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            int top = this.getContentY();
            int left = this.getContentX();
            int width = OsdElementList.this.getRowWidth();
            int height = this.getContentHeight();
            Font font = Minecraft.getInstance().font;
            int cbX = left + 1;
            int cbY = top + (height - 8) / 2;
            graphics.fill(cbX, cbY, cbX + 8, cbY + 8, -11184811);
            graphics.fill(cbX + 1, cbY + 1, cbX + 8 - 1, cbY + 8 - 1, -14013910);
            if (this.element.isVisible()) {
                graphics.fill(cbX + 2, cbY + 2, cbX + 8 - 2, cbY + 8 - 2, -17664);
            }

            int textX = cbX + 8 + 3;
            int textY = top + (height - 9) / 2 + 1;
            int maxTextW = left + width - textX;
            String name = this.type.getDisplayName();
            if (font.width(name) > maxTextW) {
                while (name.length() > 1 && font.width(name + "..") > maxTextW) {
                    name = name.substring(0, name.length() - 1);
                }

                name = name + "..";
            }

            graphics.text(font, name, textX, textY, -855310, true);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (event.button() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT) {
                this.element.setVisible(!this.element.isVisible());
                OsdElementList.this.onVisibilityChange.run();
                return true;
            } else {
                return false;
            }
        }

        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
    }

    public abstract static class Entry extends net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry<OsdElementList.Entry> {
    }
}
