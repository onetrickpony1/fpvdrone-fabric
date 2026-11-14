package com.fpvdrone.mixin;

import com.fpvdrone.FPVMod;
import com.fpvdrone.client.FpvStatusChecker;
import com.fpvdrone.item.FpvGogglesItem;
import com.fpvdrone.render.FrameCounter;
import com.fpvdrone.render.LowResRenderer;
import com.fpvdrone.video.FrozenFrameManager;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.GameRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The goggles' video feed, as the original's GameRendererMixin: before the frame, decide whether the
 * low-res pass runs; after the world (just before the GUI), shrink and re-enlarge the picture, hold it
 * when the signal stutters, or show the drone's frozen last frame when the signal is gone.
 */
@Mixin(GameRenderer.class)
public abstract class VideoFeedMixin {
    @Unique
    private boolean fpvdrone$wasProcessing;
    @Unique
    private boolean fpvdrone$noSignalMode;

    @Inject(method = "render", at = @At("HEAD"))
    private void fpvdrone$beforeRender(CallbackInfo ci) {
        FrameCounter.increment();
        com.fpvdrone.render.GogglesHudOverlay.tickBattery();
        this.fpvdrone$wasProcessing = false;
        this.fpvdrone$noSignalMode = false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        boolean renderLevel = ((GameRenderer) (Object) this).gameRenderState().shouldRenderLevel;
        this.fpvdrone$noSignalMode = fpvdrone$noSignalDrone(mc) != null;
        LowResRenderer renderer = LowResRenderer.getInstance();
        if (renderer.isEnabled() && renderLevel) {
            renderer.beginRender();
            this.fpvdrone$wasProcessing = renderer.isRendering();
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V"))
    private void fpvdrone$beforeGui(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        boolean renderLevel = ((GameRenderer) (Object) this).gameRenderState().shouldRenderLevel;
        if (this.fpvdrone$noSignalMode && renderLevel) {
            UUID frozenDroneId = null;
            ItemStack helmet = mc.player != null ? mc.player.getItemBySlot(EquipmentSlot.HEAD) : ItemStack.EMPTY;
            if (helmet.getItem() instanceof FpvGogglesItem) {
                frozenDroneId = FpvGogglesItem.getLinkedDroneId(helmet).orElse(null);
            }
            if (frozenDroneId == null && mc.player != null) {
                frozenDroneId = FPVMod.getActiveDroneId(mc.player.getUUID()).orElse(null);
            }
            if (frozenDroneId == null) {
                LowResRenderer lr = LowResRenderer.getInstance();
                if (lr.isFrozenFrameMode()) {
                    frozenDroneId = lr.getFrozenFrameDroneId();
                }
            }
            if (frozenDroneId != null && FrozenFrameManager.getInstance().hasFrozenFrame(frozenDroneId)) {
                FrozenFrameManager.getInstance().renderFrozenFrame(frozenDroneId, mc.gameRenderer.mainRenderTarget());
            }
        }
        // 1.1.8: a flown loitering munition keeps its last frame (and shows it once the link drops)
        if (renderLevel) {
            com.fpvdrone.client.MunitionClientState.v5suI(mc.gameRenderer.mainRenderTarget());
        }
        LowResRenderer renderer = LowResRenderer.getInstance();
        if (renderer.isRendering()) {
            renderer.endRender();
        }
        this.fpvdrone$wasProcessing = false;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void fpvdrone$afterRender(CallbackInfo ci) {
        if (this.fpvdrone$wasProcessing) {
            LowResRenderer renderer = LowResRenderer.getInstance();
            if (renderer.isRendering()) {
                renderer.endRender();
            }
        }
        this.fpvdrone$wasProcessing = false;
    }

    /** The drone whose video has no signal: the goggles' linked drone, the flown one, or the frozen one. */
    @Unique
    private static UUID fpvdrone$noSignalDrone(Minecraft mc) {
        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getItem() instanceof FpvGogglesItem) {
            Optional<UUID> linked = FpvGogglesItem.getLinkedDroneId(helmet);
            if (linked.isPresent() && FpvStatusChecker.hasNoSignalState(linked.get())) {
                return linked.get();
            }
        }
        Optional<UUID> active = FPVMod.getActiveDroneId(mc.player.getUUID());
        if (active.isPresent() && FpvStatusChecker.hasNoSignalState(active.get())) {
            return active.get();
        }
        LowResRenderer lr = LowResRenderer.getInstance();
        return lr.isFrozenFrameMode() ? lr.getFrozenFrameDroneId() : null;
    }
}
