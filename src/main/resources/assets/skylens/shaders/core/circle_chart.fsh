#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

layout(std140) uniform CircleChartUniform {
    vec4 colors[8];
    int colorCount;
    vec2 center;
    float outerRadius;
    float innerRadius;
    float progress;
    float time;
    float startAngle;
    int reverse;
    int invert;
};

in vec4 vertexColor;
out vec4 fragColor;

const float TAU = 6.2831853;
const float edgeSoftness = 1.5;

vec4 getColor(int i) {
    return colors[i];
}

void main() {
    if (vertexColor.a == 0.0) discard;

    vec2 pos = gl_FragCoord.xy - center;
    float dist = length(pos);

    // antialiasing
    float edgeAlphaOuter = 1.0 - smoothstep(outerRadius - edgeSoftness, outerRadius, dist);
    float edgeAlphaInner = smoothstep(innerRadius, innerRadius + edgeSoftness, dist);
    float edgeAlpha = edgeAlphaOuter * edgeAlphaInner;
    if (edgeAlpha <= 0.0) discard;

    // angles
    float angle = atan(pos.y, pos.x);
    angle = mod(angle + TAU, TAU);

    float angleOffset = mod(angle - startAngle + TAU, TAU);
    float angularLength = progress * TAU;

    float angleAlpha;
    if (progress >= 1.0) angleAlpha = 1.0;
    else {
        float angleSoft = edgeSoftness / outerRadius;
        angleAlpha = 1.0 - smoothstep(angularLength - angleSoft, angularLength, angleOffset);
        if (invert == 1) angleAlpha = 1.0 - angleAlpha;
    }

    float finalAlpha = edgeAlpha * angleAlpha * vertexColor.a;
    if (finalAlpha <= 0.0) discard;

    // interpolation
    float factor = angleOffset / TAU;
    if (reverse == 1) factor = 1.0 - factor;

    float scaled = factor * float(max(colorCount - 1, 1));
    int index = int(floor(scaled));
    float localFactor = fract(scaled);

    vec4 c1 = getColor(index);
    vec4 c2 = getColor(min(index + 1, colorCount - 1));
    vec4 color = mix(c1, c2, localFactor);

    fragColor = vec4(color.rgb, color.a * finalAlpha);
}