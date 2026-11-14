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
        com.fpvdrone.client.particle.FpvParticles.init();
        FpvPlayerBodyRenderer.init();
        KeyManager.init();
        // 1.1.8 thermal camera: pipelines, keys, footprints, frame hooks.
        com.fpvdrone.client.thermal.ThermalRenderHook.init();
        com.fpvdrone.render.CameraManager.init();
        HudHider.init();
        com.fpvdrone.render.GogglesHudOverlay.init();
        com.fpvdrone.render.StickHudOverlay.init();
        com.fpvdrone.client.interact.InteractMenuOverlay.init();
        com.fpvdrone.gui.GuiEvents.init();
        ItemClientSetup.init();
        // 1.1.8 dropped bomb
        EntityRendererRegistry.register(ModEntities.DROPPED_BOMB.get(), com.fpvdrone.client.renderer.DroppedBombRenderer::new);
        com.fpvdrone.client.model.BedrockModels.init();
        // 1.1.8 tactical map: live terrain sampling and recon store (Forge subscribers in 1.1.8)
        com.fpvdrone.client.map.MapTerrainSampler.register();
    }
}
