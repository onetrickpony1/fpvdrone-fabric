package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.item.FpvGogglesItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * In FPV the camera sits inside a small drone that flies close to blocks: the near plane comes in to
 * 0.01 (vanilla 0.05) so walls and gates it skims past aren't cut open, as the original's
 * getProjectionMatrix hook did.
 */
@Mixin(Camera.class)
public class FpvNearPlaneMixin {
    @ModifyArg(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setupPerspective(FFFFF)V"), index = 0)
    private float fpvdrone$nearPlane(float zNear) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return zNear;
        }
        if (FPVMod.getPhysicsState(mc.player.getUUID()) != null) {
            return 0.01F;
        }
        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof FpvGogglesItem && FpvGogglesItem.getLinkedDroneId(helmet).isPresent()) {
            return 0.01F;
        }
        return zNear;
    }
}
