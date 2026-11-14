package com.fpvdrone.item;

import com.fpvdrone.util.ItemData;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.util.Prediction;

/**
 * 1.1.8 20-litre fuel can for the combustion-engine drones. A can with no tag is full. Sneak + right-click in the air
 * pours it out. Filling and draining drones is done by the ramp and loitering-munition entities (they read and write
 * the level with {@link #getFuel} / {@link #setFuel}).
 */
public class FuelCanItem extends Item {
    public static final double CAPACITY_LITRES = 20.0;
    private static final String NBT_FUEL = "FuelLiters";

    public FuelCanItem(Properties properties) {
        super(properties);
    }

    public static double getFuel(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        return tag != null && tag.contains(NBT_FUEL) ? Mth.clamp(tag.getDoubleOr(NBT_FUEL, CAPACITY_LITRES), 0.0, CAPACITY_LITRES) : CAPACITY_LITRES;
    }

    public static void setFuel(ItemStack stack, double litres) {
        double value = Mth.clamp(litres, 0.0, CAPACITY_LITRES);
        ItemData.edit(stack, tag -> tag.putDouble(NBT_FUEL, value));
    }

    public static boolean isFull(ItemStack stack) {
        return getFuel(stack) >= 19.999999;
    }

    public static boolean isEmpty(ItemStack stack) {
        return getFuel(stack) <= 1.0E-6;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown() && !isEmpty(stack)) {
            if (!level.isClientSide()) {
                ItemStack emptied = stack.copy();
                emptied.setCount(1);
                setFuel(emptied, 0.0);
                if (stack.getCount() <= 1) {
                    player.setItemInHand(hand, emptied);
                } else {
                    stack.shrink(1);
                    if (!player.getInventory().add(emptied)) {
                        player.drop(emptied, false, Prediction.SERVER_ONLY);
                    }
                }

                level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.7F, 0.9F);
                player.sendOverlayMessage(Component.translatable("message.fpvdrone.fuel_poured_out"));
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stack.getCount() == 1 && !isFull(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        double ratio = getFuel(stack) / CAPACITY_LITRES;
        return Mth.clamp((int)Math.round(ratio * 13.0), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float ratio = (float)(getFuel(stack) / CAPACITY_LITRES);
        int r = 224;
        int g = (int)(96.0F + 80.0F * ratio);
        int b = 16;
        return r << 16 | g << 8 | b;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        UsageTooltipItem.appendUsage(this.getDescriptionId(), tooltip);
        tooltip.accept(
            Component.translatable("item.fpvdrone.fuel_can.level", String.format("%.1f", getFuel(stack)), String.format("%.0f", CAPACITY_LITRES))
                .withStyle(Style.EMPTY.withColor(16749622))
        );
    }
}
