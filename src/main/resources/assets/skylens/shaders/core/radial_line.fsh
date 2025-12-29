#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

layout(std140) uniform RadialLineUniform {
    vec4 lineColor;
    vec2 center;
    float radius;
    float startAngle;
    float angleThickness;
    float fadeSoftness;
    float thickness;
    int mode;
};

in vec4 vertexColor;
out vec4 fragColor;

const float TAU = 6.28318530718;

void main() {
    if (vertexColor.a == 0.0) discard;

    vec2 dir = gl_FragCoord.xy - center;
    float distSq = dot(dir, dir);

    if (distSq > radius * radius) discard;

    float invLen = inversesqrt(distSq);
    vec2 dirN = dir * invLen;

    vec2 startDir = vec2(cos(startAngle), sin(startAngle));

    float angleAlpha = 1.0;

    if (mode == 0 || mode == 2) {
        float cross = startDir.x * dirN.y - startDir.y * dirN.x;
        if (cross < 0.0) discard;

        float dotv = dot(startDir, dirN);
        float cosLimit = cos(angleThickness);

        angleAlpha = smoothstep(cosLimit, 1.0, dotv);
        if (angleAlpha <= 0.0) discard;
    }

    float thicknessAlpha = 1.0;

    if (mode == 2) {
        float proj = dot(dir, startDir);
        vec2 diff = dir - startDir * proj;

        float d2 = dot(diff, diff);

        float t0 = thickness * thickness;
        float t1 = (thickness + 1.0);
        t1 *= t1;

        thicknessAlpha = 1.0 - smoothstep(t0, t1, d2);
        if (thicknessAlpha <= 0.0) discard;
    }

    float dist = sqrt(distSq);
    float radialAlpha = 1.0 - smoothstep(radius - fadeSoftness, radius, dist);

    fragColor = vec4(lineColor.rgb, lineColor.a * angleAlpha * thicknessAlpha * radialAlpha) * ColorModulator;
}