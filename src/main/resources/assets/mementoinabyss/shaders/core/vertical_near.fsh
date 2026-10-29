#version 330
#moj_import <minecraft:fog.glsl>
uniform sampler2D Sampler0;
in vec2 texCoord;
in vec4 vertexColor;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;
out vec4 fragColor;
void main() {
    vec4 color = texture(Sampler0, texCoord) * vertexColor;
    if (color.a < 0.1) discard;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance,
        FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
