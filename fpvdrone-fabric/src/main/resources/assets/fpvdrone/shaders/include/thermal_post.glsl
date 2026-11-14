#ifndef FPVDRONE_THERMAL_POST_GLSL
#define FPVDRONE_THERMAL_POST_GLSL

// Shared parameters of the thermal post passes (1.1.8 PostProcessContext + per-pass uniforms).
layout(std140) uniform ThermalPost {
    mat4 InvProjMat;      // turns a depth sample back into a view-space position
    vec2 TexSize;
    vec2 Direction;       // depth of field: (1,0) then (0,1)
    float AmbientTemp;
    float Absorption;
    float FpnReduction;
    float FocusDistance;
    float Aperture;
    int FrameCount;
    int ZZeroToOne;       // depth range of the device
    int UseDepth;         // 0 when the depth buffer is not usable (shader packs)
};

// Distance from the camera, in blocks, of a depth sample (reversed or not, any depth range).
float thermal_linear_depth(vec2 uv, float depth) {
    float z = ZZeroToOne == 1 ? depth : depth * 2.0 - 1.0;
    vec4 view = InvProjMat * vec4(uv * 2.0 - 1.0, z, 1.0);
    return length(view.xyz / view.w);
}

// True for pixels nothing was drawn on (sky). 26.x clears depth to 0 (reversed Z).
bool thermal_is_sky(float depth) {
    return depth <= 0.00001;
}

#endif
