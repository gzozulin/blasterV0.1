#version 300 es

precision highp float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

out vec3 vFragPosition;
out vec2 vTexCoords;
out vec3 vNormal;

uniform mat4 uModelM;
uniform mat4 uViewM;
uniform mat4 uProjectionM;

void main()
{
    vec4 worldPos = uModelM * vec4(aPosition, 1.0);
    vFragPosition = worldPos.xyz;
    vTexCoords = aTexCoords;

    mat3 normalMatrix = transpose(inverse(mat3(uModelM)));
    vNormal = normalMatrix * aNormal;

    gl_Position = uProjectionM * uViewM * worldPos;
}
