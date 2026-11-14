package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPacketListener.class})
public class ClientPacketListenerMixin {
    @Inject(
        method = {"handleMovePlayer"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void fpvdrone$suppressFpvPositionSnap(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            UUID playerId = mc.player.getUUID();
            if (!FPVMod.getActiveDroneId(playerId).isEmpty() || com.fpvdrone.client.MunitionClientState.iElAbEZe()) {
                mc.player.connection.send(new ServerboundAcceptTeleportationPacket(packet.id(), mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot()));
                ci.cancel();
            }
        }
    }
}
