package com.fpvdrone.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;

/** Side-safe client queries for code that also runs on a dedicated server (tooltips, shared item logic). */
public final class ClientState {
    private static final boolean CLIENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

    private ClientState() {
    }

    public static boolean hasShiftDown() {
        return CLIENT && Minecraft.getInstance().hasShiftDown();
    }
}
