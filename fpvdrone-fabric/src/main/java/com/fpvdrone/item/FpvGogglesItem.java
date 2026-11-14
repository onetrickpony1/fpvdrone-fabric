package com.fpvdrone.item;

import com.fpvdrone.FPVClientMod;
import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.entity.DroneLinkManager;
import com.fpvdrone.util.ClientState;
import com.fpvdrone.util.ItemData;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.util.GeckoLibUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Head-slot goggles (equippable component set in ModItems); the Forge version was a leather ArmorItem with a GeckoLib model.
 * Worn goggles are drawn by {@link com.fpvdrone.client.renderer.FpvGogglesArmorRenderer} through GeckoLib's armour hook.
 */
public class FpvGogglesItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final String NBT_LINKED_DRONE = "LinkedDroneUUID";
    public static final String NBT_CHANNEL = "VideoChannel";
    public static final String NBT_DRONE_POS_X = "DroneLastPosX";
    public static final String NBT_DRONE_POS_Y = "DroneLastPosY";
    public static final String NBT_DRONE_POS_Z = "DroneLastPosZ";
    private static final String NBT_LINKED_CHANNELS = "LinkedChannels";
    private static final String NBT_LINKED_DRONES = "LinkedDrones";

    public FpvGogglesItem(Properties properties) {
        super(properties);
    }

    /** Client only: GeckoLib asks for this once, on the physical client, to find the worn-goggles renderer. */
    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private com.fpvdrone.client.renderer.@Nullable FpvGogglesArmorRenderer renderer;

            @Override
            public @Nullable GeoArmorRenderer<?, ?> getGeoArmorRenderer(ItemStack itemStack, EquipmentSlot equipmentSlot) {
                if (this.renderer == null) {
                    this.renderer = new com.fpvdrone.client.renderer.FpvGogglesArmorRenderer();
                }

                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public static boolean canEquip(ItemStack stack, EquipmentSlot slot, Entity entity) {
        if (slot == EquipmentSlot.HEAD && entity instanceof Player player) {
            Optional<UUID> linkedId = getLinkedDroneId(stack);
            if (linkedId.isEmpty()) {
                return false;
            } else if (player.level().isClientSide()) {
                Optional<DroneEntity> droneOpt = FPVClientMod.findDroneById(player, linkedId.get());
                return droneOpt.isPresent() ? droneOpt.get().hasBattery() : true;
            } else {
                return player.level() instanceof ServerLevel serverLevel && serverLevel.getEntity(linkedId.get()) instanceof DroneEntity drone
                    ? drone.hasBattery()
                    : DroneLinkManager.isDroneLinked(linkedId.get());
            }
        } else {
            return false;
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<UUID> linkedId = getLinkedDroneId(stack);
        if (linkedId.isEmpty()) {
            if (level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("item.fpvdrone.fpv_goggles.not_linked").withStyle(Style.EMPTY.withColor(16749622)));
            }

            return InteractionResult.FAIL;
        } else {
            DroneEntity drone = null;
            if (level.isClientSide()) {
                Optional<DroneEntity> droneOpt = FPVClientMod.findDroneById(player, linkedId.get());
                if (droneOpt.isPresent()) {
                    drone = droneOpt.get();
                }
            } else if (level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(linkedId.get());
                if (entity instanceof DroneEntity) {
                    drone = (DroneEntity)entity;
                }
            }

            if (drone != null) {
                if (!drone.hasBattery()) {
                    if (level.isClientSide()) {
                        player.sendOverlayMessage(Component.translatable("item.fpvdrone.fpv_goggles.no_battery").withStyle(Style.EMPTY.withColor(16749622)));
                    }

                    return InteractionResult.FAIL;
                }
            } else if (!level.isClientSide() && !DroneLinkManager.isDroneLinked(linkedId.get())) {
                player.sendOverlayMessage(Component.translatable("item.fpvdrone.fpv_goggles.drone_not_found").withStyle(Style.EMPTY.withColor(16749622)));
                return InteractionResult.FAIL;
            }

            return super.use(level, player, hand);
        }
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof Player player) {
            migrateOldFormat(stack);
            Map<Integer, UUID> drones = getLinkedDrones(stack);
            if (!drones.isEmpty()) {
                UUID activeDrone = FPVMod.getServerActiveDroneId(player.getUUID());
                boolean changed = false;
                Iterator<Entry<Integer, UUID>> it = drones.entrySet().iterator();

                while (it.hasNext()) {
                    Entry<Integer, UUID> entry = it.next();
                    UUID droneId = entry.getValue();
                    if (!DroneLinkManager.isDroneLinked(droneId)
                        && (activeDrone == null || !activeDrone.equals(droneId))
                        && level instanceof ServerLevel serverLevel) {
                        Entity found = serverLevel.getEntity(droneId);
                        if (found instanceof DroneEntity drone && !drone.isRemoved() && drone.isAlive()) {
                            DroneLinkManager.linkDrone(droneId, player.getUUID());
                        } else if (found != null) {
                            it.remove();
                            changed = true;
                        } else if (!DroneLinkManager.isDroneLinked(droneId)) {
                            it.remove();
                            changed = true;
                        }
                    }
                }

                if (changed) {
                    saveLinkedDrones(stack, drones);
                    int currentCh = getChannel(stack);
                    if (!drones.containsKey(currentCh) && !drones.isEmpty()) {
                        Entry<Integer, UUID> first = new TreeMap<>(drones).firstEntry();
                        setChannel(stack, first.getKey());
                        setLinkedDroneId(stack, first.getValue());
                    } else if (drones.isEmpty()) {
                        clearLink(stack);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        Level level = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Minecraft.getInstance().level : null;
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && Minecraft.getInstance().hasShiftDown()) {
            tooltip.accept(Component.translatable("item.fpvdrone.fpv_goggles.usage", ClientState.exitKeyLabel()).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("item.fpvdrone.hold_shift").withStyle(ChatFormatting.DARK_GRAY));
        }

        Map<Integer, UUID> drones = getLinkedDrones(stack);
        if (!drones.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int i = 0;

            for (int ch : new TreeMap<>(drones).keySet()) {
                if (i > 0) {
                    sb.append(", ");
                }

                sb.append("R").append(ch);
                i++;
            }

            tooltip.accept(Component.translatable("item.fpvdrone.fpv_goggles.linked", new Object[]{sb.toString()}).withStyle(Style.EMPTY.withColor(16749622)));
            Optional<UUID> activeId = getLinkedDroneId(stack);
            if (activeId.isPresent() && level != null) {
                DroneEntity drone = null;
                if (level.isClientSide()) {
                    Optional<DroneEntity> droneOpt = FPVClientMod.findDroneByIdForClient(activeId.get());
                    if (droneOpt.isPresent()) {
                        drone = droneOpt.get();
                    }
                } else if (level instanceof ServerLevel serverLevel) {
                    Entity entity = serverLevel.getEntity(activeId.get());
                    if (entity instanceof DroneEntity) {
                        drone = (DroneEntity)entity;
                    }
                }

                if (drone != null) {
                    if (!drone.hasBattery()) {
                        tooltip.accept(Component.translatable("item.fpvdrone.fpv_goggles.no_battery").withStyle(Style.EMPTY.withColor(16749622).withBold(true)));
                    } else {
                        tooltip.accept(Component.translatable("item.fpvdrone.fpv_goggles.ready").withStyle(Style.EMPTY.withColor(16749622)));
                    }
                } else {
                    tooltip.accept(Component.translatable("item.fpvdrone.fpv_goggles.drone_not_found").withStyle(Style.EMPTY.withColor(16749622)));
                }
            }
        } else {
            tooltip.accept(Component.translatable("item.fpvdrone.fpv_goggles.not_linked").withStyle(Style.EMPTY.withColor(16749622)));
        }
    }

    public static Optional<UUID> getLinkedDroneId(ItemStack stack) {
        return ItemData.getUUID(ItemData.get(stack), "LinkedDroneUUID");
    }

    public static void setLinkedDroneId(ItemStack stack, UUID droneId) {
        ItemData.edit(stack, tag -> ItemData.putUUID(tag, "LinkedDroneUUID", droneId));
    }

    public static void clearLink(ItemStack stack) {
        ItemData.edit(stack, tag -> {
            tag.remove("LinkedDroneUUID");
            tag.remove("LinkedChannels");
            tag.remove("LinkedDrones");
        });
    }

    public static boolean isLinked(ItemStack stack) {
        Map<Integer, UUID> drones = getLinkedDrones(stack);
        return !drones.isEmpty() ? true : getLinkedDroneId(stack).isPresent();
    }

    public static void setLastDronePosition(ItemStack stack, double x, double y, double z) {
        ItemData.edit(stack, tag -> {
            tag.putDouble("DroneLastPosX", x);
            tag.putDouble("DroneLastPosY", y);
            tag.putDouble("DroneLastPosZ", z);
        });
    }

    public static void setChannel(ItemStack stack, int channel) {
        ItemData.edit(stack, tag -> tag.putByte("VideoChannel", (byte)channel));
    }

    public static int getChannel(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        return tag != null ? tag.getByteOr("VideoChannel", (byte)1) : 1;
    }

    public static Map<Integer, UUID> getLinkedDrones(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        if (tag == null) {
            return new HashMap<>();
        } else if (tag.getCompound("LinkedDrones").isEmpty()) {
            return new HashMap<>();
        } else {
            CompoundTag map = tag.getCompoundOrEmpty("LinkedDrones");
            Map<Integer, UUID> result = new HashMap<>();

            for (String key : map.keySet()) {
                try {
                    int channel = Integer.parseInt(key);
                    UUID uuid = UUID.fromString(map.getStringOr(key, ""));
                    result.put(channel, uuid);
                } catch (Exception var8) {
                }
            }

            return result;
        }
    }

    public static void saveLinkedDrones(ItemStack stack, Map<Integer, UUID> drones) {
        CompoundTag map = new CompoundTag();

        for (Entry<Integer, UUID> entry : drones.entrySet()) {
            map.putString(String.valueOf(entry.getKey()), entry.getValue().toString());
        }

        ItemData.edit(stack, tag -> tag.put("LinkedDrones", map));
    }

    public static void linkDroneOnChannel(ItemStack stack, int channel, UUID droneId) {
        Map<Integer, UUID> drones = getLinkedDrones(stack);
        drones.put(channel, droneId);
        saveLinkedDrones(stack, drones);
        setLinkedDroneId(stack, droneId);
        setChannel(stack, channel);
    }

    public static void unlinkChannel(ItemStack stack, int channel) {
        Map<Integer, UUID> drones = getLinkedDrones(stack);
        drones.remove(channel);
        saveLinkedDrones(stack, drones);
    }

    public static Optional<UUID> getDroneOnChannel(ItemStack stack, int channel) {
        Map<Integer, UUID> drones = getLinkedDrones(stack);
        UUID uuid = drones.get(channel);
        return Optional.ofNullable(uuid);
    }

    public static List<Integer> getAllLinkedChannels(ItemStack stack) {
        Map<Integer, UUID> drones = getLinkedDrones(stack);
        if (!drones.isEmpty()) {
            return new ArrayList<>(new TreeMap<>(drones).keySet());
        } else {
            CompoundTag tag = ItemData.get(stack);
            if (tag != null && tag.getList("LinkedChannels").isPresent()) {
                ListTag list = tag.getListOrEmpty("LinkedChannels");
                List<Integer> channels = new ArrayList<>();

                for (int i = 0; i < list.size(); i++) {
                    channels.add(list.get(i) instanceof net.minecraft.nbt.NumericTag n ? n.intValue() : 0);
                }

                return channels;
            } else {
                List<Integer> single = new ArrayList<>();
                single.add(getChannel(stack));
                return single;
            }
        }
    }

    @Deprecated
    public static void addLinkedChannel(ItemStack stack, int channel) {
    }

    public static void clearLinkedChannels(ItemStack stack) {
        ItemData.edit(stack, tag -> {
            tag.remove("LinkedChannels");
            tag.remove("LinkedDrones");
        });
    }

    private static void migrateOldFormat(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        if (tag != null) {
            if (tag.getCompound("LinkedDrones").isEmpty()) {
                if (ItemData.hasUUID(tag, "LinkedDroneUUID")) {
                    UUID droneId = ItemData.getUUID(tag, "LinkedDroneUUID").get();
                    int channel = getChannel(stack);
                    Map<Integer, UUID> drones = new HashMap<>();
                    drones.put(channel, droneId);
                    saveLinkedDrones(stack, drones);
                    ItemData.edit(stack, t -> t.remove("LinkedChannels"));
                }
            }
        }
    }

}
