package com.fpvdrone.mixin;

import com.fpvdrone.sync.ObserverRange;
import com.fpvdrone.util.ParticleSendRange;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 1.1.8 DroneTrackingRangeMixin: drones, munitions and dropped bombs are sent to players out to the observer range
 * (the larger of the config's drone observer range and the Distant Horizons LOD radius), not capped at the view distance.
 */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public class DroneTrackingRangeMixin {
    @Shadow
    @Final
    Entity entity;

    @Redirect(method = "updatePlayer", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int fpvdrone$liftTrackingRange(int range, int viewDistanceBlocks) {
        return this.entity instanceof ObserverRange ? ParticleSendRange.get() : Math.min(range, viewDistanceBlocks);
    }

    /**
     * 26.3 only sends an entity whose chunk the player has loaded (1.20.1 had no such check), which would hide every
     * drone beyond the view distance again; observer-range entities skip it, as they did in 1.1.8.
     */
    @WrapOperation(
        method = "updatePlayer",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ChunkMap;isChunkTracked(Lnet/minecraft/server/level/ServerPlayer;II)Z")
    )
    private boolean fpvdrone$trackOutsideLoadedChunks(ChunkMap map, ServerPlayer player, int chunkX, int chunkZ, Operation<Boolean> original) {
        return this.entity instanceof ObserverRange || original.call(map, player, chunkX, chunkZ);
    }
}
