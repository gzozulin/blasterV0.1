uniform mat4 uMvp;
attribute vec4 vPosition;
void main() {
    gl_Position = uMvp * vPosition;
}