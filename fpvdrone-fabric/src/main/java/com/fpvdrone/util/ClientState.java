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

    /** 1.1.8 tooltips name the exit-view binding ("CH6", "V", ...); empty on a dedicated server. */
    public static String exitKeyLabel() {
        return CLIENT ? com.fpvdrone.client.KeyManager.exitLabel() : "";
    }

    /** The client's level, or null on a dedicated server. */
    public static net.minecraft.world.level.@org.jspecify.annotations.Nullable Level level() {
        return CLIENT ? Client.level() : null;
    }

    /** The client's player, or null on a dedicated server. */
    public static net.minecraft.world.entity.player.@org.jspecify.annotations.Nullable Player player() {
        return CLIENT ? Client.player() : null;
    }

    /** Loaded only on the client: returning ClientLevel/LocalPlayer as common types makes the verifier load them. */
    private static final class Client {
        static net.minecraft.world.level.Level level() {
            return Minecraft.getInstance().level;
        }

        static net.minecraft.world.entity.player.Player player() {
            return Minecraft.getInstance().player;
        }
    }
}
