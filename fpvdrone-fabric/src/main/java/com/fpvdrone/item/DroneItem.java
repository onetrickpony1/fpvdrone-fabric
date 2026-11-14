package com.fpvdrone.item;

import com.fpvdrone.util.ClientState;
import com.fpvdrone.util.ItemData;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntitySpawnReason;
import org.jspecify.annotations.Nullable;

import com.fpvdrone.client.BatteryData;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.registry.ModEntities;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class DroneItem extends Item {
    public static final int PLACEMENT_RANGE = 10;

    public DroneItem(Properties properties) {
        super(properties);
    }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        if (player == null) {
            return InteractionResult.FAIL;
        } else if (!this.isValidPlacement(level, clickedPos, player)) {
            return InteractionResult.FAIL;
        } else {
            if (!level.isClientSide()) {
                this.spawnDrone(level, clickedPos, player, context.getItemInHand());
                if (!player.getAbilities().instabuild) {
                    context.getItemInHand().shrink(1);
                }
            }

            return InteractionResult.SUCCESS;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        if (ClientState.hasShiftDown()) {
            tooltip.accept(Component.translatable("item.fpvdrone.drone.usage").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("item.fpvdrone.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        this.appendPayloadTooltip(stack, tooltip);
    }

    protected void appendPayloadTooltip(ItemStack stack, Consumer<Component> tooltip) {
        CompoundTag tag = ItemData.get(stack);
        if (tag != null) {
            if (tag.getBooleanOr("HasBattery", false) && tag.contains("BatteryData")) {
                BatteryData data = BatteryData.fromNbt(tag.getCompoundOrEmpty("BatteryData"));
                int color = batteryChargeColor(data);
                tooltip.accept(
                    Component.translatable(
                            "item.fpvdrone.drone.battery_info",
                            new Object[]{data.getCells() + "S", data.getRemainingMah(), data.getCapacityMah(), String.format("%.1f", data.getVoltage())}
                        )
                        .withStyle(Style.EMPTY.withColor(color))
                );
            } else {
                tooltip.accept(Component.translatable("item.fpvdrone.drone.no_battery").withStyle(Style.EMPTY.withColor(16749622)));
            }

            if (tag.getBooleanOr("HasRpg", false)) {
                tooltip.accept(Component.translatable("item.fpvdrone.drone.has_rpg").withStyle(Style.EMPTY.withColor(16733525)));
            }

            if (tag.getBooleanOr("HasHdCamera", false)) {
                tooltip.accept(Component.translatable("item.fpvdrone.drone.has_hd_camera").withStyle(Style.EMPTY.withColor(5636095)));
            }
        }
    }

    public boolean isBarVisible(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        if (tag != null && tag.getBooleanOr("HasBattery", false) && tag.contains("BatteryData")) {
            BatteryData data = BatteryData.fromNbt(tag.getCompoundOrEmpty("BatteryData"));
            return !data.isFullCharge();
        } else {
            return false;
        }
    }

    public int getBarWidth(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        if (tag != null && tag.contains("BatteryData")) {
            BatteryData data = BatteryData.fromNbt(tag.getCompoundOrEmpty("BatteryData"));
            float ratio = (float)data.getRemainingMah() / data.getCapacityMah();
            return Math.round(ratio * 13.0F);
        } else {
            return 0;
        }
    }

    public int getBarColor(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        if (tag != null && tag.contains("BatteryData")) {
            BatteryData data = BatteryData.fromNbt(tag.getCompoundOrEmpty("BatteryData"));
            float ratio = (float)data.getRemainingMah() / data.getCapacityMah();
            return Mth.hsvToRgb(ratio / 3.0F, 1.0F, 1.0F);
        } else {
            return 65280;
        }
    }

    private static int batteryChargeColor(BatteryData data) {
        float ratio = (float)data.getRemainingMah() / data.getCapacityMah();
        int r = 255;
        int g = (int)(85.0F + 63.0F * ratio);
        int b = (int)(85.0F + -31.0F * ratio);
        return r << 16 | g << 8 | b;
    }

    private boolean isValidPlacement(Level level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        if (!state.isSolidRender()) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.fpvdrone.invalid_placement").withStyle(Style.EMPTY.withColor(16749622)));
            }

            return false;
        } else {
            Vec3 playerPos = player.position();
            Vec3 targetPos = Vec3.atCenterOf(pos);
            double distance = playerPos.distanceTo(targetPos);
            if (distance > 10.0) {
                if (!level.isClientSide()) {
                    player.sendOverlayMessage(Component.translatable("message.fpvdrone.too_far").withStyle(Style.EMPTY.withColor(16749622)));
                }

                return false;
            } else {
                return true;
            }
        }
    }

    protected void spawnDrone(Level level, BlockPos pos, Player player, ItemStack stack) {
        DroneEntity drone = (DroneEntity)ModEntities.DRONE.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (drone != null) {
            BlockPos spawnPos = pos.above();
            drone.setPos(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            float yaw = player.getYRot();
            drone.setYRot(yaw);
            drone.yRotO = yaw;
            drone.yBodyRot = yaw;
            drone.yBodyRotO = yaw;
            drone.yHeadRot = yaw;
            drone.yHeadRotO = yaw;
            drone.setOwner(player.getUUID());
            CompoundTag tag = ItemData.get(stack);
            if (tag != null) {
                if (tag.getBooleanOr("HasBattery", false) && tag.contains("BatteryData")) {
                    BatteryData data = BatteryData.fromNbt(tag.getCompoundOrEmpty("BatteryData"));
                    drone.setBattery(data);
                    drone.setHasBattery(true);
                }

                if (tag.getBooleanOr("HasHdCamera", false)) {
                    drone.setHasHdCamera(true);
                }

                if (tag.getBooleanOr("HasRpg", false)) {
                    drone.setHasRpg(true);
                }
            }

            this.configureDrone(drone);
            level.addFreshEntity(drone);
        }
    }

    protected void configureDrone(DroneEntity drone) {
    }
}
