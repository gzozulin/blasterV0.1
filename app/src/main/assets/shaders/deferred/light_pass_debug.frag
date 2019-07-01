#version 300 es

precision highp float;

in vec2 vTexCoord;

uniform sampler2D uTexturePosition;

out vec4 oFragColor;

void main()
{
    oFragColor = texture(uTexturePosition, vTexCoord) + vec4(1, 0, 0, 1);
}