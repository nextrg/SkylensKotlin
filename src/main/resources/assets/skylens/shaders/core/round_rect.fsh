// Olympus implementation that accounts for 4 pixel padding
#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

layout(std140) uniform RoundedRectangleUniform {
    vec4 borderColor;
    vec4 borderRadius;
    float borderWidth;
    vec2 size;
    vec2 center;
    float scaleFactor;
};

in vec4 vertexColor;
out vec4 fragColor;

const float edgeSoftness = 1.0;

// From: https://iquilezles.org/articles/distfunctions2d/
float sdRoundedBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x  = (p.y > 0.0) ? r.x  : r.y;
    vec2 q = abs(p)-b+r.x;
    return min(max(q.x,q.y),0.0) + length(max(q,0.0)) - r.x;
}

void main() {
    if (vertexColor.a == 0.0) discard;

    float paddingScaled = 4.0 * scaleFactor;
    float r = (borderRadius.x * 0.82) * scaleFactor * max((size.x - paddingScaled) / size.x, 1.0);
    vec2 halfSize = size / 2.0;

    float dist = sdRoundedBox(gl_FragCoord.xy - center, halfSize, vec4(r));

    // antialiasing
    float aa = edgeSoftness;
    float smoothed = min(1.0 - smoothstep(-aa, aa, dist), vertexColor.a);
    float border = min(1.0 - smoothstep(borderWidth - aa, borderWidth + aa, abs(dist)), borderColor.a);

    if (border > 0.0) {
        fragColor = borderColor * vec4(1.0, 1.0, 1.0, border) * ColorModulator;
    } else {
        fragColor = vertexColor * vec4(1.0, 1.0, 1.0, smoothed) * ColorModulator;
    }
}