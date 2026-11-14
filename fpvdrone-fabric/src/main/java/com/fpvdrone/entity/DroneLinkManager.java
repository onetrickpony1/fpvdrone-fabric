package com.fpvdrone.entity;

import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.item.RemoteControlItem;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DroneLinkManager {
    private static final Map<UUID, UUID> droneToPlayer = new ConcurrentHashMap<>();

    public static void linkDrone(UUID droneId, UUID playerId) {
        droneToPlayer.put(droneId, playerId);
    }

    public static void unlinkDrone(UUID droneId) {
        droneToPlayer.remove(droneId);
    }

    public static boolean isDroneLinked(UUID droneId) {
        return droneToPlayer.containsKey(droneId);
    }

    public static Optional<DroneEntity> findLinkedDrone(Level level, ItemStack itemStack) {
        Optional<UUID> droneId;
        if (itemStack.getItem() instanceof FpvGogglesItem) {
            droneId = FpvGogglesItem.getLinkedDroneId(itemStack);
        } else {
            droneId = RemoteControlItem.getLinkedDroneId(itemStack);
        }

        if (droneId.isEmpty()) {
            return Optional.empty();
        } else {
            UUID targetId = droneId.get();
            return level instanceof ServerLevel serverLevel && serverLevel.getEntity(targetId) instanceof DroneEntity drone
                ? Optional.of(drone)
                : Optional.empty();
        }
    }
}
