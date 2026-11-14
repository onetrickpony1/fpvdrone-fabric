package com.fpvdrone.mixin;

import com.fpvdrone.FPVClientMod;
import com.fpvdrone.FPVMod;
import com.fpvdrone.entity.DroneEntity;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Minecraft.class})
public class MinecraftMixin {
    @Inject(
        method = {"isPaused"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$preventPauseInFpv(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = (Minecraft)(Object)this;
        if (mc.player != null && FPVMod.isArmed(mc.player.getUUID())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = {"handleKeybinds"},
        at = {@At("HEAD")}
    )
    private void fpvdrone$drainFpvLockedKeys(CallbackInfo ci) {
        Minecraft mc = (Minecraft)(Object)this;
        if (mc.player != null && !FPVMod.getActiveDroneId(mc.player.getUUID()).isEmpty()) {
            Options opts = mc.options;

            while (opts.keyDrop.consumeClick()) {
            }

            while (opts.keySwapOffhand.consumeClick()) {
            }

            for (KeyMapping km : opts.keyHotbarSlots) {
                while (km.consumeClick()) {
                }
            }

            while (opts.keyPickItem.consumeClick()) {
            }
        } else if (mc.player != null) {
            // 1.1.8: while the interact menu has options, its key (F by default) does not also swap hands.
            com.fpvdrone.client.interact.InteractMenu.Menu menu = com.fpvdrone.client.interact.InteractMenu.current();
            if (menu != null && !menu.options().isEmpty()) {
                while (mc.options.keySwapOffhand.consumeClick()) {
                }
            }
        }
    }

    @Inject(
        method = {"tick"},
        at = {@At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;animateTick(III)V"
        )}
    )
    private void fpvdrone$syncPlayerToDroneBeforeAnimateTick(CallbackInfo ci) {
        Minecraft mc = (Minecraft)(Object)this;
        if (mc.player != null) {
            UUID playerId = mc.player.getUUID();
            Optional<UUID> activeDroneId = FPVMod.getActiveDroneId(playerId);
            if (!activeDroneId.isEmpty()) {
                Optional<DroneEntity> droneOpt = FPVClientMod.getActiveDrone(playerId);
                if (!droneOpt.isEmpty()) {
                    DroneEntity drone = droneOpt.get();
                    mc.player.setPos(drone.getX(), drone.getY(), drone.getZ());
                }
            }
        }
    }
}
