#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPosition;
layout (location = 4) in float aIsAlive;

uniform mat4 uModelM;
uniform mat4 uProjectionM;
uniform mat4 uViewM;

out float vIsAlive;

void main() {
    vIsAlive = aIsAlive;
    //if (vIsAlive == 0.0) {
    //    return;
    //}
    mat4 mvp =  uProjectionM * uViewM * uModelM;
    gl_Position = mvp * vec4(aPosition, 1.0);
}