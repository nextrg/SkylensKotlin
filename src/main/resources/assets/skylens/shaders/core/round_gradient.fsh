#version 150

layout(std140) uniform RoundGradientUniform {
    vec4 colors[8];
    int colorCount;
    vec4 borderColor;
    vec4 borderRadius;
    vec2 size;
    vec2 center;
    float borderWidth;
    float scaleFactor;
    float time;
    int gradientDir;
};

in vec4 vertexColor;
out vec4 fragColor;

const float edgeSoftness = 1.0;

float sdRoundedBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

vec4 getGradientColor(float factor) {
    float segment = factor * float(colorCount);
    int index = int(floor(segment)) % colorCount;
    float localT = fract(segment);

    vec4 c0;
    vec4 c1;

    c0 = colors[index];
    c1 = colors[(index + 1) % colorCount];

    return mix(c0, c1, localT);
}

void main() {
    vec2 halfSize = size / 2.0;
    vec2 pos = gl_FragCoord.xy - center;
    float distance = sdRoundedBox(pos, halfSize, borderRadius * scaleFactor);

    float gradientOffset = fract(time);
    float gradientFactor;

    if (gradientDir == 0) {
        gradientFactor = (pos.y + halfSize.y) / size.y + gradientOffset;
    } else if (gradientDir == 1) {
        gradientFactor = (pos.x + halfSize.x) / size.x + gradientOffset;
    } else {
        gradientFactor = ((pos.x + halfSize.x) + (pos.y + halfSize.y)) / (size.x + size.y) + gradientOffset;
    }

    gradientFactor = fract(gradientFactor);

    vec4 color = getGradientColor(gradientFactor);

    float aa = edgeSoftness;
    float smoothed = min(1.0 - smoothstep(-aa, aa, distance), vertexColor.a);
    float border = min(1.0 - smoothstep(borderWidth - aa, borderWidth + aa, abs(distance)), borderColor.a);

    if (border > 0.0) {
        fragColor = borderColor * vec4(1.0, 1.0, 1.0, border);
    } else {
        fragColor = color * vec4(1.0, 1.0, 1.0, smoothed);
    }
}