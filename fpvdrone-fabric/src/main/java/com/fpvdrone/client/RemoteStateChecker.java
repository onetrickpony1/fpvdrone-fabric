package com.fpvdrone.client;

import com.fpvdrone.item.RemoteControlItem;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RemoteStateChecker {
    private static final int CACHE_VALIDITY_TICKS = 20;
    private static final Map<RemoteStateChecker.CacheKey, RemoteStateChecker.CacheEntry> cache = new ConcurrentHashMap<>();

    public static boolean hasLinkedRemote(Player player, UUID droneId) {
        if (player != null && droneId != null) {
            RemoteStateChecker.CacheKey key = new RemoteStateChecker.CacheKey(player.getUUID(), droneId);
            RemoteStateChecker.CacheEntry entry = cache.get(key);
            long currentTime = player.level().getGameTime();
            if (entry != null && currentTime - entry.timestamp < 20L) {
                return entry.hasRemote;
            } else {
                boolean result = findLinkedRemote(player, droneId).isPresent();
                cache.put(key, new RemoteStateChecker.CacheEntry(result, currentTime));
                return result;
            }
        } else {
            return false;
        }
    }

    public static Optional<ItemStack> findLinkedRemote(Player player, UUID droneId) {
        if (player != null && droneId != null) {
            Inventory inventory = player.getInventory();

            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (isRemoteLinkedToDrone(stack, droneId)) {
                    return Optional.of(stack);
                }
            }

            ItemStack offhand = player.getOffhandItem();
            return isRemoteLinkedToDrone(offhand, droneId) ? Optional.of(offhand) : Optional.empty();
        } else {
            return Optional.empty();
        }
    }

    public static void invalidateCache(UUID playerId) {
        cache.entrySet().removeIf(e -> e.getKey().playerId.equals(playerId));
    }

    public static void clearCache() {
        cache.clear();
    }

    private static boolean isRemoteLinkedToDrone(ItemStack stack, UUID droneId) {
        if (!stack.isEmpty() && stack.getItem() instanceof RemoteControlItem) {
            Optional<UUID> linkedId = RemoteControlItem.getLinkedDroneId(stack);
            return linkedId.isPresent() && linkedId.get().equals(droneId);
        } else {
            return false;
        }
    }

    private record CacheEntry(boolean hasRemote, long timestamp) {
    }

    private record CacheKey(UUID playerId, UUID droneId) {
    }
}
