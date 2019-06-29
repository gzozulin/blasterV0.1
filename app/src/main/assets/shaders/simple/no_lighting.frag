#version 300 es

precision mediump float;

in vec2 vTexCoord;

uniform sampler2D uTexture0;

out vec4 oFragColor;

void main() {
    oFragColor = texture(uTexture0, vTexCoord);
}