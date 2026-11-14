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
                // 1.1.8-PENDING: LoiteringMunitionEntity (ModEntities "loitering_munition"). 1.1.8 does:
                //   LoiteringMunitionEntity m = ModEntities.LOITERING_MUNITION.get().create(level, ...);
                //   m.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
                //   m.setup("lytui_drun", yaw, player.getUUID());
                //   m.setFuel(m.usesFuel() ? DroneFuel.get(stack) : 20.0);
                //   m.setRocketPods(RocketPodMask.installed(stack), RocketPodMask.loaded(stack));
                //   level.addFreshEntity(m); then shrinks the stack unless creative.
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

    /** Returns false while the loitering-munition entity is not ported, so the item is not used up. */
    private static boolean spawnMunition(Level level, BlockPos pos, Player player, ItemStack stack) {
        return false;
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
