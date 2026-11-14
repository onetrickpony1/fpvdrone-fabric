package com.fpvdrone.util;

import com.fpvdrone.FpvConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/**
 * How far (blocks) explosion particles and decals are sent: the drone observer range, or further when Distant
 * Horizons draws terrain beyond it (1.1.8 reads DH's LOD radius from its config file).
 */
public final class ParticleSendRange {
    private static final int MAX = 16384;
    private static final int MIN = 64;
    private static final long RECHECK_MS = 10000L;
    private static Boolean dhLoaded;
    private static volatile int dhRange = -1;
    private static volatile long lastCheck = 0L;

    private ParticleSendRange() {
    }

    public static int get() {
        int observer = FpvConfig.getDroneObserverRange();
        int dh = distantHorizonsRange();
        int range = dh > 0 ? Math.max(observer, dh) : observer;
        return Math.min(MAX, Math.max(MIN, range));
    }

    /** Client-side fade distance for particles: the render distance, or the DH radius when that is larger. */
    public static int clientFade(int renderDistanceBlocks) {
        int dh = distantHorizonsRange();
        return dh <= 0 ? renderDistanceBlocks : Math.min(MAX, Math.max(renderDistanceBlocks, dh));
    }

    public static boolean isDistantHorizonsLoaded() {
        if (dhLoaded == null) {
            FabricLoader loader = FabricLoader.getInstance();
            dhLoaded = loader.isModLoaded("distanthorizons") || loader.isModLoaded("distant_horizons");
        }

        return dhLoaded;
    }

    private static int distantHorizonsRange() {
        if (!isDistantHorizonsLoaded()) {
            return -1;
        }

        long now = System.currentTimeMillis();
        if (now - lastCheck > RECHECK_MS || dhRange < 0) {
            lastCheck = now;
            int read = readDhRadius();
            dhRange = read > 0 ? read : 4096;
        }

        return dhRange;
    }

    private static int readDhRadius() {
        Path config = FabricLoader.getInstance().getConfigDir();
        Path[] candidates = new Path[]{
            config.resolve("DistantHorizons.toml"),
            FabricLoader.getInstance().getGameDir().resolve("configs").resolve("DistantHorizons.toml"),
            config.resolve("distanthorizons").resolve("DistantHorizons.toml")
        };

        for (Path path : candidates) {
            try {
                if (Files.isRegularFile(path)) {
                    for (String raw : Files.readAllLines(path)) {
                        String line = raw.trim();
                        if (!line.startsWith("#") && line.startsWith("lodChunkRenderDistanceRadius")) {
                            int eq = line.indexOf('=');
                            if (eq >= 0) {
                                String value = line.substring(eq + 1);
                                int hash = value.indexOf('#');
                                if (hash >= 0) {
                                    value = value.substring(0, hash);
                                }

                                return Math.min(MAX, Math.max(MIN, Integer.parseInt(value.trim()) * 16));
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return -1;
    }
}
