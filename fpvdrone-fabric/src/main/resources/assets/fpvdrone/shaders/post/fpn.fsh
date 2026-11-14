#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 post/fpn.fsh: fixed-pattern noise that stays still on screen; NUC calibration lowers it for a while.

#include <fpvdrone:thermal_post.glsl>

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

float staticHash(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    float T = texture(InSampler, texCoord).r;
    vec2 pixelPos = floor(texCoord * TexSize);
    float noise = staticHash(pixelPos) * 2.0 - 1.0;
    float amplitude = 0.0003 * (1.0 - FpnReduction);
    float result = clamp(T + noise * amplitude, 0.0, 1.0);
    fragColor = vec4(result, result, result, 1.0);
}
