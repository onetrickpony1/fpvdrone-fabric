package com.fpvdrone.mixin;

import com.fpvdrone.item.FpvGogglesItem;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({InventoryMenu.class})
public class InventoryMenuMixin {
    @Inject(
        method = {"quickMoveStack"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockShiftClickGoggles(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        InventoryMenu menu = (InventoryMenu)(Object)this;
        if (index >= 0 && index < menu.slots.size()) {
            Slot sourceSlot = (Slot)menu.slots.get(index);
            if (sourceSlot != null && sourceSlot.hasItem()) {
                if (index != 5) {
                    ItemStack stack = sourceSlot.getItem();
                    if (stack.getItem() instanceof FpvGogglesItem) {
                        if (!fpvdrone$isValidGoggles(player, stack)) {
                            cir.setReturnValue(ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }

    @Unique
    private static boolean fpvdrone$isValidGoggles(Player player, ItemStack goggles) {
        Optional<UUID> linkedId = FpvGogglesItem.getLinkedDroneId(goggles);
        if (linkedId.isEmpty()) {
            player.sendOverlayMessage(Component.translatable("fpvdrone.goggles.equip_blocked.no_link").withStyle(Style.EMPTY.withColor(16749622)));
            return false;
        } else {
            return true;
        }
    }
}
