package com.fpvdrone;

import com.fpvdrone.video.VideoResolution;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class FpvConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FpvConfig.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "fpvdrone";
    private static final String CONFIG_FILE = "fpvdrone.json";
    private static int simpleRange = 0;
    private static final int BASE_MAX_RANGE = 350;
    private static final int BASE_FREEZE_START = 100;
    private static final int BASE_GROUND_PROX_DIST = 30;
    private static final int BASE_GROUND_PROX_HEIGHT = 15;
    private static int maxRange = 350;
    private static int freezeStartDistance = 100;
    private static float obstaclePenaltyMultiplier = 1.0F;
    private static float groundProximityPenaltyMax = 35.0F;
    private static int groundProximityDistanceStart = 30;
    private static int groundProximityHeightThreshold = 15;
    private static float heightBonusPerBlock = 0.1F;
    private static int heightBaseline = 64;
    private static float signalSmoothing = 0.15F;
    private static int zeroSignalFramesForDisconnect = 30;
    private static float freezeIntensity = 5.0F;
    private static float failsafeDescentRate = 0.5F;
    private static float failsafeHorizontalDamping = 0.95F;
    private static float explosionBlockPower = 0.0F;
    private static float explosionBlastRadius = 10.0F;
    private static float explosionBlastDamage = 40.0F;
    private static float explosionKillZone = 2.0F;
    private static boolean explosionOpensDoors = true;
    private static float kamikazeExplosionPower = 4.0F;
    private static int shrapnelCount = 128;
    private static boolean droneCollisionDamage = true;
    private static int droneObserverRange = 500;
    private static float defaultAngle = 25.0F;
    private static float fov = 120.0F;
    private static boolean forceKbm = false;
    private static boolean stickOverlay = false;
    private static VideoResolution videoResolution = VideoResolution.RES_25;
    private static String minecraftLanguage = null;

    public static void setMinecraftLanguage(String langCode) {
        if (langCode != null && !langCode.equals(minecraftLanguage)) {
            minecraftLanguage = langCode.toLowerCase(Locale.ROOT);
            save();
        }
    }

    private FpvConfig() {
    }

    public static int getMaxRange() {
        return maxRange;
    }

    public static int getFreezeStartDistance() {
        return freezeStartDistance;
    }

    public static float getObstaclePenaltyMultiplier() {
        return obstaclePenaltyMultiplier;
    }

    public static float getGroundProximityPenaltyMax() {
        return groundProximityPenaltyMax;
    }

    public static int getGroundProximityDistanceStart() {
        return groundProximityDistanceStart;
    }

    public static int getGroundProximityHeightThreshold() {
        return groundProximityHeightThreshold;
    }

    public static float getHeightBonusPerBlock() {
        return heightBonusPerBlock;
    }

    public static int getHeightBaseline() {
        return heightBaseline;
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

    public static float getExplosionBlockPower() {
        return explosionBlockPower;
    }

    public static float getExplosionBlastRadius() {
        return explosionBlastRadius;
    }

    public static float getExplosionBlastDamage() {
        return explosionBlastDamage;
    }

    public static float getExplosionKillZone() {
        return explosionKillZone;
    }

    public static boolean getExplosionOpensDoors() {
        return explosionOpensDoors;
    }

    public static float getKamikazeExplosionPower() {
        return kamikazeExplosionPower;
    }

    public static int getShrapnelCount() {
        return shrapnelCount;
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

    public static VideoResolution getVideoResolution() {
        return videoResolution;
    }

    public static void setVideoResolution(VideoResolution res) {
        videoResolution = res != null ? res : VideoResolution.RES_25;
        save();
    }

    private static Path getConfigDir() {
        return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("fpvdrone");
    }

    public static void load() {
        try {
            Path dir = getConfigDir();
            Files.createDirectories(dir);
            Path file = dir.resolve("fpvdrone.json");
            if (!Files.exists(file)) {
                LOGGER.info("No fpvdrone.json found, creating default config");
                save();
                return;
            }

            String raw = Files.readString(file);
            String cleaned = stripComments(raw);
            JsonObject json = JsonParser.parseString(cleaned).getAsJsonObject();
            if (json.has("simpleRange")) {
                simpleRange = clampInt(json.get("simpleRange").getAsInt(), 0, 10000);
            }

            if (json.has("maxRange")) {
                maxRange = clampInt(json.get("maxRange").getAsInt(), 50, 10000);
            }

            if (json.has("freezeStartDistance")) {
                freezeStartDistance = clampInt(json.get("freezeStartDistance").getAsInt(), 0, 10000);
            }

            if (json.has("obstaclePenaltyMultiplier")) {
                obstaclePenaltyMultiplier = clampFloat(json.get("obstaclePenaltyMultiplier").getAsFloat(), 0.0F, 10.0F);
            }

            if (json.has("groundProximityPenaltyMax")) {
                groundProximityPenaltyMax = clampFloat(json.get("groundProximityPenaltyMax").getAsFloat(), 0.0F, 100.0F);
            }

            if (json.has("groundProximityDistanceStart")) {
                groundProximityDistanceStart = clampInt(json.get("groundProximityDistanceStart").getAsInt(), 0, 10000);
            }

            if (json.has("groundProximityHeightThreshold")) {
                groundProximityHeightThreshold = clampInt(json.get("groundProximityHeightThreshold").getAsInt(), 1, 256);
            }

            if (json.has("heightBonusPerBlock")) {
                heightBonusPerBlock = clampFloat(json.get("heightBonusPerBlock").getAsFloat(), 0.0F, 10.0F);
            }

            if (json.has("heightBaseline")) {
                heightBaseline = clampInt(json.get("heightBaseline").getAsInt(), -64, 320);
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

            if (json.has("explosionBreaksBlocks")) {
                boolean legacy = json.get("explosionBreaksBlocks").getAsBoolean();
                explosionBlockPower = legacy ? 1.0F : 0.0F;
            }

            if (json.has("explosionBlockPower")) {
                explosionBlockPower = clampFloat(json.get("explosionBlockPower").getAsFloat(), 0.0F, 2.0F);
            }

            if (json.has("explosionBlastRadius")) {
                explosionBlastRadius = clampFloat(json.get("explosionBlastRadius").getAsFloat(), 1.0F, 32.0F);
            }

            if (json.has("explosionBlastDamage")) {
                explosionBlastDamage = clampFloat(json.get("explosionBlastDamage").getAsFloat(), 0.0F, 200.0F);
            }

            if (json.has("explosionKillZone")) {
                explosionKillZone = clampFloat(json.get("explosionKillZone").getAsFloat(), 0.0F, 16.0F);
            }

            if (json.has("explosionOpensDoors")) {
                explosionOpensDoors = json.get("explosionOpensDoors").getAsBoolean();
            }

            if (json.has("kamikazeExplosionPower")) {
                kamikazeExplosionPower = clampFloat(json.get("kamikazeExplosionPower").getAsFloat(), 1.0F, 10.0F);
            }

            if (json.has("shrapnelCount")) {
                shrapnelCount = clampInt(json.get("shrapnelCount").getAsInt(), 8, 512);
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

            if (json.has("videoResolution")) {
                try {
                    videoResolution = VideoResolution.valueOf(json.get("videoResolution").getAsString());
                } catch (IllegalArgumentException var6) {
                    LOGGER.warn("Invalid videoResolution in config, using default");
                }
            }

            applySimpleRange();
            LOGGER.info("Loaded FPV config from {}", file);
            save();
        } catch (Exception var7) {
            LOGGER.error("Failed to load fpvdrone.json, using defaults", var7);
        }
        loadDefinitions();
    }

    /** 1.1.8: drones, batteries, projectiles, armour, ramps and weapon damage rules from data/fpvdrone in the mod jar. */
    public static void loadDefinitions() {
        try {
            com.fpvdrone.data.ProjectileDefinitions.dOfFwvo();
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

    private static void applySimpleRange() {
        if (simpleRange > 0) {
            float scale = simpleRange / 350.0F;
            maxRange = simpleRange;
            freezeStartDistance = Math.round(100.0F * scale);
            groundProximityDistanceStart = Math.round(30.0F * scale);
            groundProximityHeightThreshold = clampInt(Math.round(15.0F * scale), 1, 256);
            LOGGER.info("simpleRange={}: auto-scaled distances (scale={}x)", simpleRange, String.format("%.2f", scale));
        }
    }

    public static void save() {
        try {
            Path dir = getConfigDir();
            Files.createDirectories(dir);
            Path file = dir.resolve("fpvdrone.json");
            Map<String, String> lang = loadConfigTranslations();
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  // ═══════════════════════════════════════════════════════\n");
            sb.append("  // FPV Drone — General Config\n");
            sb.append("  // ═══════════════════════════════════════════════════════\n");
            appendComment(sb, lang, "fpvdrone.config.header");
            sb.append("\n");
            sb.append("  // ── ").append(lang.getOrDefault("fpvdrone.config.section.simpleMode", "Simple Mode")).append(" ──\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.simpleRange");
            sb.append("  // Range: 0 (off) or 50 — 10000\n");
            sb.append("  \"simpleRange\": ").append(simpleRange).append(",\n");
            sb.append("\n");
            sb.append("  // ── ").append(lang.getOrDefault("fpvdrone.config.section.signal", "Signal / Range")).append(" ──\n");
            appendComment(sb, lang, "fpvdrone.config.section.signalNote");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.maxRange");
            sb.append("  // Range: 50 — 10000\n");
            sb.append("  \"maxRange\": ").append(maxRange).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.freezeStartDistance");
            sb.append("  // Range: 0 — 10000\n");
            sb.append("  \"freezeStartDistance\": ").append(freezeStartDistance).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.obstaclePenaltyMultiplier");
            sb.append("  // Range: 0.0 — 10.0\n");
            sb.append("  \"obstaclePenaltyMultiplier\": ").append(obstaclePenaltyMultiplier).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.groundProximityPenaltyMax");
            sb.append("  // Range: 0.0 — 100.0\n");
            sb.append("  \"groundProximityPenaltyMax\": ").append(groundProximityPenaltyMax).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.groundProximityDistanceStart");
            sb.append("  // Range: 0 — 10000\n");
            sb.append("  \"groundProximityDistanceStart\": ").append(groundProximityDistanceStart).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.groundProximityHeightThreshold");
            sb.append("  // Range: 1 — 256\n");
            sb.append("  \"groundProximityHeightThreshold\": ").append(groundProximityHeightThreshold).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.heightBonusPerBlock");
            sb.append("  // Range: 0.0 — 10.0\n");
            sb.append("  \"heightBonusPerBlock\": ").append(heightBonusPerBlock).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.heightBaseline");
            sb.append("  // Range: -64 — 320\n");
            sb.append("  \"heightBaseline\": ").append(heightBaseline).append(",\n");
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
            appendComment(sb, lang, "fpvdrone.config.explosionBlockPower");
            sb.append("  // Range: 0.0 — 2.0\n");
            sb.append("  \"explosionBlockPower\": ").append(explosionBlockPower).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.explosionBlastRadius");
            sb.append("  // Range: 1.0 — 32.0\n");
            sb.append("  \"explosionBlastRadius\": ").append(explosionBlastRadius).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.explosionBlastDamage");
            sb.append("  // Range: 0.0 — 200.0\n");
            sb.append("  \"explosionBlastDamage\": ").append(explosionBlastDamage).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.explosionKillZone");
            sb.append("  // Range: 0.0 — 16.0\n");
            sb.append("  \"explosionKillZone\": ").append(explosionKillZone).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.explosionOpensDoors");
            sb.append("  \"explosionOpensDoors\": ").append(explosionOpensDoors).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.kamikazeExplosionPower");
            sb.append("  // Range: 1.0 — 10.0\n");
            sb.append("  \"kamikazeExplosionPower\": ").append(kamikazeExplosionPower).append(",\n");
            sb.append("\n");
            appendComment(sb, lang, "fpvdrone.config.shrapnelCount");
            sb.append("  // Range: 8 — 512\n");
            sb.append("  \"shrapnelCount\": ").append(shrapnelCount).append(",\n");
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
            sb.append("  // Video resolution scale: RES_10, RES_15, RES_20, RES_25, RES_33, RES_50\n");
            sb.append("  \"videoResolution\": \"").append(videoResolution.name()).append("\"\n");
            sb.append("}\n");
            Files.writeString(file, sb.toString());
            LOGGER.info("Saved FPV config to {}", file);
        } catch (IOException var4) {
            LOGGER.error("Failed to save fpvdrone.json", var4);
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

        try {
            label65: {
                try (InputStream is = FpvConfig.class.getResourceAsStream(path)) {
                    if (is != null) {
                        JsonObject json = JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)).getAsJsonObject();

                        for (Entry<String, JsonElement> entry : json.entrySet()) {
                            if (entry.getKey().startsWith("fpvdrone.config.")) {
                                map.put(entry.getKey(), entry.getValue().getAsString());
                            }
                        }
                        break label65;
                    }
                }

                return;
            }
        } catch (Exception var9) {
            LOGGER.warn("Could not load lang file {}: {}", code, var9.getMessage());
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

    public static void applyServerConfig(
        int sMaxRange,
        int sFreezeStartDistance,
        float sObstaclePenaltyMultiplier,
        float sGroundProximityPenaltyMax,
        int sGroundProximityDistanceStart,
        int sGroundProximityHeightThreshold,
        float sHeightBonusPerBlock,
        int sHeightBaseline,
        float sSignalSmoothing,
        int sZeroSignalFramesForDisconnect,
        float sFreezeIntensity,
        float sFailsafeDescentRate,
        float sFailsafeHorizontalDamping,
        float sExplosionBlockPower,
        float sExplosionBlastRadius,
        float sExplosionBlastDamage,
        float sExplosionKillZone,
        boolean sExplosionOpensDoors,
        float sKamikazeExplosionPower,
        int sShrapnelCount,
        boolean sDroneCollisionDamage
    ) {
        maxRange = sMaxRange;
        freezeStartDistance = sFreezeStartDistance;
        obstaclePenaltyMultiplier = sObstaclePenaltyMultiplier;
        groundProximityPenaltyMax = sGroundProximityPenaltyMax;
        groundProximityDistanceStart = sGroundProximityDistanceStart;
        groundProximityHeightThreshold = sGroundProximityHeightThreshold;
        heightBonusPerBlock = sHeightBonusPerBlock;
        heightBaseline = sHeightBaseline;
        signalSmoothing = sSignalSmoothing;
        zeroSignalFramesForDisconnect = sZeroSignalFramesForDisconnect;
        freezeIntensity = sFreezeIntensity;
        failsafeDescentRate = sFailsafeDescentRate;
        failsafeHorizontalDamping = sFailsafeHorizontalDamping;
        explosionBlockPower = sExplosionBlockPower;
        explosionBlastRadius = sExplosionBlastRadius;
        explosionBlastDamage = sExplosionBlastDamage;
        explosionKillZone = sExplosionKillZone;
        explosionOpensDoors = sExplosionOpensDoors;
        kamikazeExplosionPower = sKamikazeExplosionPower;
        shrapnelCount = sShrapnelCount;
        droneCollisionDamage = sDroneCollisionDamage;
        LOGGER.info("Applied server config: maxRange={}, freezeStart={}", maxRange, freezeStartDistance);
    }
}
