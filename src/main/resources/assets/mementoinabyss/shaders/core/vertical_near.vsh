#version 330
#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
out vec2 texCoord;
out vec4 vertexColor;
out float sphericalVertexDistance;
out float cylindricalVertexDistance;
void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * view;
    texCoord = UV0;
    vec2 light = clamp(vec2(UV2) / 240.0, 0.0, 1.0);
    light = light / (4.0 - 3.0 * light);
    float brightness = clamp(ColorModulator.a + max(light.x, light.y), 0.0, 1.0);
    vertexColor = vec4(Color.rgb * brightness, Color.a);
    sphericalVertexDistance = fog_spherical_distance(view.xyz);
    cylindricalVertexDistance = fog_cylindrical_distance(view.xyz);
}
