package com.fpvdrone.client;

import com.fpvdrone.entity.DroneEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Villagers within 24 blocks of a drone treat it as a hostile, run 16 blocks away from it and ring a nearby bell
 * (at most once every 100 ticks). Runs on the server side; the Forge version listened to LivingTickEvent, here it is
 * called from the start of Villager.tick (see VillagerPanicMixin), which is where that event fired.
 */
public class VillagerPanicHandler {
    private static final double PANIC_RANGE = 24.0;
    private static final int BELL_COOLDOWN_TICKS = 100;
    private static final int TICK_INTERVAL = 10;
    private static final int BELL_CACHE_TICKS = 200;
    private static long lastBellRing = 0L;
    private static final Map<BlockPos, Long> bellCache = new HashMap<>();
    private static long lastCacheUpdate = 0L;

    public static void onVillagerTick(Villager villager) {
        if (villager.tickCount % TICK_INTERVAL != 0) {
            return;
        }

        if (!(villager.level() instanceof ServerLevel level)) {
            return;
        }

        AABB searchBox = villager.getBoundingBox().inflate(PANIC_RANGE);
        DroneEntity drone = level.getNearestEntity(
            DroneEntity.class, TargetingConditions.forNonCombat(), villager, villager.getX(), villager.getY(), villager.getZ(), searchBox
        );
        if (drone == null) {
            return;
        }

        villager.getBrain().setMemory(MemoryModuleType.NEAREST_HOSTILE, drone);
        Vec3 awayDir = villager.position().subtract(drone.position()).normalize();
        Vec3 fleeTarget = villager.position().add(awayDir.scale(16.0));
        villager.getNavigation().moveTo(fleeTarget.x, fleeTarget.y, fleeTarget.z, 1.0);
        long gameTime = level.getGameTime();
        if (gameTime - lastBellRing > BELL_COOLDOWN_TICKS) {
            BlockPos bellPos = getCachedBell(level, villager.blockPosition(), gameTime);
            if (bellPos != null && level.getBlockState(bellPos).getBlock() instanceof BellBlock bell) {
                bell.attemptToRing(level, bellPos, null);
                lastBellRing = gameTime;
            }
        }
    }

    private static BlockPos getCachedBell(Level level, BlockPos center, long gameTime) {
        if (gameTime - lastCacheUpdate > BELL_CACHE_TICKS) {
            bellCache.clear();
            lastCacheUpdate = gameTime;
        }

        BlockPos chunkKey = new BlockPos(center.getX() >> 4, 0, center.getZ() >> 4);
        if (bellCache.containsKey(chunkKey)) {
            long packed = bellCache.get(chunkKey);
            return packed == Long.MIN_VALUE ? null : BlockPos.of(packed);
        } else {
            BlockPos found = findBellInArea(level, center);
            bellCache.put(chunkKey, found == null ? Long.MIN_VALUE : found.asLong());
            return found;
        }
    }

    private static BlockPos findBellInArea(Level level, BlockPos center) {
        int range = 32;

        for (int y = -4; y <= 4; y++) {
            for (int x = -range; x <= range; x += 4) {
                for (int z = -range; z <= range; z += 4) {
                    BlockPos pos = center.offset(x, y, z);
                    if (level.getBlockState(pos).getBlock() instanceof BellBlock) {
                        return pos;
                    }
                }
            }
        }

        return null;
    }
}
