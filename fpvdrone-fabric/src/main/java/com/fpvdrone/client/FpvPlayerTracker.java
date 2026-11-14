package com.fpvdrone.client;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import org.jspecify.annotations.Nullable;
import net.minecraft.client.Minecraft;

public final class FpvPlayerTracker {
    private static final Set<UUID> frozenPlayers = ConcurrentHashMap.newKeySet();
    private static final Set<UUID> needsSnap = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> exitedPlayers = new ConcurrentHashMap<>();
    private static final Map<UUID, float[]> exitRotation = new ConcurrentHashMap<>();
    private static final long EXITED_TIMEOUT_TICKS = 20L;
    public static LongSupplier fpvdrone$clockOverride = null;

    private FpvPlayerTracker() {
    }

    public static void freeze(UUID playerUUID) {
        exitedPlayers.remove(playerUUID);
        exitRotation.remove(playerUUID);
        needsSnap.remove(playerUUID);
        frozenPlayers.add(playerUUID);
    }

    public static boolean unfreeze(UUID playerUUID, float yaw, float pitch) {
        boolean wasFrozen = frozenPlayers.remove(playerUUID);
        exitedPlayers.put(playerUUID, getGameTime());
        exitRotation.put(playerUUID, new float[]{yaw, pitch});
        needsSnap.add(playerUUID);
        return wasFrozen;
    }

    public static boolean isFrozen(UUID playerUUID) {
        return frozenPlayers.contains(playerUUID);
    }

    public static boolean consumeSnap(UUID playerUUID) {
        return needsSnap.remove(playerUUID);
    }

    public static boolean hasRecentlyExited(UUID playerUUID) {
        Long exitTick = exitedPlayers.get(playerUUID);
        if (exitTick == null) {
            return false;
        } else {
            long now = getGameTime();
            if (now - exitTick > 20L) {
                exitedPlayers.remove(playerUUID);
                exitRotation.remove(playerUUID);
                return false;
            } else {
                return true;
            }
        }
    }

    @Nullable
    public static float[] getExitRotation(UUID playerUUID) {
        return exitRotation.get(playerUUID);
    }

    private static long getGameTime() {
        if (fpvdrone$clockOverride != null) {
            return fpvdrone$clockOverride.getAsLong();
        } else {
            Minecraft mc = Minecraft.getInstance();
            return mc != null && mc.level != null ? mc.level.getGameTime() : 0L;
        }
    }
}
