#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:fog.glsl>

in vec3 skyboxPosition;
out vec4 fragColor;

void main() {
    // The floor is white. The lower part of each side eases into its selected tint,
    // hiding the hard seam a flat-colored cube would otherwise expose.
    float sideBlend = smoothstep(-100.0, -55.0, skyboxPosition.y);
    vec3 skyColor = mix(vec3(1.0), ColorModulator.rgb, sideBlend);

    // Project the viewing ray onto vanilla's sky planes at Y=+/-16. The cube's
    // mesh size must not limit fog coverage at the horizon.
    vec3 skyPosition = skyboxPosition * (16.0 / max(abs(skyboxPosition.y), 0.0001));
    fragColor = apply_fog(vec4(skyColor, 1.0), fog_spherical_distance(skyPosition),
        fog_cylindrical_distance(skyPosition), 0.0, FogSkyEnd, FogSkyEnd, FogSkyEnd, FogColor);
}
