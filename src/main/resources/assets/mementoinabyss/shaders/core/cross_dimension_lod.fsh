#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 tileCoord;
flat in vec2 spriteMin;
flat in vec2 spriteMax;

out vec4 fragColor;

void main() {
    vec2 spriteSize = spriteMax - spriteMin;
    vec2 halfTexel = 0.5 / vec2(textureSize(Sampler0, 0));
    vec2 atlasCoord = spriteMin + fract(tileCoord) * spriteSize;
    atlasCoord = clamp(atlasCoord, spriteMin + halfTexel, spriteMax - halfTexel);

    // Derivatives from the unwrapped coordinate keep mip selection stable at tile boundaries.
    vec4 color = textureGrad(Sampler0, atlasCoord,
            dFdx(tileCoord) * spriteSize, dFdy(tileCoord) * spriteSize);
    color *= vertexColor * ColorModulator;
    color.a = 1.0;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
            FogEnvironmentalStart, FogEnvironmentalEnd,
            FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
