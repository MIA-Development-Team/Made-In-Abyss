#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 tileCoord;
out vec2 worldHorizontalPosition;
flat out vec2 spriteMin;
flat out vec2 spriteMax;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;
    sphericalVertexDistance = fog_spherical_distance(viewPosition.xyz);
    cylindricalVertexDistance = fog_cylindrical_distance(viewPosition.xyz);
    float shade = Normal.y < -0.5 ? 0.6 : (Normal.y > 0.5 ? 1.0 : 0.8);
    vertexColor = vec4(vec3(shade), 1.0);
    worldHorizontalPosition = Position.xz;
    if (abs(Normal.x) > 0.5) {
        tileCoord = vec2(Position.z, -Position.y);
    } else if (abs(Normal.y) > 0.5) {
        tileCoord = Position.xz;
    } else {
        tileCoord = vec2(Position.x, -Position.y);
    }
    spriteMin = vec2(UV1) / 32767.0;
    spriteMax = vec2(UV2) / 32767.0;
}
