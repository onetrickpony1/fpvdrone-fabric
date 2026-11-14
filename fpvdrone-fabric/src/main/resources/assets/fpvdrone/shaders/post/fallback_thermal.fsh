#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 post/fallback_thermal.fsh: a temperature picture made from the normally drawn scene (colour brightness and
// distance), for when the thermal block shaders cannot be used (Sodium, Iris).

#include <fpvdrone:thermal_post.glsl>

uniform sampler2D InSampler;     // scene colour
uniform sampler2D DepthSampler;  // scene depth

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec4 color = texture(InSampler, texCoord);
    float depth = texture(DepthSampler, texCoord).r;

    // Sky / void
    if (UseDepth == 1 && thermal_is_sky(depth)) {
        float skyTemp = AmbientTemp * 0.3;
        fragColor = vec4(skyTemp, skyTemp, skyTemp, 1.0);
        return;
    }

    float linearDepth = UseDepth == 1 ? thermal_linear_depth(texCoord, depth) : 32.0;
    float luma = dot(color.rgb, vec3(0.299, 0.587, 0.114));

    // Smooth depth fade (atmospheric scattering) cools distant objects.
    float fogDensity = 0.015;
    float depthFactor = exp(-linearDepth * fogDensity);
    float terrainTemp = AmbientTemp + (luma * 0.15) + (depthFactor * 0.25);

    // Bright (entities drawn hot, lava, fire) or intensely red pixels count as heat.
    float heatSignal = max(color.r, luma);
    float finalTemp = mix(terrainTemp, heatSignal, smoothstep(0.4, 0.8, heatSignal));
    finalTemp = clamp(finalTemp, 0.0, 1.0);
    fragColor = vec4(finalTemp, finalTemp, finalTemp, 1.0);
}
