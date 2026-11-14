package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.client.DronePlayerRenderer;
import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.item.FpvGogglesItem;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The player preview in the survival inventory. Always flagged as an inventory render so a pilot's model is not hidden there.
 * While flying (or armed, or wearing goggles whose drone has no signal) the player's rotation is driven by the drone, so the
 * preview is posed from the mouse alone, with the original mod's own (linear) mouse-follow factors.
 */
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

    @WrapOperation(
        method = "extractBackground",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;extractEntityInInventoryFollowsMouse(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"
        )
    )
    private void fpvdrone$fpvInventoryPlayer(
        GuiGraphicsExtractor graphics,
        int x0,
        int y0,
        int x1,
        int y1,
        int size,
        float offsetY,
        float mouseX,
        float mouseY,
        LivingEntity entity,
        Operation<Void> original
    ) {
        if (!fpvdrone$shouldOverride()) {
            original.call(graphics, x0, y0, x1, y1, size, offsetY, mouseX, mouseY, entity);
            return;
        }

        DronePlayerRenderer.setRenderingInventory(true);
        try {
            fpvdrone$extractPlayerInInventory(graphics, x0, y0, x1, y1, size, offsetY, mouseX, mouseY, entity);
        } finally {
            DronePlayerRenderer.setRenderingInventory(false);
        }
    }

    @Unique
    private static void fpvdrone$extractPlayerInInventory(
        GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int size, float offsetY, float mouseX, float mouseY, LivingEntity entity
    ) {
        float centerX = (x0 + x1) / 2.0F;
        float xOffset = centerX - mouseX;
        float yOffset = y0 + 50 - 30 - mouseY;
        Quaternionf rotation = new Quaternionf().rotateZ((float)Math.PI);
        Quaternionf xRotation = new Quaternionf().rotateX(yOffset * 0.02F);
        rotation.mul(xRotation);
        EntityRenderState renderState = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity).createRenderState(entity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        if (renderState instanceof LivingEntityRenderState living) {
            // Original: body yaw 180 + x * 0.5, head yaw 180 + x * 0.8 (the render state wants the head relative to the body).
            living.bodyRot = 180.0F + xOffset * 0.5F;
            living.yRot = xOffset * 0.8F - xOffset * 0.5F;
            living.xRot = -yOffset * 0.35F;
            // In FPV the player's hitbox is drone-sized (0.25 high). 26.3 places the preview by the hitbox height
            // (1.20 placed it by the feet), so use the standing size or the model ends up off-centre and clipped.
            net.minecraft.world.entity.EntityDimensions standing = entity.getType().getDimensions();
            living.boundingBoxWidth = standing.width();
            living.boundingBoxHeight = standing.height();
            living.scale = 1.0F;
        }

        Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + offsetY, 0.0F);
        graphics.entity(renderState, size, translation, rotation, xRotation, x0, y0, x1, y1);
    }

    @Unique
    private static boolean fpvdrone$shouldOverride() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return false;
        }

        if (FPVMod.isArmed(player.getUUID()) || FPVMod.getActiveDroneId(player.getUUID()).isPresent()) {
            return true;
        }

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof FpvGogglesItem)) {
            return false;
        }

        Optional<UUID> linkedId = FpvGogglesItem.getLinkedDroneId(helmet);
        return linkedId.isPresent() && FpvStatusChecker.hasNoSignalState(linkedId.get());
    }
}
