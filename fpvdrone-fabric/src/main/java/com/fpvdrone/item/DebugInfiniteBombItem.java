package com.fpvdrone.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * 1.1.8 debug item (/give only, not in the creative tab): right-clicking a bomb-bay drone fills every station and is
 * never used up. The filling is done by the drone entity.
 */
public class DebugInfiniteBombItem extends Item {
    public DebugInfiniteBombItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.fpvdrone.debug_infinite_bomb.usage").withStyle(ChatFormatting.RED));
    }
}
