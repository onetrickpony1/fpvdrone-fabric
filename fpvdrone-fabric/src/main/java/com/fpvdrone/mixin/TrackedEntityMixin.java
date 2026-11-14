package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.PilotBodyEntity;
import com.fpvdrone.entity.PilotBodyManager;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
    targets = {"net.minecraft.server.level.ChunkMap$TrackedEntity"}
)
public class TrackedEntityMixin {
    @Shadow
    @Final
    Entity entity;
    @Unique
    private double fpvdrone$savedX;
    @Unique
    private double fpvdrone$savedY;
    @Unique
    private double fpvdrone$savedZ;
    @Unique
    private float fpvdrone$savedYRot;
    @Unique
    private float fpvdrone$savedXRot;
    @Unique
    private boolean fpvdrone$didSwap;

    @Inject(
        method = {"updatePlayers"},
        at = {@At("HEAD")}
    )
    private void fpvdrone$beforeUpdatePlayers(List<ServerPlayer> players, CallbackInfo ci) {
        this.fpvdrone$didSwap = false;
        if (this.entity instanceof ServerPlayer trackedPlayer) {
            UUID playerId = trackedPlayer.getUUID();
            if (FPVMod.hasActiveDrone(playerId)) {
                PilotBodyEntity pilotBody = PilotBodyManager.getPilotBody(playerId);
                if (pilotBody != null && pilotBody.isAlive()) {
                    this.fpvdrone$savedX = trackedPlayer.getX();
                    this.fpvdrone$savedY = trackedPlayer.getY();
                    this.fpvdrone$savedZ = trackedPlayer.getZ();
                    this.fpvdrone$savedYRot = trackedPlayer.getYRot();
                    this.fpvdrone$savedXRot = trackedPlayer.getXRot();
                    this.fpvdrone$didSwap = true;
                    Vec3 pilotPos = pilotBody.position();
                    trackedPlayer.setPosRaw(pilotPos.x, pilotPos.y, pilotPos.z);
                    trackedPlayer.setYRot(pilotBody.getYRot());
                    trackedPlayer.setXRot(pilotBody.getXRot());
                }
            }
        }
    }

    @Inject(
        method = {"updatePlayers"},
        at = {@At("RETURN")}
    )
    private void fpvdrone$afterUpdatePlayers(List<ServerPlayer> players, CallbackInfo ci) {
        if (this.fpvdrone$didSwap) {
            this.fpvdrone$didSwap = false;
            if (this.entity instanceof ServerPlayer trackedPlayer) {
                trackedPlayer.setPosRaw(this.fpvdrone$savedX, this.fpvdrone$savedY, this.fpvdrone$savedZ);
                trackedPlayer.setYRot(this.fpvdrone$savedYRot);
                trackedPlayer.setXRot(this.fpvdrone$savedXRot);
            }
        }
    }
}
