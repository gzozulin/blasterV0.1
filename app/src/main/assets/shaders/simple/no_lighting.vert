#version 100

#define IN attribute
#define OUT varying

IN vec3 aPosition;
IN vec3 aColor;

OUT vec3 vColor;

uniform mat4 uMvp;

void main() {
    gl_Position = uMvp * vec4(aPosition, 1f);
    vColor = aColor;
}