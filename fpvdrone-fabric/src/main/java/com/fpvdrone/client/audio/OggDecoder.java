package com.fpvdrone.client.audio;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.stb.STBVorbisInfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

/** Decodes into buffers owned by LWJGL, so the upload path can safely use memFree. */
final class OggDecoder {
    record Decoded(ShortBuffer pcm, int sampleRate) {}

    static Decoded decodeMono(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        ByteBuffer input = MemoryUtil.memAlloc(bytes.length);
        long decoder = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            input.put(bytes).flip();
            decoder = STBVorbis.stb_vorbis_open_memory(input, stack.mallocInt(1), null);
            if (decoder == 0) return null;
            STBVorbisInfo info = STBVorbis.stb_vorbis_get_info(decoder, STBVorbisInfo.malloc(stack));
            int channels = info.channels();
            int frames = STBVorbis.stb_vorbis_stream_length_in_samples(decoder);
            if (channels <= 0 || frames <= 0) return null;
            ShortBuffer interleaved = MemoryUtil.memAllocShort(Math.multiplyExact(frames, channels));
            try {
                int read = STBVorbis.stb_vorbis_get_samples_short_interleaved(decoder, channels, interleaved);
                if (read <= 0) return null;
                ShortBuffer mono = MemoryUtil.memAllocShort(read);
                for (int i = 0; i < read; i++) {
                    int sample = 0;
                    for (int channel = 0; channel < channels; channel++) sample += interleaved.get(i * channels + channel);
                    mono.put(i, (short)(sample / channels));
                }
                return new Decoded(mono, info.sample_rate());
            } finally {
                MemoryUtil.memFree(interleaved);
            }
        } finally {
            if (decoder != 0) STBVorbis.stb_vorbis_close(decoder);
            MemoryUtil.memFree(input);
        }
    }
}
