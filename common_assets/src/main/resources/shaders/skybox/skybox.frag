#version 300 es

precision mediump float;

in vec3 vTexCoord;

uniform samplerCube uTexSkybox;

layout (location = 0) out vec4 oFragColor;

void main()
{
    oFragColor = texture(uTexSkybox, vTexCoord);
}