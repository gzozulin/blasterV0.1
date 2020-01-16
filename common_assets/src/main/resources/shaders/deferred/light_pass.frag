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
uniform Light uLights[128];

const float ambientTerm         = 0.7;
const float specularPower       = 2.0;

const float lightConstantAtt    = 1.0;
const float lightLinearAtt      = 0.8;
const float lightQuadraticAtt   = 0.2;

out vec4 oFragColor;

// todo: spot light is done by comparing the angle (dot prod) between light dir an vec from light to fragment
// https://www.lighthouse3d.com/tutorials/glsl-tutorial/spotlights/

float attenuation(float distance) {
    return 1.0 / (1.0 + lightLinearAtt * distance + lightQuadraticAtt * distance * distance);
}

vec3 lightContrib(vec3 viewDir, vec3 lightDir, vec3 fragNormal, vec3 lightIntensity, float attenuation) {
    vec3 contribution = vec3(0.0);
    vec3 attenuatedLight = lightIntensity * attenuation;
    // diffuse
    float diffuseTerm = dot(fragNormal, lightDir);
    if (diffuseTerm > 0.0) {
        contribution += diffuseTerm * attenuatedLight;
    }
    // specular
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float specularTerm = dot(fragNormal, halfwayDir);
    if (specularTerm > 0.0) {
        contribution += pow(specularTerm, specularPower) * attenuatedLight;
    }
    return contribution;
}

vec3 pointLightContrib(vec3 viewDir, vec3 fragPosition, vec3 fragNormal, vec3 lightVector, vec3 lightIntensity) {
    vec3 direction = lightVector - fragPosition;
    float attenuation = attenuation(length(direction));
    vec3 lightDir = normalize(direction);
    return lightContrib(viewDir, lightDir, fragNormal, lightIntensity, attenuation);
}

vec3 dirLightContrib(vec3 viewDir, vec3 fragNormal, vec3 lightVector, vec3 lightIntensity) {
    float attenuation = 1.0; // no attenuation
    vec3 lightDir = -normalize(lightVector);
    return lightContrib(viewDir, lightDir, fragNormal, lightIntensity, attenuation);
}

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
        lighting += pointLightContrib(viewDir, fragPosition, fragNormal, uLights[i].vector, uLights[i].intensity);
    }

    for (int i = 0; i < uLightsDirCnt; ++i) {
        lighting += dirLightContrib(viewDir, fragNormal, uLights[i].vector, uLights[i].intensity);
    }

    lighting *= fragDiffuse;
    oFragColor = vec4(lighting, 1.0);
}
