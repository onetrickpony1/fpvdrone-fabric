package com.fpvdrone.client;

import com.fpvdrone.client.renderer.DroneRenderer;
import com.fpvdrone.registry.ModEntities;
import com.fpvdrone.render.PilotBodyRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ClientSetup {
    public static void init() {
        EntityRendererRegistry.register(ModEntities.PILOT_BODY.get(), PilotBodyRenderer::new);
        EntityRendererRegistry.register(ModEntities.DRONE.get(), DroneRenderer::new);
        KeyManager.init();
        HudHider.init();
    }
}
