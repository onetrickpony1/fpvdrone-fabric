package com.fpvdrone.item;

import com.fpvdrone.util.ItemData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * 1.1.8 fuel stored on a combustion-engine drone item (FP-1, Lytyi, Orlan-10): litres in custom data "FuelLiters",
 * 0 to 20. Unlike the fuel can, a drone item with no tag is empty. Used by the ramp and loitering-munition entities.
 */
public final class DroneFuel {
    public static final String NBT_FUEL = "FuelLiters";
    public static final double TANK_LITRES = 20.0;

    private DroneFuel() {
    }

    public static double get(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        return tag != null && tag.contains(NBT_FUEL) ? Mth.clamp(tag.getDoubleOr(NBT_FUEL, 0.0), 0.0, TANK_LITRES) : 0.0;
    }

    public static void set(ItemStack stack, double litres) {
        double value = Mth.clamp(litres, 0.0, TANK_LITRES);
        if (value <= 1.0E-6) {
            if (ItemData.get(stack) != null) {
                ItemData.edit(stack, tag -> tag.remove(NBT_FUEL));
            }
        } else {
            ItemData.edit(stack, tag -> tag.putDouble(NBT_FUEL, value));
        }
    }

    public static boolean hasFuel(ItemStack stack) {
        return get(stack) > 1.0E-6;
    }
}
