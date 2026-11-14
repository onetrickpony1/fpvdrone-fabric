package com.fpvdrone.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A sound in the data files (a drone's looping engine, a projectile's explosion or launch): either one
 * {@code SoundId} or several distance {@code Layers} that cross-fade into each other (close, middle, far).
 *
 * <p>JSON: {@code {"SoundId": "drone_engine", "MaxRange": 80, "Volume": 1.2}} or
 * {@code {"Layers": [{"SoundId": "a", "MaxRange": 48}, {"SoundId": "b", "MaxRange": 128}], "MaxRange": 128,
 * "CrossFadeWidth": 32, "Volume": 1.6}}.
 */
public record SoundProfile(float maxRange, float volume, List<Layer> layers) {
    private static final float DEFAULT_CROSS_FADE = 16.0F;
    /** Shared default drone engine: drone_engine, 80 blocks, volume 1.2 (the 1.1.4 constants). */
    public static final SoundProfile DRONE_ENGINE = single("drone_engine", 80.0F, 1.2F);
    /** Shared default explosion: bomblet_close up to 48 blocks, bomblet_distant to 128, 32-block cross-fade, 1.6. */
    public static final SoundProfile BOMBLET = layered("bomblet_close", "bomblet_distant", 48.0F, 128.0F, 32.0F, 1.6F);

    private static final Codec<LayerJson> LAYER_JSON = RecordCodecBuilder.create(
        i -> i.group(Codec.STRING.fieldOf("SoundId").forGetter(LayerJson::soundId), Codec.FLOAT.fieldOf("MaxRange").forGetter(LayerJson::maxRange))
            .apply(i, LayerJson::new)
    );
    private static final Codec<ProfileJson> PROFILE_JSON = RecordCodecBuilder.create(
        i -> i.group(
                Codec.STRING.optionalFieldOf("SoundId").forGetter(ProfileJson::soundId),
                Codec.floatRange(1.0F, 1024.0F).fieldOf("MaxRange").forGetter(ProfileJson::maxRange),
                Codec.floatRange(0.0F, 8.0F).optionalFieldOf("Volume", 1.0F).forGetter(ProfileJson::volume),
                Codec.floatRange(1.0F, 1024.0F).optionalFieldOf("CrossFadeWidth", DEFAULT_CROSS_FADE).forGetter(ProfileJson::crossFadeWidth),
                LAYER_JSON.listOf().optionalFieldOf("Layers", List.of()).forGetter(ProfileJson::layers)
            )
            .apply(i, ProfileJson::new)
    );
    public static final Codec<SoundProfile> CODEC = PROFILE_JSON.comapFlatMap(SoundProfile::fromJson, SoundProfile::toJson);

    public SoundProfile {
        if (layers == null || layers.isEmpty()) {
            throw new IllegalArgumentException("SoundProfile needs at least one layer");
        }
    }

    public static SoundProfile single(String soundId, float maxRange, float volume) {
        return new SoundProfile(maxRange, volume, List.of(new Layer(soundId, 0.0F, 0.0F, maxRange, maxRange)));
    }

    public static SoundProfile layered(String close, String distant, float closeRange, float maxRange, float crossFade, float volume) {
        return build(List.of(new LayerJson(close, closeRange), new LayerJson(distant, maxRange)), maxRange, crossFade, volume);
    }

    public boolean isLayered() {
        return this.layers.size() > 1;
    }

    /** Per-layer gain at a distance: linear fall-off to MaxRange times each layer's cross-fade window. */
    public float[] layerGains(float distance) {
        float falloff = Math.max(0.0F, 1.0F - distance / Math.max(1.0F, this.maxRange));
        float[] gains = new float[this.layers.size()];

        for (int i = 0; i < gains.length; i++) {
            gains[i] = falloff * this.layers.get(i).weight(distance);
        }

        return gains;
    }

    private static DataResult<SoundProfile> fromJson(ProfileJson json) {
        if (!json.layers().isEmpty()) {
            return DataResult.success(build(json.layers(), json.maxRange(), json.crossFadeWidth(), json.volume()));
        }

        return json.soundId().isEmpty()
            ? DataResult.error(() -> "SoundProfile needs either SoundId or a non-empty Layers list")
            : DataResult.success(single(json.soundId().get(), json.maxRange(), json.volume()));
    }

    private static SoundProfile build(List<LayerJson> in, float maxRange, float crossFade, float volume) {
        float half = Math.min(crossFade, maxRange) * 0.5F;
        List<Layer> out = new ArrayList<>();
        float previousEnd = 0.0F;

        for (int i = 0; i < in.size(); i++) {
            LayerJson layer = in.get(i);
            boolean first = i == 0;
            boolean last = i == in.size() - 1;
            float end = last ? maxRange : Math.max(previousEnd, Math.min(layer.maxRange(), maxRange));
            float fadeInStart = first ? 0.0F : previousEnd - half;
            float fadeInEnd = first ? 0.0F : previousEnd + half;
            float fadeOutStart = last ? maxRange : end - half;
            float fadeOutEnd = last ? maxRange : end + half;
            out.add(new Layer(layer.soundId(), fadeInStart, fadeInEnd, fadeOutStart, fadeOutEnd));
            previousEnd = end;
        }

        return new SoundProfile(maxRange, volume, out);
    }

    private static ProfileJson toJson(SoundProfile profile) {
        List<Layer> layers = profile.layers();
        if (layers.size() == 1) {
            return new ProfileJson(Optional.of(layers.get(0).soundId()), profile.maxRange(), profile.volume(), DEFAULT_CROSS_FADE, List.of());
        }

        float crossFade = layers.get(0).fadeOutEnd() - layers.get(0).fadeOutStart();
        List<LayerJson> out = new ArrayList<>();

        for (int i = 0; i < layers.size(); i++) {
            Layer layer = layers.get(i);
            float range = i == layers.size() - 1 ? profile.maxRange() : (layer.fadeOutStart() + layer.fadeOutEnd()) / 2.0F;
            out.add(new LayerJson(layer.soundId(), range));
        }

        return new ProfileJson(Optional.empty(), profile.maxRange(), profile.volume(), crossFade, out);
    }

    /** One sound with a sine fade-in and cosine fade-out window over distance. */
    public record Layer(String soundId, float fadeInStart, float fadeInEnd, float fadeOutStart, float fadeOutEnd) {
        public float weight(float distance) {
            if (distance < this.fadeInStart || distance > this.fadeOutEnd) {
                return 0.0F;
            } else if (distance < this.fadeInEnd && this.fadeInEnd > this.fadeInStart) {
                float t = (distance - this.fadeInStart) / (this.fadeInEnd - this.fadeInStart);
                return (float)Math.sin(t * (Math.PI / 2));
            } else if (distance > this.fadeOutStart && this.fadeOutEnd > this.fadeOutStart) {
                float t = (distance - this.fadeOutStart) / (this.fadeOutEnd - this.fadeOutStart);
                return (float)Math.cos(t * (Math.PI / 2));
            } else {
                return 1.0F;
            }
        }
    }

    private record ProfileJson(Optional<String> soundId, float maxRange, float volume, float crossFadeWidth, List<LayerJson> layers) {
    }

    private record LayerJson(String soundId, float maxRange) {
    }
}
