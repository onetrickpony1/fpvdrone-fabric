package com.fpvdrone.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SettingsLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsLoader.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "config/fpvdrone";
    private static final String CONTROLLER_CONFIG_FILE = "controller.json";
    private static final String RATE_PROFILE_FILE = "rates.json";

    private static Path getConfigDir() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config/fpvdrone");
    }

    private static void ensureConfigDir() throws IOException {
        Path dir = getConfigDir();
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    public static void saveControllerConfig(ControllerConfig config) {
        try {
            ensureConfigDir();
            JsonObject json = new JsonObject();
            JsonObject channels = new JsonObject();
            channels.addProperty("throttle", config.getThrottleChannel());
            channels.addProperty("yaw", config.getYawChannel());
            channels.addProperty("pitch", config.getPitchChannel());
            channels.addProperty("roll", config.getRollChannel());
            channels.addProperty("armButton", config.getArmButton());
            channels.addProperty("armChannel", config.getArmChannel());
            json.add("channels", channels);
            JsonObject arm = new JsonObject();
            arm.addProperty("channel", config.getArmChannel());
            arm.addProperty("isButton", config.isArmIsButton());
            arm.addProperty("invert", config.isInvertArm());
            json.add("arm", arm);
            JsonObject inversion = new JsonObject();
            inversion.addProperty("throttle", config.isInvertThrottle());
            inversion.addProperty("yaw", config.isInvertYaw());
            inversion.addProperty("pitch", config.isInvertPitch());
            inversion.addProperty("roll", config.isInvertRoll());
            json.add("inversion", inversion);
            json.addProperty("deadzone", config.getDeadzone());
            JsonArray axisRange = new JsonArray();

            for (float v : config.getAxisRange()) {
                axisRange.add(v);
            }

            json.add("axisRange", axisRange);
            JsonObject device = new JsonObject();
            device.addProperty("guid", config.getPreferredJoystickGuid());
            device.addProperty("name", config.getPreferredJoystickName());
            json.add("device", device);
            config.writeBindings(json);
            Path file = getConfigDir().resolve("controller.json");
            Files.writeString(file, GSON.toJson(json));
            LOGGER.info("Saved controller config to {}", file);
        } catch (IOException var10) {
            LOGGER.error("Failed to save controller config", var10);
        }
    }

    public static void loadControllerConfig(ControllerConfig config) {
        try {
            Path file = getConfigDir().resolve("controller.json");
            if (!Files.exists(file)) {
                LOGGER.info("No controller config found, using defaults");
                return;
            }

            String content = Files.readString(file);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            if (json.has("channels")) {
                JsonObject channels = json.getAsJsonObject("channels");
                if (channels.has("throttle")) {
                    config.setThrottleChannel(channels.get("throttle").getAsInt());
                }

                if (channels.has("yaw")) {
                    config.setYawChannel(channels.get("yaw").getAsInt());
                }

                if (channels.has("pitch")) {
                    config.setPitchChannel(channels.get("pitch").getAsInt());
                }

                if (channels.has("roll")) {
                    config.setRollChannel(channels.get("roll").getAsInt());
                }

                if (channels.has("armButton")) {
                    config.setArmButton(channels.get("armButton").getAsInt());
                }

                if (channels.has("armChannel")) {
                    config.setArmChannel(channels.get("armChannel").getAsInt());
                }
            }

            if (json.has("arm")) {
                JsonObject arm = json.getAsJsonObject("arm");
                if (arm.has("channel")) {
                    config.setArmChannel(arm.get("channel").getAsInt());
                }

                if (arm.has("isButton")) {
                    config.setArmIsButton(arm.get("isButton").getAsBoolean());
                }

                if (arm.has("invert")) {
                    config.setInvertArm(arm.get("invert").getAsBoolean());
                }
            }

            if (json.has("inversion")) {
                JsonObject inversion = json.getAsJsonObject("inversion");
                if (inversion.has("throttle")) {
                    config.setInvertThrottle(inversion.get("throttle").getAsBoolean());
                }

                if (inversion.has("yaw")) {
                    config.setInvertYaw(inversion.get("yaw").getAsBoolean());
                }

                if (inversion.has("pitch")) {
                    config.setInvertPitch(inversion.get("pitch").getAsBoolean());
                }

                if (inversion.has("roll")) {
                    config.setInvertRoll(inversion.get("roll").getAsBoolean());
                }
            }

            config.readBindings(json);
            if (json.has("deadzone")) {
                config.setDeadzone(json.get("deadzone").getAsFloat());
            }

            if (json.has("axisRange")) {
                JsonArray arr = json.getAsJsonArray("axisRange");
                if (arr.size() == 16) {
                    float[] range = new float[16];

                    for (int i = 0; i < 16; i++) {
                        range[i] = arr.get(i).getAsFloat();
                    }

                    config.setAxisRange(range);
                }
            }

            if (json.has("device")) {
                JsonObject device = json.getAsJsonObject("device");
                if (device.has("guid")) {
                    config.setPreferredJoystickGuid(device.get("guid").getAsString());
                }

                if (device.has("name")) {
                    config.setPreferredJoystickName(device.get("name").getAsString());
                }
            }

            LOGGER.info("Loaded controller config from {}", file);
        } catch (Exception var7) {
            LOGGER.error("Failed to load controller config", var7);
        }
    }

    public static void saveRateProfile(ControllerConfig config) {
        try {
            ensureConfigDir();
            JsonObject json = new JsonObject();
            JsonObject rcRate = new JsonObject();
            rcRate.addProperty("throttle", config.getRcRate(0));
            rcRate.addProperty("yaw", config.getRcRate(1));
            rcRate.addProperty("pitch", config.getRcRate(2));
            rcRate.addProperty("roll", config.getRcRate(3));
            json.add("rcRate", rcRate);
            JsonObject superRate = new JsonObject();
            superRate.addProperty("throttle", config.getSuperRate(0));
            superRate.addProperty("yaw", config.getSuperRate(1));
            superRate.addProperty("pitch", config.getSuperRate(2));
            superRate.addProperty("roll", config.getSuperRate(3));
            json.add("superRate", superRate);
            JsonObject expo = new JsonObject();
            expo.addProperty("throttle", config.getExpo(0));
            expo.addProperty("yaw", config.getExpo(1));
            expo.addProperty("pitch", config.getExpo(2));
            expo.addProperty("roll", config.getExpo(3));
            json.add("expo", expo);
            Path file = getConfigDir().resolve("rates.json");
            Files.writeString(file, GSON.toJson(json));
            LOGGER.info("Saved rate profile to {}", file);
        } catch (IOException var6) {
            LOGGER.error("Failed to save rate profile", var6);
        }
    }

    public static void loadRateProfile(ControllerConfig config) {
        try {
            Path file = getConfigDir().resolve("rates.json");
            if (!Files.exists(file)) {
                LOGGER.info("No rate profile found, using defaults");
                return;
            }

            String content = Files.readString(file);
            JsonObject json = JsonParser.parseString(content).getAsJsonObject();
            if (json.has("rcRate")) {
                JsonObject rcRate = json.getAsJsonObject("rcRate");
                if (rcRate.has("throttle")) {
                    config.setRcRate(0, rcRate.get("throttle").getAsFloat());
                }

                if (rcRate.has("yaw")) {
                    config.setRcRate(1, rcRate.get("yaw").getAsFloat());
                }

                if (rcRate.has("pitch")) {
                    config.setRcRate(2, rcRate.get("pitch").getAsFloat());
                }

                if (rcRate.has("roll")) {
                    config.setRcRate(3, rcRate.get("roll").getAsFloat());
                }
            }

            if (json.has("superRate")) {
                JsonObject superRate = json.getAsJsonObject("superRate");
                if (superRate.has("throttle")) {
                    config.setSuperRate(0, superRate.get("throttle").getAsFloat());
                }

                if (superRate.has("yaw")) {
                    config.setSuperRate(1, superRate.get("yaw").getAsFloat());
                }

                if (superRate.has("pitch")) {
                    config.setSuperRate(2, superRate.get("pitch").getAsFloat());
                }

                if (superRate.has("roll")) {
                    config.setSuperRate(3, superRate.get("roll").getAsFloat());
                }
            }

            if (json.has("expo")) {
                JsonObject expo = json.getAsJsonObject("expo");
                if (expo.has("throttle")) {
                    config.setExpo(0, expo.get("throttle").getAsFloat());
                }

                if (expo.has("yaw")) {
                    config.setExpo(1, expo.get("yaw").getAsFloat());
                }

                if (expo.has("pitch")) {
                    config.setExpo(2, expo.get("pitch").getAsFloat());
                }

                if (expo.has("roll")) {
                    config.setExpo(3, expo.get("roll").getAsFloat());
                }
            }

            LOGGER.info("Loaded rate profile from {}", file);
        } catch (Exception var5) {
            LOGGER.error("Failed to load rate profile", var5);
        }
    }

    public static void saveAll(ControllerConfig config) {
        saveControllerConfig(config);
        saveRateProfile(config);
    }

    public static void loadAll(ControllerConfig config) {
        loadControllerConfig(config);
        loadRateProfile(config);
    }
}
