#version 300 es

in vec2 vTexCoord;
in vec3 vFragPosition;
in vec3 vNormal;

uniform sampler2D uTexDiffuse;
uniform sampler2D uTexSpecular;

layout (location = 0) out vec3 oPosition;
layout (location = 1) out vec3 oNormal;
layout (location = 2) out vec4 oAlbedoSpec;

void main()
{
    vec4 diffuse = texture(uTexDiffuse, vTexCoord);
    if (diffuse.a < 0.1) {
        discard;
    }

    oAlbedoSpec.rgb = diffuse.rgb;

    // store the fragment position vector in the first gbuffer texture
    oPosition = vFragPosition;
    // also store the per-fragment normals into the gbuffer
    oNormal = normalize(vNormal);

    // store specular intensity in gAlbedoSpec's alpha component
    oAlbedoSpec.a = texture(uTexSpecular, vTexCoord).r;
}
