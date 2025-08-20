package org.nextrg.skylens.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.PlayerStats
import org.nextrg.skylens.features.HudEditor.Companion.hudEditor
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.RenderUtil
import org.nextrg.skylens.helpers.RenderUtil.drawText
import org.nextrg.skylens.helpers.VariablesUtil.animateFloat
import org.nextrg.skylens.helpers.VariablesUtil.degreesToRadians
import org.nextrg.skylens.helpers.VariablesUtil.hexStringToInt
import org.nextrg.skylens.helpers.VariablesUtil.hexTransparent
import org.nextrg.skylens.helpers.VariablesUtil.quad
import org.nextrg.skylens.renderables.Renderables.drawLine
import org.nextrg.skylens.renderables.Renderables.drawPie
import java.lang.Math.clamp

object PressureDisplay {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var transition = 0f
    private var transitionX = 0f
    private var transitionY = 0f
    private var hidden = true
    private var transitionDuration = 300L

    private var color1 = 0; private var color2 = 0; private var color3 = 0
    private var theme_nighttime = Triple("afafaf", "3d3d41", "1d1d21")
    private var theme_peach = Triple("ffdab9", "f0a080", "AC5f4A")
    private var lineTransparency = 130

    private var animatedPressure = 0f

    private val themes = mapOf(
        0 to (theme_nighttime to 130),
        1 to (theme_peach to 180)
    )

    private fun updateTheme() {
        val (colors, transparency) =
            themes[ModConfig.pressureDisplayTheme] ?: (theme_nighttime to 130)
        val (c1, c2, c3) = colors
        color1 = hexStringToInt(c1)
        color2 = hexStringToInt(c2)
        color3 = hexStringToInt(c3)
        lineTransparency = transparency
    }

    fun show() {
        if (hidden) {
            scope.launch {
                hidden = false
                transition(true)
            }
        }
    }

    fun hide() {
        if (!hidden) {
            scope.launch {
                hidden = true
                transition(false)
            }
        }
    }

    private suspend fun transition(show: Boolean) {
        animateFloat(if (show) 0f else 1f, if (show) 1f else 0f, transitionDuration, ::quad).collect { value ->
            transition = value
            transitionX = value
            transitionY = value
        }
    }

    fun updateConfigValues() {
        updateTheme()
    }

    fun prepare() {
        updateConfigValues()
        HudLayerRegistrationCallback.EVENT.register(HudLayerRegistrationCallback { wrap: LayeredDrawerWrapper ->
            wrap.attachLayerAfter(
                IdentifiedLayer.HOTBAR_AND_BARS,
                Identifier.of("skylens", "pressure-display"),
                PressureDisplay::prepareRender
            )
        })
    }

    fun prepareRender(drawContext: DrawContext, renderTickCounter: RenderTickCounter) {
        render(drawContext)
    }

    fun highlight(context: DrawContext) {
        val (x, y) = getPosition()

        val margin = 1
        val intX = x.toInt() - margin - 12
        val intY = y.toInt() - margin

        context.fill(intX, intY - 14, intX + 24 + margin * 2, intY + 21 + margin * 2, 0x14FFFFFF)
    }

    fun getPosition(): Pair<Float, Float> {
        val (baseX, baseY) = RenderUtil.computePosition(
            RenderUtil.ElementPos(
                anchorKey = ModConfig.pressureDisplayAnchor.toString(),
                offsetX = ModConfig.pressureDisplayX.toFloat(),
                offsetY = ModConfig.pressureDisplayY.toFloat(),
                clampX = { pos, screenW -> clamp(pos, 13f, screenW.toFloat() - 13f) },
                clampY = { pos, screenH -> clamp(pos, 15f, screenH.toFloat() - 22) }
            )
        )

        val (screenW, screenH) = RenderUtil.getScaledWidthHeight()
        val (ax, ay) = RenderUtil.anchors[ModConfig.pressureDisplayAnchor.toString()] ?: floatArrayOf(0.5f, 1f)

        val anchorX = screenW * ax - 50 * ax
        val anchorY = screenH * ay - 8 * ay
        val marginX = 2 * (1 - ax * 2)
        val marginY = 2 * (1 - ay * 2)

        fun map(value: Float): Float = 120f * (value - 0.5f)
        var offsetX = map(ax)
        var offsetY = map(ay)
        offsetX -= (baseX - anchorX - marginX) * (1 - ax * 2)
        offsetY += (baseY - anchorY - marginY) * (1 - ay * 2)

        if (ModConfig.pressureDisplayAnchor.toString() in listOf("TopMiddle", "BottomMiddle")) {
            transitionX = 1f
        }

        val finalX = baseX + offsetX - offsetX * (if (hudEditor) 1f else transitionX)
        val finalY = baseY + offsetY - offsetY * (if (hudEditor) 1f else transitionY)

        return finalX to finalY
    }

    fun render(drawContext: DrawContext, isHudEditor: Boolean = false) {
        if (!isHudEditor && (!ModConfig.pressureDisplay || transition == 0f) || !onSkyblock()) return
        animatedPressure += (PlayerStats.pressure - animatedPressure) * 0.09f
        animatedPressure = clamp(animatedPressure, 0f, 1f)

        val min = degreesToRadians(225f)
        val max = degreesToRadians(225f + 90f - 360f)

        val (x, y) = getPosition()
        draw(drawContext, x, y, MathHelper.lerp(quad(animatedPressure), min, max))
    }

    private fun getPressureString(): String = (PlayerStats.pressure * 100).toInt().toString() + "%"

    private fun draw(drawContext: DrawContext, x: Float, y: Float, value: Float) {
        val meterY = y + 8f

        // Background
        val bgRadius = 13f
        drawPie(drawContext, x, meterY, 1.01f, bgRadius, 0f, color2, 0f, 0f)
        drawPie(drawContext, x, meterY, 1.01f, bgRadius - 1f, 0f, color3, 0f, 0f)

        // Markers
        val markerRadius = 7.5f
        drawMarkerLines(drawContext, x, meterY, hexTransparent(color2, lineTransparency))
        drawPie(drawContext, x, meterY, 1.01f * 3/4, markerRadius, 0f, color2, degreesToRadians(-135f), 0f)
        drawPie(drawContext, x, meterY, 1.01f, markerRadius - 0.675f, 0f, color3, 0f, 0f)

        // Measure
        val lineRadius = 10.25f
        drawLine(drawContext, x, meterY, value, lineRadius, hexTransparent(color1, 35), 1f, 0.25f, 1f, 0)
        drawPie(drawContext, x, meterY, 1.01f * 1/6, lineRadius + 1.75f, 0f, color3, degreesToRadians(135f), 0f)
        drawLine(drawContext, x, meterY, value, lineRadius, color1, 1f, 0.25f, 1f, 2)

        val circleRadius = 1.5f
        drawPie(drawContext, x, meterY, 1.01f, circleRadius,0f, color2, 0f, 0f)

        drawText(drawContext, getPressureString(), x, y - 14f, 0xFFFFFFFF.toInt(), 1f, true, true)
    }

    private fun drawMarkerLines(drawContext: DrawContext, x: Float, y: Float, color: Int) {
        val steps = 8
        for (i in 0..steps) {
            val last = i == steps
            val lineColor = if (!last) color else 0xFF993333.toInt()
            val boldness = (if (!last) 0.1f else 0.5f)
            drawLine(drawContext, x, y, degreesToRadians(225f) - degreesToRadians((i * 10 * 27/steps).toFloat()), 10f, hexTransparent(lineColor, 60), 1f, boldness, 0f, 0)
            drawLine(drawContext, x, y, degreesToRadians(225f) - degreesToRadians((i * 10 * 27/steps).toFloat()), 10f, lineColor, 1f, boldness, 0f, 2)
        }
    }
}