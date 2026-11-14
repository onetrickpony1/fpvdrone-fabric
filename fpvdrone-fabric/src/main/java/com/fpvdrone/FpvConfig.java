package com.fpvdrone;

import com.fpvdrone.video.VideoResolution;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global mod config ({@code config/fpvdrone/fpvdrone.json}), 1.1.8 layout.
 *
 * <p>1.1.8 moved every per-drone number (link range, signal penalties, explosion power, shrapnel count, blast
 * radius/damage, kill zone, door opening) into the data files ({@code data/fpvdrone/drones|projectiles}). What is
 * left here is global: a range multiplier, signal smoothing, failsafe, the block-breaking master switch, collision
 * damage, the observer range and the client settings.
 */
public class FpvConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FpvConfig.class);
    private static final String CONFIG_DIR = "fpvdrone";
    private static final String CONFIG_FILE = "fpvdrone.json";
    private static float signalRangeMultiplier = 1.0F;
    private static float signalSmoothing = 0.15F;
    private static int zeroSignalFramesForDisconnect = 30;
    private static float freezeIntensity = 5.0F;
    private static float failsafeDescentRate = 0.5F;
    private static float failsafeHorizontalDamping = 0.95F;
    private static boolean explosionBreaksBlocks = true;
    private static boolean droneCollisionDamage = true;
    private static int droneObserverRange = 2048;
    private static float defaultAngle = 25.0F;
    private static float fov = 120.0F;
    private static boolean forceKbm = false;
    private static boolean stickOverlay = false;
    private static VideoResolution videoResolution = VideoResolution.RES_25;
    private static boolean liveMapObservation = true;
    private static boolean parallelParticleTicking = true;
    private static String minecraftLanguage = null;

    public static void setMinecraftLanguage(String langCode) {
        if (langCode != null && !langCode.equals(minecraftLanguage)) {
            minecraftLanguage = langCode.toLowerCase(Locale.ROOT);
            save();
        }
    }

    private FpvConfig() {
    }

    /** Scales every drone's MaxRange and FreezeStartDistance (1.1.8: applied to each drone's data). */
    public static float getSignalRangeMultiplier() {
        return signalRangeMultiplier;
    }

    public static float getSignalSmoothing() {
        return signalSmoothing;
    }

    public static int getZeroSignalFramesForDisconnect() {
        return zeroSignalFramesForDisconnect;
    }

    public static float getFreezeIntensity() {
        return freezeIntensity;
    }

    public static float getFailsafeDescentRate() {
        return failsafeDescentRate;
    }

    public static float getFailsafeHorizontalDamping() {
        return failsafeHorizontalDamping;
    }

    /** Master switch: when false no drone/warhead explosion breaks blocks; when true each projectile's BlockPower applies. */
    public static boolean isExplosionBreaksBlocks() {
        return explosionBreaksBlocks;
    }

    public static void setExplosionBreaksBlocks(boolean value) {
        explosionBreaksBlocks = value;
        save();
    }

    public static boolean getDroneCollisionDamage() {
        return droneCollisionDamage;
    }

    public static int getDroneObserverRange() {
        return droneObserverRange;
    }

    public static float getDefaultAngle() {
        return defaultAngle;
    }

    public static void setDefaultAngle(float angle) {
        defaultAngle = angle;
        save();
    }

    public static float getFov() {
        return fov;
    }

    public static void setFov(float value) {
        fov = value;
        save();
    }

    public static boolean isForceKbm() {
        return forceKbm;
    }

    public static void setForceKbm(boolean value) {
        forceKbm = value;
        save();
    }

    public static boolean isStickOverlay() {
        return stickOverlay;
    }

    public static void setStickOverlay(boolean value) {
        stickOverlay = value;
        save();
    }

    public static boolean isLiveMapObservation() {
        return liveMapObservation;
    }

    public static void setLiveMapObservation(boolean value) {
        liveMapObservation = value;
        save();
    }

    public static boolean isParallelParticleTicking() {
        return parallelParticleTicking;
    }

    public static void setParallelParticleTicking(boolean value) {
        parallelParticleTicking = value;
        save();
    }

    public static VideoResolution getVideoResolution() {
        return videoResolution;
    }

    public static void setVideoResolution(VideoResolution res) {
        videoResolution = res != null ? res : VideoResolution.RES_25;
        save();
    }

    private static Path getConfigDir() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR);
    }

    public static void load() {
        try {
            Path dir = getConfigDir();
            Files.createDirectories(dir);
            Path file = dir.resolve(CONFIG_FILE);
            if (!Files.exists(file)) {
                LOGGER.info("No fpvdrone.json found, creating default config");
                save();
            } else {
                String raw = Files.readString(file);
                JsonObject json = JsonParser.parseString(stripComments(raw)).getAsJsonObject();
                if (json.has("signalRangeMultiplier")) {
                    signalRangeMultiplier = clampFloat(json.get("signalRangeMultiplier").getAsFloat(), 0.05F, 20.0F);
                }

                if (json.has("signalSmoothing")) {
                    signalSmoothing = clampFloat(json.get("signalSmoothing").getAsFloat(), 0.01F, 1.0F);
                }

                if (json.has("zeroSignalFramesForDisconnect")) {
                    zeroSignalFramesForDisconnect = clampInt(json.get("zeroSignalFramesForDisconnect").getAsInt(), 1, 600);
                }

                if (json.has("freezeIntensity")) {
                    freezeIntensity = clampFloat(json.get("freezeIntensity").getAsFloat(), 0.0F, 50.0F);
                }

                if (json.has("failsafeDescentRate")) {
                    failsafeDescentRate = clampFloat(json.get("failsafeDescentRate").getAsFloat(), 0.0F, 10.0F);
                }

                if (json.has("failsafeHorizontalDamping")) {
                    failsafeHorizontalDamping = clampFloat(json.get("failsafeHorizontalDamping").getAsFloat(), 0.0F, 1.0F);
                }

                // A 1.1.4 file: any block power above zero turns the new master switch on.
                if (json.has("explosionBlockPower")) {
                    explosionBreaksBlocks = json.get("explosionBlockPower").getAsFloat() > 0.01F;
                }

                if (json.has("explosionBreaksBlocks")) {
                    explosionBreaksBlocks = json.get("explosionBreaksBlocks").getAsBoolean();
                }

                if (json.has("droneCollisionDamage")) {
                    droneCollisionDamage = json.get("droneCollisionDamage").getAsBoolean();
                }

                if (json.has("droneObserverRange")) {
                    droneObserverRange = clampInt(json.get("droneObserverRange").getAsInt(), 64, 2048);
                }

                if (json.has("defaultAngle")) {
                    defaultAngle = json.get("defaultAngle").getAsFloat();
                }

                if (json.has("fov")) {
                    fov = json.get("fov").getAsFloat();
                }

                if (json.has("forceKbm")) {
                    forceKbm = json.get("forceKbm").getAsBoolean();
                }

                if (json.has("stickOverlay")) {
                    stickOverlay = json.get("stickOverlay").getAsBoolean();
                }

                if (json.has("liveMapObservation")) {
                    liveMapObservation = json.get("liveMapObservation").getAsBoolean();
                }

                if (json.has("parallelParticleTicking")) {
                    parallelParticleTicking = json.get("parallelParticleTicking").getAsBoolean();
                }

                if (json.has("videoResolution")) {
                    try {
                        videoResolution = VideoResolution.valueOf(json.get("videoResolution").getAsString());
                    } catch (IllegalArgumentException e) {
                        LOGGER.warn("Invalid videoResolution in config, using default");
                    }
                }

                LOGGER.info("Loaded FPV config from {}", file);
                // Rewrites the file in the 1.1.8 layout, which drops the 1.1.4 per-drone keys.
                save();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load fpvdrone.json, using defaults", e);
        }
        loadDefinitions();
    }

    /** 1.1.8: drones, batteries, projectiles, armour, ramps and weapon damage rules from data/fpvdrone in the mod jar. */
    public static void loadDefinitions() {
        try {
            com.fpvdrone.data.ProjectileDefinitions.load();
            com.fpvdrone.data.BatteryDefinitions.wuhB();
            com.fpvdrone.data.DroneDefinitions.wuhB();
            com.fpvdrone.data.ArmorDefinitions.ibzfxic();
            com.fpvdrone.data.RampDefinitions.ibzfxic();
            com.fpvdrone.data.WeaponDamageRules.ibzfxic();
            com.fpvdrone.data.DroneItemIndex.v5suI();
        } catch (Exception e) {
            LOGGER.error("Failed to load drone definitions", e);
        }
    }

    public static void save() {
        try {
            Path dir = getConfigDir();
            Files.createDirectories(dir);
            Path file = dir.resolve(CONFIG_FILE);
            Map<String, String> lang = loadConfigTranslations();
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  // ═══════════════════════════════════════════════════════\n");
            sb.append("  // FPV Drone — General Config\n");
            sb.append("  // ═══════════════════════════════════════════════════════\n");
            appendComment(sb, lang, "fpvdrone.config.header");
            sb.append("\n");
            sb.append("  // ── ").append(lang.getOrDefault("fpvdrone.config.section.signal", "Signal / Range")).append(" ──\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.signalRangeMultiplier");
            sb.append("  // Range: 0.05 — 20.0\n");
            sb.append("  \"signalRangeMultiplier\": ").append(signalRangeMultiplier).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.signalSmoothing");
            sb.append("  // Range: 0.01 — 1.0\n");
            sb.append("  \"signalSmoothing\": ").append(signalSmoothing).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.zeroSignalFramesForDisconnect");
            sb.append("  // Range: 1 — 600\n");
            sb.append("  \"zeroSignalFramesForDisconnect\": ").append(zeroSignalFramesForDisconnect).append(",\n");
            sb.append("\n");
            sb.append("  // ── ").append(lang.getOrDefault("fpvdrone.config.section.videoFreeze", "Video Freeze")).append(" ──\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.freezeIntensity");
            sb.append("  // Range: 0.0 — 50.0\n");
            sb.append("  \"freezeIntensity\": ").append(freezeIntensity).append(",\n");
            sb.append("\n");
            sb.append("  // ── ").append(lang.getOrDefault("fpvdrone.config.section.failsafe", "Failsafe")).append(" ──\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.failsafeDescentRate");
            sb.append("  // Range: 0.0 — 10.0\n");
            sb.append("  \"failsafeDescentRate\": ").append(failsafeDescentRate).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.failsafeHorizontalDamping");
            sb.append("  // Range: 0.0 — 1.0\n");
            sb.append("  \"failsafeHorizontalDamping\": ").append(failsafeHorizontalDamping).append(",\n");
            sb.append("\n");
            sb.append("  // ── ").append(lang.getOrDefault("fpvdrone.config.section.explosion", "Explosion")).append(" ──\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.explosionBreaksBlocks");
            sb.append("  \"explosionBreaksBlocks\": ").append(explosionBreaksBlocks).append(",\n");
            sb.append("\n");
            sb.append("  // ── ").append(lang.getOrDefault("fpvdrone.config.section.damage", "Damage")).append(" ──\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.droneCollisionDamage");
            sb.append("  \"droneCollisionDamage\": ").append(droneCollisionDamage).append(",\n");
            sb.append("\n");
            sb.append("  // ── ").append(lang.getOrDefault("fpvdrone.config.section.observerRange", "Observer Range")).append(" ──\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.droneObserverRange");
            sb.append("  // Range: 64 — 2048\n");
            sb.append("  \"droneObserverRange\": ").append(droneObserverRange).append(",\n");
            sb.append("\n");
            sb.append("  // ── Client Settings (camera, input, video — ignored on dedicated servers) ──\n");
            sb.append("\n");
            sb.append("  // Camera tilt angle in degrees\n");
            sb.append("  \"defaultAngle\": ").append(defaultAngle).append(",\n");
            sb.append("\n");
            sb.append("  // Field of view in degrees\n");
            sb.append("  \"fov\": ").append(fov).append(",\n");
            sb.append("\n");
            sb.append("  // Force keyboard+mouse input, ignore connected controller\n");
            sb.append("  \"forceKbm\": ").append(forceKbm).append(",\n");
            sb.append("\n");
            sb.append("  // Show stick position overlay during flight\n");
            sb.append("  \"stickOverlay\": ").append(stickOverlay).append(",\n");
            sb.append("\n");
            sb.append("  // Fill the drone map from your loaded chunks as you explore (self-writing map)\n");
            sb.append("  \"liveMapObservation\": ").append(liveMapObservation).append(",\n");
            sb.append("\n");
            sb.append("  // Tick explosion/detonation particles in parallel (multi-threaded) for less lag\n");
            sb.append("  // during a big burst. Self-disables for the session if a tick ever errors.\n");
            sb.append("  \"parallelParticleTicking\": ").append(parallelParticleTicking).append(",\n");
            sb.append("\n");
            sb.append("  // Video resolution scale: RES_10, RES_15, RES_20, RES_25, RES_33, RES_50\n");
            sb.append("  \"videoResolution\": \"").append(videoResolution.name()).append("\"\n");
            sb.append("}\n");
            Files.writeString(file, sb.toString());
        } catch (IOException e) {
            LOGGER.error("Failed to save fpvdrone.json", e);
        }
    }

    private static Map<String, String> loadConfigTranslations() {
        String code;
        if (minecraftLanguage != null) {
            code = minecraftLanguage;
        } else {
            String language = Locale.getDefault().getLanguage();
            if ("ru".equals(language)) {
                code = "ru_ru";
            } else if ("de".equals(language)) {
                code = "de_de";
            } else if ("es".equals(language)) {
                code = "es_es";
            } else if ("zh".equals(language)) {
                code = "zh_cn";
            } else {
                code = "en_us";
            }
        }

        Map<String, String> map = new HashMap<>();
        fillTranslations(map, "en_us");
        if (!"en_us".equals(code)) {
            fillTranslations(map, code);
        }

        return map;
    }

    private static void fillTranslations(Map<String, String> map, String code) {
        String path = "/assets/fpvdrone/lang/" + code + ".json";

        try (InputStream is = FpvConfig.class.getResourceAsStream(path)) {
            if (is != null) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();

                for (Entry<String, JsonElement> entry : json.entrySet()) {
                    if (entry.getKey().startsWith("fpvdrone.config.")) {
                        map.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not load lang file {}: {}", code, e.getMessage());
        }
    }

    private static void appendComment(StringBuilder sb, Map<String, String> lang, String key) {
        String text = lang.getOrDefault(key, key);

        for (String line : text.split("\n")) {
            sb.append("  // ").append(line).append("\n");
        }
    }

    private static String stripComments(String text) {
        StringBuilder sb = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (escaped) {
                sb.append(c);
                escaped = false;
            } else if (c == '\\' && inString) {
                sb.append(c);
                escaped = true;
            } else if (c == '"') {
                inString = !inString;
                sb.append(c);
            } else if (!inString && c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                while (i < text.length() && text.charAt(i) != '\n') {
                    i++;
                }

                sb.append('\n');
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clampFloat(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Server values pushed to every client on join and after {@code /fpvdrones explosive}. */
    public static void applyServerConfig(
        float sSignalRangeMultiplier,
        float sSignalSmoothing,
        int sZeroSignalFramesForDisconnect,
        float sFreezeIntensity,
        float sFailsafeDescentRate,
        float sFailsafeHorizontalDamping,
        boolean sExplosionBreaksBlocks,
        boolean sDroneCollisionDamage
    ) {
        signalRangeMultiplier = sSignalRangeMultiplier;
        signalSmoothing = sSignalSmoothing;
        zeroSignalFramesForDisconnect = sZeroSignalFramesForDisconnect;
        freezeIntensity = sFreezeIntensity;
        failsafeDescentRate = sFailsafeDescentRate;
        failsafeHorizontalDamping = sFailsafeHorizontalDamping;
        explosionBreaksBlocks = sExplosionBreaksBlocks;
        droneCollisionDamage = sDroneCollisionDamage;
        LOGGER.info("Applied server config: signalRangeMultiplier={}, explosionBreaksBlocks={}", signalRangeMultiplier, explosionBreaksBlocks);
    }
}
