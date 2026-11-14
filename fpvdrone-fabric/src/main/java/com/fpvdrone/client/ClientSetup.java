package com.fpvdrone.client;

import com.fpvdrone.client.renderer.BlurDiscRenderer;
import com.fpvdrone.client.renderer.DroneRenderer;
import com.fpvdrone.client.renderer.KirpichikiRenderer;
import com.fpvdrone.registry.ModEntities;
import com.fpvdrone.render.PilotBodyRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ClientSetup {
    public static void init() {
        EntityRendererRegistry.register(ModEntities.PILOT_BODY.get(), PilotBodyRenderer::new);
        EntityRendererRegistry.register(ModEntities.DRONE.get(), DroneRenderer::new);
        // Custom render pipelines must be registered before the shaders are compiled at the first resource load.
        DroneRenderer.registerPipelines();
        BlurDiscRenderer.init();
        KirpichikiRenderer.init();
        FpvPlayerBodyRenderer.init();
        KeyManager.init();
        HudHider.init();
        com.fpvdrone.render.GogglesHudOverlay.init();
        com.fpvdrone.render.StickHudOverlay.init();
        com.fpvdrone.gui.GuiEvents.init();
    }
}
