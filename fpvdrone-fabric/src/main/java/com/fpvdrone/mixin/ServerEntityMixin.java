package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.PilotBodyEntity;
import com.fpvdrone.entity.PilotBodyManager;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerEntity.class})
public class ServerEntityMixin {
    @Shadow
    @Final
    private Entity entity;
    @Unique
    private double fpvdrone$savedX;
    @Unique
    private double fpvdrone$savedY;
    @Unique
    private double fpvdrone$savedZ;
    @Unique
    private double fpvdrone$savedOldX;
    @Unique
    private double fpvdrone$savedOldY;
    @Unique
    private double fpvdrone$savedOldZ;
    @Unique
    private float fpvdrone$savedYRot;
    @Unique
    private float fpvdrone$savedXRot;
    @Unique
    private float fpvdrone$savedYRotO;
    @Unique
    private float fpvdrone$savedXRotO;
    @Unique
    private float fpvdrone$savedYHeadRot;
    @Unique
    private float fpvdrone$savedYBodyRot;
    @Unique
    private boolean fpvdrone$didSwap;
    @Unique
    private int fpvdrone$headRotFixTicks = 0;
    @Unique
    private float fpvdrone$lastPilotYaw = 0.0F;
    @Unique
    private float fpvdrone$lastPilotPitch = 0.0F;
    @Unique
    private static final Logger fpvdrone$LOGGER = LoggerFactory.getLogger("FPV-ServerEntity");

    @Inject(
        method = {"sendChanges"},
        at = {@At("HEAD")}
    )
    private void fpvdrone$beforeSendChanges(CallbackInfo ci) {
        this.fpvdrone$didSwap = false;
        if (this.entity instanceof ServerPlayer player) {
            boolean hasActive = FPVMod.hasActiveDrone(player.getUUID());
            if (!hasActive) {
                if (this.fpvdrone$headRotFixTicks > 0) {
                    this.fpvdrone$headRotFixTicks--;
                    player.setYRot(this.fpvdrone$lastPilotYaw);
                    player.setXRot(this.fpvdrone$lastPilotPitch);
                    player.yRotO = this.fpvdrone$lastPilotYaw;
                    player.xRotO = this.fpvdrone$lastPilotPitch;
                    player.setYHeadRot(this.fpvdrone$lastPilotYaw);
                    player.yBodyRot = this.fpvdrone$lastPilotYaw;
                }
            } else {
                this.fpvdrone$headRotFixTicks = 10;
                PilotBodyEntity pilotBody = PilotBodyManager.getPilotBody(player.getUUID());
                if (pilotBody != null && pilotBody.isAlive()) {
                    this.fpvdrone$saveAndSwapToPilotBody(player, pilotBody);
                }
            }
        }
    }

    @Unique
    private void fpvdrone$saveAndSwapToPilotBody(ServerPlayer player, PilotBodyEntity pilotBody) {
        this.fpvdrone$savedX = player.getX();
        this.fpvdrone$savedY = player.getY();
        this.fpvdrone$savedZ = player.getZ();
        this.fpvdrone$savedOldX = player.xOld;
        this.fpvdrone$savedOldY = player.yOld;
        this.fpvdrone$savedOldZ = player.zOld;
        this.fpvdrone$savedYRot = player.getYRot();
        this.fpvdrone$savedXRot = player.getXRot();
        this.fpvdrone$savedYRotO = player.yRotO;
        this.fpvdrone$savedXRotO = player.xRotO;
        this.fpvdrone$savedYHeadRot = player.getYHeadRot();
        this.fpvdrone$savedYBodyRot = player.yBodyRot;
        this.fpvdrone$didSwap = true;
        Vec3 pilotPos = pilotBody.position();
        player.setPosRaw(pilotPos.x, pilotPos.y, pilotPos.z);
        player.xOld = pilotPos.x;
        player.yOld = pilotPos.y;
        player.zOld = pilotPos.z;
        float pilotYaw = pilotBody.getYRot();
        float pilotPitch = pilotBody.getXRot();
        player.setYRot(pilotYaw);
        player.setXRot(pilotPitch);
        player.yRotO = pilotYaw;
        player.xRotO = pilotPitch;
        player.setYHeadRot(pilotYaw);
        player.yBodyRot = pilotYaw;
        this.fpvdrone$lastPilotYaw = pilotYaw;
        this.fpvdrone$lastPilotPitch = pilotPitch;
    }

    @Inject(
        method = {"sendChanges"},
        at = {@At("RETURN")}
    )
    private void fpvdrone$afterSendChanges(CallbackInfo ci) {
        if (this.fpvdrone$didSwap) {
            this.fpvdrone$didSwap = false;
            if (this.entity instanceof ServerPlayer player) {
                player.setPosRaw(this.fpvdrone$savedX, this.fpvdrone$savedY, this.fpvdrone$savedZ);
                player.xOld = this.fpvdrone$savedOldX;
                player.yOld = this.fpvdrone$savedOldY;
                player.zOld = this.fpvdrone$savedOldZ;
                player.setYRot(this.fpvdrone$savedYRot);
                player.setXRot(this.fpvdrone$savedXRot);
                player.yRotO = this.fpvdrone$savedYRotO;
                player.xRotO = this.fpvdrone$savedXRotO;
                player.setYHeadRot(this.fpvdrone$savedYHeadRot);
                player.yBodyRot = this.fpvdrone$savedYBodyRot;
            }
        }
    }
}
