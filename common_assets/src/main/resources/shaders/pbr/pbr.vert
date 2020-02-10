#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 uModelM;
uniform mat4 uProjectionM;
uniform mat4 uViewM;

out vec3 vWorldPos;
out vec2 vTexCoord;
out vec3 vNormal;

void main() {
    vTexCoord = aTexCoord;
    mat4 mvp =  uProjectionM * uViewM * uModelM;
    gl_Position = mvp * vec4(aPosition, 1.0);
    vWorldPos = gl_Position.xyz;
}