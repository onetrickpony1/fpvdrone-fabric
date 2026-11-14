package com.fpvdrone.item;

import java.util.List;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 tooltip block (Record_Fpp72u75): rows of "&lt;label&gt; [icon] [item name]", e.g. "Required: [battery]",
 * "Installed: [RPG-7]". A row without a stack is a plain text row ("+3 more"). Drawn by
 * {@link com.fpvdrone.client.ItemIconTooltipRenderer}.
 */
public record ItemIconTooltip(List<Row> rows) implements TooltipComponent {
    public record Row(@Nullable ItemStack stack, String labelKey, int count) {
        public boolean isText() {
            return this.stack == null;
        }
    }
}
