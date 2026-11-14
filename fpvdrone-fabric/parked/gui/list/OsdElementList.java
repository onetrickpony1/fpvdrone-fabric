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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;

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
        super(Minecraft.getInstance(), width, height, y, y + height, 14);
        this.x0 = x;
        this.x1 = x + width;
        this.layout = layout;
        this.onVisibilityChange = onVisibilityChange;
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
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
        double scroll = this.getScrollAmount();
        this.buildEntries();
        this.setScrollAmount(scroll);
    }

    protected int getScrollbarPosition() {
        return this.x1 - 6;
    }

    public int getRowWidth() {
        return this.x1 - this.x0 - 10;
    }

    public int getRowLeft() {
        return this.x0 + 2;
    }

    protected void renderDecorations(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private static class CategoryHeader extends OsdElementList.Entry {
        private final String label;

        CategoryHeader(String category) {
            this.label = category.toUpperCase();
        }

        public void render(
            GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks
        ) {
            Font font = Minecraft.getInstance().font;
            int textY = top + (height - 9) / 2 + 2;
            graphics.drawString(font, this.label, left + 1, textY, -17664, true);
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

        public void render(
            GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks
        ) {
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

            graphics.drawString(font, name, textX, textY, -855310, true);
        }

        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
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
