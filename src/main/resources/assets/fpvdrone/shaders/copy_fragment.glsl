#version 120

varying vec2 vTex;

uniform sampler2D inTex;

void main() {
    vec4 samp = texture2D(inTex, vTex);
    gl_FragColor = vec4(samp.r, samp.g, samp.b, samp.a);

    //    float a = atan(tanAngle) / (170 * 3.14159 / 180) + 0.5;
//        gl_FragColor = vec4(1, 0, 0, 1);
}