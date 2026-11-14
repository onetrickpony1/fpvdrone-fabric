package com.fpvdrone.mixin;

import com.fpvdrone.item.FpvGogglesItem;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({AbstractContainerMenu.class})
public abstract class ContainerClickMixin {
    @Inject(
        method = {"clicked"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$blockDragDropGoggles(int slotId, int button, ContainerInput clickType, Player player, CallbackInfo ci) {
        if ((Object)this instanceof InventoryMenu) {
            if (slotId == 5) {
                ItemStack toCheck = ItemStack.EMPTY;
                if (clickType == ContainerInput.PICKUP || clickType == ContainerInput.PICKUP_ALL) {
                    ItemStack carried = ((AbstractContainerMenu)(Object)this).getCarried();
                    if (carried.isEmpty()) {
                        return;
                    }

                    toCheck = carried;
                } else if (clickType == ContainerInput.SWAP) {
                    toCheck = player.getInventory().getItem(button);
                }

                if (!toCheck.isEmpty() && toCheck.getItem() instanceof FpvGogglesItem) {
                    Optional<UUID> linkedId = FpvGogglesItem.getLinkedDroneId(toCheck);
                    if (linkedId.isEmpty()) {
                        player.sendOverlayMessage(Component.translatable("fpvdrone.goggles.equip_blocked.no_link").withStyle(Style.EMPTY.withColor(16749622)));
                        ci.cancel();
                    }
                }
            }
        }
    }
}
