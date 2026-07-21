#version 330

#moj_import <minecraft:dynamictransforms.glsl>

in float skyboxY;
out vec4 fragColor;

void main() {
    // The floor is white. The lower part of each side eases into the active fog color,
    // hiding the hard seam a flat-colored cube would otherwise expose.
    float fogBlend = smoothstep(-100.0, -55.0, skyboxY);
    fragColor = mix(vec4(1.0), ColorModulator, fogBlend);
}
