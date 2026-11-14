package com.fpvdrone.client;

import com.fpvdrone.FPVMod;
import com.fpvdrone.item.FpvGogglesItem;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * While the player looks through linked goggles (camera at the drone, not flying), vanilla skips the player's own body
 * because it is the camera entity. This puts it back so the pilot can see themselves from the drone.
 * The Forge version drew the player at RenderLevelStageEvent AFTER_ENTITIES; here the player's render state is added to
 * the level's entity list right after extraction, so it is drawn with the other entities.
 */
public class FpvPlayerBodyRenderer {
    private static boolean isRenderingPlayer = false;

    private FpvPlayerBodyRenderer() {
    }

    public static boolean isRenderingPlayer() {
        return isRenderingPlayer;
    }

    /** Registers the extraction hook; call once during client init. */
    public static void init() {
        LevelRenderEvents.END_EXTRACTION.register(FpvPlayerBodyRenderer::onEndExtraction);
    }

    private static void onEndExtraction(LevelExtractionContext context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) {
            return;
        }

        if (FPVMod.isArmed(mc.player.getUUID())) {
            return;
        }

        ItemStack helmet = mc.player.getItemBySlot(EquipmentSlot.HEAD);
        if (!(helmet.getItem() instanceof FpvGogglesItem)) {
            return;
        }

        Optional<UUID> linkedId = FpvGogglesItem.getLinkedDroneId(helmet);
        if (linkedId.isEmpty() || FpvStatusChecker.hasNoSignalState(linkedId.get())) {
            return;
        }

        // Vanilla draws the camera entity itself when the camera is detached; do not add a second copy.
        if (context.camera().entity() != mc.player || context.camera().isDetached()) {
            return;
        }

        // Same rule as the original's RenderPlayerEvent hook: a player flying a drone is not drawn.
        if (DronePlayerRenderer.shouldHide(mc.player)) {
            return;
        }

        float partialTick = context.deltaTracker().getGameTimeDeltaPartialTick(true);
        isRenderingPlayer = true;
        try {
            EntityRenderState state = mc.getEntityRenderDispatcher().extractEntity(mc.player, partialTick);
            // The original placed the body at the player's current (not interpolated) position.
            state.x = mc.player.getX();
            state.y = mc.player.getY();
            state.z = mc.player.getZ();
            context.levelState().entityRenderStates.add(state);
        } finally {
            isRenderingPlayer = false;
        }
    }
}
