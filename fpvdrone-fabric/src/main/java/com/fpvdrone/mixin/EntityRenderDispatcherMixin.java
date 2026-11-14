package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.client.DronePlayerRenderer;
import com.fpvdrone.entity.DroneEntity;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.render.FrameCounter;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Which entities the level draws:
 * <ul>
 *   <li>any player who is currently flying a drone is hidden (their body is shown by the pilot-body entity instead);</li>
 *   <li>the drone you fly or watch through your goggles is drawn at most once per frame (the original's double-render guard);</li>
 *   <li>with the clean feed (F6) on, your own drone is not drawn at all (F1 only hides the HUD, as in the original).</li>
 * </ul>
 * The original's other parts need no code here: drone shadows are off (shadow radius 0), and the thermal camera that
 * turned all shadows off is not part of this port.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Unique
    private long fpvdrone$lastDroneRenderFrame = -1L;
    @Unique
    private UUID fpvdrone$lastDroneRenderId = null;

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void fpvdrone$hideFpvPlayers(E entity, Frustum culler, double camX, double camY, double camZ, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && DronePlayerRenderer.shouldHide(player)) {
            cir.setReturnValue(false);
            return;
        }

        if (entity instanceof DroneEntity drone) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }

            if (com.fpvdrone.client.CleanFeed.hidesDrone(drone)) {
                cir.setReturnValue(false);
                return;
            }

            if (fpvdrone$shouldGuard(mc, drone)) {
                long currentFrame = FrameCounter.get();
                UUID droneId = drone.getUUID();
                if (currentFrame == this.fpvdrone$lastDroneRenderFrame && droneId.equals(this.fpvdrone$lastDroneRenderId)) {
                    cir.setReturnValue(false);
                    return;
                }

                this.fpvdrone$lastDroneRenderFrame = currentFrame;
                this.fpvdrone$lastDroneRenderId = droneId;
            }
        }
    }

    /** The drone the local player flies, or the one their worn goggles are linked to. */
    @Unique
    private static boolean fpvdrone$shouldGuard(Minecraft mc, DroneEntity drone) {
        boolean isActiveDrone = FPVMod.getActiveDroneId(mc.player.getUUID()).map(id -> id.equals(drone.getUUID())).orElse(false);
        if (isActiveDrone && FPVMod.getPhysicsState(mc.player.getUUID()) != null) {
            return true;
        }

        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof FpvGogglesItem) {
            Optional<UUID> linkedId = FpvGogglesItem.getLinkedDroneId(helmet);
            return linkedId.isPresent() && linkedId.get().equals(drone.getUUID());
        }

        return false;
    }
}
