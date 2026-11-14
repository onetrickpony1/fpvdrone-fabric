package com.fpvdrone.client.audio;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

/**
 * OpenAL helpers shared by the engine and explosion audio (1.1.8). Sources are placed relative to the camera, so
 * vanilla's sound engine moving the AL listener cannot shift them; the engines also survive an AL context swap
 * (sound device change) and pause with the game.
 */
final class AlAudio {
    private AlAudio() {
    }

    static boolean isFinite(double x, double y, double z) {
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z);
    }

    /** Clears stale AL errors left by other code, so the next check reports only ours. */
    static void clearErrors() {
        int guard = 0;

        while (AL10.alGetError() != 0 && guard++ < 16) {
        }
    }

    static int checkError(Logger logger, String where) {
        int err = AL10.alGetError();
        if (err != 0) {
            logger.warn("[FPV Audio] OpenAL error 0x{} at {}", Integer.toHexString(err), where);
            clearErrors();
        }

        return err;
    }

    static void setPaused(int source, boolean paused) {
        if (source >= 0) {
            if (paused) {
                AL10.alSourcePause(source);
            } else {
                AL10.alSourcePlay(source);
            }
        }
    }

    static byte[] readSound(String name, String extension, boolean warn, Logger logger) {
        Identifier id = Identifier.fromNamespaceAndPath("fpvdrone", "sounds/" + name + "." + extension);
        Optional<Resource> res = Minecraft.getInstance().getResourceManager().getResource(id);
        if (res.isEmpty()) {
            if (warn) {
                logger.warn("[FPV Audio] Resource not found: {}", id);
            }

            return null;
        }

        try (InputStream is = res.get().open()) {
            return is.readAllBytes();
        } catch (Exception e) {
            if (warn) {
                logger.warn("[FPV Audio] Read failed for {}: {}", name, e.getMessage());
            }

            return null;
        }
    }

    /** Uploads mono 16-bit PCM and frees it; 0 when there is nothing to upload. */
    static int upload(OggDecoder.Decoded decoded) {
        if (decoded == null || decoded.pcm() == null) {
            return 0;
        }

        try {
            int buf = AL10.alGenBuffers();
            AL10.alBufferData(buf, AL10.AL_FORMAT_MONO16, decoded.pcm(), decoded.sampleRate());
            return buf;
        } finally {
            MemoryUtil.memFree(decoded.pcm());
        }
    }

    /** Decodes a RIFF WAV (8/16/24/32-bit integer or 32-bit float) to mono 16-bit PCM. */
    static OggDecoder.Decoded decodeWav(byte[] bytes) {
        if (bytes == null || bytes.length < 44) {
            return null;
        }

        try {
            ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
            if (bb.getInt(0) != 0x46464952) {
                return null;
            }

            int format = 1;
            int channels = 1;
            int sampleRate = 44100;
            int bits = 16;
            int dataStart = -1;
            int dataLen = 0;
            int pos = 12;

            while (pos + 8 <= bytes.length) {
                int chunkId = bb.getInt(pos);
                int chunkLen = bb.getInt(pos + 4);
                int body = pos + 8;
                if (chunkId == 0x20746d66) {
                    format = bb.getShort(body) & 0xFFFF;
                    channels = bb.getShort(body + 2) & 0xFFFF;
                    sampleRate = bb.getInt(body + 4);
                    bits = bb.getShort(body + 14) & 0xFFFF;
                    if (format == 0xFFFE && chunkLen >= 26) {
                        format = bb.getShort(body + 24) & 0xFFFF;
                    }
                } else if (chunkId == 0x61746164) {
                    dataStart = body;
                    dataLen = chunkLen;
                }

                pos = body + chunkLen + (chunkLen & 1);
            }

            if (dataStart < 0 || channels < 1 || bits < 8) {
                return null;
            }

            dataLen = Math.min(dataLen, bytes.length - dataStart);
            int bytesPerSample = bits / 8;
            int frameSize = bytesPerSample * channels;
            int frames = dataLen / frameSize;
            boolean isFloat = format == 3;
            ShortBuffer out = MemoryUtil.memAllocShort(frames);

            for (int f = 0; f < frames; f++) {
                int frame = dataStart + f * frameSize;
                long sum = 0L;

                for (int c = 0; c < channels; c++) {
                    int at = frame + c * bytesPerSample;
                    sum += switch (bits) {
                        case 8 -> (bytes[at] & 255) - 128 << 8;
                        case 16 -> bb.getShort(at);
                        case 24 -> (bytes[at] & 255 | (bytes[at + 1] & 255) << 8 | bytes[at + 2] << 16) >> 8;
                        case 32 -> isFloat ? (int)(Math.max(-1.0F, Math.min(1.0F, bb.getFloat(at))) * 32767.0F) : bb.getInt(at) >> 16;
                        default -> 0;
                    };
                }

                out.put(f, (short)Math.max(-32768, Math.min(32767, (int)(sum / channels))));
            }

            out.position(0).limit(frames);
            return new OggDecoder.Decoded(out, sampleRate);
        } catch (Exception e) {
            return null;
        }
    }

    /** Notices when the OpenAL context the buffers were made in is gone (device change, sound reload). */
    static final class ContextWatch {
        private long context = 0L;

        void set(long context) {
            this.context = context;
        }

        boolean lost() {
            long current = ALC10.alcGetCurrentContext();
            return this.context != 0L && current != this.context;
        }
    }

    /** Camera basis for listener-relative source positions (right, up, -forward). */
    static final class ListenerFrame {
        final Vec3 position;
        private final Vector3f right = new Vector3f();
        private final Vector3f up = new Vector3f();
        private final Vector3f forward = new Vector3f();

        ListenerFrame(Camera camera) {
            this.position = camera.position();
            Vector3f look = new Vector3f(camera.forwardVector());
            Vector3f camUp = new Vector3f(camera.upVector());
            look.normalize(this.forward);
            this.forward.cross(camUp, this.right).normalize();
            this.right.cross(this.forward, this.up).normalize();
        }

        void toRelative(double x, double y, double z, float[] out) {
            double dx = x - this.position.x;
            double dy = y - this.position.y;
            double dz = z - this.position.z;
            out[0] = (float)(dx * this.right.x + dy * this.right.y + dz * this.right.z);
            out[1] = (float)(dx * this.up.x + dy * this.up.y + dz * this.up.z);
            out[2] = (float)(-(dx * this.forward.x + dy * this.forward.y + dz * this.forward.z));
        }
    }
}
