#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 4) in float aIsAlive;
layout (location = 5) in vec3 aParticlePosition;

uniform mat4 uModelM;
uniform mat4 uProjectionM;
uniform mat4 uViewM;

uniform vec3 uViewPosition;

out float vIsAlive;

out vec2 vTexCoord;

mat4 billboardM() {
    vec3 from = uViewPosition;
    vec3 to = aParticlePosition;

    vec3 forward = normalize(from - to);
    vec3 right = normalize(cross(vec3(0.0, 1.0, 0.0), forward));
    vec3 up = cross(forward, right);

    mat4 matrix = mat4(1.0);
    matrix[0][0] = right.x;
    matrix[0][1] = right.y;
    matrix[0][2] = right.z;
    matrix[1][0] = up.x;
    matrix[1][1] = up.y;
    matrix[1][2] = up.z;
    matrix[2][0] = forward.x;
    matrix[2][1] = forward.y;
    matrix[2][2] = forward.z;
    matrix[3][0] = to.x;
    matrix[3][1] = to.y;
    matrix[3][2] = to.z;
    return matrix;
}

void main() {
    vIsAlive = aIsAlive;
    vTexCoord = aTexCoord;
    gl_Position = uProjectionM * uViewM * uModelM * billboardM() * vec4(aPosition, 1.0);
}