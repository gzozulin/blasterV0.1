#version 300 es

precision mediump float;

in float vIsAlive;

layout (location = 0) out vec4 oFragColor;

void main() {
    /*if (vIsAlive == 0.0) {
        discard;
    }*/
    oFragColor = vec4(1, 0, 0, 1);
}