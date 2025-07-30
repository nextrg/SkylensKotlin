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

uniform mat4 modelViewMat;
uniform mat4 projMat;
uniform vec4 borderColor;
uniform vec4 borderRadius;
uniform vec2 size;
uniform vec2 center;
uniform float borderWidth;
uniform float scaleFactor;
uniform float time;
uniform int gradientDirection;

out vec4 fragColor;

float sdRoundedBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

vec4 getGradientColor(float factor) {
    float segment = factor * 8.0;
    int index = int(floor(segment)) % 8;
    float localT = fract(segment);

    vec4 c0;
    vec4 c1;

    if (index == 0) { c0 = color0; c1 = color1; }
    else if (index == 1) { c0 = color1; c1 = color2; }
    else if (index == 2) { c0 = color2; c1 = color3; }
    else if (index == 3) { c0 = color3; c1 = color4; }
    else if (index == 4) { c0 = color4; c1 = color5; }
    else if (index == 5) { c0 = color5; c1 = color6; }
    else if (index == 6) { c0 = color6; c1 = color7; }
    else { c0 = color7; c1 = color0; }

    return mix(c0, c1, localT);
}

void main() {
    vec2 halfSize = size / 2.0;
    vec2 pos = gl_FragCoord.xy - center;
    float distance = sdRoundedBox(pos, halfSize, borderRadius * scaleFactor);

    float gradientOffset = fract(time);
    float gradientFactor;

    if (gradientDirection == 0) {
        gradientFactor = (pos.y + halfSize.y) / size.y + gradientOffset;
    } else if (gradientDirection == 1) {
        gradientFactor = (pos.x + halfSize.x) / size.x + gradientOffset;
    } else {
        gradientFactor = ((pos.x + halfSize.x) + (pos.y + halfSize.y)) / (size.x + size.y) + gradientOffset;
    }

    gradientFactor = fract(gradientFactor);

    vec4 color = getGradientColor(gradientFactor);
    float smoothed = min(1.0 - distance, color.a);
    float border = min(1.0 - smoothstep(borderWidth, borderWidth, abs(distance)), borderColor.a);

    if (border > 0.0) {
        fragColor = borderColor * vec4(1.0, 1.0, 1.0, border);
    } else {
        fragColor = color * vec4(1.0, 1.0, 1.0, smoothed);
    }
}