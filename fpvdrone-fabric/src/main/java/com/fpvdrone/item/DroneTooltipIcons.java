package com.fpvdrone.item;

import com.fpvdrone.client.BatteryData;
import com.fpvdrone.data.ArmorDefinition;
import com.fpvdrone.data.AttachableEntry;
import com.fpvdrone.data.BatteryDefinition;
import com.fpvdrone.data.BatteryDefinitions;
import com.fpvdrone.data.DroneDefinitions;
import com.fpvdrone.data.DroneItemIndex;
import com.fpvdrone.data.ProjectileDefinition;
import com.fpvdrone.data.ProjectileDefinitions;
import com.fpvdrone.data.TooltipRows;
import com.fpvdrone.util.ItemData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * 1.1.8 item-icon tooltips (IiFdBcsKm, a Forge GatherComponents handler there; here the items return it from
 * getTooltipImage): a drone item lists the batteries and payloads its data file allows, marking the fitted ones
 * "Installed" (the battery with its charge in %); the rocket pod mount lists the 70 mm rocket; the vest its mine.
 */
public final class DroneTooltipIcons {
    private DroneTooltipIcons() {
    }

    public static Optional<TooltipComponent> forItem(ItemStack stack) {
        if (DroneDefinitions.v5suI().isEmpty()) {
            return Optional.empty();
        }

        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Optional<String> droneId = DroneItemIndex.v5suI(itemId.toString());
        if (droneId.isEmpty()) {
            return Optional.empty();
        }

        List<TooltipRows.KKeMvpuBgEY> rows = TooltipRows.v5suI(droneId.get());
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        String installedBattery = installedBatteryItem(stack);
        String installedProjectile = installedProjectileItem(stack);
        List<ItemIconTooltip.Row> out = new ArrayList<>();
        for (TooltipRows.KKeMvpuBgEY row : rows) {
            if (row.v5suI()) {
                out.add(new ItemIconTooltip.Row(null, row.wuhB(), row.dOfFwvo()));
            } else {
                Item item = itemFor(row.ibzfxic());
                if (item != null) {
                    ItemStack icon = new ItemStack(item);
                    String label = row.wuhB();
                    if (row.ibzfxic().equals(installedBattery)) {
                        appendChargePercent(icon, stack);
                        label = "tooltip.fpvdrone.installed";
                    } else if (row.ibzfxic().equals(installedProjectile)) {
                        label = "tooltip.fpvdrone.installed";
                    }

                    out.add(new ItemIconTooltip.Row(icon, label, 0));
                }
            }
        }

        return out.isEmpty() ? Optional.empty() : Optional.of(new ItemIconTooltip(out));
    }

    /** Rocket pod mount: the 70 mm rocket, optional. */
    public static Optional<TooltipComponent> forRocketPodMount() {
        if (ProjectileDefinitions.v5suI().isEmpty()) {
            return Optional.empty();
        }

        Item item = itemFor(ProjectileDefinitions.wuhB("70mm_rocket").v5suI());
        return item == null
            ? Optional.empty()
            : Optional.of(new ItemIconTooltip(List.of(new ItemIconTooltip.Row(new ItemStack(item), "tooltip.fpvdrone.optional", 0))));
    }

    /** The mine vest: each projectile its armour definition accepts, optional. */
    public static Optional<TooltipComponent> forOpener(OpenerItem opener) {
        ArmorDefinition def = opener.getDefinition();
        if (def == null || ProjectileDefinitions.v5suI().isEmpty()) {
            return Optional.empty();
        }

        List<ItemIconTooltip.Row> out = new ArrayList<>();
        for (AttachableEntry entry : def.zTT3nw6()) {
            Item item = itemFor(ProjectileDefinitions.wuhB(entry.v5suI()).v5suI());
            if (item != null) {
                out.add(new ItemIconTooltip.Row(new ItemStack(item), "tooltip.fpvdrone.optional", 0));
            }
        }

        return out.isEmpty() ? Optional.empty() : Optional.of(new ItemIconTooltip(out));
    }

    private static @Nullable Item itemFor(String id) {
        Identifier key = Identifier.tryParse(id);
        if (key == null) {
            return null;
        }

        Item item = BuiltInRegistries.ITEM.getValue(key);
        return item == Items.AIR ? null : item;
    }

    private static @Nullable String installedBatteryItem(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        if (tag != null && tag.getBooleanOr("HasBattery", false) && tag.contains("BatteryData")) {
            BatteryData data = BatteryData.fromNbt(tag.getCompoundOrEmpty("BatteryData"));
            BatteryDefinition def = BatteryDefinitions.ibzfxic(data.getTypeId());
            return def != null ? def.ibzfxic() : null;
        } else {
            return null;
        }
    }

    private static @Nullable String installedProjectileItem(ItemStack stack) {
        CompoundTag tag = ItemData.get(stack);
        if (tag != null && tag.getBooleanOr("HasRpg", false)) {
            String id = tag.getStringOr("ProjectileId", "");
            ProjectileDefinition def = ProjectileDefinitions.wuhB(id.isEmpty() ? "rpg7" : id);
            return def != null ? def.v5suI() : null;
        } else {
            return null;
        }
    }

    private static void appendChargePercent(ItemStack icon, ItemStack drone) {
        CompoundTag tag = ItemData.get(drone);
        BatteryData data = BatteryData.fromNbt(tag.getCompoundOrEmpty("BatteryData"));
        int percent = Math.round(100.0F * data.getRemainingMah() / data.getCapacityMah());
        icon.set(DataComponents.CUSTOM_NAME, icon.getHoverName().copy().append(Component.literal(" • " + percent + "%")));
    }
}
