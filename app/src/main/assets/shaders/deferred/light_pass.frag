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
    vec3 fragPosition = texture(uTexPosition, vTexCoord).rgb;
    vec3 fragNormal = texture(uTexNormal, vTexCoord).rgb;
    vec4 fragAlbedoSpec = texture(uTexAlbedoSpec, vTexCoord);
    vec3 fragDiffuse = fragAlbedoSpec.rgb;
    float fragSpecular = fragAlbedoSpec.a;
    vec3 lighting  = fragDiffuse * lightAmbient;
    vec3 viewDir  = normalize(uViewPosition - fragPosition);
    for (int i = 0; i < LIGHTS_CNT; ++i) {
        float distance = length(uLights[i].position - fragPosition);
        float attenuation = 1.0 / (1.0 + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
        if (attenuation > 0.1) {
            // diffuse
            vec3 lightDir = normalize(uLights[i].position - fragPosition);
            float diffuseTerm = dot(fragNormal, lightDir);
            if (diffuseTerm > 0.1) {
                lighting += diffuseTerm * fragDiffuse * uLights[i].color * attenuation;
            }
            // specular
            vec3 halfwayDir = normalize(lightDir + viewDir);
            float specularTerm = dot(fragNormal, halfwayDir);
            if (specularTerm > 0.1) {
                lighting += pow(specularTerm, specularPower) * uLights[i].color * fragSpecular * attenuation;
            }
        }
    }
    oFragColor = vec4(lighting, 1.0);
}
