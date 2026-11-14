package com.fpvdrone.client;

import com.fpvdrone.item.ItemIconTooltip;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/** 1.1.8 (Ie7jNk3S9): draws {@link ItemIconTooltip} rows, 18 px each: orange label, item icon, "[item name]". */
public class ItemIconTooltipRenderer implements ClientTooltipComponent {
    private static final int ROW_HEIGHT = 18;
    private static final int ICON = 16;
    private static final int GAP = 4;
    private static final int TEXT_Y = 4;
    private static final int LABEL_COLOR = 16749622;
    private final List<ItemIconTooltip.Row> rows;

    public ItemIconTooltipRenderer(ItemIconTooltip tooltip) {
        this.rows = tooltip.rows();
    }

    @Override
    public int getHeight(Font font) {
        return ROW_HEIGHT * this.rows.size();
    }

    @Override
    public int getWidth(Font font) {
        int labelWidth = this.labelColumnWidth(font);
        int width = 0;
        for (ItemIconTooltip.Row row : this.rows) {
            int w = row.isText() ? font.width(label(row)) : ICON + GAP + labelWidth + GAP + font.width(name(row));
            width = Math.max(width, w);
        }

        return width;
    }

    @Override
    public void extractImage(Font font, int x, int y, int w, int h, GuiGraphicsExtractor graphics) {
        int labelWidth = this.labelColumnWidth(font);
        for (int i = 0; i < this.rows.size(); i++) {
            ItemIconTooltip.Row row = this.rows.get(i);
            int rowY = y + i * ROW_HEIGHT;
            if (row.isText()) {
                graphics.text(font, label(row), x, rowY + TEXT_Y, 0xFFFFFFFF);
            } else {
                graphics.item(row.stack(), x, rowY);
                graphics.text(font, label(row), x + ICON + GAP, rowY + TEXT_Y, 0xFFFFFFFF);
                graphics.text(font, name(row), x + ICON + GAP + labelWidth + GAP, rowY + TEXT_Y, 0xFFFFFFFF);
            }
        }
    }

    private static Component name(ItemIconTooltip.Row row) {
        return Component.literal("[")
            .withStyle(ChatFormatting.WHITE)
            .append(row.stack().getHoverName().copy().withStyle(ChatFormatting.WHITE))
            .append(Component.literal("]").withStyle(ChatFormatting.WHITE));
    }

    private int labelColumnWidth(Font font) {
        int width = Math.max(font.width(Component.translatable("tooltip.fpvdrone.required")), font.width(Component.translatable("tooltip.fpvdrone.optional")));
        for (ItemIconTooltip.Row row : this.rows) {
            if (!row.isText()) {
                width = Math.max(width, font.width(label(row)));
            }
        }

        return width;
    }

    private static Component label(ItemIconTooltip.Row row) {
        return row.isText()
            ? Component.translatable(row.labelKey(), row.count()).withStyle(ChatFormatting.GRAY)
            : Component.translatable(row.labelKey()).withStyle(Style.EMPTY.withColor(LABEL_COLOR));
    }
}
