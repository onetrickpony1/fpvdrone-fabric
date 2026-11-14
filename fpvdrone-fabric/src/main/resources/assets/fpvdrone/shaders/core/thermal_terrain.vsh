#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 rendertype_solid_thermal.vsh for 26.3 terrain: passes the lightmap levels and the camera-relative position
// (the 26.3 block vertex has no normal; the fragment shader derives it from the position).

#include <minecraft:globals.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:terrainglobals.glsl>
#ifndef MULTIDRAW_TERRAIN
    #include <minecraft:chunksection.glsl>
#endif

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV2;
#ifdef MULTIDRAW_TERRAIN
layout(location = 4) in ivec3 ChunkPosition;
layout(location = 5) in float ChunkVisibility;
#endif

layout(location = 0) out vec4 vertexColor;
layout(location = 1) out vec2 texCoord0;
layout(location = 2) out vec3 relPos;
layout(location = 3) out vec2 lightCoord;

void main() {
    vec3 pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
    relPos = pos;
    // x = block light, y = sky light, both 0..1
    lightCoord = clamp(vec2(UV2) / 256.0, vec2(0.0), vec2(1.0));
}
