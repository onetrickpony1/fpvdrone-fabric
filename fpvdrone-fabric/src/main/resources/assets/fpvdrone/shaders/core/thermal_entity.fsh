#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 rendertype_entity_solid_thermal.fsh: every entity is drawn at one hot temperature (0.92), keeping only
// the texture's cut-outs and invisibility. Used with vanilla's core/entity vertex shader.

#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

#ifdef PER_FACE_LIGHTING
layout(location = 2) in vec4 vertexPerFaceColorBack;
layout(location = 3) in vec4 vertexPerFaceColorFront;
#else
layout(location = 2) in vec4 vertexColor;
#endif
layout(location = 6) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 texColor = texture(Sampler0, texCoord0);
#ifdef PER_FACE_LIGHTING
    vec4 faceVertexColor = gl_FrontFacing ? vertexPerFaceColorFront : vertexPerFaceColorBack;
#else
    vec4 faceVertexColor = vertexColor;
#endif
    float alpha = texColor.a * faceVertexColor.a * ColorModulator.a;
    if (alpha < 0.1) {
        discard;
    }
    float T = 0.92;
    fragColor = vec4(T, T, T, 1.0);
}
