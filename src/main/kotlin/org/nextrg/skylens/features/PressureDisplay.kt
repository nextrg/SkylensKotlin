package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.PlayerStats
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.RenderUtil
import org.nextrg.skylens.helpers.RenderUtil.drawText
import org.nextrg.skylens.helpers.VariablesUtil.degreesToRadians
import org.nextrg.skylens.helpers.VariablesUtil.hexStringToInt
import org.nextrg.skylens.helpers.VariablesUtil.hexTransparent
import org.nextrg.skylens.helpers.VariablesUtil.quad
import org.nextrg.skylens.renderables.Renderables.drawLine
import org.nextrg.skylens.renderables.Renderables.drawPie
import java.lang.Math.clamp

object PressureDisplay {
    private var animatedPressure = 0f

    fun prepare() {
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
        val intX = x.toInt() - margin - 13
        val intY = y.toInt() - margin

        context.fill(intX, intY - 14, intX + 26 + margin * 2, intY + 21 + margin * 2, 0x14FFFFFF)
    }

    fun getPosition(): Pair<Float, Float> {
        return RenderUtil.computePosition(
            RenderUtil.ElementPos(
                anchorKey = ModConfig.pressureDisplayAnchor.toString(),
                offsetX = ModConfig.pressureDisplayX.toFloat(),
                offsetY = ModConfig.pressureDisplayY.toFloat(),
                clampX = { pos, screenW -> clamp(pos, 14.5f, screenW.toFloat() - 14.5f) },
                clampY = { pos, screenH -> clamp(pos, 15f, screenH.toFloat() - 22) }
            )
        )
    }

    fun render(drawContext: DrawContext) {
        if (!onSkyblock() || !ModConfig.pressureDisplay) return
        animatedPressure += (PlayerStats.pressure - animatedPressure) * 0.09f

        val min = degreesToRadians(225f)
        val max = degreesToRadians(225f + 90f - 360f)
        val displayPressure = MathHelper.lerp(quad(animatedPressure), min, max)

        val (x, y) = getPosition()

        val color1 = hexStringToInt("afafaf")
        val color2 = hexStringToInt("3d3d41")
        val color3 = hexStringToInt("1d1d21")
        
        val meterY = y + 8f

        // Background
        drawPie(drawContext, x, meterY, 1.01f, 13f, color2, 0f, 0f, false, false)
        drawPie(drawContext, x, meterY, 1.01f, 12f, color3, 0f, 0f, false, false)

        drawMarkerLines(drawContext, x, meterY, hexTransparent(color2, 130))

        drawPie(drawContext, x, meterY, 1.01f * 3/4, 7.5f, color2, degreesToRadians(-45f), 0f, false, false)
        drawPie(drawContext, x, meterY, 1.01f, 6.825f, color3, 0f, 0f, false, false)

        // Measure
        drawLine(drawContext, x, meterY, displayPressure, 10.25f, hexTransparent(color1, 35), 1f, 0.25f, 1f, 0)
        drawPie(drawContext, x, meterY, 1.01f * 1/6, 12f, color3, degreesToRadians(225f), 0f, false, false)
        drawLine(drawContext, x, meterY, displayPressure, 10.25f, color1, 1f, 0.25f, 1f, 2)
        drawPie(drawContext, x, meterY, 1.01f,  1.5f, color2, 0f, 0f, false, false)

        drawText(drawContext, (PlayerStats.pressure * 100).toInt().toString() + "%", x, y - 14f, color1, 1f, true, true)
    }

    private fun drawMarkerLines(drawContext: DrawContext, x: Float, y: Float, color: Int) {
        val steps = 8;
        for (i in 0..steps) {
            val lastTwo = i > steps - 2
            val lineColor = if (!lastTwo) color else 0xFF993333.toInt()
            val boldness = (if (!lastTwo) 0.1f else 0.5f)
            drawLine(drawContext, x, y, degreesToRadians(225f) - degreesToRadians((i * 10 * 27/steps).toFloat()), 10f , hexTransparent(lineColor, 60), 1f, boldness, 0f, 0)
            drawLine(drawContext, x, y, degreesToRadians(225f) - degreesToRadians((i * 10 * 27/steps).toFloat()), 10f , lineColor, 1f, boldness, 0f, 2)
        }
    }
}