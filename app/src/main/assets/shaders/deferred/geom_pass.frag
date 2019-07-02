#version 300 es

precision highp float;

in vec2 vTexCoord;
in vec3 vFragPosition;
in vec3 vNormal;

uniform sampler2D uTextureDiffuse;
uniform sampler2D uTextureSpecular;

layout (location = 0) out vec3 oPosition;
layout (location = 1) out vec3 oNormal;
layout (location = 2) out vec4 oAlbedoSpec;

void main()
{
    // store the fragment position vector in the first gbuffer texture
    oPosition = vFragPosition;
    // also store the per-fragment normals into the gbuffer
    oNormal = normalize(vNormal);
    // and the diffuse per-fragment color
    oAlbedoSpec.rgb = texture(uTextureDiffuse, vTexCoord).rgb;
    // store specular intensity in gAlbedoSpec's alpha component
    oAlbedoSpec.a = texture(uTextureSpecular, vTexCoord).r;
}
