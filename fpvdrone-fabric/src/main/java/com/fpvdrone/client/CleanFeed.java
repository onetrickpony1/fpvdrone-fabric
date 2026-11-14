package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

/**
 * Clean feed (port addition, F6, off by default): a recording view of the goggles feed. It hides the OSD, the stick
 * overlay, the low-resolution and stutter effects, and your own drone (body, props, blur discs, launch bricks).
 * Signal loss still cuts the feed. Creative pilots may use it on any drone; in survival the drone needs an
 * HD Digital Camera, and the clean feed then draws {@link #SURVIVAL_CURRENT_MULTIPLIER} times the current.
 */
public final class CleanFeed {
    public static final float SURVIVAL_CURRENT_MULTIPLIER = 1.35F;
    private static boolean toggled = false;

    private CleanFeed() {
    }

    /** The F6 key. */
    public static void toggle(Minecraft mc) {
        Player player = mc.player;
        if (player == null) {
            return;
        }

        if (!toggled) {
            Optional<DroneEntity> drone = FPVMod.getActiveDrone(player.getUUID());
            if (drone.isPresent() && !isAllowed(player, drone.get())) {
                player.sendOverlayMessage(Component.translatable("fpvdrone.clean_feed.needs_hd_camera").withStyle(Style.EMPTY.withColor(16749622)));
                return;
            }
        }

        toggled = !toggled;
        player.sendOverlayMessage(Component.translatable(toggled ? "fpvdrone.clean_feed.on" : "fpvdrone.clean_feed.off").withStyle(Style.EMPTY.withColor(16749622)));
    }

    public static boolean isToggled() {
        return toggled;
    }

    public static void setToggled(boolean value) {
        toggled = value;
    }

    private static boolean isAllowed(Player player, DroneEntity drone) {
        return player.isCreative() || drone.hasHdCamera();
    }

    /** True while the local player flies a drone in FPV with the clean feed on and allowed for that drone. */
    public static boolean isActive() {
        if (!toggled) {
            return false;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        Optional<DroneEntity> drone = FPVMod.getActiveDrone(player.getUUID());
        return drone.isPresent() && isAllowed(player, drone.get());
    }

    /** True for the drone the local player is flying while the clean feed is active (it is not drawn). */
    public static boolean hidesDrone(DroneEntity drone) {
        if (!isActive()) {
            return false;
        }

        Player player = Minecraft.getInstance().player;
        Optional<UUID> active = FPVMod.getActiveDroneId(player.getUUID());
        return active.isPresent() && active.get().equals(drone.getUUID());
    }

    /** Battery current multiplier: survival clean feed costs extra, creative is free. */
    public static float currentMultiplier() {
        Player player = Minecraft.getInstance().player;
        return isActive() && player != null && !player.isCreative() ? SURVIVAL_CURRENT_MULTIPLIER : 1.0F;
    }
}
