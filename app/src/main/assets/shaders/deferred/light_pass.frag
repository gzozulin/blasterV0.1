#version 300 es

in vec2 vTexCoord;

uniform sampler2D uTexPosition;
uniform sampler2D uTexNormal;
uniform sampler2D uTexAlbedoSpec;

uniform vec3 uViewPosition;

struct Light {
    vec3 position;
    vec3 color;
};

const int LIGHTS_CNT = 16;
uniform Light uLights[LIGHTS_CNT];

const float ambientTerm         = 0.1;
const float lightConstantAtt    = 1.0;
const float lightLinearAtt      = 0.8;
const float lightQuadraticAtt   = 0.2;
const float specularPower       = 16.0;

out vec4 oFragColor;

void main()
{
    vec4 positionLookup = texture(uTexPosition, vTexCoord);
    if (positionLookup.a != 1.0) {
        discard;
    }

    vec3 fragPosition = positionLookup.rgb;
    vec3 fragNormal = texture(uTexNormal, vTexCoord).rgb;
    vec4 fragAlbedoSpec = texture(uTexAlbedoSpec, vTexCoord);
    vec3 fragDiffuse = fragAlbedoSpec.rgb;
    float fragSpecular = fragAlbedoSpec.a;

    vec3 viewDir  = normalize(uViewPosition - fragPosition);

    vec3 ambientContribution  = vec3(ambientTerm);
    vec3 diffuseContribution = vec3(0);
    vec3 specularContribution = vec3(0);

    for (int i = 0; i < LIGHTS_CNT; ++i) {
        float distance = length(uLights[i].position - fragPosition);
        float attenuation = 1.0 / (1.0 + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
        if (attenuation > 0.1) {
            vec3 attenuatedLight = uLights[i].color * attenuation;
            vec3 lightDir = normalize(uLights[i].position - fragPosition);

            // diffuse
            float diffuseTerm = dot(fragNormal, lightDir);
            if (diffuseTerm > 0.0) {
                diffuseContribution += diffuseTerm * attenuatedLight;
            }

            // specular
            vec3 halfwayDir = normalize(lightDir + viewDir);
            float specularTerm = dot(fragNormal, halfwayDir);
            if (specularTerm > 0.0) {
                specularContribution += pow(specularTerm, specularPower) * attenuatedLight;
            }
        }
    }

    ambientContribution *= fragDiffuse;
    diffuseContribution *= fragDiffuse;
    specularContribution *= fragSpecular;
    oFragColor = vec4(ambientContribution + diffuseContribution + specularContribution, 1.0);
}
