package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FpvStatusChecker {
    private static final double MAX_DRONE_RANGE = 500.0;
    private static final Set<UUID> lostSignalDrones = Collections.synchronizedSet(new HashSet<>());
    private static final Set<UUID> destroyedDrones = Collections.synchronizedSet(new HashSet<>());
    private static final Set<UUID> fallingDrones = Collections.synchronizedSet(new HashSet<>());

    public static void markDroneDestroyed(UUID droneId) {
        destroyedDrones.add(droneId);
        fallingDrones.remove(droneId);
    }

    public static void markSignalLost(UUID droneId) {
        lostSignalDrones.add(droneId);
    }

    public static void markDroneFalling(UUID droneId) {
        fallingDrones.add(droneId);
    }

    public static boolean isDroneFalling(UUID droneId) {
        return fallingDrones.contains(droneId);
    }

    public static void clearAll() {
        destroyedDrones.clear();
        lostSignalDrones.clear();
        fallingDrones.clear();
    }

    public static void clearDroneState(UUID droneId) {
        destroyedDrones.remove(droneId);
        lostSignalDrones.remove(droneId);
        fallingDrones.remove(droneId);
    }

    public static boolean isDroneDestroyed(UUID droneId) {
        return destroyedDrones.contains(droneId);
    }

    public static boolean hasNoSignalState(UUID droneId) {
        return destroyedDrones.contains(droneId) || lostSignalDrones.contains(droneId);
    }

    public static FpvStatusChecker.FpvReadyState checkReadyState(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof FpvGogglesItem)) {
            return FpvStatusChecker.FpvReadyState.NO_GOGGLES;
        } else {
            Optional<UUID> gogglesLink = FpvGogglesItem.getLinkedDroneId(helmet);
            if (gogglesLink.isEmpty()) {
                return FpvStatusChecker.FpvReadyState.GOGGLES_NOT_LINKED;
            } else {
                UUID droneId = gogglesLink.get();
                if (destroyedDrones.contains(droneId)) {
                    return FpvStatusChecker.FpvReadyState.DRONE_DESTROYED;
                } else if (lostSignalDrones.contains(droneId)) {
                    return FpvStatusChecker.FpvReadyState.SIGNAL_LOST;
                } else {
                    Optional<DroneEntity> drone = FPVMod.findDroneById(player, droneId);
                    if (drone.isEmpty()) {
                        return FpvStatusChecker.FpvReadyState.DRONE_NOT_FOUND;
                    } else {
                        double distance = player.distanceTo((Entity)drone.get());
                        if (distance > 500.0) {
                            return FpvStatusChecker.FpvReadyState.DRONE_OUT_OF_RANGE;
                        } else {
                            return !drone.get().hasBattery() ? FpvStatusChecker.FpvReadyState.NO_BATTERY : FpvStatusChecker.FpvReadyState.READY;
                        }
                    }
                }
            }
        }
    }

    public static Component getStatusMessage(FpvStatusChecker.FpvReadyState state) {
        return (Component)(switch (state) {
            case READY -> getArmKeyMessage();
            case NO_GOGGLES -> Component.translatable("fpvdrone.status.wear_goggles");
            case GOGGLES_NOT_LINKED -> Component.translatable("fpvdrone.status.link_goggles");
            case DRONE_OUT_OF_RANGE -> Component.translatable("fpvdrone.status.out_of_range");
            case DRONE_NOT_FOUND -> Component.translatable("fpvdrone.status.drone_not_found");
            case DRONE_DESTROYED -> Component.empty();
            case SIGNAL_LOST -> Component.empty();
            case NO_BATTERY -> Component.translatable("fpvdrone.status.no_battery");
        });
    }

    private static Component getArmKeyMessage() {
        ControllerReader reader = ControllerReader.getInstance();
        if (reader.isPhysicalControllerConnected()) {
            ControllerConfig config = ControllerReader.getConfig();
            if (config.getArmChannel() >= 0) {
                String keyName = config.isArmIsButton() ? "Button " + (config.getArmChannel() + 1) : "Axis " + (config.getArmChannel() + 1);
                return Component.literal("Press [" + keyName + "] or [" + KeyManager.FPV_TOGGLE_KEY.getTranslatedKeyMessage().getString() + "] to arm motors");
            }
        }

        return Component.translatable("fpvdrone.status.ready");
    }

    public static enum FpvReadyState {
        READY,
        NO_GOGGLES,
        GOGGLES_NOT_LINKED,
        DRONE_OUT_OF_RANGE,
        DRONE_NOT_FOUND,
        DRONE_DESTROYED,
        SIGNAL_LOST,
        NO_BATTERY;
    }
}
