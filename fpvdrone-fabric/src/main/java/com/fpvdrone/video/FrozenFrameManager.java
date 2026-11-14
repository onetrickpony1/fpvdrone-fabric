package com.fpvdrone.video;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The last video frame of each drone, shown frozen when its signal is lost. */
public class FrozenFrameManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrozenFrameManager.class);
    private static final FrozenFrameManager instance = new FrozenFrameManager();
    private final Map<UUID, RenderTarget> frozenFrames = new HashMap<>();

    private FrozenFrameManager() {
    }

    public static FrozenFrameManager getInstance() {
        return instance;
    }

    public void captureFrame(UUID droneId, RenderTarget source) {
        if (droneId == null || source == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        RenderTarget target = this.frozenFrames.get(droneId);
        if (target == null || target.width != width || target.height != height) {
            if (target != null) {
                target.destroyBuffers();
            }
            try {
                target = new TextureTarget("fpvdrone frozen frame", width, height, GpuFormat.RGBA8_UNORM, null);
                this.frozenFrames.put(droneId, target);
            } catch (Exception e) {
                LOGGER.error("Failed to create frozen frame target", e);
                return;
            }
        }
        GpuBlit.copy(source, target, FilterMode.NEAREST);
    }

    public boolean renderFrozenFrame(UUID droneId, RenderTarget destination) {
        if (droneId == null || destination == null) {
            return false;
        }
        RenderTarget source = this.frozenFrames.get(droneId);
        if (source == null) {
            return false;
        }
        GpuBlit.copy(source, destination, FilterMode.NEAREST);
        return true;
    }

    public boolean hasFrozenFrame(UUID droneId) {
        return droneId != null && this.frozenFrames.containsKey(droneId);
    }

    public void pruneStale(Set<UUID> retain) {
        if (this.frozenFrames.isEmpty()) {
            return;
        }
        Iterator<Entry<UUID, RenderTarget>> it = this.frozenFrames.entrySet().iterator();
        int freed = 0;
        while (it.hasNext()) {
            Entry<UUID, RenderTarget> e = it.next();
            if (!retain.contains(e.getKey())) {
                if (e.getValue() != null) {
                    e.getValue().destroyBuffers();
                }
                it.remove();
                freed++;
            }
        }
        if (freed > 0) {
            LOGGER.debug("FrozenFrameManager.pruneStale: released {} stale frame(s); {} remain", freed, this.frozenFrames.size());
        }
    }

    public void clearFrame(UUID droneId) {
        if (droneId != null) {
            RenderTarget target = this.frozenFrames.remove(droneId);
            if (target != null) {
                target.destroyBuffers();
            }
        }
    }

    public void cleanup() {
        for (RenderTarget target : this.frozenFrames.values()) {
            target.destroyBuffers();
        }
        this.frozenFrames.clear();
    }
}
