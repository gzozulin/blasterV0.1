#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPosition;

uniform mat4 uProjectionM;
uniform mat4 uViewM;

out vec3 vTexCoord;

void main()
{
    vTexCoord = aPosition;
    gl_Position = uProjectionM * uViewM * vec4(aPosition, 1.0);
}