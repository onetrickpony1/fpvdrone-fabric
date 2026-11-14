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
    public static final RegistryObject<Item> DRONE = register("drone", p -> new DroneItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> REMOTE_CONTROL = register("remote_control", p -> new RemoteControlItem(p), new Properties().stacksTo(1));
    public static final RegistryObject<Item> FPV_GOGGLES = register("fpv_goggles", p -> new FpvGogglesItem(p), new Properties().stacksTo(1).component(
            net.minecraft.core.component.DataComponents.EQUIPPABLE,
            // The asset id makes the armour layer (and GeckoLib's goggles model) handle the head slot instead of drawing the item sprite.
            net.minecraft.world.item.equipment.Equippable.builder(net.minecraft.world.entity.EquipmentSlot.HEAD).setAsset(GOGGLES_ASSET).build()
        ));
    public static final RegistryObject<Item> BATTERY = register("battery", p -> new BatteryItem(p), new Properties().stacksTo(16));
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
    public static final RegistryObject<Item> ELECTRIC_MOTOR = register("electric_motor", p -> new ElectricMotorItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> PROPELLER = register("propeller", p -> new PropellerItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> PROCESSOR = register("processor", p -> new ProcessorItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> RPG7 = register("rpg7", p -> new Rpg7Item(p), new Properties().stacksTo(16));
    public static final RegistryObject<Item> PIVOT_CONTROLLER = register("pivot_controller", p -> new PivotControllerItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<Item> BOMB_HOLDER = register("bomb_holder", p -> new BombHolderItem(p), new Properties().stacksTo(8));
    public static final RegistryObject<CreativeModeTab> FPV_TAB = new RegistryObject<>(Registry.register(
        BuiltInRegistries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath("fpvdrone", "fpvdrone_tab"),
        FabricCreativeModeTab.builder()
            .title(Component.translatable("itemGroup.fpvdrone"))
            .icon(() -> new ItemStack((ItemLike)DRONE.get()))
            .displayItems((params, output) -> {
                output.accept((ItemLike)DRONE.get());
                output.accept((ItemLike)THERMAL_DRONE.get());
                output.accept((ItemLike)REMOTE_CONTROL.get());
                output.accept((ItemLike)FPV_GOGGLES.get());
                output.accept((ItemLike)BATTERY.get());
                output.accept((ItemLike)RPG7.get());
                output.accept((ItemLike)ANTENNA.get());
                output.accept((ItemLike)CAMERA.get());
                output.accept((ItemLike)THERMAL.get());
                output.accept((ItemLike)HD_CAMERA.get());
                output.accept((ItemLike)ELECTRIC_MOTOR.get());
                output.accept((ItemLike)PROPELLER.get());
                output.accept((ItemLike)BETAFLIGHT.get());
                output.accept((ItemLike)PIVOT_CONTROLLER.get());
                output.accept((ItemLike)BOMB_HOLDER.get());
                output.accept((ItemLike)BOMB_3.get());
            })
            .build()
    ));

    private static RegistryObject<Item> register(String name, Function<Properties, Item> factory, Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("fpvdrone", name));
        return new RegistryObject<>(Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key))));
    }

    public static void init() {
    }
}
