#version 300 es

precision mediump float;

layout (location = 0) in vec3 aPosition;
layout (location = 1) in vec2 aTexCoord;

uniform int uCharIndex;

out vec2 vTexCoord;

const float CHAR_STEP = 32.0f / 512.0f;
const int CHARS_IN_ROW  = 16;

void main() {
    float charX = float(uCharIndex % CHARS_IN_ROW);
    float charY = float(uCharIndex / CHARS_IN_ROW);
    float texCoordX = (charX + aTexCoord.x) * CHAR_STEP;
    float texCoordY = 1.0f - (charY + aTexCoord.y) * CHAR_STEP;
    vTexCoord = vec2(texCoordX, texCoordY);
    gl_Position = vec4(aPosition, 1.0);
}