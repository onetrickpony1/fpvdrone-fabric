package com.fpvdrone.item;

import java.util.Optional;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

/**
 * 1.1.8: rocket pod mount, a one-way upgrade fitted to a compatible drone (the ramp and loitering-munition entities
 * take it, playing the rocket_pod_install sound). Behaviour lives in the entity that accepts it.
 */
public class RocketPodMountItem extends UsageTooltipItem {
    public RocketPodMountItem(Properties properties) {
        super(properties);
    }

    /** 1.1.8: shows the 70 mm rocket as an "Optional" item icon. */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return DroneTooltipIcons.forRocketPodMount();
    }
}
