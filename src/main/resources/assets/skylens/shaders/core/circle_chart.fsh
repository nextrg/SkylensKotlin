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

const float PI = 3.1415926;
const float TAU = 6.2831853;
const float edgeSoftness = 1.5;

vec4 getColor(int i) {
    return colors[i];
}

void main() {
    if (vertexColor.a == 0.0) discard;

    vec2 pos = gl_FragCoord.xy - center;
    float dist = dot(pos, pos);
    float outer = outerRadius * outerRadius;
    float inner = innerRadius * innerRadius;
    float innerAlpha = 2.0 * innerRadius * edgeSoftness;

    // antialiasing
    float edgeAlphaOuter =
        1.0 - smoothstep(
        outer - 2.0 * outerRadius * edgeSoftness,
        outer,
        dist
    );
    float edgeAlphaInner =
    smoothstep(inner, inner + innerAlpha, dist);

    float edgeAlpha = edgeAlphaOuter * edgeAlphaInner;
    if (edgeAlpha <= 0.0) discard;

    float innerMin = (innerRadius - edgeSoftness);
    float outerMax = (outerRadius + edgeSoftness);

    innerMin = innerMin * innerMin;
    outerMax = outerMax * outerMax;

    if (dist < innerMin || dist > outerMax) discard;

    // angles
    float angle = pos.y / pos.x;
    angle = atan(angle);
    if (pos.x < 0.0) angle += PI;
    if (angle < 0.0) angle += TAU;

    float angleOffset = mod(angle - startAngle + TAU, TAU);
    float angularLength = progress * TAU;

    float angleAlpha;
    if (progress >= 1.0) angleAlpha = 1.0;
    else {
        float angleSoft = edgeSoftness / outerRadius;
        angleAlpha = 1.0 - smoothstep(angularLength - angleSoft, angularLength, angleOffset);
        angleAlpha = mix(angleAlpha, 1.0 - angleAlpha, float(invert));
    }

    float finalAlpha = edgeAlpha * angleAlpha * vertexColor.a;
    if (finalAlpha <= 0.0) discard;

    // interpolation
    float factor = mod(angleOffset / TAU + time, 1.0);
    factor = mix(factor, 1.0 - factor, float(reverse));

    float scaled = factor * float(max(colorCount - 1, 1));
    int index = int(floor(scaled));
    float localFactor = fract(scaled);

    vec4 c1 = getColor(index);
    vec4 c2 = getColor(min(index + 1, colorCount - 1));
    vec4 color = mix(c1, c2, localFactor);

    fragColor = vec4(color.rgb, color.a * finalAlpha);
}