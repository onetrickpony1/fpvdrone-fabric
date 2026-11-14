package com.fpvdrone.client.audio;

import com.fpvdrone.data.SoundProfile;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Explosion and launch sounds played straight through OpenAL (1.1.8): each projectile has a sound profile with
 * distance layers that cross-fade (close / distant, or more), explosions close together in time merge into one louder
 * voice, indoor blasts use the indoor ambience pair, sources sit relative to the camera, and voices pause with the game.
 */
public class ExplosionAudioEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExplosionAudioEngine.class);
    private static final int POOL_SIZE = 16;
    private static final float MIN_AUDIBLE_GAIN = 0.01F;
    private static final float MERGE_RADIUS = 5.0F;
    private static final long MERGE_WINDOW_MS = 200L;
    private static final float MAX_GAIN_BOOST = 2.0F;
    private static final ExplosionAudioEngine INSTANCE = new ExplosionAudioEngine();
    private static final String INDOORS = "ambience_indoors";
    private static final String INDOORS_DISTANT = "ambience_indoors_distant";
    /** Decoded up front on a background thread so the first explosion does not stall. */
    private static final String[] PRELOAD = new String[]{
        "bomblet_close0", "bomblet_close1", "bomblet_distant0", "bomblet_distant1", "ambience_indoors0", "ambience_indoors_distant0"
    };
    private boolean initialized = false;
    private final int[] poolSources = new int[POOL_SIZE];
    private final ArrayDeque<Integer> freeSources = new ArrayDeque<>(POOL_SIZE);
    private final List<Voice> activeVoices = new ArrayList<>();
    private final float[] rel = new float[3];
    private boolean paused = false;
    private final AlAudio.ContextWatch contextWatch = new AlAudio.ContextWatch();
    private final Map<String, Integer> buffers = new HashMap<>();
    private volatile OggDecoder.Decoded[] pcmReady = null;
    private boolean decodeStarted = false;

    private ExplosionAudioEngine() {
    }

    public static ExplosionAudioEngine get() {
        return INSTANCE;
    }

    public void initIfNeeded() {
        this.startBackgroundDecodeIfNeeded();
        this.init();
    }

    private synchronized void startBackgroundDecodeIfNeeded() {
        if (!this.decodeStarted) {
            this.decodeStarted = true;
            byte[][] raw = new byte[PRELOAD.length][];

            for (int i = 0; i < PRELOAD.length; i++) {
                raw[i] = AlAudio.readSound(PRELOAD[i], "ogg", true, LOGGER);
            }

            Thread t = new Thread(() -> {
                OggDecoder.Decoded[] out = new OggDecoder.Decoded[PRELOAD.length];

                for (int i = 0; i < PRELOAD.length; i++) {
                    out[i] = decodeOgg(PRELOAD[i], raw[i]);
                }

                this.pcmReady = out;
            }, "FPVDrone-Explosion-AudioDecode");
            t.setDaemon(true);
            t.start();
        }
    }

    private void init() {
        if (!this.initialized) {
            long context = ALC10.alcGetCurrentContext();
            if (context != 0L) {
                OggDecoder.Decoded[] ready = this.pcmReady;
                if (ready != null) {
                    for (int i = 0; i < POOL_SIZE; i++) {
                        this.poolSources[i] = AL10.alGenSources();
                        AL10.alSourcef(this.poolSources[i], AL10.AL_MIN_GAIN, 0.0F);
                        this.freeSources.add(this.poolSources[i]);
                    }

                    for (int i = 0; i < PRELOAD.length; i++) {
                        int buf = AlAudio.upload(ready[i]);
                        if (buf != 0) {
                            this.buffers.put(PRELOAD[i], buf);
                        }
                    }

                    this.pcmReady = null;
                    this.contextWatch.set(context);
                    this.initialized = true;
                }
            }
        }
    }

    /** The AL context changed under us: every handle is invalid, start over (no deletes, they belong to the old one). */
    private void resetAfterContextLoss() {
        this.activeVoices.clear();
        this.freeSources.clear();
        this.buffers.clear();
        this.initialized = false;
        this.contextWatch.set(0L);
        this.decodeStarted = false;
        this.pcmReady = null;
        this.paused = false;
    }

    public void onDisconnect() {
        if (this.initialized) {
            this.stopAndClearAllVoices();
        }
    }

    public void shutdown() {
        if (this.initialized) {
            this.stopAndClearAllVoices();
            AL10.alDeleteSources(this.poolSources);

            for (int buf : this.buffers.values()) {
                if (buf != 0) {
                    AL10.alDeleteBuffers(buf);
                }
            }

            this.buffers.clear();
            this.initialized = false;
            this.freeSources.clear();
        }
    }

    private void stopAndClearAllVoices() {
        for (Voice v : this.activeVoices) {
            for (int src : v.sources) {
                if (src >= 0) {
                    this.releaseSource(src);
                }
            }
        }

        this.activeVoices.clear();
    }

    /** An explosion; {@code projectileId} picks the sound profile ("" = the shared bomblet default). */
    public void playExplosion(Vec3 worldPos, boolean indoors, int closeVariant, int distantVariant, float power, String projectileId) {
        this.initIfNeeded();
        if (!this.initialized) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameRenderer == null || !AlAudio.isFinite(worldPos.x, worldPos.y, worldPos.z)) {
            return;
        }

        AlAudio.clearErrors();
        SoundProfile profile = explosionProfile(projectileId);
        this.updateListener(mc);
        AlAudio.ListenerFrame frame = new AlAudio.ListenerFrame(mc.gameRenderer.mainCamera());
        float distance = (float)frame.position.distanceTo(worldPos);
        if (!indoors) {
            this.playProfile(mc, frame, worldPos, distance, profile, power, true, closeVariant, distantVariant);
        } else {
            float[] gains = indoorGains(distance, profile.maxRange());
            if (gains[0] <= 0.0F && gains[1] <= 0.0F) {
                return;
            }

            float master = masterVolume(mc);
            if (this.mergeIntoRecent(worldPos, power)) {
                return;
            }

            int[] bufs = new int[]{this.variantBuffer(INDOORS, 0), this.variantBuffer(INDOORS_DISTANT, 0)};
            frame.toRelative(worldPos.x, worldPos.y, worldPos.z, this.rel);
            Voice voice = new Voice(worldPos, true, profile, 2);

            for (int i = 0; i < 2; i++) {
                if (gains[i] >= MIN_AUDIBLE_GAIN && bufs[i] != 0) {
                    int src = this.allocateSource();
                    if (src >= 0) {
                        this.setupSource(src, bufs[i], this.rel, gains[i] * master * profile.volume());
                        voice.sources[i] = src;
                    }
                }
            }

            this.startVoice(voice);
        }

        AlAudio.checkError(LOGGER, "ExplosionAudioEngine.playExplosion");
    }

    /** A projectile was fired: its launch sound profile, when it has one. */
    public void playLaunch(Vec3 worldPos, String projectileId) {
        this.initIfNeeded();
        if (!this.initialized) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameRenderer == null || !AlAudio.isFinite(worldPos.x, worldPos.y, worldPos.z)) {
            return;
        }

        SoundProfile profile = launchProfile(projectileId);
        if (profile != null) {
            AlAudio.clearErrors();
            this.updateListener(mc);
            AlAudio.ListenerFrame frame = new AlAudio.ListenerFrame(mc.gameRenderer.mainCamera());
            this.playProfile(mc, frame, worldPos, (float)frame.position.distanceTo(worldPos), profile, 1.0F, false, 0, 0);
            AlAudio.checkError(LOGGER, "ExplosionAudioEngine.playLaunch");
        }
    }

    /** The projectile's own explosion sound, or the shared bomblet profile (1.1.8). */
    private static SoundProfile explosionProfile(@Nullable String projectileId) {
        return com.fpvdrone.data.ProjectileDefinitions.find(projectileId)
            .map(com.fpvdrone.data.ProjectileDefinition::explosionSound)
            .orElse(SoundProfile.BOMBLET);
    }

    /** The projectile's launch sound; null when it has none (1.1.8). */
    private static @Nullable SoundProfile launchProfile(@Nullable String projectileId) {
        return com.fpvdrone.data.ProjectileDefinitions.find(projectileId).map(com.fpvdrone.data.ProjectileDefinition::launchSound).orElse(null);
    }

    private void playProfile(
        Minecraft mc, AlAudio.ListenerFrame frame, Vec3 worldPos, float distance, SoundProfile profile, float power, boolean merge, int closeVariant, int distantVariant
    ) {
        float[] gains = profile.layerGains(distance);
        float loudest = 0.0F;

        for (float g : gains) {
            loudest = Math.max(loudest, g);
        }

        if (loudest <= 0.0F) {
            return;
        }

        float master = masterVolume(mc);
        if (merge && this.mergeIntoRecent(worldPos, power)) {
            return;
        }

        frame.toRelative(worldPos.x, worldPos.y, worldPos.z, this.rel);
        List<SoundProfile.Layer> layers = profile.layers();
        int count = layers.size();
        Voice voice = new Voice(worldPos, false, profile, count);

        for (int i = 0; i < count; i++) {
            if (!(gains[i] < MIN_AUDIBLE_GAIN)) {
                String sound = layers.get(i).soundId();
                // A two-layer profile keeps the random close/distant variants (name0, name1) of 1.1.4.
                int buf = count == 2 && i == 0
                    ? this.variantBuffer(sound, closeVariant)
                    : (count == 2 && i == 1 ? this.variantBuffer(sound, distantVariant) : this.buffer(sound));
                if (buf != 0) {
                    int src = this.allocateSource();
                    if (src < 0) {
                        break;
                    }

                    this.setupSource(src, buf, this.rel, gains[i] * master * profile.volume());
                    voice.sources[i] = src;
                }
            }
        }

        this.startVoice(voice);
    }

    private boolean mergeIntoRecent(Vec3 worldPos, float power) {
        long now = System.currentTimeMillis();

        for (Voice candidate : this.activeVoices) {
            if (now - candidate.startMillis <= MERGE_WINDOW_MS && (float)candidate.worldPos.distanceTo(worldPos) <= MERGE_RADIUS) {
                candidate.gainBoost = Math.min(candidate.gainBoost + power / 5.0F, MAX_GAIN_BOOST);
                return true;
            }
        }

        return false;
    }

    private void startVoice(Voice voice) {
        boolean any = false;

        for (int src : voice.sources) {
            any |= src >= 0;
        }

        if (any) {
            this.activeVoices.add(voice);

            for (int src : voice.sources) {
                if (src >= 0) {
                    AL10.alSourcePlay(src);
                }
            }
        }
    }

    public void tick() {
        if (this.initialized && this.contextWatch.lost()) {
            this.resetAfterContextLoss();
        }

        this.initIfNeeded();
        if (!this.initialized) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer == null) {
            return;
        }

        boolean nowPaused = mc.isPaused();
        if (nowPaused != this.paused) {
            for (Voice v : this.activeVoices) {
                for (int src : v.sources) {
                    AlAudio.setPaused(src, nowPaused);
                }
            }

            this.paused = nowPaused;
        }

        if (nowPaused) {
            return;
        }

        AlAudio.clearErrors();
        this.updateListener(mc);
        if (this.activeVoices.isEmpty()) {
            return;
        }

        AlAudio.ListenerFrame frame = new AlAudio.ListenerFrame(mc.gameRenderer.mainCamera());
        float master = masterVolume(mc);
        Iterator<Voice> it = this.activeVoices.iterator();

        while (it.hasNext()) {
            Voice voice = it.next();
            float distance = (float)frame.position.distanceTo(voice.worldPos);
            frame.toRelative(voice.worldPos.x, voice.worldPos.y, voice.worldPos.z, this.rel);
            float[] gains = voice.indoors ? indoorGains(distance, voice.profile.maxRange()) : voice.profile.layerGains(distance);
            boolean alive = false;

            for (int i = 0; i < voice.sources.length; i++) {
                int src = voice.sources[i];
                if (src >= 0) {
                    float gain = (i < gains.length ? gains[i] : 0.0F) * master * voice.gainBoost * voice.profile.volume();
                    boolean finished = AL10.alGetSourcei(src, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING;
                    if (!finished && !(gain < MIN_AUDIBLE_GAIN)) {
                        AL10.alSource3f(src, AL10.AL_POSITION, this.rel[0], this.rel[1], this.rel[2]);
                        AL10.alSourcef(src, AL10.AL_GAIN, gain);
                        alive = true;
                    } else {
                        this.releaseSource(src);
                        voice.sources[i] = -1;
                    }
                }
            }

            if (!alive) {
                it.remove();
            }
        }

        AlAudio.checkError(LOGGER, "ExplosionAudioEngine.tick");
    }

    /** Indoor pair: close ambience to 8 blocks, cross-fade to 16, distant ambience out to the profile's range. */
    private static float[] indoorGains(float d, float maxRange) {
        float full = 8.0F;
        float att = d <= full ? 1.0F : Math.max(0.0F, 1.0F - (d - full) / (maxRange - full));
        if (d < 8.0F) {
            return new float[]{att, 0.0F};
        } else if (d < 16.0F) {
            float t = (d - 8.0F) / 8.0F;
            return new float[]{(1.0F - t) * att, t * att};
        } else if (d < 64.0F) {
            return new float[]{0.0F, att};
        } else if (d < maxRange) {
            float t = (d - 64.0F) / Math.max(1.0F, maxRange - 64.0F);
            return new float[]{0.0F, (1.0F - t) * att};
        } else {
            return new float[]{0.0F, 0.0F};
        }
    }

    private static float masterVolume(Minecraft mc) {
        return mc.options.getSoundSourceVolume(SoundSource.MASTER) * mc.options.getSoundSourceVolume(SoundSource.BLOCKS);
    }

    private int allocateSource() {
        if (!this.freeSources.isEmpty()) {
            return this.freeSources.poll();
        }

        Voice oldest = null;

        for (Voice v : this.activeVoices) {
            if (v.hasSource() && (oldest == null || v.startMillis < oldest.startMillis)) {
                oldest = v;
            }
        }

        if (oldest != null) {
            for (int i = 0; i < oldest.sources.length; i++) {
                if (oldest.sources[i] >= 0) {
                    this.releaseSource(oldest.sources[i]);
                    oldest.sources[i] = -1;
                }
            }

            this.activeVoices.remove(oldest);
        }

        return this.freeSources.isEmpty() ? -1 : this.freeSources.poll();
    }

    private void releaseSource(int sourceId) {
        AL10.alSourceStop(sourceId);
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, 0);
        this.freeSources.add(sourceId);
    }

    private void setupSource(int sourceId, int bufId, float[] relPos, float gain) {
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufId);
        AL10.alSource3f(sourceId, AL10.AL_POSITION, relPos[0], relPos[1], relPos[2]);
        AL10.alSourcei(sourceId, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
        AL10.alSourcef(sourceId, AL10.AL_ROLLOFF_FACTOR, 0.0F);
        AL10.alSourcef(sourceId, AL10.AL_MIN_GAIN, 0.0F);
        AL10.alSourcef(sourceId, AL10.AL_MAX_GAIN, 2.0F);
        AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
        AL10.alSourcei(sourceId, AL10.AL_LOOPING, AL10.AL_FALSE);
    }

    private void updateListener(Minecraft mc) {
        Camera camera = mc.gameRenderer.mainCamera();
        Vec3 pos = camera.position();
        AL10.alListener3f(AL10.AL_POSITION, (float)pos.x, (float)pos.y, (float)pos.z);
        AL10.alListener3f(AL10.AL_VELOCITY, 0.0F, 0.0F, 0.0F);
        org.joml.Vector3fc look = camera.forwardVector();
        org.joml.Vector3fc up = camera.upVector();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer orientation = stack.mallocFloat(6);
            orientation.put(look.x()).put(look.y()).put(look.z());
            orientation.put(up.x()).put(up.y()).put(up.z());
            orientation.flip();
            AL10.alListenerfv(AL10.AL_ORIENTATION, orientation);
        }
    }

    private static OggDecoder.Decoded decodeOgg(String soundName, byte[] bytes) {
        try {
            return OggDecoder.decodeMono(bytes);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to decode FPV sound {}", soundName, e);
            return null;
        }
    }

    /** {@code name + variant}, else {@code name0}, else plain {@code name}. */
    private int variantBuffer(String name, int variant) {
        int v = Math.max(0, variant);
        int buf = this.buffer(name + v);
        if (buf == 0 && v != 0) {
            buf = this.buffer(name + "0");
        }

        if (buf == 0) {
            buf = this.buffer(name);
        }

        return buf;
    }

    /** Loads a sound on first use (data files may name any sound): .wav first, then .ogg. Cached, misses too. */
    private int buffer(String name) {
        Integer cached = this.buffers.get(name);
        if (cached != null) {
            return cached;
        }

        byte[] wav = AlAudio.readSound(name, "wav", false, LOGGER);
        OggDecoder.Decoded decoded = wav != null ? AlAudio.decodeWav(wav) : decodeOgg(name, AlAudio.readSound(name, "ogg", false, LOGGER));
        int buf = AlAudio.upload(decoded);
        this.buffers.put(name, buf);
        return buf;
    }

    private static class Voice {
        final Vec3 worldPos;
        final boolean indoors;
        final SoundProfile profile;
        final long startMillis = System.currentTimeMillis();
        final int[] sources;
        float gainBoost = 1.0F;

        Voice(Vec3 worldPos, boolean indoors, SoundProfile profile, int layers) {
            this.worldPos = worldPos;
            this.indoors = indoors;
            this.profile = profile;
            this.sources = new int[layers];
            Arrays.fill(this.sources, -1);
        }

        boolean hasSource() {
            for (int src : this.sources) {
                if (src >= 0) {
                    return true;
                }
            }

            return false;
        }
    }
}
