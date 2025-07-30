#version 150

uniform vec4 color0;
uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;
uniform vec4 color5;
uniform vec4 color6;
uniform vec4 color7;
uniform int colorCount;

uniform vec2 center;
uniform float radius;
uniform float progress;
uniform float time;
uniform float startAngle;
uniform int reverse;
uniform int invert;

out vec4 fragColor;

const float TAU = 6.2831853;
const float edgeSoftness = 1.5;

vec4 getColor(int i) {
    if (i == 0) return color0;
    if (i == 1) return color1;
    if (i == 2) return color2;
    if (i == 3) return color3;
    if (i == 4) return color4;
    if (i == 5) return color5;
    if (i == 6) return color6;
    if (i == 7) return color7;
    return color0;
}

void main() {
    vec2 pos = gl_FragCoord.xy - center;
    float angle = atan(pos.y, pos.x);
    angle = mod(angle + TAU, TAU);

    float dist = length(pos);
    float edgeAlpha = 1.0 - smoothstep(radius - edgeSoftness, radius, dist);
    if (edgeAlpha <= 0.0) discard;

    float angleOffset = mod(angle - startAngle + TAU, TAU);
    float angularLength = progress * TAU;

    float angleSoft = edgeSoftness / radius;
    float angleAlpha = 1.0 - smoothstep(angularLength - angleSoft, angularLength, angleOffset);

    if (invert == 1)
    angleAlpha = 1.0 - angleAlpha;

    float finalAlpha = edgeAlpha * angleAlpha;
    if (finalAlpha <= 0.0) discard;

    float factor = fract(angleOffset / TAU + time);
    if (reverse == 1)
    factor = 1.0 - factor;

    float scaled = factor * float(colorCount - 1);
    int index = int(floor(scaled));
    float localFactor = fract(scaled);

    vec4 c1 = getColor(index);
    vec4 c2 = getColor(min(index + 1, colorCount - 1));

    vec4 color = mix(c1, c2, localFactor);
    fragColor = vec4(color.rgb, color.a * finalAlpha);
}