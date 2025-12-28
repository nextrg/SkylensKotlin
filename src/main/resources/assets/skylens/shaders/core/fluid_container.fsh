#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

layout(std140) uniform FluidContainerUniform {
    vec4 fillColor;
    vec4 borderRadius;
    vec2 size;
    vec2 center;
    vec2 offset;
    float scaleFactor;
    int waveDirection;
};

in vec4 vertexColor;
out vec4 fragColor;

const float waveAmplitude = 1.66;
const float waveFrequency = 0.25;
const float edgeSmooth = 1.33;

// From: https://iquilezles.org/articles/distfunctions2d/
float sdRoundedBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

float calcWaterLevel()
{
    float surfacePos;
    float level;

    if (waveDirection == 1) { // Bottom
        surfacePos = center.y + offset.y - sin((gl_FragCoord.x + offset.x) * waveFrequency) * waveAmplitude;
        level = smoothstep(surfacePos + edgeSmooth, surfacePos - edgeSmooth, gl_FragCoord.y);
    } else if (waveDirection == 2) { // Left
        surfacePos = center.x + offset.x + sin((gl_FragCoord.y + offset.y) * waveFrequency) * waveAmplitude;
        level = smoothstep(surfacePos + edgeSmooth, surfacePos - edgeSmooth, gl_FragCoord.x);
    } else if (waveDirection == 3) { // Right
        surfacePos = center.x + offset.x - sin((gl_FragCoord.y + offset.y) * waveFrequency) * waveAmplitude;
        level = smoothstep(surfacePos + edgeSmooth, surfacePos - edgeSmooth, gl_FragCoord.x);
    } else { // Top
        surfacePos = center.y + offset.y + sin((gl_FragCoord.x + offset.x) * waveFrequency) * waveAmplitude;
        level = smoothstep(surfacePos + edgeSmooth, surfacePos - edgeSmooth, gl_FragCoord.y);
    }

    return level;
}

void main() {
    if (vertexColor.a == 0.0) discard;

    vec2 halfSize = size * 0.5;
    vec2 uv = gl_FragCoord.xy - center;

    float dist = sdRoundedBox(uv, halfSize, borderRadius * scaleFactor);
    float inside = step(dist, 0.0);

    float waterLevel = calcWaterLevel();

    vec4 color = vec4(0.0);

    if (inside > 0.5) {
        color = fillColor;
        color.a *= waterLevel;
    }

    float aa = fwidth(dist);
    color.a *= smoothstep(0.0, aa, -dist);

    fragColor = color * ColorModulator;
}