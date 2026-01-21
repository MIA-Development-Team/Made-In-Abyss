#version 150

in vec4 vertexColor;
in vec2 texCoord;
in float time;

uniform vec4 ColorModulator;

out vec4 fragColor;

float noise(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

float smoothNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    
    float a = noise(i);
    float b = noise(i + vec2(1.0, 0.0));
    float c = noise(i + vec2(0.0, 1.0));
    float d = noise(i + vec2(1.0, 1.0));
    
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * smoothNoise(p);
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    vec2 uv = texCoord;
    
    float t = time * 0.001;
    
    vec2 distortedUV = uv + vec2(
        fbm(uv * 3.0 + t),
        fbm(uv * 3.0 - t + 100.0)
    ) * 0.1;
    
    float n = fbm(distortedUV * 5.0 + t * 0.5);
    
    vec3 deepPurple = vec3(0.35, 0.15, 0.45);
    vec3 darkBlue = vec3(0.25, 0.3, 0.4);
    vec3 abyssBlack = vec3(0.12, 0.12, 0.15);
    vec3 highlight = vec3(0.6, 0.4, 0.8);
    
    vec3 color = mix(abyssBlack, deepPurple, n);
    color = mix(color, darkBlue, smoothNoise(distortedUV * 8.0 + t));
    
    float pulse = sin(t * 2.0) * 0.5 + 0.5;
    float edge = smoothstep(0.3, 0.7, n + pulse * 0.2);
    color += highlight * edge * 0.5;
    
    float glow = smoothstep(0.0, 0.5, n) * pulse * 0.4;
    color += vec3(0.3, 0.1, 0.5) * glow;
    
    float alpha = vertexColor.a * ColorModulator.a;
    alpha += pulse * 0.1;
    
    fragColor = vec4(color * vertexColor.rgb * ColorModulator.rgb, alpha);
}
