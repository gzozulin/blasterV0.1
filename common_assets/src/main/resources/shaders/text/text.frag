#version 300 es

precision mediump float;

in vec2 vTexCoord;

uniform sampler2D uTexDiffuse;

uniform vec3 uColor;

layout (location = 0) out vec4 oFragColor;

void main() {
    oFragColor = texture(uTexDiffuse, vTexCoord);
    if (oFragColor.r == 1.0 && oFragColor.g == 1.0 && oFragColor.b == 1.0) {
        discard;
    }
    oFragColor.r = (1.0 - oFragColor.r) * uColor.r;
    oFragColor.g = (1.0 - oFragColor.g) * uColor.g;
    oFragColor.b = (1.0 - oFragColor.b) * uColor.b;
}