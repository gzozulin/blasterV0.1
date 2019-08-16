#version 300 es

precision highp float;

in vec2 vTexCoord;

uniform sampler2D uTexPosition;
uniform sampler2D uTexNormal;
uniform sampler2D uTexDiffuse;

uniform vec3 uViewPosition;

struct Light {
    vec3 position;
    vec3 color;
};

const int LIGHTS_CNT = 16;
uniform Light uLights[LIGHTS_CNT];

const float ambientTerm         = 0.7;
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
    vec3 fragDiffuse = texture(uTexDiffuse, vTexCoord).rgb;

    vec3 viewDir  = normalize(uViewPosition - fragPosition);
    vec3 lighting  = vec3(ambientTerm);

    for (int i = 0; i < LIGHTS_CNT; ++i) {
        float distance = length(uLights[i].position - fragPosition);
        float attenuation = 1.0 / (1.0 + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
        if (attenuation > 0.1) {
            vec3 attenuatedLight = uLights[i].color * attenuation;
            vec3 lightDir = normalize(uLights[i].position - fragPosition);

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
