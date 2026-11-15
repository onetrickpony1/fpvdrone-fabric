package com.fpvdrone.client;

import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.util.ParticleSendRange;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * 1.1.8 FsOzhg: with Distant Horizons, drones standing in chunks the client has not loaded (out in the LOD terrain)
 * are still drawn. 1.1.8 skipped them in the entity pass and drew them in a pass of its own after the weather, with the
 * saved camera matrices and full-bright light. In 26.3 the entity pass is fed by LevelExtractor, which drops entities
 * whose section is not compiled; {@code LevelExtractorFarDroneMixin} lets far drones through instead, so they are
 * drawn in the normal pass with the real camera, and {@code DroneRenderer} gives them full-bright light.
 */
public final class FarDrones {
    private FarDrones() {
    }

    /**
     * 26.3 fix (not in 1.1.8): the client only ticks entities in loaded chunks, so a far drone would never apply its
     * position updates and would hang where it was first seen. Far drones that are not ticking step their
     * interpolation here, once per client tick, as their own tick would.
     */
    public static void register() {
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.level == null || mc.isPaused() || !ParticleSendRange.isDistantHorizonsLoaded()) {
                return;
            }
            for (net.minecraft.world.entity.Entity e : mc.level.entitiesForRendering()) {
                if (e instanceof DroneEntity drone && !mc.level.isTickingEntity(drone) && isFar(drone)) {
                    drone.setOldPosAndRot();
                    if (drone.isInterpolating()) {
                        drone.getInterpolation().interpolate();
                    }
                }
            }
        });
    }

    /** 1.1.8 FsOzhg.v5suI(DroneEntity): Distant Horizons is loaded and the drone's chunk is not loaded on the client. */
    public static boolean isFar(DroneEntity drone) {
        if (!ParticleSendRange.isDistantHorizonsLoaded()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        int chunkX = Mth.floor(drone.getX()) >> 4;
        int chunkZ = Mth.floor(drone.getZ()) >> 4;
        return !mc.level.getChunkSource().hasChunk(chunkX, chunkZ);
    }
}
