#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 post/netd_noise.fsh: per-frame sensor read-out noise, a little stronger on cold areas.

#include <fpvdrone:thermal_post.glsl>

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

float hash(vec2 p, float seed) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33 + seed);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    float T = texture(InSampler, texCoord).r;
    vec2 pixelPos = texCoord * TexSize;
    float seed = float(FrameCount) * 0.7123;
    float noise = hash(pixelPos, seed) * 2.0 - 1.0;
    float amplitude = 0.0005 + 0.0003 * (1.0 - T);
    float result = clamp(T + noise * amplitude, 0.0, 1.0);
    fragColor = vec4(result, result, result, 1.0);
}
