#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:fog.glsl>

// Match the leading matrix/color layout of the engine's DynamicUniforms transform storage.
layout(std140) uniform AbyssSkyFog {
    mat4 AbyssSkyModelView;
    vec4 AbyssSkyTransmission;
};

in float skyboxY;
out vec4 fragColor;

void main() {
    // The floor is white. The lower part of each side eases into its selected tint,
    // hiding the hard seam a flat-colored cube would otherwise expose.
    float sideBlend = smoothstep(-100.0, -55.0, skyboxY);
    vec3 skyColor = mix(vec3(1.0), ColorModulator.rgb, sideBlend);
    float fogBlend = clamp(1.0 - AbyssSkyTransmission.x, 0.0, 1.0) * FogColor.a;
    fragColor = vec4(mix(skyColor, FogColor.rgb, fogBlend), 1.0);
}
