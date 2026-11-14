package com.fpvdrone.item;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.Optional;

/**
 * 1.1.8 "Lytyi" long-range drone: placed on the ground as a loitering munition (it takes off by being pushed down a
 * runway), unlike the FP-1/Lancet/Orlan, which launch from a ramp. Carries its fuel and rocket pods as item data.
 */
public class LytuiDroneItem extends Item {
    public LytuiDroneItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        } else if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        } else {
            if (!level.isClientSide()) {
                BlockPos pos = context.getClickedPos().above();
                ItemStack stack = context.getItemInHand();
                if (!spawnMunition(level, pos, player, stack)) {
                    return InteractionResult.PASS;
                }

                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }

            return InteractionResult.SUCCESS;
        }
    }

    /** 1.1.8: the Lytui on the clicked block, facing the player's heading, with the item's fuel and rocket pods. */
    private static boolean spawnMunition(Level level, BlockPos pos, Player player, ItemStack stack) {
        com.fpvdrone.entity.LoiteringMunitionEntity munition =
            com.fpvdrone.registry.ModEntities.LOITERING_MUNITION.get().create(level, net.minecraft.world.entity.EntitySpawnReason.SPAWN_ITEM_USE);
        if (munition == null) {
            return false;
        }

        float yaw = player.getYRot();
        munition.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0F);
        munition.v5suI("lytui_drun", yaw, player.getUUID());
        munition.v5suI(munition.sTrE() ? DroneFuel.get(stack) : 20.0);
        munition.v5suI(RocketPodMask.installed(stack), RocketPodMask.loaded(stack));
        level.addFreshEntity(munition);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        UsageTooltipItem.appendUsage(this.getDescriptionId(), tooltip);
        DroneItem.appendFuel(stack, tooltip);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return DroneTooltipIcons.forItem(stack);
    }
}
