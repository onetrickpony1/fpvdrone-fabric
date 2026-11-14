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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class WeightItem extends Item {
    private static final float WEIGHT_INCREMENT = 250.0F;

    public WeightItem(Properties properties) {
        super(properties);
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof DroneEntity drone) {
            if (!player.level().isClientSide()) {
                drone.addWeight(250.0F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }

                player.sendOverlayMessage(Component.translatable("item.fpvdrone.weight.applied").withStyle(Style.EMPTY.withColor(16749622)));
            }

            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }
}
