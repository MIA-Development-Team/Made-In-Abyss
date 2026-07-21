#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(std140) uniform LodFog {
    // x/y are the LOD-specific render-distance fog start/end in blocks.
    vec4 LodFogDistances;
};

layout(std140) uniform LodLight {
    // xy: center in source-dimension block coordinates, z: full-light radius,
    // w: outward fade distance. Negative radius means unrestricted.
    vec4 LodLightRegion;
    // x: ambient brightness outside the sky-lit region.
    vec4 LodLightLevels;
};

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 tileCoord;
in vec2 worldHorizontalPosition;
flat in vec2 spriteMin;
flat in vec2 spriteMax;

out vec4 fragColor;

float smootherstep(float edge0, float edge1, float value) {
    float t = clamp((value - edge0) / (edge1 - edge0), 0.0, 1.0);
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

void main() {
    vec2 spriteSize = spriteMax - spriteMin;
    vec2 halfTexel = 0.5 / vec2(textureSize(Sampler0, 0));
    vec2 atlasCoord = spriteMin + fract(tileCoord) * spriteSize;
    atlasCoord = clamp(atlasCoord, spriteMin + halfTexel, spriteMax - halfTexel);

    // Derivatives from the unwrapped coordinate keep mip selection stable at tile boundaries.
    vec4 color = textureGrad(Sampler0, atlasCoord,
            dFdx(tileCoord) * spriteSize, dFdy(tileCoord) * spriteSize);
    if (color.a <= 0.001) discard;

    float fade = vertexColor.a * ColorModulator.a;
    float dither = fract(52.9829189 * fract(dot(gl_FragCoord.xy, vec2(0.06711056, 0.00583715))));
    if (fade >= 0.0) {
        if (dither > fade) discard;
    } else {
        // A negative fade is the outgoing mesh. Its mask is exactly complementary
        // to the incoming mesh, avoiding both holes and overlapping depth writes.
        if (dither <= -fade) discard;
    }

    vec2 lightOffset = worldHorizontalPosition - LodLightRegion.xy;
    float lightDistance = length(lightOffset);
    float skyLightFactor = LodLightRegion.z < 0.0 ? 1.0
            : (LodLightRegion.w <= 0.0 ? step(lightDistance, LodLightRegion.z)
            : 1.0 - smootherstep(LodLightRegion.z, LodLightRegion.z + LodLightRegion.w, lightDistance));
    // Keep LOD lighting continuous. Real chunks are limited to vanilla's 16 light levels,
    // while this continuous value prevents those levels from becoming large visible bands.
    float curvedSkyLight = skyLightFactor / (4.0 - 3.0 * skyLightFactor);
    float regionalBrightness = clamp(LodLightLevels.x + curvedSkyLight, 0.0, 1.0);
    color.rgb *= vertexColor.rgb * ColorModulator.rgb * regionalBrightness;
    color.a = 1.0;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
            FogEnvironmentalStart, FogEnvironmentalEnd,
            LodFogDistances.x, LodFogDistances.y, FogColor);
}
