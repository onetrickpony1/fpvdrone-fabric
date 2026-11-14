package com.fpvdrone.item;

import com.fpvdrone.util.ClientState;
import com.fpvdrone.util.ItemData;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import org.jspecify.annotations.Nullable;

import com.fpvdrone.entity.DroneEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class BetaFlightItem extends Item {
    public BetaFlightItem(Properties properties) {
        super(properties);
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof DroneEntity drone) {
            if (player.level().isClientSide()) {
                this.openConfigScreen(drone);
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (ClientState.hasShiftDown()) {
            tooltip.accept(Component.translatable("item.fpvdrone.betaflight.usage").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("item.fpvdrone.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private void openConfigScreen(DroneEntity drone) {
        // Drone/OSD configuration screen not ported yet (GUI phase).
    }
}
