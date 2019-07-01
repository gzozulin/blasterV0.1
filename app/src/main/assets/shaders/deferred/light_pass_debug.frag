#version 300 es

precision highp float;

in vec2 vTexCoord;

uniform sampler2D uTexturePosition;

out vec4 oFragColor;

void main()
{
    oFragColor = vec4(texture(uTexturePosition, vTexCoord).rgb, 1);
}