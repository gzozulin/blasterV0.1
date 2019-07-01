#version 300 es

precision mediump float;

in vec2 vTexCoord;

uniform sampler2D uTextureDiffuse;

layout (location = 0) out vec4 oFragColor;

void main() {
    oFragColor = texture(uTextureDiffuse, vTexCoord);
    if (oFragColor.a < 0.1) {
        discard;
    }
}