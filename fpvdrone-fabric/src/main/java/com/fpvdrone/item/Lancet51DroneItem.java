package com.fpvdrone.item;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

/** 1.1.8 Lancet-51 loitering munition: cannot be placed by hand; it is installed on a launch ramp (the ramp entity takes the item). */
public class Lancet51DroneItem extends DroneItem {
    public Lancet51DroneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        UsageTooltipItem.appendUsage(this.getDescriptionId(), tooltip);
    }

    @Override
    protected String droneType() {
        return "lancet51";
    }
}
