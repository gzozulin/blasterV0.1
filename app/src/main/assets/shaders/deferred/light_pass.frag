#version 300 es

precision highp float;

in vec2 vTexCoord;

uniform sampler2D uTexturePosition;
uniform sampler2D uTextureNormal;
uniform sampler2D uTextureAlbedoSpec;

uniform vec3 uLightPos;
uniform vec3 uLightColor;

uniform vec3 uViewPosition;

const float lightAmbient = 0.1f;
const float lightConstantAttennuation = 1.0f;
const float lightLinearAttennuation = 0.6f;
const float lightQuadraticAttennuation = 1.5f;

out vec4 oFragColor;

void main()
{
    // retrieve data from gbuffer
    vec3 fragPosIn = texture(uTexturePosition, vTexCoord).rgb;
    vec3 normalIn = texture(uTextureNormal, vTexCoord).rgb;
    vec3 diffuseIn = texture(uTextureAlbedoSpec, vTexCoord).rgb;
    float specularIn = texture(uTextureAlbedoSpec, vTexCoord).a;

    // then calculate lighting as usual
    vec3 lighting  = diffuseIn * lightAmbient; // hard-coded ambient component
    vec3 viewDir  = normalize(uViewPosition - fragPosIn);

    // diffuse
    vec3 lightDir = normalize(uLightPos - fragPosIn);
    vec3 diffuse = max(dot(normalIn, lightDir), 0.0) * diffuseIn * uLightColor;

    // specular
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normalIn, halfwayDir), 0.0), 16.0);
    vec3 specular = uLightColor * spec * specularIn;

    // attenuation
    float distance = length(uLightPos - fragPosIn);
    float attenuation = 1.0 / (1.0 + lightLinearAttennuation * distance + lightQuadraticAttennuation * distance * distance);
    diffuse *= attenuation;
    specular *= attenuation;
    lighting += diffuse + specular;

    oFragColor = vec4(lighting, 1.0);
}
