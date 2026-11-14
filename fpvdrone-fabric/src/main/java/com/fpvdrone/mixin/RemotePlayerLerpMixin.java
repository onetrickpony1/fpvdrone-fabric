package com.fpvdrone.mixin;

import com.fpvdrone.client.FpvPlayerTracker;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({RemotePlayer.class})
public abstract class RemotePlayerLerpMixin {
    @Inject(
        method = {"tick"},
        at = {@At("RETURN")}
    )
    private void fpvdrone$forceExitRotationAfterTick(CallbackInfo ci) {
        RemotePlayer self = (RemotePlayer)(Object)this;
        UUID playerId = self.getUUID();
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            if (mc.player == null || !mc.player.getUUID().equals(playerId)) {
                if (FpvPlayerTracker.hasRecentlyExited(playerId)) {
                    float[] rot = FpvPlayerTracker.getExitRotation(playerId);
                    if (rot != null) {
                        float yaw = rot[0];
                        float pitch = rot[1];
                        self.setYRot(yaw);
                        self.setXRot(pitch);
                        self.yRotO = yaw;
                        self.xRotO = pitch;
                        self.setYHeadRot(yaw);
                        self.yHeadRotO = yaw;
                        self.setYBodyRot(yaw);
                        self.yBodyRotO = yaw;
                    }
                }
            }
        }
    }
}
