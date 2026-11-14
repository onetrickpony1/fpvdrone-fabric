package com.fpvdrone.item;

import com.fpvdrone.util.ClientState;
import com.fpvdrone.util.ItemData;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import org.jspecify.annotations.Nullable;

import com.fpvdrone.client.BatteryData;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class BatteryItem extends Item {
    private static final String NBT_BATTERY = "BatteryData";

    public BatteryItem(Properties properties) {
        super(properties);
    }

    public static BatteryData getBatteryData(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        return tag != null && tag.contains("BatteryData") ? BatteryData.fromNbt(tag.getCompoundOrEmpty("BatteryData")) : BatteryData.defaults();
    }

    public static void setBatteryData(ItemStack stack, BatteryData data) {
        ItemData.edit(stack, tag -> tag.put("BatteryData", data.toNbt()));
    }

    public boolean isBarVisible(ItemStack stack) {
        BatteryData data = getBatteryData(stack);
        return !data.isFullCharge();
    }

    public int getBarWidth(ItemStack stack) {
        BatteryData data = getBatteryData(stack);
        float ratio = (float)data.getRemainingMah() / data.getCapacityMah();
        return Math.round(ratio * 13.0F);
    }

    public int getBarColor(ItemStack stack) {
        BatteryData data = getBatteryData(stack);
        float ratio = (float)data.getRemainingMah() / data.getCapacityMah();
        return Mth.hsvToRgb(ratio / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (ClientState.hasShiftDown()) {
            tooltip.accept(Component.translatable("item.fpvdrone.battery.usage").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("item.fpvdrone.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        BatteryData data = getBatteryData(stack);
        tooltip.accept(
            Component.literal(
                    String.format(
                        "%dS • %d/%dmAh • %.1fV • %dC", data.getCells(), data.getRemainingMah(), data.getCapacityMah(), data.getVoltage(), data.getCRating()
                    )
                )
                .withStyle(Style.EMPTY.withColor(chargeColor(data)))
        );
    }

    public static int chargeColor(BatteryData data) {
        float ratio = (float)data.getRemainingMah() / data.getCapacityMah();
        int r = 255;
        int g = (int)(85.0F + 63.0F * ratio);
        int b = (int)(85.0F + -31.0F * ratio);
        return r << 16 | g << 8 | b;
    }
}
