package com.fpvdrone.util;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;

/** The item's old NBT tag now lives in the minecraft:custom_data component; these helpers keep the original key layout. */
public final class ItemData {
    private ItemData() {
    }

    /** A copy of the stack's custom data, or null when it has none (like the old getTag()). */
    public static @Nullable CompoundTag get(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? null : data.copyTag();
    }

    /** Edits the stack's custom data in place (replaces getOrCreateTag() mutation). */
    public static void edit(ItemStack stack, Consumer<CompoundTag> editor) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, editor);
    }

    public static boolean hasUUID(@Nullable CompoundTag tag, String key) {
        return tag != null && tag.read(key, UUIDUtil.CODEC).isPresent();
    }

    public static Optional<UUID> getUUID(@Nullable CompoundTag tag, String key) {
        return tag == null ? Optional.empty() : tag.read(key, UUIDUtil.CODEC);
    }

    public static void putUUID(CompoundTag tag, String key, UUID value) {
        tag.store(key, UUIDUtil.CODEC, value);
    }
}
