#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 4) in float aIsAlive;
layout (location = 5) in vec3 aParticlePosition;

uniform mat4 uModelM;
uniform mat4 uProjectionM;
uniform mat4 uViewM;

out float vIsAlive;

out vec2 vTexCoord;

void main() {
    vIsAlive = aIsAlive;
    vTexCoord = aTexCoord;
    if (vIsAlive == 0.0) {
        return;
    }
    mat4 mvp =  uProjectionM * uViewM * uModelM;
    gl_Position = mvp * vec4(aPosition + aParticlePosition, 1.0);
}