#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 post/palette_lut.fsh: grey temperature to colour through a 256x1 palette.

uniform sampler2D InSampler;
uniform sampler2D LutSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 0) out vec4 fragColor;

void main() {
    float T = texture(InSampler, texCoord).r;
    vec3 color = texture(LutSampler, vec2(T, 0.5)).rgb;
    fragColor = vec4(color, 1.0);
}
