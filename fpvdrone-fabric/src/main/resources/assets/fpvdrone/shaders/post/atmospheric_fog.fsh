#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 post/atmospheric_fog.fsh: a very light, uniform blend towards the ambient temperature (more in rain).

#include <fpvdrone:thermal_post.glsl>

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    float T = texture(InSampler, texCoord).r;
    float fogFactor = clamp(Absorption * 10.0, 0.0, 0.08);
    float fogged = mix(T, AmbientTemp, fogFactor);
    fragColor = vec4(fogged, fogged, fogged, 1.0);
}
