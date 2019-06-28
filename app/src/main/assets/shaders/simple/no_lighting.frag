#version 100

#define IN varying

precision mediump float;

IN vec3 vColor;

void main() {
    gl_FragColor = vec4(vColor, 1f);
}