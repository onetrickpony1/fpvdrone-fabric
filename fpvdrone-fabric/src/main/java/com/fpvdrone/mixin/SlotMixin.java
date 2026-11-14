package com.fpvdrone.mixin;

import com.fpvdrone.FPVClientMod;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroneLinkManager;
import com.fpvdrone.item.FpvGogglesItem;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Slot.class})
public class SlotMixin {
    @Shadow
    @Final
    public Container container;
    @Shadow
    @Final
    private int slot;

    @Inject(
        method = {"mayPlace"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$onMayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof FpvGogglesItem) {
            if (this.container instanceof Inventory inventory) {
                if (this.slot == 5) {
                    Player player = inventory.player;
                    if (player != null) {
                        Optional<UUID> linkedDroneId = FpvGogglesItem.getLinkedDroneId(stack);
                        if (linkedDroneId.isEmpty()) {
                            cir.setReturnValue(false);
                        } else {
                            DroneEntity drone = null;
                            if (player.level().isClientSide()) {
                                Optional<DroneEntity> droneOpt = FPVClientMod.findDroneById(player, linkedDroneId.get());
                                if (droneOpt.isPresent()) {
                                    drone = droneOpt.get();
                                }
                            } else if (player.level() instanceof ServerLevel serverLevel) {
                                Entity entity = serverLevel.getEntity(linkedDroneId.get());
                                if (entity instanceof DroneEntity) {
                                    drone = (DroneEntity)entity;
                                }
                            }

                            if (drone != null) {
                                if (!drone.hasBattery()) {
                                    cir.setReturnValue(false);
                                    return;
                                }
                            } else if (!player.level().isClientSide() && !DroneLinkManager.isDroneLinked(linkedDroneId.get())) {
                                cir.setReturnValue(false);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
