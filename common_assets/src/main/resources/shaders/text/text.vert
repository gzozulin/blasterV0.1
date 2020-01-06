#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform int uCharIndex;
uniform vec2 uCharStart;
uniform float uCharScale;

out vec2 vTexCoord;

const float CHAR_STEP = 32.0 / 512.0;
const int CHARS_IN_ROW  = 16;

void calculateTexCoords() {
    float charIndexX = float(uCharIndex % CHARS_IN_ROW);
    float charIndexY = float(uCharIndex / CHARS_IN_ROW);
    float texCoordX = (charIndexX + aTexCoord.x) * CHAR_STEP;
    float texCoordY = (charIndexY + 1.0 - aTexCoord.y) * CHAR_STEP;
    vTexCoord = vec2(texCoordX, texCoordY);
}

void calculatePosition() {
    float positionX = uCharStart.x + aPosition.x * uCharScale;
    float positionY = uCharStart.y + aPosition.y * uCharScale;
    gl_Position = vec4(positionX, positionY, 0.0, 1.0);
}

void main() {
    calculateTexCoords();
    calculatePosition();
}
