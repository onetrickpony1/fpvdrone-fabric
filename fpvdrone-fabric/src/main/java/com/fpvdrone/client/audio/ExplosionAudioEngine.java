package com.fpvdrone.client.audio;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplosionAudioEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExplosionAudioEngine.class);
    private static final int POOL_SIZE = 16;
    private static final float MIN_AUDIBLE_GAIN = 0.01F;
    private static final float MERGE_RADIUS = 5.0F;
    private static final long MERGE_WINDOW_MS = 200L;
    private static final float MAX_GAIN_BOOST = 2.0F;
    private static final float EXPLOSION_BOOST = 1.6F;
    private static final ExplosionAudioEngine INSTANCE = new ExplosionAudioEngine();
    private boolean initialized = false;
    private final int[] poolSources = new int[16];
    private final ArrayDeque<Integer> freeSources = new ArrayDeque<>(16);
    private final List<ExplosionAudioEngine.Voice> activeVoices = new ArrayList<>();
    private final int[] closeBuffers = new int[2];
    private final int[] distantBuffers = new int[2];
    private final int[] indoorsBuffers = new int[2];
    private static final String[] OGG_NAMES = new String[]{
        "bomblet_close0", "bomblet_close1", "bomblet_distant0", "bomblet_distant1", "ambience_indoors0", "ambience_indoors_distant0"
    };
    private volatile ExplosionAudioEngine.DecodedOgg[] pcmReady = null;
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
            byte[][] rawBytes = new byte[OGG_NAMES.length][];

            for (int i = 0; i < OGG_NAMES.length; i++) {
                rawBytes[i] = readResourceBytes(OGG_NAMES[i]);
            }

            Thread t = new Thread(() -> {
                ExplosionAudioEngine.DecodedOgg[] out = new ExplosionAudioEngine.DecodedOgg[OGG_NAMES.length];

                for (int i = 0; i < OGG_NAMES.length; i++) {
                    out[i] = decodeOgg(OGG_NAMES[i], rawBytes[i]);
                }

                this.pcmReady = out;
            }, "FPVDrone-Explosion-AudioDecode");
            t.setDaemon(true);
            t.start();
        }
    }

    private void init() {
        if (!this.initialized) {
            if (ALC10.alcGetCurrentContext() != 0L) {
                ExplosionAudioEngine.DecodedOgg[] ready = this.pcmReady;
                if (ready != null) {
                    for (int i = 0; i < 16; i++) {
                        this.poolSources[i] = AL10.alGenSources();
                        AL10.alSourcef(this.poolSources[i], 4109, 0.0F);
                        this.freeSources.add(this.poolSources[i]);
                    }

                    this.closeBuffers[0] = uploadPcm(ready[0]);
                    this.closeBuffers[1] = uploadPcm(ready[1]);
                    this.distantBuffers[0] = uploadPcm(ready[2]);
                    this.distantBuffers[1] = uploadPcm(ready[3]);
                    this.indoorsBuffers[0] = uploadPcm(ready[4]);
                    this.indoorsBuffers[1] = uploadPcm(ready[5]);
                    this.pcmReady = null;
                    LOGGER.info(
                        "[FPV Audio] init complete — bufs close={}/{} distant={}/{} indoors={}/{}",
                        new Object[]{
                            this.closeBuffers[0],
                            this.closeBuffers[1],
                            this.distantBuffers[0],
                            this.distantBuffers[1],
                            this.indoorsBuffers[0],
                            this.indoorsBuffers[1]
                        }
                    );
                    this.initialized = true;
                }
            }
        }
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

            for (int b : this.closeBuffers) {
                if (b != 0) {
                    AL10.alDeleteBuffers(b);
                }
            }

            for (int bx : this.distantBuffers) {
                if (bx != 0) {
                    AL10.alDeleteBuffers(bx);
                }
            }

            for (int bxx : this.indoorsBuffers) {
                if (bxx != 0) {
                    AL10.alDeleteBuffers(bxx);
                }
            }

            this.initialized = false;
            this.freeSources.clear();
        }
    }

    private void stopAndClearAllVoices() {
        for (ExplosionAudioEngine.Voice v : this.activeVoices) {
            if (v.closeSourceId >= 0) {
                AL10.alSourceStop(v.closeSourceId);
                AL10.alSourcei(v.closeSourceId, 4105, 0);
                this.freeSources.add(v.closeSourceId);
            }

            if (v.distantSourceId >= 0) {
                AL10.alSourceStop(v.distantSourceId);
                AL10.alSourcei(v.distantSourceId, 4105, 0);
                this.freeSources.add(v.distantSourceId);
            }
        }

        this.activeVoices.clear();
    }

    public void playExplosion(Vec3 worldPos, boolean indoors, int closeVariant, int distantVariant, float power) {
        this.init();
        if (this.initialized) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.gameRenderer != null) {
                this.updateListener(mc);
                Vec3 listenerPos = mc.gameRenderer.mainCamera().position();
                float d = (float)listenerPos.distanceTo(worldPos);
                float[] gains = computeGains(d, indoors);
                float closeGain = gains[0];
                float distantGain = gains[1];
                if (!(closeGain <= 0.0F) || !(distantGain <= 0.0F)) {
                    float master = mc.options.getSoundSourceVolume(SoundSource.MASTER) * mc.options.getSoundSourceVolume(SoundSource.BLOCKS);
                    long now = System.currentTimeMillis();

                    for (ExplosionAudioEngine.Voice candidate : this.activeVoices) {
                        if (now - candidate.startMillis <= 200L && (float)candidate.worldPos.distanceTo(worldPos) <= 5.0F) {
                            candidate.gainBoost = Math.min(candidate.gainBoost + power / 5.0F, 2.0F);
                            return;
                        }
                    }

                    int cv = Math.max(0, Math.min(closeVariant, 1));
                    int dv = Math.max(0, Math.min(distantVariant, 1));
                    int closeBuf = indoors ? this.indoorsBuffers[0] : this.closeBuffers[cv];
                    int distantBuf = indoors ? this.indoorsBuffers[1] : this.distantBuffers[dv];
                    ExplosionAudioEngine.Voice voice = new ExplosionAudioEngine.Voice(worldPos, indoors);
                    if (closeGain >= 0.01F && closeBuf != 0) {
                        int src = this.allocateSource();
                        if (src >= 0) {
                            this.setupSource(src, closeBuf, worldPos, closeGain * master * 1.6F);
                            voice.closeSourceId = src;
                        }
                    }

                    if (distantGain >= 0.01F && distantBuf != 0) {
                        int src = this.allocateSource();
                        if (src >= 0) {
                            this.setupSource(src, distantBuf, worldPos, distantGain * master * 1.6F);
                            voice.distantSourceId = src;
                        }
                    }

                    if (voice.closeSourceId >= 0 || voice.distantSourceId >= 0) {
                        this.activeVoices.add(voice);
                        if (voice.closeSourceId >= 0) {
                            AL10.alSourcePlay(voice.closeSourceId);
                        }

                        if (voice.distantSourceId >= 0) {
                            AL10.alSourcePlay(voice.distantSourceId);
                        }
                    }
                }
            }
        }
    }

    public void tick() {
        this.init();
        if (this.initialized) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameRenderer != null) {
                this.updateListener(mc);
                if (!this.activeVoices.isEmpty()) {
                    Vec3 listenerPos = mc.gameRenderer.mainCamera().position();
                    float master = mc.options.getSoundSourceVolume(SoundSource.MASTER) * mc.options.getSoundSourceVolume(SoundSource.BLOCKS);
                    Iterator<ExplosionAudioEngine.Voice> it = this.activeVoices.iterator();

                    while (it.hasNext()) {
                        ExplosionAudioEngine.Voice voice = it.next();
                        float d = (float)listenerPos.distanceTo(voice.worldPos);
                        float[] gains = computeGains(d, voice.indoors);
                        float closeGain = gains[0] * master * voice.gainBoost * 1.6F;
                        float distantGain = gains[1] * master * voice.gainBoost * 1.6F;
                        float wx = (float)voice.worldPos.x;
                        float wy = (float)voice.worldPos.y;
                        float wz = (float)voice.worldPos.z;
                        if (voice.closeSourceId >= 0) {
                            boolean finished = AL10.alGetSourcei(voice.closeSourceId, 4112) != 4114;
                            if (!finished && !(closeGain < 0.01F)) {
                                AL10.alSource3f(voice.closeSourceId, 4100, wx, wy, wz);
                                AL10.alSourcef(voice.closeSourceId, 4106, closeGain);
                            } else {
                                this.releaseSource(voice.closeSourceId);
                                voice.closeSourceId = -1;
                            }
                        }

                        if (voice.distantSourceId >= 0) {
                            boolean finished = AL10.alGetSourcei(voice.distantSourceId, 4112) != 4114;
                            if (!finished && !(distantGain < 0.01F)) {
                                AL10.alSource3f(voice.distantSourceId, 4100, wx, wy, wz);
                                AL10.alSourcef(voice.distantSourceId, 4106, distantGain);
                            } else {
                                this.releaseSource(voice.distantSourceId);
                                voice.distantSourceId = -1;
                            }
                        }

                        if (voice.closeSourceId < 0 && voice.distantSourceId < 0) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    private static float[] computeGains(float d, boolean indoors) {
        float fullRadius = indoors ? 8.0F : 32.0F;
        float att = d <= fullRadius ? 1.0F : Math.max(0.0F, 1.0F - (d - fullRadius) / (128.0F - fullRadius));
        if (indoors) {
            if (d < 8.0F) {
                return new float[]{att, 0.0F};
            } else if (d < 16.0F) {
                float t = (d - 8.0F) / 8.0F;
                return new float[]{(1.0F - t) * att, t * att};
            } else if (d < 64.0F) {
                return new float[]{0.0F, att};
            } else if (d < 128.0F) {
                float t = (d - 64.0F) / 64.0F;
                return new float[]{0.0F, (1.0F - t) * att};
            } else {
                return new float[]{0.0F, 0.0F};
            }
        } else if (d < 32.0F) {
            return new float[]{att, 0.0F};
        } else if (d < 64.0F) {
            float t = (d - 32.0F) / 32.0F;
            return new float[]{(1.0F - t) * att, t * att};
        } else if (d < 128.0F) {
            float t = (d - 64.0F) / 64.0F;
            return new float[]{0.0F, (1.0F - t) * att};
        } else {
            return new float[]{0.0F, 0.0F};
        }
    }

    private int allocateSource() {
        if (!this.freeSources.isEmpty()) {
            return this.freeSources.poll();
        } else {
            ExplosionAudioEngine.Voice oldest = null;

            for (ExplosionAudioEngine.Voice v : this.activeVoices) {
                if ((v.closeSourceId >= 0 || v.distantSourceId >= 0) && (oldest == null || v.startMillis < oldest.startMillis)) {
                    oldest = v;
                }
            }

            if (oldest != null) {
                if (oldest.closeSourceId >= 0) {
                    AL10.alSourceStop(oldest.closeSourceId);
                    AL10.alSourcei(oldest.closeSourceId, 4105, 0);
                    this.freeSources.add(oldest.closeSourceId);
                    oldest.closeSourceId = -1;
                }

                if (oldest.distantSourceId >= 0) {
                    AL10.alSourceStop(oldest.distantSourceId);
                    AL10.alSourcei(oldest.distantSourceId, 4105, 0);
                    this.freeSources.add(oldest.distantSourceId);
                    oldest.distantSourceId = -1;
                }

                this.activeVoices.remove(oldest);
            }

            return this.freeSources.isEmpty() ? -1 : this.freeSources.poll();
        }
    }

    private void releaseSource(int sourceId) {
        AL10.alSourceStop(sourceId);
        AL10.alSourcei(sourceId, 4105, 0);
        this.freeSources.add(sourceId);
    }

    private void setupSource(int sourceId, int bufId, Vec3 worldPos, float gain) {
        AL10.alSourcei(sourceId, 4105, bufId);
        AL10.alSource3f(sourceId, 4100, (float)worldPos.x, (float)worldPos.y, (float)worldPos.z);
        AL10.alSourcei(sourceId, 514, 0);
        AL10.alSourcef(sourceId, 4129, 0.0F);
        AL10.alSourcef(sourceId, 4109, 0.0F);
        AL10.alSourcef(sourceId, 4110, 2.0F);
        AL10.alSourcef(sourceId, 4106, gain);
        AL10.alSourcei(sourceId, 4103, 0);
    }

    private void updateListener(Minecraft mc) {
        Camera camera = mc.gameRenderer.mainCamera();
        Vec3 pos = camera.position();
        AL10.alListener3f(4100, (float)pos.x, (float)pos.y, (float)pos.z);
        AL10.alListener3f(4102, 0.0F, 0.0F, 0.0F);
        org.joml.Vector3fc look = camera.forwardVector();
        org.joml.Vector3fc up = camera.upVector();
        MemoryStack stack = MemoryStack.stackPush();

        try {
            FloatBuffer orientation = stack.mallocFloat(6);
            orientation.put(look.x()).put(look.y()).put(look.z());
            orientation.put(up.x()).put(up.y()).put(up.z());
            orientation.flip();
            AL10.alListenerfv(4111, orientation);
        } catch (Throwable var10) {
            if (stack != null) {
                try {
                    stack.close();
                } catch (Throwable var9) {
                    var10.addSuppressed(var9);
                }
            }

            throw var10;
        }

        if (stack != null) {
            stack.close();
        }
    }

    private static byte[] readResourceBytes(String soundName) {
        Identifier rl = Identifier.fromNamespaceAndPath("fpvdrone", "sounds/" + soundName + ".ogg");
        Optional<Resource> optRes = Minecraft.getInstance().getResourceManager().getResource(rl);
        if (optRes.isEmpty()) {
            LOGGER.warn("[FPV Audio] Resource not found: {}", rl);
            return null;
        } else {
            try {
                byte[] var4;
                try (InputStream is = optRes.get().open()) {
                    var4 = is.readAllBytes();
                }

                return var4;
            } catch (Exception var8) {
                LOGGER.warn("[FPV Audio] Read failed for {}: {}", soundName, var8.getMessage());
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

    private static int uploadPcm(ExplosionAudioEngine.DecodedOgg decoded) {
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

    private static class Voice {
        final Vec3 worldPos;
        final boolean indoors;
        final long startMillis = System.currentTimeMillis();
        int closeSourceId = -1;
        int distantSourceId = -1;
        float gainBoost = 1.0F;

        Voice(Vec3 worldPos, boolean indoors) {
            this.worldPos = worldPos;
            this.indoors = indoors;
        }
    }
}
