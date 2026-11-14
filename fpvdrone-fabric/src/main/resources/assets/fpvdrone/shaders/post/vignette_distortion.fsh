#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 post/vignette_distortion.fsh: very slight barrel distortion and corner darkening of the thermal lens.

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    vec2 center = vec2(0.5, 0.5);
    vec2 offset = texCoord - center;
    float k = 0.02;
    float r2 = dot(offset, offset);
    vec2 distorted = center + offset * (1.0 + k * r2);
    vec3 color;
    if (distorted.x < 0.0 || distorted.x > 1.0 || distorted.y < 0.0 || distorted.y > 1.0) {
        color = vec3(0.0);
    } else {
        color = texture(InSampler, distorted).rgb;
    }
    float r = length(offset);
    float vignette = 1.0 - smoothstep(0.6, 1.2, r);
    fragColor = vec4(color * vignette, 1.0);
}
