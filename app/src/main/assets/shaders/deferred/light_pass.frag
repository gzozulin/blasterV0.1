#version 300 es

precision highp float;

out vec4 oFragColor;

in vec2 vTexCoord;

uniform sampler2D uPosition;
uniform sampler2D uNormal;
uniform sampler2D uAlbedoSpec;

struct Light {
    vec3 Position;
    vec3 Color;

    float Linear;
    float Quadratic;
};
const int NR_LIGHTS = 32;
uniform Light lights[NR_LIGHTS];

uniform vec3 uViewPosition;

void main()
{
    // retrieve data from gbuffer
    vec3 FragPos = texture(uPosition, vTexCoord).rgb;
    vec3 Normal = texture(uNormal, vTexCoord).rgb;
    vec3 Diffuse = texture(uAlbedoSpec, vTexCoord).rgb;
    float Specular = texture(uAlbedoSpec, vTexCoord).a;

    // then calculate lighting as usual
    vec3 lighting  = Diffuse * 0.1; // hard-coded ambient component
    vec3 viewDir  = normalize(uViewPosition - FragPos);
    for(int i = 0; i < NR_LIGHTS; ++i)
    {
        // diffuse
        vec3 lightDir = normalize(lights[i].Position - FragPos);
        vec3 diffuse = max(dot(Normal, lightDir), 0.0) * Diffuse * lights[i].Color;
        // specular
        vec3 halfwayDir = normalize(lightDir + viewDir);
        float spec = pow(max(dot(Normal, halfwayDir), 0.0), 16.0);
        vec3 specular = lights[i].Color * spec * Specular;
        // attenuation
        float distance = length(lights[i].Position - FragPos);
        float attenuation = 1.0 / (1.0 + lights[i].Linear * distance + lights[i].Quadratic * distance * distance);
        diffuse *= attenuation;
        specular *= attenuation;
        lighting += diffuse + specular;
    }
    oFragColor = vec4(lighting, 1.0);
}
