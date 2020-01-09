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

mat4 lookAt(vec3 eye, vec3 target, vec3 updir) {
    // compute the forward vector from target to eye
    vec3 forward = eye - target;
    forward = normalize(forward);

    // compute the left vector
    vec3 left = cross(updir, forward);
    left = normalize(left);

    // recompute the orthonormal up vector
    vec3 up = cross(forward, left);

    mat4 matrix = mat4(1.0);

    // set rotation part, inverse rotation matrix: M^-1 = M^T for Euclidean transform
    matrix[0][0] = left.x;
    matrix[0][1] = left.y;
    matrix[0][2] = left.z;

    matrix[1][0] = up.x;
    matrix[1][1] = up.y;
    matrix[1][2] = up.z;

    matrix[2][0] = forward.x;
    matrix[2][1] = forward.y;
    matrix[2][2] = forward.z;

    // set translation part
    matrix[3][0] = -left.x * eye.x - left.y * eye.y - left.z * eye.z;
    matrix[3][1] = -up.x * eye.x - up.y * eye.y - up.z * eye.z;
    matrix[3][2] = -forward.x * eye.x - forward.y * eye.y - forward.z * eye.z;

    return matrix;
}

void main() {
    vIsAlive = aIsAlive;
    vTexCoord = aTexCoord;
    mat4 billbM = lookAt(uViewPosition, aParticlePosition, vec3(0.0, 1.0, 0.0));
    mat4 mvp =  uProjectionM * uViewM * uModelM * billbM;
    gl_Position = mvp * vec4(aPosition, 1.0);
}