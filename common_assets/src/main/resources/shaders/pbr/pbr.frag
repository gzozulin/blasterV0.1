#version 300 es

precision mediump float;

const float PI = 3.14159265359;

in vec3 vWorldPos;
in vec2 vTexCoord;
in vec3 vNormal;

vec3 albedo = vec3(0.5);
float metallic = 0.4;
float roughness = 0.7;
float ao = 0.3;

struct Light {
    vec3 vector;
    vec3 intensity;
};

uniform int uLightsPointCnt;
uniform int uLightsDirCnt;
uniform Light uLights[128];

const float ambientTerm = 0.03;

const float lightConstantAtt    = 0.9;
const float lightLinearAtt      = 0.7;
const float lightQuadraticAtt   = 0.3;

uniform vec3 uEye;

layout (location = 0) out vec4 oFragColor;

float distributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness*roughness;
    float a2 = a*a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;
    float nom = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;
    return nom / denom;
}

float geometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1.0);
    float k = (r*r) / 8.0;
    float nom = NdotV;
    float denom = NdotV * (1.0 - k) + k;
    return nom / denom;
}

float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2 = geometrySchlickGGX(NdotV, roughness);
    float ggx1 = geometrySchlickGGX(NdotL, roughness);
    return ggx1 * ggx2;
}

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

void main() {
    vec3 N = normalize(vNormal);
    vec3 V = normalize(uEye - vWorldPos);

    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallic);

    // reflectance equation
    vec3 Lo = vec3(0.0);
    for (int i = 0; i < uLightsPointCnt; ++i) {

        // calculate per-light radiance
        vec3 L = normalize(uLights[i].vector - vWorldPos);
        vec3 H = normalize(V + L);
        float distance = length(uLights[i].vector - vWorldPos);
        float attenuation = 1.0 / (distance * distance);
        vec3 radiance = uLights[i].intensity * attenuation;

        // cook-torrance brdf
        float NDF = distributionGGX(N, H, roughness);
        float G = geometrySmith(N, V, L, roughness);
        vec3 F = fresnelSchlick(max(dot(H, V), 0.0), F0);
        vec3 kS = F;
        vec3 kD = vec3(1.0) - kS;
        kD *= 1.0 - metallic;
        vec3 nominator = NDF * G * F;
        float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.001;
        vec3 specular = nominator / denominator;

        // add to outgoing radiance Lo
        float NdotL = max(dot(N, L), 0.0);
        Lo += (kD * albedo / PI + specular) * radiance * NdotL;
    }

    vec3 ambient = vec3(ambientTerm) * albedo * ao;
    vec3 color = ambient + Lo;

    color = color / (color + vec3(1.0));
    color = pow(color, vec3(1.0/2.2));

    oFragColor = vec4(color, 1.0);
}