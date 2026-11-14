package com.fpvdrone.item;

import com.fpvdrone.util.ClientState;
import com.fpvdrone.util.ItemData;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import org.jspecify.annotations.Nullable;

import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroneLinkManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class RemoteControlItem extends Item {
    public static final String NBT_LINKED_DRONE = "LinkedDroneUUID";
    public static final String NBT_CHANNEL = "VideoChannel";

    public RemoteControlItem(Properties properties) {
        super(properties);
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof Player player) {
            Optional<UUID> linkedId = getLinkedDroneId(stack);
            if (!linkedId.isEmpty()) {
                UUID droneId = linkedId.get();
                if (!DroneLinkManager.isDroneLinked(droneId) && level instanceof ServerLevel serverLevel) {
                    Entity found = serverLevel.getEntity(droneId);
                    if (found instanceof DroneEntity drone && !drone.isRemoved() && drone.isAlive()) {
                        DroneLinkManager.linkDrone(droneId, player.getUUID());
                    } else if (found != null) {
                        clearLink(stack);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (ClientState.hasShiftDown()) {
            tooltip.accept(Component.translatable("item.fpvdrone.remote_control.usage").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("item.fpvdrone.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        if (isLinked(stack)) {
            int ch = getChannel(stack);
            tooltip.accept(Component.translatable("item.fpvdrone.remote_control.linked", new Object[]{"R" + ch}).withStyle(Style.EMPTY.withColor(16749622)));
        } else {
            tooltip.accept(Component.translatable("item.fpvdrone.remote_control.not_linked").withStyle(Style.EMPTY.withColor(16749622)));
        }
    }

    public static Optional<UUID> getLinkedDroneId(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        return ItemData.getUUID(tag, "LinkedDroneUUID");
    }

    public static void setLinkedDroneId(ItemStack stack, UUID droneId) {
        ItemData.edit(stack, tag -> ItemData.putUUID(tag, "LinkedDroneUUID", droneId));
    }

    public static void clearLink(ItemStack stack) {
        ItemData.edit(stack, tag -> tag.remove("LinkedDroneUUID"));
    }

    public static boolean isLinked(ItemStack stack) {
        return getLinkedDroneId(stack).isPresent();
    }

    public static void setChannel(ItemStack stack, int channel) {
        ItemData.edit(stack, tag -> tag.putByte("VideoChannel", (byte)channel));
    }

    public static int getChannel(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        return tag != null ? tag.getByteOr("VideoChannel", (byte)1) : 1;
    }
}
