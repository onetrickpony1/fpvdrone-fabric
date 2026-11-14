#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 post/depth_of_field.fsh: separable blur whose strength grows with distance from the focus plane.
// Drawn twice: Direction (1,0) then (0,1).

#include <fpvdrone:thermal_post.glsl>

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

float computeCoC(float depth) {
    float diff = depth - FocusDistance;
    float deadZone = FocusDistance * 0.08;
    float absDiff = abs(diff);
    if (absDiff < deadZone) return 0.0;
    float excess = absDiff - deadZone;
    float coc = excess / max(FocusDistance, 1.0) * Aperture * TexSize.x;
    if (diff < 0.0) coc *= 0.5;
    return min(coc, 10.0);
}

void main() {
    vec2 texel = 1.0 / TexSize;
    vec2 stepDir = Direction * texel;
    float rawDepth = texture(DepthSampler, texCoord).r;
    // Sky counts as far away.
    float centerDepth = thermal_is_sky(rawDepth) ? 1.0e4 : thermal_linear_depth(texCoord, rawDepth);
    vec3 sharp = texture(InSampler, texCoord).rgb;

    // Geometry closer than ~1.5 blocks is the local drone model: keep it sharp.
    if (centerDepth < 1.5) {
        fragColor = vec4(sharp, 1.0);
        return;
    }

    float coc = computeCoC(centerDepth);
    if (coc < 0.5) {
        fragColor = vec4(sharp, 1.0);
        return;
    }

    vec2 blurStep = stepDir * (coc / 6.0);
    float w[7] = float[](0.1964, 0.1747, 0.1225, 0.0676, 0.0294, 0.0100, 0.0027);
    float total = w[0];
    vec3 acc = sharp * w[0];
    for (int i = 1; i <= 6; i++) {
        vec2 off = blurStep * float(i);
        acc += texture(InSampler, texCoord + off).rgb * w[i];
        acc += texture(InSampler, texCoord - off).rgb * w[i];
        total += w[i] * 2.0;
    }
    acc /= total;
    float blend = smoothstep(0.5, 3.0, coc);
    fragColor = vec4(mix(sharp, acc, blend), 1.0);
}
