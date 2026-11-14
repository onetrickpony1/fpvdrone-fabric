package com.fpvdrone.registry;

import com.fpvdrone.item.AntennaItem;
import com.fpvdrone.item.BatteryItem;
import com.fpvdrone.item.BetaFlightItem;
import com.fpvdrone.item.BombHolderItem;
import com.fpvdrone.item.BombItem;
import com.fpvdrone.item.CameraItem;
import com.fpvdrone.item.DroneItem;
import com.fpvdrone.item.ElectricMotorItem;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.item.PivotControllerItem;
import com.fpvdrone.item.ProcessorItem;
import com.fpvdrone.item.PropellerItem;
import com.fpvdrone.item.RemoteControlItem;
import com.fpvdrone.item.Rpg7Item;
import com.fpvdrone.item.ThermalDroneItem;
import com.fpvdrone.item.ThermalItem;
import com.fpvdrone.item.WeightItem;
import com.fpvdrone.item.DebugInfiniteBombItem;
import com.fpvdrone.item.DjiControllerItem;
import com.fpvdrone.item.Explosive40mmItem;
import com.fpvdrone.item.Fp1DroneItem;
import com.fpvdrone.item.FuelCanItem;
import com.fpvdrone.item.Lancet51DroneItem;
import com.fpvdrone.item.LytuiDroneItem;
import com.fpvdrone.item.MavicDroneItem;
import com.fpvdrone.item.Mortar120Item;
import com.fpvdrone.item.MortarMine120Item;
import com.fpvdrone.item.OpenerItem;
import com.fpvdrone.item.Orlan10DroneItem;
import com.fpvdrone.item.RampItem;
import com.fpvdrone.item.Rgd5Item;
import com.fpvdrone.item.Rocket70mmItem;
import com.fpvdrone.item.RocketPodMountItem;
import com.fpvdrone.item.Tm62Item;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import org.jspecify.annotations.Nullable;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;

public class ModItems {
    /** Equipment asset id of the goggles; no equipment json exists, GeckoLib draws the worn model. */
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> GOGGLES_ASSET = ResourceKey.create(
        net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, net.minecraft.resources.Identifier.fromNamespaceAndPath("fpvdrone", "fpv_goggles")
    );
    /** 1.1.8 mine vest's equipment asset id; no equipment json exists, GeckoLib draws the worn model. */
    public static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> OPENER_ASSET = ResourceKey.create(
        net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, net.minecraft.resources.Identifier.fromNamespaceAndPath("fpvdrone", "opener")
    );
    public static final RegistryObject<Item> DRONE = register("drone", p -> new DroneItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> REMOTE_CONTROL = register("remote_control", p -> new RemoteControlItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> FPV_GOGGLES = register("fpv_goggles", p -> new FpvGogglesItem(p), new Properties().stacksTo(1).component(
            net.minecraft.core.component.DataComponents.EQUIPPABLE,
            // The asset id makes the armour layer (and GeckoLib's goggles model) handle the head slot instead of drawing the item sprite.
            net.minecraft.world.item.equipment.Equippable.builder(net.minecraft.world.entity.EquipmentSlot.HEAD).setAsset(GOGGLES_ASSET).build()
        ));
    public static final RegistryObject<Item> BATTERY = register("battery", p -> new BatteryItem(p), new Properties().stacksTo(16));
    /** 1.1.8: the Mavic's battery (battery definition "dji"). */
    public static final RegistryObject<Item> BATTERY_DJI = register("battery_dji", p -> new BatteryItem(p, "dji"), new Properties().stacksTo(16));
    public static final RegistryObject<Item> ANTENNA = register("antenna", p -> new AntennaItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> CAMERA = register("camera", p -> new CameraItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> BOMB_1 = register("bomb_1", p -> new BombItem(p, 1), new Properties().stacksTo(16));
    public static final RegistryObject<Item> BOMB_2 = register("bomb_2", p -> new BombItem(p, 2), new Properties().stacksTo(16));
    public static final RegistryObject<Item> BOMB_3 = register("bomb_3", p -> new BombItem(p, 3), new Properties().stacksTo(16));
    public static final RegistryObject<Item> BETAFLIGHT = register("betaflight", p -> new BetaFlightItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> WEIGHT = register("weight", p -> new WeightItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> THERMAL = register("thermal", p -> new ThermalItem(p), new Properties().stacksTo(8));
    /** Port addition: allows the clean feed (F6) in survival. */
    public static final RegistryObject<Item> HD_CAMERA = register("hd_camera", p -> new com.fpvdrone.item.HdCameraItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> THERMAL_DRONE = register("thermal_drone", p -> new ThermalDroneItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> MAVIC_DRONE = register("mavic_drone", p -> new MavicDroneItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> FP1_DRONE = register("fp1_drone", p -> new Fp1DroneItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> LYTUI_DRONE = register("lytui_drone", p -> new LytuiDroneItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> LANCET51_DRONE = register("lancet51_drone", p -> new Lancet51DroneItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> ORLAN10_DRONE = register("orlan10_drone", p -> new Orlan10DroneItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> ELECTRIC_MOTOR = register("electric_motor", p -> new ElectricMotorItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> PROPELLER = register("propeller", p -> new PropellerItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> PROCESSOR = register("processor", p -> new ProcessorItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> RPG7 = register("rpg7", p -> new Rpg7Item(p), new Properties().stacksTo(16));
    public static final RegistryObject<Item> RGD5 = register("rgd5", p -> new Rgd5Item(p), new Properties().stacksTo(16));
    public static final RegistryObject<Item> TM62 = register("tm62", p -> new Tm62Item(p), new Properties().stacksTo(16));
    /** 1.1.8 "Can Opener" mine vest: iron chestplate stats; worn model drawn by GeckoLib (asset id with no equipment json). */
    public static final RegistryObject<Item> OPENER = register("opener", p -> new OpenerItem(p), new Properties().stacksTo(1)
        .humanoidArmor(net.minecraft.world.item.equipment.ArmorMaterials.IRON, net.minecraft.world.item.equipment.ArmorType.CHESTPLATE)
        .component(
            net.minecraft.core.component.DataComponents.EQUIPPABLE,
            net.minecraft.world.item.equipment.Equippable.builder(net.minecraft.world.entity.EquipmentSlot.CHEST)
                .setEquipSound(net.minecraft.world.item.equipment.ArmorMaterials.IRON.equipSound())
                .setAsset(OPENER_ASSET)
                .build()
        ));
    public static final RegistryObject<Item> DJI_CONTROLLER = register("dji_controller", p -> new DjiControllerItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> PIVOT_CONTROLLER = register("pivot_controller", p -> new PivotControllerItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> BOMB_HOLDER = register("bomb_holder", p -> new BombHolderItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> EXPLOSIVE_40MM = register("40mm_explosive", p -> new Explosive40mmItem(p), new Properties().stacksTo(16));
    /** 1.1.8 debug item, /give only (not in the creative tab). */
    public static final RegistryObject<Item> DEBUG_INFINITE_BOMB = register("debug_infinite_bomb", p -> new DebugInfiniteBombItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> MORTAR_120 = register("mortar_120", p -> new Mortar120Item(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> MORTAR_MINE_120 = register("mortar_mine_120", p -> new MortarMine120Item(p), new Properties().stacksTo(16));
    public static final RegistryObject<Item> REB_JAMMER = register(
        "reb_jammer", p -> new BlockItem(ModBlocks.REB_JAMMER.get(), p), new Properties().useBlockDescriptionPrefix()
    );
    public static final RegistryObject<Item> RAMP = register("ramp", p -> new RampItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> FUEL_CAN = register("fuel_can", p -> new FuelCanItem(p), new Properties().stacksTo(4));
    public static final RegistryObject<Item> ROCKET_70MM = register("70mm_rocket", p -> new Rocket70mmItem(p), new Properties().stacksTo(16));
    public static final RegistryObject<Item> ROCKET_POD_MOUNT = register("rocket_pod_mount", p -> new RocketPodMountItem(p), new Properties().stacksTo(4));
    public static final RegistryObject<CreativeModeTab> FPV_TAB = new RegistryObject<>(Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath("fpvdrone", "fpvdrone_tab"),
        FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.fpvdrone"))
            .icon(() -> new ItemStack((ItemLike)DRONE.get()))
            .displayItems((params, output) -> {
                // 1.1.8 order; hd_camera (port addition) after thermal. 1.1.8 left out bomb_1/2/3, weight, processor,
                // the debug bomb and the jammer block.
                output.accept((ItemLike)DRONE.get());
                output.accept((ItemLike)THERMAL_DRONE.get());
                output.accept((ItemLike)REMOTE_CONTROL.get());
                output.accept((ItemLike)FPV_GOGGLES.get());
                output.accept((ItemLike)MAVIC_DRONE.get());
                output.accept((ItemLike)DJI_CONTROLLER.get());
                output.accept((ItemLike)FP1_DRONE.get());
                output.accept((ItemLike)LYTUI_DRONE.get());
                output.accept((ItemLike)LANCET51_DRONE.get());
                output.accept((ItemLike)ORLAN10_DRONE.get());
                output.accept((ItemLike)RAMP.get());
                output.accept((ItemLike)FUEL_CAN.get());
                output.accept((ItemLike)ROCKET_POD_MOUNT.get());
                output.accept((ItemLike)RPG7.get());
                output.accept((ItemLike)RGD5.get());
                output.accept((ItemLike)EXPLOSIVE_40MM.get());
                output.accept((ItemLike)ROCKET_70MM.get());
                output.accept((ItemLike)MORTAR_120.get());
                output.accept((ItemLike)MORTAR_MINE_120.get());
                output.accept((ItemLike)TM62.get());
                output.accept((ItemLike)BATTERY.get());
                output.accept((ItemLike)BATTERY_DJI.get());
                output.accept((ItemLike)OPENER.get());
                output.accept((ItemLike)ANTENNA.get());
                output.accept((ItemLike)CAMERA.get());
                output.accept((ItemLike)THERMAL.get());
                output.accept((ItemLike)HD_CAMERA.get());
                output.accept((ItemLike)ELECTRIC_MOTOR.get());
                output.accept((ItemLike)PROPELLER.get());
                output.accept((ItemLike)BETAFLIGHT.get());
                output.accept((ItemLike)PIVOT_CONTROLLER.get());
                output.accept((ItemLike)BOMB_HOLDER.get());
            })
            .build()
    ));

    /** 1.1.8: the item of a battery definition ("default" -> battery, "dji" -> battery_dji); the plain battery if unknown. */
    public static Item batteryItemFor(String batteryType) {
        com.fpvdrone.data.BatteryDefinition def = com.fpvdrone.data.BatteryDefinitions.ibzfxic(batteryType);
        Identifier id = def != null ? Identifier.tryParse(def.ibzfxic()) : null;
        Item item = id != null ? BuiltInRegistries.ITEM.getValue(id) : null;
        return item != null && item != net.minecraft.world.item.Items.AIR ? item : BATTERY.get();
    }

    /** 1.1.8: the item of a projectile id, for the built-in projectiles only. */
    public static @Nullable Item projectileItemFor(@Nullable String projectileId) {
        if (projectileId == null) {
            return null;
        }

        return switch (projectileId) {
            case "rpg7" -> RPG7.get();
            case "rgd5" -> RGD5.get();
            case "40mm_explosive" -> EXPLOSIVE_40MM.get();
            case "70mm_rocket" -> ROCKET_70MM.get();
            case "tm62" -> TM62.get();
            default -> null;
        };
    }

    /** 1.1.8: the item a drone of the given definition id drops when picked up. */
    public static Item droneItemFor(String droneType) {
        if ("fp1".equals(droneType)) {
            return FP1_DRONE.get();
        } else if ("thermal".equals(droneType)) {
            return THERMAL_DRONE.get();
        } else if ("mavic".equals(droneType)) {
            return MAVIC_DRONE.get();
        } else if ("lancet51".equals(droneType)) {
            return LANCET51_DRONE.get();
        } else if ("orlan10".equals(droneType)) {
            return ORLAN10_DRONE.get();
        } else {
            return !"lytui_drun".equals(droneType) && !"lytui_drone".equals(droneType) ? DRONE.get() : LYTUI_DRONE.get();
        }
    }

    /** 1.1.8: first stack of the item anywhere in the player's inventory, or null. */
    public static @Nullable ItemStack findInInventory(Player player, Item item) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && stack.is(item)) {
                return stack;
            }
        }

        return null;
    }

    /** 1.1.8: first stack whose item is of the given class anywhere in the player's inventory, or null. */
    public static @Nullable ItemStack findInInventory(Player player, Class<? extends Item> itemClass) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
                return stack;
            }
        }

        return null;
    }

    private static RegistryObject<Item> register(String name, Function<Properties, Item> factory, Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fpvdrone", name));
        return new RegistryObject<>(Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key))));
    }

    public static void init() {
    }
}
