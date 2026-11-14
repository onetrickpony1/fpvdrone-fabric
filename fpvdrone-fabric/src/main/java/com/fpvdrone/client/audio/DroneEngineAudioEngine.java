package com.fpvdrone.client.audio;

import com.fpvdrone.FPVClientMod;
import com.fpvdrone.data.SoundProfile;
import com.fpvdrone.entity.DroneEntity;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Looping motor sound of every armed drone in view, through OpenAL with a low-pass filter for distance and walls.
 * 1.1.8: each aircraft has its own sound profile (one loop, or distance layers that cross-fade), the pool grew to 32
 * sources, sources sit relative to the camera, voices pause with the game and survive an AL context swap, and fixed-wing
 * loitering munitions are heard too (always at full throttle).
 */
public class DroneEngineAudioEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(DroneEngineAudioEngine.class);
    private static final int POOL_SIZE = 32;
    private static final float SILENT = 0.0016F;
    private static final float LP_START = 32.0F;
    private static final float LP_GAINHF_MIN = 0.05F;
    private static final float PITCH_FAR_DROP = 0.25F;
    private static final float OCCLUSION_GAIN = 0.45F;
    private static final float OCCLUSION_HF = 0.18F;
    private static final float SMOOTH = 0.18F;
    private static final String DEFAULT_SOUND = "drone_engine";
    private static final DroneEngineAudioEngine INSTANCE = new DroneEngineAudioEngine();
    private boolean initialized = false;
    private final Map<String, Integer> buffers = new HashMap<>();
    private volatile OggDecoder.Decoded pcmReady = null;
    private boolean decodeStarted = false;
    private final ArrayDeque<Integer> freeSources = new ArrayDeque<>(POOL_SIZE);
    private final ArrayDeque<Integer> freeFilters = new ArrayDeque<>(POOL_SIZE);
    private final Map<UUID, Voice> voices = new HashMap<>();
    private boolean efxAvailable = false;
    private final AlAudio.ContextWatch contextWatch = new AlAudio.ContextWatch();
    private final float[] rel = new float[3];
    private boolean paused = false;

    private DroneEngineAudioEngine() {
    }

    public static DroneEngineAudioEngine get() {
        return INSTANCE;
    }

    public void initIfNeeded() {
        this.startBackgroundDecodeIfNeeded();
        this.init();
    }

    private synchronized void startBackgroundDecodeIfNeeded() {
        if (!this.decodeStarted) {
            this.decodeStarted = true;
            byte[] bytes = AlAudio.readSound(DEFAULT_SOUND, "ogg", true, LOGGER);
            Thread t = new Thread(() -> this.pcmReady = decode(DEFAULT_SOUND, bytes), "FPVDrone-Engine-AudioDecode");
            t.setDaemon(true);
            t.start();
        }
    }

    private void init() {
        if (!this.initialized) {
            long context = ALC10.alcGetCurrentContext();
            if (context != 0L) {
                OggDecoder.Decoded ready = this.pcmReady;
                if (ready != null) {
                    for (int i = 0; i < POOL_SIZE; i++) {
                        int src = AL10.alGenSources();
                        AL10.alSourcef(src, AL10.AL_MIN_GAIN, 0.0F);
                        AL10.alSourcef(src, AL10.AL_MAX_GAIN, 2.0F);
                        this.freeSources.add(src);
                    }

                    try {
                        int testFilter = EXTEfx.alGenFilters();
                        if (testFilter != 0) {
                            EXTEfx.alDeleteFilters(testFilter);
                            this.efxAvailable = true;

                            for (int i = 0; i < POOL_SIZE; i++) {
                                int flt = EXTEfx.alGenFilters();
                                EXTEfx.alFilteri(flt, EXTEfx.AL_FILTER_TYPE, EXTEfx.AL_FILTER_LOWPASS);
                                EXTEfx.alFilterf(flt, EXTEfx.AL_LOWPASS_GAIN, 1.0F);
                                EXTEfx.alFilterf(flt, EXTEfx.AL_LOWPASS_GAINHF, 1.0F);
                                this.freeFilters.add(flt);
                            }
                        }
                    } catch (Exception e) {
                        this.efxAvailable = false;
                    }

                    this.buffers.put(DEFAULT_SOUND, AlAudio.upload(ready));
                    this.pcmReady = null;
                    this.contextWatch.set(context);
                    this.initialized = true;
                }
            }
        }
    }

    /** The AL context changed: every handle is invalid, so forget them all and initialise again. */
    private void resetAfterContextLoss() {
        this.voices.clear();
        this.freeSources.clear();
        this.freeFilters.clear();
        this.buffers.clear();
        this.efxAvailable = false;
        this.initialized = false;
        this.contextWatch.set(0L);
        this.decodeStarted = false;
        this.pcmReady = null;
        this.paused = false;
    }

    public void onDisconnect() {
        if (this.initialized) {
            this.stopAndClearAll();
        }
    }

    public void shutdown() {
        if (this.initialized) {
            this.stopAndClearAll();

            for (int buf : this.buffers.values()) {
                if (buf != 0) {
                    AL10.alDeleteBuffers(buf);
                }
            }

            this.buffers.clear();

            for (int src : this.freeSources) {
                AL10.alDeleteSources(src);
            }

            this.freeSources.clear();
            if (this.efxAvailable) {
                for (int flt : this.freeFilters) {
                    EXTEfx.alDeleteFilters(flt);
                }

                this.freeFilters.clear();
            }

            this.initialized = false;
        }
    }

    private void stopAndClearAll() {
        for (Voice voice : this.voices.values()) {
            for (int i = 0; i < voice.layers; i++) {
                this.releaseLayer(voice, i);
            }
        }

        this.voices.clear();
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
        if (mc.level == null || mc.gameRenderer == null) {
            return;
        }

        boolean nowPaused = mc.isPaused();
        if (nowPaused != this.paused) {
            for (Voice voice : this.voices.values()) {
                for (int i = 0; i < voice.layers; i++) {
                    AlAudio.setPaused(voice.sources[i], nowPaused);
                }
            }

            this.paused = nowPaused;
        }

        if (nowPaused) {
            return;
        }

        AlAudio.clearErrors();
        AlAudio.ListenerFrame frame = new AlAudio.ListenerFrame(mc.gameRenderer.mainCamera());
        Vec3 listenerPos = frame.position;
        float master = mc.options.getSoundSourceVolume(SoundSource.MASTER) * mc.options.getSoundSourceVolume(SoundSource.BLOCKS);
        UUID localFpvDroneId = mc.player != null ? FPVClientMod.getActiveDroneId(mc.player.getUUID()).orElse(null) : null;
        Map<UUID, Emitter> emitters = new HashMap<>();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof DroneEntity drone && drone.areMotorsArmed()) {
                Vec3 pos = drone.position();
                if (AlAudio.isFinite(pos.x, pos.y, pos.z)) {
                    // 1.1.8-PENDING: the drone definition's "Sounds" profile (DroneDefinition.sounds()); every bundled
                    // FPV drone uses the shared default, so this already matches 1.1.8.
                    SoundProfile profile = SoundProfile.DRONE_ENGINE;
                    emitters.put(drone.getUUID(), new Emitter(pos, profile, drone.getSyncedThrottle(), drone.getUUID().equals(localFpvDroneId)));
                }
            }
            // 1.1.8-PENDING: LoiteringMunitionEntity (not ported): alive, not crashed -> Emitter(pos, its profile, 1.0F, false).
        }

        for (Entry<UUID, Emitter> entry : emitters.entrySet()) {
            UUID id = entry.getKey();
            Emitter emitter = entry.getValue();
            SoundProfile profile = emitter.profile();
            float maxRange = profile.maxRange();
            float distance = (float)listenerPos.distanceTo(emitter.pos());
            if (distance >= maxRange) {
                this.release(id);
                continue;
            }

            float throttleFactor = emitter.throttle();
            // Your own drone is silent in FPV: the goggles feed has no motor sound.
            float targetGain = emitter.ownFpv() ? 0.0F : linearGain(distance, maxRange) * profile.volume() * throttleFactor * master;
            float targetGainHF = lowPassGain(distance, maxRange);
            float targetPitch = pitch(distance, maxRange);
            if (isOccluded(mc.level, listenerPos, emitter.pos())) {
                targetGain *= OCCLUSION_GAIN;
                targetGainHF *= OCCLUSION_HF;
            }

            int layerCount = profile.layers().size();
            String[] sounds = new String[layerCount];
            float[] weights = new float[layerCount];

            for (int i = 0; i < layerCount; i++) {
                SoundProfile.Layer layer = profile.layers().get(i);
                sounds[i] = layer.soundId();
                weights[i] = layer.weight(distance);
            }

            Voice voice = this.voices.get(id);
            if (voice == null || voice.layers != layerCount) {
                if (voice != null) {
                    this.release(id);
                }

                voice = new Voice(layerCount);
                voice.gainHF = targetGainHF;
                voice.pitch = targetPitch;
                this.voices.put(id, voice);
            }

            voice.gainHF += (targetGainHF - voice.gainHF) * SMOOTH;
            voice.pitch += (targetPitch - voice.pitch) * SMOOTH;
            frame.toRelative(emitter.pos().x, emitter.pos().y, emitter.pos().z, this.rel);

            for (int i = 0; i < layerCount; i++) {
                float layerGain = targetGain * weights[i];
                if (voice.sources[i] < 0) {
                    if (layerGain < SILENT) {
                        continue;
                    }

                    int buf = this.buffer(sounds[i]);
                    if (buf == 0) {
                        continue;
                    }

                    int src = this.allocateSource(id);
                    if (src < 0) {
                        continue;
                    }

                    int flt = this.efxAvailable && !this.freeFilters.isEmpty() ? this.freeFilters.poll() : -1;
                    voice.sources[i] = src;
                    voice.filters[i] = flt;
                    voice.gains[i] = 0.0F;
                    AL10.alSourcei(src, AL10.AL_BUFFER, buf);
                    AL10.alSource3f(src, AL10.AL_POSITION, this.rel[0], this.rel[1], this.rel[2]);
                    AL10.alSourcei(src, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
                    AL10.alSourcef(src, AL10.AL_ROLLOFF_FACTOR, 0.0F);
                    AL10.alSourcef(src, AL10.AL_MIN_GAIN, 0.0F);
                    AL10.alSourcef(src, AL10.AL_MAX_GAIN, 2.0F);
                    AL10.alSourcef(src, AL10.AL_GAIN, 0.0F);
                    AL10.alSourcef(src, AL10.AL_PITCH, voice.pitch);
                    AL10.alSourcei(src, AL10.AL_LOOPING, AL10.AL_TRUE);
                    if (this.efxAvailable && flt >= 0) {
                        EXTEfx.alFilterf(flt, EXTEfx.AL_LOWPASS_GAINHF, voice.gainHF);
                        AL10.alSourcei(src, EXTEfx.AL_DIRECT_FILTER, flt);
                    }

                    AL10.alSourcePlay(src);
                }

                voice.gains[i] += (layerGain - voice.gains[i]) * SMOOTH;
                int src = voice.sources[i];
                int flt = voice.filters[i];
                if (src >= 0) {
                    AL10.alSource3f(src, AL10.AL_POSITION, this.rel[0], this.rel[1], this.rel[2]);
                    AL10.alSourcef(src, AL10.AL_GAIN, voice.gains[i]);
                    AL10.alSourcef(src, AL10.AL_PITCH, voice.pitch);
                    if (this.efxAvailable && flt >= 0) {
                        EXTEfx.alFilterf(flt, EXTEfx.AL_LOWPASS_GAINHF, voice.gainHF);
                        AL10.alSourcei(src, EXTEfx.AL_DIRECT_FILTER, flt);
                    }

                    if (layerGain < SILENT && voice.gains[i] < SILENT) {
                        this.releaseLayer(voice, i);
                    }
                }
            }
        }

        Iterator<Entry<UUID, Voice>> it = this.voices.entrySet().iterator();

        while (it.hasNext()) {
            Entry<UUID, Voice> entry = it.next();
            if (!emitters.containsKey(entry.getKey())) {
                Voice voice = entry.getValue();

                for (int i = 0; i < voice.layers; i++) {
                    this.releaseLayer(voice, i);
                }

                it.remove();
            }
        }

        AlAudio.checkError(LOGGER, "DroneEngineAudioEngine.tick");
    }

    private static float linearGain(float d, float maxRange) {
        return d >= maxRange ? 0.0F : 1.0F - d / maxRange;
    }

    private static float lowPassGain(float d, float maxRange) {
        if (maxRange <= LP_START || d <= LP_START) {
            return 1.0F;
        } else if (d >= maxRange) {
            return LP_GAINHF_MIN;
        } else {
            float t = (d - LP_START) / (maxRange - LP_START);
            return 1.0F - t * (1.0F - LP_GAINHF_MIN);
        }
    }

    private static float pitch(float d, float maxRange) {
        float t = Math.min(d / maxRange, 1.0F);
        return 1.0F - t * PITCH_FAR_DROP;
    }

    private static boolean isOccluded(Level level, Vec3 from, Vec3 to) {
        if (level == null) {
            return false;
        }

        ClipContext ctx = new ClipContext(from, to, Block.VISUAL, Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty());
        HitResult hit = level.clip(ctx);
        return hit != null && hit.getType() == HitResult.Type.BLOCK;
    }

    /** A free source, else steals one from the quietest other drone. */
    private int allocateSource(UUID forId) {
        if (!this.freeSources.isEmpty()) {
            return this.freeSources.poll();
        }

        UUID quietest = null;
        float quietestGain = Float.MAX_VALUE;

        for (Entry<UUID, Voice> entry : this.voices.entrySet()) {
            if (!entry.getKey().equals(forId)) {
                Voice voice = entry.getValue();
                float total = 0.0F;
                boolean playing = false;

                for (int i = 0; i < voice.layers; i++) {
                    if (voice.sources[i] >= 0) {
                        playing = true;
                        total += Math.max(0.0F, voice.gains[i]);
                    }
                }

                if (playing && total < quietestGain) {
                    quietestGain = total;
                    quietest = entry.getKey();
                }
            }
        }

        if (quietest != null) {
            this.release(quietest);
            if (!this.freeSources.isEmpty()) {
                return this.freeSources.poll();
            }
        }

        return -1;
    }

    /** Loads an engine loop on first use (data files may name any sound in assets/fpvdrone/sounds). */
    private int buffer(String name) {
        if (name == null || name.isEmpty()) {
            name = DEFAULT_SOUND;
        }

        Integer cached = this.buffers.get(name);
        if (cached != null) {
            return cached;
        }

        int buf = AlAudio.upload(decode(name, AlAudio.readSound(name, "ogg", true, LOGGER)));
        this.buffers.put(name, buf);
        return buf;
    }

    private void release(UUID id) {
        Voice voice = this.voices.remove(id);
        if (voice != null) {
            for (int i = 0; i < voice.layers; i++) {
                this.releaseLayer(voice, i);
            }
        }
    }

    private void releaseLayer(Voice voice, int layer) {
        int src = voice.sources[layer];
        int flt = voice.filters[layer];
        if (src >= 0) {
            if (this.efxAvailable && flt >= 0) {
                AL10.alSourcei(src, EXTEfx.AL_DIRECT_FILTER, 0);
            }

            AL10.alSourceStop(src);
            AL10.alSourcei(src, AL10.AL_BUFFER, 0);
            this.freeSources.add(src);
            if (this.efxAvailable && flt >= 0) {
                this.freeFilters.add(flt);
            }

            voice.sources[layer] = -1;
            voice.filters[layer] = -1;
            voice.gains[layer] = 0.0F;
        }
    }

    private static OggDecoder.Decoded decode(String soundName, byte[] bytes) {
        try {
            return OggDecoder.decodeMono(bytes);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to decode FPV sound {}", soundName, e);
            return null;
        }
    }

    private record Emitter(Vec3 pos, SoundProfile profile, float throttle, boolean ownFpv) {
    }

    private static final class Voice {
        final int layers;
        final int[] sources;
        final int[] filters;
        final float[] gains;
        float gainHF = 1.0F;
        float pitch = 1.0F;

        Voice(int layers) {
            this.layers = layers;
            this.sources = new int[layers];
            this.filters = new int[layers];
            this.gains = new float[layers];
            Arrays.fill(this.sources, -1);
            Arrays.fill(this.filters, -1);
        }
    }
}
