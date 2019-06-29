#version 300 es

precision highp float;

layout (location = 0) out vec3 oPosition;
layout (location = 1) out vec3 oNormal;
layout (location = 2) out vec4 oAlbedoSpec;

in vec2 vTexCoord;
in vec3 vFragPos;
in vec3 vNormal;

//uniform sampler2D texture_diffuse1;
//uniform sampler2D texture_specular1;

void main()
{
    // store the fragment position vector in the first gbuffer texture
    oPosition = vFragPos;
    // also store the per-fragment normals into the gbuffer
    oNormal = normalize(vNormal);
    // and the diffuse per-fragment color
    oAlbedoSpec.rgb = texture(texture_diffuse1, vTexCoord).rgb;
    // store specular intensity in gAlbedoSpec's alpha component
    oAlbedoSpec.a = texture(texture_specular1, vTexCoord).r;
}
