#version 330
#extension GL_ARB_separate_shader_objects : require

// 1.1.8 rendertype_solid_thermal / rendertype_cutout_thermal / rendertype_translucent_thermal (.fsh) in one file.
// Sampler0 is the thermal atlas: R = base temperature, G = emissivity, B = material type, A = thermal mass.
// Heat from torches/lava uses the block light level; the sun warms faces turned to it.

#include <fpvdrone:thermal_terrain.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 texCoord0;
layout(location = 2) in vec3 relPos;
layout(location = 3) in vec2 lightCoord;

layout(location = 0) out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {
    vec4 thermalData = texture(Sampler0, texCoord0);
    float baseTemp   = thermalData.r;
    float emissivity = thermalData.g;
    float matType    = thermalData.b;

#ifdef THERMAL_CUTOUT
    // Texels that were transparent in the block texture are left empty in the thermal atlas.
    if (thermalData.a < 0.01) {
        discard;
    }
#endif
#ifdef THERMAL_TRANSLUCENT
    if (vertexColor.a < 0.01) {
        discard;
    }
#endif

    if (emissivity < 0.02) {
        baseTemp   = 0.12;
        emissivity = 0.90;
        matType    = 0.0;
    }

    // Face normal from the position (1.1.8 had a vertex normal), turned towards the camera.
    vec3 worldNormal = normalize(cross(dFdx(relPos), dFdy(relPos)));
    if (dot(worldNormal, relPos) > 0.0) {
        worldNormal = -worldNormal;
    }

    float blockLight = lightCoord.x;
    float skyLight = lightCoord.y;
    float blockHeat = pow(blockLight, 1.5) * 0.65;

    float upFacing = max(worldNormal.y, 0.0);
    float roofFactor = smoothstep(0.25, 0.35, baseTemp);
    float roofCool = upFacing * upFacing * 0.22 * roofFactor;

    float T_apparent;
#ifdef THERMAL_TRANSLUCENT
    if (matType > 0.6 && matType < 0.9) {
        // Water
        float wave = hash(texCoord0 * 50.0 + vec2(float(ThermalFrameCount) * 0.01)) * 0.02;
        T_apparent = baseTemp + wave;
        T_apparent = mix(T_apparent, 1.0, blockHeat);
        T_apparent = T_apparent - roofCool;
    } else if (matType > 0.1 && matType < 0.4) {
        // Glass is opaque and cold in long-wave infrared.
        T_apparent = baseTemp;
    } else
#endif
    {
        // Solar heating: the sun is overhead at noon (ThermalGameTime = 0.25).
        float sunAngle = (ThermalGameTime - 0.25) * 2.0 * 3.14159265;
        vec3 sunDir = normalize(vec3(0.0, cos(sunAngle), sin(sunAngle)));
        float lambert = max(dot(worldNormal, sunDir), 0.0);

        bool isMetal = (matType > 0.4 && matType < 0.6);
        float solarCoeff = isMetal ? 0.45 : 0.18;
        float solarHeat = lambert * skyLight * ThermalWeatherMod * solarCoeff;
        float dayFactor = max(0.0, cos((ThermalGameTime - 0.25) * 2.0 * 3.14159265));
        float metalRetained = isMetal ? dayFactor * 0.18 : 0.0;

        float T_surface = mix(baseTemp, 1.0, blockHeat);
        T_surface = T_surface + solarHeat + metalRetained;
        T_surface = T_surface - roofCool;

        T_apparent = mix(ThermalAmbientTemp, T_surface, emissivity);
    }

    T_apparent = clamp(T_apparent, 0.0, 1.0);
    fragColor = vec4(T_apparent, T_apparent, T_apparent, 1.0);
}
