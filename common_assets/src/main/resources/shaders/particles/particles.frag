#version 300 es

precision mediump float;

in float vIsAlive;

in vec2 vTexCoord;

uniform sampler2D uTexDiffuse;

layout (location = 0) out vec4 oFragColor;

void main() {
    if (vIsAlive == 0.0) {
        discard;
    }
    oFragColor = texture(uTexDiffuse, vTexCoord);
    if (oFragColor.a < 0.1) {
        discard;
    }
}