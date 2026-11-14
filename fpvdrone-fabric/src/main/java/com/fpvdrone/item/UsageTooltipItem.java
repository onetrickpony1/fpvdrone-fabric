package com.fpvdrone.item;

import com.fpvdrone.util.ClientState;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Port helper: the "hold Shift" / "&lt;item&gt;.usage" tooltip that most 1.1.8 items repeat word for word
 * (item.fpvdrone.&lt;id&gt;.usage in grey with Shift held, item.fpvdrone.hold_shift otherwise).
 */
public class UsageTooltipItem extends Item {
    public UsageTooltipItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        appendUsage(this.getDescriptionId(), tooltip);
    }

    public static void appendUsage(String descriptionId, Consumer<Component> tooltip, Object... args) {
        if (ClientState.hasShiftDown()) {
            tooltip.accept(Component.translatable(descriptionId + ".usage", args).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("item.fpvdrone.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
