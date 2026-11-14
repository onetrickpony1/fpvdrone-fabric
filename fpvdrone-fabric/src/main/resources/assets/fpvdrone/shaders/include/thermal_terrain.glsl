#ifndef FPVDRONE_THERMAL_TERRAIN_GLSL
#define FPVDRONE_THERMAL_TERRAIN_GLSL

// 1.1.8 UniformManager values for the thermal block shaders.
layout(std140) uniform ThermalTerrain {
    float ThermalGameTime;   // day time 0..1 (0.25 = noon)
    float ThermalBiomeTemp;
    float ThermalAmbientTemp;
    float ThermalWeatherMod;
    int ThermalFrameCount;
};

#endif
