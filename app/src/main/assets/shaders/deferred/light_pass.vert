#version 300 es

precision highp float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoords;

out vec2 vTexCoords;

void main()
{
    vTexCoords = aTexCoords;
    gl_Position = vec4(aPosition, 1.0);
}