#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float GameTime;

out vec4 vertexColor;
out vec2 texCoord;
out float time;

void main() {
    time = GameTime * 1200.0;
    
    vec3 pos = Position;
    float wave = sin(pos.y * 3.0 + time * 0.5) * 0.05;
    pos.x += wave;
    pos.z += wave * 0.7;
    
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    vertexColor = Color;
    texCoord = UV0;
}
