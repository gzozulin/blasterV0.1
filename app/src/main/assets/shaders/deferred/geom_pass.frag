#version 300 es

precision highp float;

in vec4 vFragPosition;
in vec2 vTexCoord;
in vec3 vNormal;

uniform sampler2D uTexDiffuse;

layout (location = 0) out vec4 oPosition;
layout (location = 1) out vec3 oNormal;
layout (location = 2) out vec4 oDiffuse;

void main()
{
    vec4 diffuse = texture(uTexDiffuse, vTexCoord);
    if (diffuse.a < 0.1) {
        discard;
    }
    oDiffuse = diffuse;
    oPosition = vFragPosition;
    oNormal = normalize(vNormal);
}
