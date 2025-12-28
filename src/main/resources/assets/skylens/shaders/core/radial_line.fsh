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

const float TAU = 6.2831853;

void main() {
    if (vertexColor.a == 0.0) discard;
    vec2 dir = gl_FragCoord.xy - center;
    float dist = length(dir);
    if (dist > radius) discard;

    float angleAlpha = 1.0;
    float thicknessAlpha = 1.0;

    if (mode == 0 || mode == 2) {
        float angle = atan(dir.y, dir.x);
        angle = mod(angle + TAU, TAU);

        if (mode == 0) {
            float angleDiff = abs(mod(angle - startAngle + TAU, TAU));
            angleAlpha = 1.0 - smoothstep(0.0, angleThickness, angleDiff);
            if (angleAlpha <= 0.0) discard;
        }
        else if (mode == 2) {
            float halfThickness = angleThickness * 0.5;

            float segStart = mod(startAngle - halfThickness + TAU, TAU);
            float segEnd = mod(startAngle + halfThickness, TAU);

            float normalizedAngle = mod(angle, TAU);

            if (segStart < segEnd) {
                if (!(normalizedAngle >= segStart && normalizedAngle <= segEnd)) discard;
            } else {
                if (!(normalizedAngle >= segStart || normalizedAngle <= segEnd)) discard;
            }

            float edgeFade = 0.03;

            float distStart = mod(normalizedAngle - segStart + TAU, TAU);
            float distEnd = mod(segEnd - normalizedAngle + TAU, TAU);

            float startEdgeAlpha = smoothstep(0.0, edgeFade, distStart);
            float endEdgeAlpha = smoothstep(0.0, edgeFade, distEnd);

            angleAlpha = min(startEdgeAlpha, endEdgeAlpha);
        }
    }

    if (mode == 1 || mode == 2) {
        vec2 lineDir = vec2(cos(startAngle), sin(startAngle));
        float projection = dot(dir, lineDir);
        vec2 closestPoint = lineDir * projection;
        float lineDist = length(dir - closestPoint);
        thicknessAlpha = 1.0 - smoothstep(thickness, thickness + 1.0, lineDist);
        if (thicknessAlpha <= 0.0) discard;
    }

    float radialAlpha = 1.0 - smoothstep(radius - fadeSoftness, radius, dist);

    float finalAlpha = radialAlpha * (
        mode == 0 ? angleAlpha :
        mode == 1 ? thicknessAlpha :
        angleAlpha * thicknessAlpha
    );

    fragColor = vec4(lineColor.rgb, lineColor.a * finalAlpha) * ColorModulator;
}