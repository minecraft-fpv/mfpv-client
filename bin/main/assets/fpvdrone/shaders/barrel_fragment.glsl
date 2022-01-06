#version 120

varying vec2 vTex;

uniform sampler2D inTex;
uniform float fov; // radians, vertical fov.
uniform float aspect; // aspect * fov = horizontal fov. so aspect = w / h

void main() {
    float xImageMax = 0.5;
    float yImageMax = 0.5;
    vec2 texCentered = vTex - vec2(0.5, 0.5); // goes from [-0.5, 0.5]. 0 has to be at center of screen.
    float imageMax = length(vec2(xImageMax, yImageMax));
    float image = length(texCentered * vec2(aspect, 1));

//    float tanAngle = image / imageMax * tan(fov * 0.5);
//    float scale = atan(tanAngle) / tanAngle;

    float angle = atan(image / imageMax * tan(fov * 0.5));
//    float scale = 2* tan(angle/2) / tan(angle);
    float scale = angle / tan(angle);
//    float scale = sin(angle) / tan(angle);
//    float scale = 2 * sin(angle / 2) / tan(angle);

    float stretch = (fov * 0.5) / tan(fov * 0.5);

//    vec2 vTexEquidistant = clamp(texCentered / scale + vec2(0.5, 0.5), vec2(0, 0), vec2(1, 1));
    vec2 vTexEquidistant = texCentered / scale * stretch + vec2(0.5, 0.5); // unintuitive: texcoord needs to be scaled in the opposite direction.

//    if (
//        vTexEquidistant.x > 1 ||
//        vTexEquidistant.x < 0 ||
//        vTexEquidistant.y > 1 ||
//        vTexEquidistant.y < 0
//    ) {
//        gl_FragColor = vec4(0, 0, 0, 1);
//    } else {
//        vec4 samp = texture2D(inTex, vTexEquidistant);
//        float s = step(1, vTexEquidistant.x) * step(1, vTexEquidistant.y);
//        gl_FragColor = vec4(samp.r, samp.g, samp.b, samp.a);
//    }

    vec4 samp = texture2D(inTex, vTexEquidistant);
    float s = step(0, vTexEquidistant.x) * step(0, vTexEquidistant.y) * step(vTexEquidistant.x, 1) * step(vTexEquidistant.y, 1);
    gl_FragColor = vec4(s * samp.r, s * samp.g, s * samp.b, samp.a);

//    float a = atan(tanAngle) / (170 * 3.14159 / 180) + 0.5;
//    gl_FragColor = vec4(a, 0, 0, 1);
}