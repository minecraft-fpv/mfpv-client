#version 120

varying vec2 vTex;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    vTex = vec2(gl_MultiTexCoord0.x, gl_MultiTexCoord0.y);
}
