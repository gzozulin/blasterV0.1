#version 300 es

precision highp float;

in vec2 vTexCoord;

uniform sampler2D uTexPosition;
uniform sampler2D uTexNormal;
uniform sampler2D uTexDiffuse;

uniform vec3 uEye;

struct Light {
    vec3 vector;
    vec3 intensity;
};

uniform int uLightsPointCnt;
uniform int uLightsDirCnt;
uniform Light uLights[64];

const float ambientTerm         = 0.7;
const float specularPower       = 2.0;

const float lightConstantAtt    = 1.0;
const float lightLinearAtt      = 0.8;
const float lightQuadraticAtt   = 0.2;

out vec4 oFragColor;

void main()
{
    vec4 positionLookup = texture(uTexPosition, vTexCoord);
    if (positionLookup.a != 1.0) {
        discard;
    }

    vec3 fragPosition = positionLookup.rgb;
    vec3 fragNormal = texture(uTexNormal, vTexCoord).rgb;
    vec3 fragDiffuse = texture(uTexDiffuse, vTexCoord).rgb;

    vec3 viewDir  = normalize(uEye - fragPosition);
    vec3 lighting  = vec3(ambientTerm);

    for (int i = 0; i < uLightsPointCnt; ++i) {
        vec3 direction = uLights[i].vector - fragPosition;

        float distance = length(direction);
        float attenuation = 1.0 / (1.0 + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
        if (attenuation > 0.0) {
            vec3 attenuatedLight = uLights[i].intensity * attenuation;
            vec3 lightDir = normalize(direction);

            // diffuse
            float diffuseTerm = dot(fragNormal, lightDir);
            if (diffuseTerm > 0.0) {
                lighting += diffuseTerm * attenuatedLight;
            }

            // specular
            vec3 halfwayDir = normalize(lightDir + viewDir);
            float specularTerm = dot(fragNormal, halfwayDir);
            if (specularTerm > 0.0) {
                lighting += pow(specularTerm, specularPower) * attenuatedLight;
            }
        }
    }

    lighting *= fragDiffuse;
    oFragColor = vec4(lighting, 1.0);
}
