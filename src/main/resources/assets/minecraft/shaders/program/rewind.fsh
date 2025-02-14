#version 150

uniform sampler2D DiffuseSampler;

uniform vec2 OutSize;
uniform vec2 InSize;
uniform float time;
in vec2 texCoord;
out vec4 fragColor;

const vec4 Zero = vec4(0.0);
const vec4 Half = vec4(0.5);
const vec4 One = vec4(1.0);
const vec4 Two = vec4(2.0);

#define PI 3.14159265359
vec4 HueShift (in vec3 Color, in float Shift)
{
    vec3 P = vec3(0.57735)*dot(vec3(0.57735),Color);

    vec3 U = Color-P;

    vec3 V = cross(vec3(0.57735),U);

    Color = U*cos(Shift*6.2832) + V*sin(Shift*6.2832) + P;

    return vec4(Color,1.0);
}

float sineClampedTimescale(float t,float offset,float clampMult) {
    return clamp(t*1.5+offset,0.0,PI*clampMult);
}

void main(){
    vec2 t = InSize / OutSize;
    vec2 uv = (2.0*texCoord-t.xy)/t.y;
    float timescale = time*1.2;
    float shakyTime = time;
    vec2 textureUv = (texCoord.xy / t.xy);
    vec2 shaky = vec2(sin(shakyTime*100.0)/100.0,cos(shakyTime*50.0)/100.0) * (sin(clamp(shakyTime*1.5+0.15,0.0,PI))*.25);
    vec2 warpy = vec2(sin(uv.x*sineClampedTimescale(timescale,0.0,1.0)),sin(uv.y*sineClampedTimescale(timescale,0.0,1.0)))*0.1;
    float distanceFromCentre = smoothstep(0.25,0.45,distance(uv,vec2(0,0))+1.0-sin(sineClampedTimescale(timescale*.9,0.0,1.0))*3.0);

    vec4 diffuseColor = texture(DiffuseSampler, textureUv+shaky+warpy*(1.0-sign(distanceFromCentre-0.5)));
    vec4 invertColor = 1.0 - diffuseColor;
    vec4 outColor = mix(diffuseColor, invertColor,clamp(distanceFromCentre*2.0,0.0,1.0));
    fragColor = HueShift(outColor.xyz,smoothstep(0.0,2.0,timescale)*0.5);
}
