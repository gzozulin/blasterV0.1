#version 300 es

in vec2 vTexCoord;

uniform sampler2D uTexPosition;
uniform sampler2D uTexNormal;
uniform sampler2D uTexAlbedoSpec;

uniform vec3 uViewPosition;

uniform vec3 uLightPos;
uniform vec3 uLightColor;

struct Light {
    vec3 position;
    vec3 color;
};

const int LIGHTS_CNT = 16;
uniform Light uLights[LIGHTS_CNT];

const float lightAmbient        = 0.1f;
const float lightConstantAtt    = 1.0f;
const float lightLinearAtt      = 0.8f;
const float lightQuadraticAtt   = 0.2f;
const float specularPower       = 16.0;

out vec4 oFragColor;

void main()
{
    vec3 fragPosIn = texture(uTexPosition, vTexCoord).rgb;
    vec3 normalIn = texture(uTexNormal, vTexCoord).rgb;
    vec4 albedoSpecIn = texture(uTexAlbedoSpec, vTexCoord);
    vec3 diffuseIn = albedoSpecIn.rgb;
    float specularIn = albedoSpecIn.a;

    vec3 lighting  = diffuseIn * lightAmbient;
    vec3 viewDir  = normalize(uViewPosition - fragPosIn);

    for (int i = 0; i < LIGHTS_CNT; ++i) {
        // diffuse
        vec3 lightDir = normalize(uLights[i].position - fragPosIn);
        vec3 diffuse = max(dot(normalIn, lightDir), 0.0) * diffuseIn * uLights[i].color;

        // specular
        vec3 halfwayDir = normalize(lightDir + viewDir);
        float spec = pow(max(dot(normalIn, halfwayDir), 0.0), specularPower);
        vec3 specular = uLights[i].color * spec * specularIn;

        // attenuation
        float distance = length(uLights[i].position - fragPosIn);
        float attenuation = 1.0 / (1.0 + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
        diffuse *= attenuation;
        specular *= attenuation;
        lighting += diffuse + specular;
    }

    oFragColor = vec4(lighting, 1.0);
}
