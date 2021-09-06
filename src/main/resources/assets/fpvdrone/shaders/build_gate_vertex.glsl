#version 120

varying vec4 vColor;

void main() {
    vColor = gl_Color;
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
}
