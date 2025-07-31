#version 150

uniform mat4 modelViewMat;
uniform mat4 projMat;
uniform vec4 fillColor;
uniform vec4 borderColor;
uniform vec4 borderRadius;
uniform vec2 size;
uniform vec2 center;
uniform float borderWidth;
uniform float scaleFactor;
uniform float offsetX;
uniform float offsetY;
uniform int waveDirection;

out vec4 fragColor;

float sdRoundedBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;
    vec2 q = abs(p) - b + r.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

float calcWaterLevel()
{
    float waveAmplitude = 1.66;
    float waveFrequency = 0.25;
    float edgeSmooth = 1.33;

    float surfacePos;
    float level;

    if (waveDirection == 1) { // Bottom
        surfacePos = center.y + offsetY - sin((gl_FragCoord.x + offsetX) * waveFrequency) * waveAmplitude;
        level = smoothstep(surfacePos + edgeSmooth, surfacePos - edgeSmooth, gl_FragCoord.y);
    } else if (waveDirection == 2) { // Left
        surfacePos = center.x + offsetX + sin((gl_FragCoord.y + offsetY) * waveFrequency) * waveAmplitude;
        level = smoothstep(surfacePos + edgeSmooth, surfacePos - edgeSmooth, gl_FragCoord.x);
    } else if (waveDirection == 3) { // Right
        surfacePos = center.x + offsetX - sin((gl_FragCoord.y + offsetY) * waveFrequency) * waveAmplitude;
        level = smoothstep(surfacePos + edgeSmooth, surfacePos - edgeSmooth, gl_FragCoord.x);
    } else { // Top
        surfacePos = center.y + offsetY + sin((gl_FragCoord.x + offsetX) * waveFrequency) * waveAmplitude;
        level = smoothstep(surfacePos + edgeSmooth, surfacePos - edgeSmooth, gl_FragCoord.y);
    }

    return level;
}

void main() {
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

    float borderDist = abs(dist);
    float borderAlpha = smoothstep(borderWidth + 0.1, borderWidth, borderDist);
    color = mix(color, borderColor, borderAlpha);

    float aa = fwidth(dist);
    color.a *= smoothstep(0.0, aa, -dist);

    fragColor = color;
}