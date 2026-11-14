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
import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
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

    /**
     * 1.1.8 dropped the battery / RPG text lines: the fitted battery and projectile now show as item icons marked
     * "Installed" (see {@link #getTooltipImage}). Only the HD camera (a port addition) keeps a text line.
     */
    protected void appendPayloadTooltip(ItemStack stack, Consumer<Component> tooltip) {
        CompoundTag tag = ItemData.get(stack);
        if (tag != null && tag.getBooleanOr("HasHdCamera", false)) {
            tooltip.accept(Component.translatable("item.fpvdrone.drone.has_hd_camera").withStyle(Style.EMPTY.withColor(5636095)));
        }
    }

    /** 1.1.8: fuel line of the combustion-engine drones (FP-1, Lytyi, Orlan-10), shown only when the tank is not empty. */
    public static void appendFuel(ItemStack stack, Consumer<Component> tooltip) {
        if (DroneFuel.hasFuel(stack)) {
            tooltip.accept(
                Component.translatable("item.fpvdrone.drone.fuel", String.format("%.1f", DroneFuel.get(stack)), String.format("%.0f", DroneFuel.TANK_LITRES))
                    .withStyle(Style.EMPTY.withColor(16749622))
            );
        }
    }

    /** 1.1.8: the compatible batteries and payloads as item icons (required / optional / installed), from the drone's data file. */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return DroneTooltipIcons.forItem(stack);
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
                    String projectileId = tag.getStringOr("ProjectileId", "");
                    if (!projectileId.isEmpty()) {
                        drone.v5suI(projectileId); // 1.1.8: fit this projectile
                    } else {
                        drone.setHasRpg(true);
                    }
                }
            }

            // 1.1.8: the drone definition ("default", "thermal", "mavic", ...), then the bomb bay load.
            drone.ibzfxic(tag != null && tag.contains("DroneType") ? tag.getStringOr("DroneType", "") : this.droneType());
            if (tag != null && tag.contains("BombsLoaded") && !drone.hasRpg()) {
                drone.v5suI(tag.getByteOr("BombsLoaded", (byte)0));
            }

            this.configureDrone(drone);
            level.addFreshEntity(drone);
        }
    }

    protected void configureDrone(DroneEntity drone) {
    }

    /** 1.1.8: id of the drone definition this item places when the stack carries no "DroneType" of its own. */
    protected String droneType() {
        return "default";
    }

    public String getDroneType() {
        return this.droneType();
    }
}
