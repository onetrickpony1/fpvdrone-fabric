package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class DronePlayerRenderer {
    private static boolean renderingInventory = false;

    public static void setRenderingInventory(boolean value) {
        renderingInventory = value;
    }

    public static boolean isRenderingInventory() {
        return renderingInventory;
    }

    /** Returns true when this player's model must not be drawn (it is flying a drone); also restores exit rotation. */
    public static boolean shouldHide(Player player) {
        if (!renderingInventory) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                UUID playerId = player.getUUID();
                boolean isLocalPlayer = playerId.equals(mc.player.getUUID());
                if (isLocalPlayer) {
                    if (!FPVMod.getActiveDroneId(playerId).isEmpty()) {
                        return true;
                    }
                } else if (FpvPlayerTracker.isFrozen(playerId) && !FpvPlayerTracker.hasRecentlyExited(playerId)) {
                    return true;
                } else {
                    if (FpvPlayerTracker.hasRecentlyExited(playerId)) {
                        float[] rot = FpvPlayerTracker.getExitRotation(playerId);
                        if (rot != null) {
                            player.setYRot(rot[0]);
                            player.setXRot(rot[1]);
                            player.yRotO = rot[0];
                            player.xRotO = rot[1];
                            player.setYHeadRot(rot[0]);
                            player.yHeadRotO = rot[0];
                            player.setYBodyRot(rot[0]);
                            player.yBodyRotO = rot[0];
                        }
                    }
                }
            }
        }

        return false;
    }
}
