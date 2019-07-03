#version 300 es

precision highp float;

in vec2 vTexCoord;

uniform sampler2D uTexPosition;

out vec4 oFragColor;

void main()
{
    vec3 position = texture(uTexPosition, vTexCoord).rgb;
    oFragColor = vec4(position, 1);
}