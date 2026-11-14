package com.fpvdrone.client.audio;

import com.fpvdrone.FPVClientMod;
import com.fpvdrone.entity.DroneEntity;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.EXTEfx;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneEngineAudioEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(DroneEngineAudioEngine.class);
    private static final int POOL_SIZE = 16;
    private static final float MAX_RANGE = 80.0F;
    private static final float BASE_GAIN = 1.2F;
    private static final float SOURCE_MAX_GAIN = 2.0F;
    private static final float THROTTLE_IDLE = 0.0F;
    private static final float LP_START = 32.0F;
    private static final float LP_GAINHF_MIN = 0.05F;
    private static final float PITCH_NEAR = 1.0F;
    private static final float PITCH_FAR = 0.75F;
    private static final float OCCLUSION_GAIN = 0.45F;
    private static final float OCCLUSION_HF = 0.18F;
    private static final float SMOOTH = 0.18F;
    private static final DroneEngineAudioEngine INSTANCE = new DroneEngineAudioEngine();
    private boolean initialized = false;
    private int engineBuffer = 0;
    private volatile DroneEngineAudioEngine.DecodedOgg pcmReady = null;
    private boolean decodeStarted = false;
    private final ArrayDeque<Integer> freeSources = new ArrayDeque<>(16);
    private final ArrayDeque<Integer> freeFilters = new ArrayDeque<>(16);
    private final Map<UUID, int[]> activeSources = new HashMap<>();
    private final Map<UUID, float[]> smoothState = new HashMap<>();
    private boolean efxAvailable = false;

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
            byte[] bytes = readResourceBytes("drone_engine");
            Thread t = new Thread(() -> this.pcmReady = decodeOgg("drone_engine", bytes), "FPVDrone-Engine-AudioDecode");
            t.setDaemon(true);
            t.start();
        }
    }

    private void init() {
        if (!this.initialized) {
            if (ALC10.alcGetCurrentContext() != 0L) {
                DroneEngineAudioEngine.DecodedOgg ready = this.pcmReady;
                if (ready != null) {
                    for (int i = 0; i < 16; i++) {
                        int src = AL10.alGenSources();
                        AL10.alSourcef(src, 4109, 0.0F);
                        AL10.alSourcef(src, 4110, 2.0F);
                        this.freeSources.add(src);
                    }

                    try {
                        int testFilter = EXTEfx.alGenFilters();
                        if (testFilter != 0) {
                            EXTEfx.alDeleteFilters(testFilter);
                            this.efxAvailable = true;

                            for (int i = 0; i < 16; i++) {
                                int flt = EXTEfx.alGenFilters();
                                EXTEfx.alFilteri(flt, 32769, 1);
                                EXTEfx.alFilterf(flt, 1, 1.0F);
                                EXTEfx.alFilterf(flt, 2, 1.0F);
                                this.freeFilters.add(flt);
                            }
                        }
                    } catch (Exception var5) {
                        this.efxAvailable = false;
                    }

                    this.engineBuffer = uploadPcm(ready);
                    this.pcmReady = null;
                    LOGGER.info("[FPV Engine Audio] init complete — buffer={} efx={}", this.engineBuffer, this.efxAvailable);
                    this.initialized = true;
                }
            }
        }
    }

    public void onDisconnect() {
        if (this.initialized) {
            this.stopAndClearAll();
        }
    }

    public void shutdown() {
        if (this.initialized) {
            this.stopAndClearAll();
            if (this.engineBuffer != 0) {
                AL10.alDeleteBuffers(this.engineBuffer);
                this.engineBuffer = 0;
            }

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
        for (int[] slot : this.activeSources.values()) {
            int src = slot[0];
            int flt = slot[1];
            if (this.efxAvailable && flt >= 0) {
                AL10.alSourcei(src, 131077, 0);
            }

            AL10.alSourceStop(src);
            AL10.alSourcei(src, 4105, 0);
            this.freeSources.add(src);
            if (this.efxAvailable && flt >= 0) {
                this.freeFilters.add(flt);
            }
        }

        this.activeSources.clear();
        this.smoothState.clear();
    }

    public void tick() {
        this.init();
        if (this.initialized && this.engineBuffer != 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.gameRenderer != null) {
                Vec3 listenerPos = mc.gameRenderer.mainCamera().position();
                float master = mc.options.getSoundSourceVolume(SoundSource.MASTER) * mc.options.getSoundSourceVolume(SoundSource.BLOCKS);
                UUID localFpvDroneId = mc.player != null ? FPVClientMod.getActiveDroneId(mc.player.getUUID()).orElse(null) : null;
                Map<UUID, DroneEntity> armedDrones = new HashMap<>();

                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof DroneEntity d && d.areMotorsArmed()) {
                        armedDrones.put(d.getUUID(), d);
                    }
                }

                for (Entry<UUID, DroneEntity> entry : armedDrones.entrySet()) {
                    UUID id = entry.getKey();
                    DroneEntity drone = entry.getValue();
                    Vec3 dronePos = drone.position();
                    float d = (float)listenerPos.distanceTo(dronePos);
                    if (d >= 80.0F) {
                        this.releaseIfPresent(id);
                    } else {
                        float throttle = drone.getSyncedThrottle();
                        float throttleFactor = 0.0F + throttle * 1.0F;
                        float targetGain = id.equals(localFpvDroneId) ? 0.0F : computeGain(d) * 1.2F * throttleFactor * master;
                        float targetGainHF = computeGainHF(d);
                        float targetPitch = computePitch(d);
                        if (isOccluded(mc.level, listenerPos, dronePos)) {
                            targetGain *= 0.45F;
                            targetGainHF *= 0.18F;
                        }

                        float wx = (float)dronePos.x;
                        float wy = (float)dronePos.y;
                        float wz = (float)dronePos.z;
                        if (this.activeSources.containsKey(id)) {
                            float[] sm = this.smoothState.get(id);
                            sm[0] += (targetGain - sm[0]) * 0.18F;
                            sm[1] += (targetGainHF - sm[1]) * 0.18F;
                            sm[2] += (targetPitch - sm[2]) * 0.18F;
                            int[] slot = this.activeSources.get(id);
                            int src = slot[0];
                            int flt = slot[1];
                            AL10.alSource3f(src, 4100, wx, wy, wz);
                            AL10.alSourcef(src, 4106, sm[0]);
                            AL10.alSourcef(src, 4099, sm[2]);
                            if (this.efxAvailable && flt >= 0) {
                                EXTEfx.alFilterf(flt, 2, sm[1]);
                                AL10.alSourcei(src, 131077, flt);
                            }
                        } else {
                            int src = this.allocateSource();
                            if (src >= 0) {
                                int flt = this.efxAvailable && !this.freeFilters.isEmpty() ? this.freeFilters.poll() : -1;
                                float[] sm = new float[]{0.0F, targetGainHF, targetPitch};
                                this.smoothState.put(id, sm);
                                AL10.alSourcei(src, 4105, this.engineBuffer);
                                AL10.alSource3f(src, 4100, wx, wy, wz);
                                AL10.alSourcei(src, 514, 0);
                                AL10.alSourcef(src, 4129, 0.0F);
                                AL10.alSourcef(src, 4109, 0.0F);
                                AL10.alSourcef(src, 4110, 2.0F);
                                AL10.alSourcef(src, 4106, 0.0F);
                                AL10.alSourcei(src, 4103, 1);
                                if (this.efxAvailable && flt >= 0) {
                                    EXTEfx.alFilterf(flt, 2, sm[1]);
                                    AL10.alSourcei(src, 131077, flt);
                                }

                                AL10.alSourcePlay(src);
                                this.activeSources.put(id, new int[]{src, flt});
                            }
                        }
                    }
                }

                Iterator<Entry<UUID, int[]>> it = this.activeSources.entrySet().iterator();

                while (it.hasNext()) {
                    Entry<UUID, int[]> entryx = it.next();
                    if (!armedDrones.containsKey(entryx.getKey())) {
                        int[] slot = entryx.getValue();
                        int src = slot[0];
                        int flt = slot[1];
                        if (this.efxAvailable && flt >= 0) {
                            AL10.alSourcei(src, 131077, 0);
                        }

                        AL10.alSourceStop(src);
                        AL10.alSourcei(src, 4105, 0);
                        this.freeSources.add(src);
                        if (this.efxAvailable && flt >= 0) {
                            this.freeFilters.add(flt);
                        }

                        this.smoothState.remove(entryx.getKey());
                        it.remove();
                    }
                }
            }
        }
    }

    private static float computeGain(float d) {
        return d >= 80.0F ? 0.0F : 1.0F - d / 80.0F;
    }

    private static float computeGainHF(float d) {
        if (d <= 32.0F) {
            return 1.0F;
        } else if (d >= 80.0F) {
            return 0.05F;
        } else {
            float t = (d - 32.0F) / 48.0F;
            return 1.0F - t * 0.95F;
        }
    }

    private static float computePitch(float d) {
        float t = Math.min(d / 80.0F, 1.0F);
        return 1.0F + t * -0.25F;
    }

    private static boolean isOccluded(Level level, Vec3 from, Vec3 to) {
        if (level == null) {
            return false;
        } else {
            ClipContext ctx = new ClipContext(from, to, Block.VISUAL, Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty());
            HitResult hit = level.clip(ctx);
            return hit != null && hit.getType() == Type.BLOCK;
        }
    }

    private int allocateSource() {
        if (!this.freeSources.isEmpty()) {
            return this.freeSources.poll();
        } else {
            LOGGER.warn("[FPV Engine Audio] Source pool exhausted — cannot play drone engine");
            return -1;
        }
    }

    private void releaseIfPresent(UUID id) {
        int[] slot = this.activeSources.remove(id);
        this.smoothState.remove(id);
        if (slot != null) {
            int src = slot[0];
            int flt = slot[1];
            if (this.efxAvailable && flt >= 0) {
                AL10.alSourcei(src, 131077, 0);
            }

            AL10.alSourceStop(src);
            AL10.alSourcei(src, 4105, 0);
            this.freeSources.add(src);
            if (this.efxAvailable && flt >= 0) {
                this.freeFilters.add(flt);
            }
        }
    }

    private static byte[] readResourceBytes(String soundName) {
        Identifier rl = Identifier.fromNamespaceAndPath("fpvdrone", "sounds/" + soundName + ".ogg");
        Optional<Resource> optRes = Minecraft.getInstance().getResourceManager().getResource(rl);
        if (optRes.isEmpty()) {
            LOGGER.warn("[FPV Engine Audio] Resource not found: {}", rl);
            return null;
        } else {
            try {
                byte[] var4;
                try (InputStream is = optRes.get().open()) {
                    var4 = is.readAllBytes();
                }

                return var4;
            } catch (Exception var8) {
                LOGGER.warn("[FPV Engine Audio] Read failed for {}: {}", soundName, var8.getMessage());
                return null;
            }
        }
    }

    private static DecodedOgg decodeOgg(String soundName, byte[] bytes) {
        try {
            var decoded = OggDecoder.decodeMono(bytes);
            return decoded == null ? null : new DecodedOgg(decoded.pcm(), decoded.sampleRate());
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to decode FPV sound {}", soundName, e);
            return null;
        }
    }

    private static int uploadPcm(DroneEngineAudioEngine.DecodedOgg decoded) {
        if (decoded != null && decoded.pcm != null) {
            int var2;
            try {
                int buf = AL10.alGenBuffers();
                AL10.alBufferData(buf, 4353, decoded.pcm, decoded.sampleRate);
                var2 = buf;
            } finally {
                MemoryUtil.memFree(decoded.pcm);
            }

            return var2;
        } else {
            return 0;
        }
    }

    private static final class DecodedOgg {
        final ShortBuffer pcm;
        final int sampleRate;

        DecodedOgg(ShortBuffer pcm, int sampleRate) {
            this.pcm = pcm;
            this.sampleRate = sampleRate;
        }
    }
}
