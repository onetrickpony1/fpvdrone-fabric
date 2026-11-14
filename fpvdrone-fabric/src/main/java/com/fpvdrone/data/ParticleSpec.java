package com.fpvdrone.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

/**
 * One entry of a {@code "particles"} list in a data file (1.1.8; the format follows Immersive Vehicles' JSON
 * particles): how many to spawn, how they move, fade, scale, recolour and which textures they cycle through.
 * Times are in ticks; vectors are {@code [x, y, z]}.
 */
public record ParticleSpec(
    String name,
    Type type,
    SpawnOrientation spawningOrientation,
    RenderOrientation renderingOrientation,
    boolean spawnEveryTick,
    boolean bright,
    boolean blended,
    float daytimeReductionFactor,
    boolean stopsOnGround,
    boolean ignoreCollision,
    int quantity,
    float distance,
    int duration,
    int durationRandomness,
    int movementDuration,
    /** -1 = not set. */
    float transparency,
    /** -1 = not set. */
    float toTransparency,
    int fadeInTransparencyTime,
    int fadeOutTransparencyTime,
    float scale,
    float toScale,
    int fadeInScaleTime,
    int fadeOutScaleTime,
    float hitboxSize,
    @Nullable String texture,
    List<String> textureList,
    List<Integer> textureDelays,
    boolean randomTexture,
    int color,
    /** -1 = not set. */
    int toColor,
    List<Integer> colorList,
    List<Integer> colorDelays,
    boolean randomColor,
    float @Nullable [] pos,
    float @Nullable [] rotationVelocity,
    float @Nullable [] spreadRandomness,
    float @Nullable [] relativeInheritedVelocityFactor,
    float @Nullable [] initialVelocity,
    float @Nullable [] movementVelocity,
    float @Nullable [] relativeMovementVelocity,
    float @Nullable [] terminalVelocity,
    boolean useBlockColor,
    boolean getBlockPropertiesFromGround
) {
    /** A flat decal stuck to a block face, picked at random from the given textures (shrapnel marks). */
    public static ParticleSpec decal(String name, List<String> textures) {
        return new ParticleSpec(
            name, Type.GENERIC, SpawnOrientation.WORLD, RenderOrientation.DECAL, false, false, false, 0.0F, false, true, 1, 0.0F, 600, 100, 0,
            1.0F, 1.0F, 0, 100, 0.75F, 0.0F, 0, 0, 0.2F, null, textures, List.of(), true, 0xFFFFFF, -1, List.of(), List.of(), false,
            new float[]{0.0F, 0.02F, 0.0F}, null, null, null, null, null, null, null, false, false
        );
    }

    /** Parses {@code json[key]}; entries that are not objects are skipped. */
    public static List<ParticleSpec> listFrom(JsonObject json, String key) {
        List<ParticleSpec> out = new ArrayList<>();
        if (json.has(key) && json.get(key).isJsonArray()) {
            int i = 0;

            for (JsonElement e : json.getAsJsonArray(key)) {
                if (e.isJsonObject()) {
                    out.add(parse(e.getAsJsonObject(), key + "[" + i++ + "]"));
                }
            }
        }

        return List.copyOf(out);
    }

    public static ParticleSpec parse(JsonObject j, String name) {
        Type type = Type.parse(str(j, "type", "generic"));
        return new ParticleSpec(
            name,
            type,
            SpawnOrientation.parse(str(j, "spawningOrientation", "world")),
            RenderOrientation.parse(str(j, "renderingOrientation", "player")),
            bool(j, "spawnEveryTick", false),
            bool(j, "isBright", type == Type.FLAME),
            bool(j, "isBlended", false),
            clamp(flt(j, "daytimeReductionFactor", 0.0F), 0.0F, 1.0F),
            bool(j, "stopsOnGround", false),
            bool(j, "ignoreCollision", false),
            clamp(integer(j, "quantity", 1), 1, 512),
            clamp(flt(j, "distance", 0.0F), 0.0F, 64.0F),
            clamp(integer(j, "duration", 0), 0, 12000),
            clamp(integer(j, "durationRandomness", 0), 0, 12000),
            clamp(integer(j, "movementDuration", 0), 0, 12000),
            j.has("transparency") ? clamp(flt(j, "transparency", 1.0F), 0.0F, 1.0F) : -1.0F,
            j.has("toTransparency") ? clamp(flt(j, "toTransparency", 0.0F), 0.0F, 1.0F) : -1.0F,
            clamp(integer(j, "fadeInTransparencyTime", 0), 0, 12000),
            clamp(integer(j, "fadeOutTransparencyTime", 0), 0, 12000),
            clamp(flt(j, "scale", 0.0F), 0.0F, 1024.0F),
            clamp(flt(j, "toScale", 0.0F), 0.0F, 1024.0F),
            clamp(integer(j, "fadeInScaleTime", 0), 0, 12000),
            clamp(integer(j, "fadeOutScaleTime", 0), 0, 12000),
            clamp(flt(j, "hitboxSize", 0.2F), 0.01F, 4.0F),
            j.has("texture") ? str(j, "texture", null) : null,
            strings(j, "textureList"),
            ints(j, "textureDelays"),
            bool(j, "randomTexture", false),
            color(j, "color", 0xFFFFFF),
            color(j, "toColor", -1),
            colorList(j),
            ints(j, "colorDelays"),
            bool(j, "randomColor", false),
            vec(j, "pos"),
            vec(j, "rotationVelocity"),
            vec(j, "spreadRandomness"),
            vec(j, "relativeInheritedVelocityFactor"),
            vec(j, "initialVelocity"),
            vec(j, "movementVelocity"),
            vec(j, "relativeMovementVelocity"),
            vec(j, "terminalVelocity"),
            bool(j, "useBlockColor", false),
            bool(j, "getBlockPropertiesFromGround", false)
        );
    }

    private static String str(JsonObject j, String k, String def) {
        try {
            return j.has(k) && !j.get(k).isJsonNull() ? j.get(k).getAsString() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static float flt(JsonObject j, String k, float def) {
        try {
            return j.has(k) ? j.get(k).getAsFloat() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static int integer(JsonObject j, String k, int def) {
        try {
            return j.has(k) ? j.get(k).getAsInt() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean bool(JsonObject j, String k, boolean def) {
        try {
            return j.has(k) ? j.get(k).getAsBoolean() : def;
        } catch (Exception e) {
            return def;
        }
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static float @Nullable [] vec(JsonObject j, String k) {
        if (j.has(k) && j.get(k).isJsonArray()) {
            JsonArray a = j.getAsJsonArray(k);
            float[] out = new float[3];

            for (int i = 0; i < 3 && i < a.size(); i++) {
                out[i] = a.get(i).getAsFloat();
            }

            return out;
        }

        return null;
    }

    private static List<String> strings(JsonObject j, String k) {
        List<String> out = new ArrayList<>();
        if (j.has(k) && j.get(k).isJsonArray()) {
            for (JsonElement e : j.getAsJsonArray(k)) {
                out.add(e.getAsString());
            }
        }

        return List.copyOf(out);
    }

    private static List<Integer> ints(JsonObject j, String k) {
        List<Integer> out = new ArrayList<>();
        if (j.has(k) && j.get(k).isJsonArray()) {
            for (JsonElement e : j.getAsJsonArray(k)) {
                out.add(e.getAsInt());
            }
        }

        return List.copyOf(out);
    }

    /** "RRGGBB" / "#RRGGBB" / "0xRRGGBB", or [r, g, b] in 0-1 or 0-255. */
    private static int color(JsonObject j, String k, int def) {
        if (!j.has(k)) {
            return def;
        }

        JsonElement e = j.get(k);

        try {
            if (e.isJsonArray()) {
                JsonArray a = e.getAsJsonArray();
                if (a.size() < 3) {
                    return def;
                }

                float r = a.get(0).getAsFloat();
                float g = a.get(1).getAsFloat();
                float b = a.get(2).getAsFloat();
                boolean unit = r <= 1.0F && g <= 1.0F && b <= 1.0F;
                int ri = clamp(Math.round(unit ? r * 255.0F : r), 0, 255);
                int gi = clamp(Math.round(unit ? g * 255.0F : g), 0, 255);
                int bi = clamp(Math.round(unit ? b * 255.0F : b), 0, 255);
                return ri << 16 | gi << 8 | bi;
            }

            return parseHex(e.getAsString(), def);
        } catch (Exception ex) {
            return def;
        }
    }

    private static int parseHex(String raw, int def) {
        String s = raw.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        } else if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }

        try {
            return Integer.parseInt(s, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static List<Integer> colorList(JsonObject j) {
        List<Integer> out = new ArrayList<>();
        if (j.has("colorList") && j.get("colorList").isJsonArray()) {
            for (JsonElement e : j.getAsJsonArray("colorList")) {
                try {
                    int c = parseHex(e.getAsString(), -1);
                    if (c != -1) {
                        out.add(c);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        return List.copyOf(out);
    }

    /** Built-in behaviour (default texture textures/particles/&lt;type&gt;.png and motion when no velocity is given). */
    public enum Type {
        SMOKE,
        FLAME,
        BUBBLE,
        BREAK,
        GENERIC;

        static Type parse(@Nullable String s) {
            if (s == null) {
                return GENERIC;
            }

            try {
                return valueOf(s.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return GENERIC;
            }
        }
    }

    /** How the quad faces: fixed, towards the camera, turning on Y only, along its motion, or flat on a block face. */
    public enum RenderOrientation {
        FIXED,
        PLAYER,
        YAXIS,
        MOTION,
        DECAL;

        static RenderOrientation parse(@Nullable String s) {
            if (s == null) {
                return PLAYER;
            }

            try {
                return valueOf(s.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return PLAYER;
            }
        }
    }

    /** FACING rotates pos and velocities into the burst's direction; the others use world axes. */
    public enum SpawnOrientation {
        ENTITY,
        WORLD,
        ATTACHED,
        FACING;

        static SpawnOrientation parse(@Nullable String s) {
            if (s == null) {
                return WORLD;
            }

            try {
                return valueOf(s.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return WORLD;
            }
        }
    }
}
