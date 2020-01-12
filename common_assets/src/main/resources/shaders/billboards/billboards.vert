#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;
layout (location = 4) in vec3 aBillbPos;

uniform mat4 uModelM;
uniform mat4 uProjectionM;
uniform mat4 uViewM;

uniform vec3 uEye;
uniform float uWidth;
uniform float uHeight;

out vec2 vTexCoord;

mat4 billboardM() {
    vec3 from = uEye;
    vec3 to = aBillbPos;

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
    vTexCoord = aTexCoord;
    vec4 position = vec4(0.0);
    position.x = aPosition.x * uWidth / 2.0;
    position.y = aPosition.y * uHeight / 2.0;
    position.z = aPosition.z;
    position.w = 1.0;
    gl_Position = uProjectionM * uViewM * uModelM * billboardM() * position;
}