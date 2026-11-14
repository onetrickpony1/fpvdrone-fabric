package com.fpvdrone.mixin;

import com.fpvdrone.client.DronePlayerRenderer;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    @Inject(method = "extractEntityInInventoryFollowsMouse", at = @At("HEAD"))
    private static void fpvdrone$showInventoryPlayer(CallbackInfo ci) {
        DronePlayerRenderer.setRenderingInventory(true);
    }
    @Inject(method = "extractEntityInInventoryFollowsMouse", at = @At("RETURN"))
    private static void fpvdrone$finishInventoryPlayer(CallbackInfo ci) {
        DronePlayerRenderer.setRenderingInventory(false);
    }
}
