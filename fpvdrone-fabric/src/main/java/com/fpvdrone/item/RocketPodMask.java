package com.fpvdrone.item;

import com.fpvdrone.util.ItemData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 1.1.8 rocket pods stored on a fixed-wing drone item: one bit per pod station for "pod fitted" and for "rocket
 * loaded" (custom data "RocketPodInstalledMask" / "RocketsLoadedMask"). Used by the ramp and loitering-munition entities.
 */
public final class RocketPodMask {
    public static final String NBT_INSTALLED = "RocketPodInstalledMask";
    public static final String NBT_LOADED = "RocketsLoadedMask";

    private RocketPodMask() {
    }

    public static byte installed(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        return tag != null ? tag.getByteOr(NBT_INSTALLED, (byte)0) : 0;
    }

    public static byte loaded(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        return tag != null ? tag.getByteOr(NBT_LOADED, (byte)0) : 0;
    }

    public static void set(ItemStack stack, byte installed, byte loaded) {
        if (installed == 0 && loaded == 0) {
            if (ItemData.get(stack) != null) {
                ItemData.edit(stack, tag -> {
                    tag.remove(NBT_INSTALLED);
                    tag.remove(NBT_LOADED);
                });
            }
        } else {
            ItemData.edit(stack, tag -> {
                tag.putByte(NBT_INSTALLED, installed);
                tag.putByte(NBT_LOADED, loaded);
            });
        }
    }
}
