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
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.PlayerStats.fuel
import org.nextrg.skylens.features.PetOverlay.getIdleProgress
import org.nextrg.skylens.features.PetOverlay.hudEditor
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.RenderUtil
import org.nextrg.skylens.helpers.RenderUtil.drawText
import org.nextrg.skylens.helpers.VariablesUtil.animateFloat
import org.nextrg.skylens.helpers.VariablesUtil.hexStringToInt
import org.nextrg.skylens.helpers.VariablesUtil.quad
import org.nextrg.skylens.renderables.Renderables.roundFluidContainer
import org.nextrg.skylens.renderables.Renderables.roundRectangleFloat
import java.lang.Math.clamp

object DrillFuelBar {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var transition = 0f
    private var transitionX = 0f
    private var transitionY = 0f
    private var hidden = true
    private var transitionDuration = 300L

    private var color1 = 0; private var color2 = 0; private var color3 = 0
    private var theme_eco = Triple("77ff77", "224a22", "102210")
    private var theme_mystic = Triple("a796ff", "413543", "251527")

    private var animatedFuel = 0f

    private val themes = mapOf(
        0 to (theme_eco),
        1 to (theme_mystic)
    )

    private fun updateTheme() {
        val colors = themes[ModConfig.drillFuelBarTheme] ?: theme_eco
        val (c1, c2, c3) = colors
        color1 = hexStringToInt(c1)
        color2 = hexStringToInt(c2)
        color3 = hexStringToInt(c3)
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
                Identifier.of("skylens", "drillfuel-bar"),
                DrillFuelBar::prepareRender
            )
        })
    }

    fun prepareRender(drawContext: DrawContext, renderTickCounter: RenderTickCounter) {
        render(drawContext)
    }

    fun highlight(context: DrawContext) {
        val (x, y) = getPosition()

        val margin = 1
        val intX = x.toInt() - margin
        val intY = y.toInt() - margin - 9

        context.fill(intX, intY, intX + 20 + margin * 2, intY + 49 + margin * 2, 0x14FFFFFF)
    }

    fun getPosition(): Pair<Float, Float> {
        val (baseX, baseY) = RenderUtil.computePosition(
            RenderUtil.ElementPos(
                anchorKey = ModConfig.drillFuelBarAnchor.toString(),
                offsetX = ModConfig.drillFuelBarX.toFloat(),
                offsetY = ModConfig.drillFuelBarY.toFloat(),
                clampX = { pos, screenW -> clamp(pos, 1f, screenW.toFloat() - (20f + 1f)) },
                clampY = { pos, screenH -> clamp(pos, 1f + 9f, screenH.toFloat() - (40f + 1f)) }
            )
        )

        val (screenW, screenH) = RenderUtil.getScaledWidthHeight()
        val (ax, ay) = RenderUtil.anchors[ModConfig.drillFuelBarAnchor.toString()] ?: floatArrayOf(0.5f, 1f)

        val anchorX = screenW * ax - 50 * ax
        val anchorY = screenH * ay - 8 * ay
        val marginX = 2 * (1 - ax * 2)
        val marginY = 2 * (1 - ay * 2)

        fun map(value: Float): Float = 120f * (value - 0.5f)
        var offsetX = map(ax)
        var offsetY = map(ay)
        offsetX -= (baseX - anchorX - marginX) * (1 - ax * 2)
        offsetY += (baseY - anchorY - marginY) * (1 - ay * 2)

        if (ModConfig.drillFuelBarAnchor.toString() in listOf("TopMiddle", "BottomMiddle")) {
            transitionX = 1f
        }

        val finalX = baseX + offsetX - offsetX * (if (hudEditor) 1f else transitionX)
        val finalY = baseY + offsetY - offsetY * (if (hudEditor) 1f else transitionY)

        return finalX to finalY
    }

    private fun parseFuel(): Float {
        try {
            val parts = fuel.split("/")
            return 2600f / parts[1].toFloat()
        } catch (ignored: Exception) {
            return 0f
        }
    }

    private fun fuelString(): String {
        val percentage = animatedFuel * 100
        val formatted = "%.0f".format(percentage).replace(",", ".")
        return "$formatted%"
    }

    fun render(drawContext: DrawContext) {
        if (!hudEditor && (!ModConfig.drillFuelBar || transition == 0f) || !onSkyblock()) return
        animatedFuel += (parseFuel() - animatedFuel) * 0.09f
        animatedFuel = clamp(animatedFuel, 0f, 1f)

        updateTheme()

        val (x, y) = getPosition()
        draw(drawContext, x, y, animatedFuel)
    }

    private fun draw(drawContext: DrawContext, x: Float, y: Float, value: Float) {
        roundRectangleFloat(drawContext, x, y, 20f, 40f, color3, 0, 4.5f, 0f)
        roundRectangleFloat(drawContext, x + 2f, y + 2f, 16f, 36f, color2, 0, 2.5f, 1f)
        if (value > 0f) {
            roundFluidContainer(drawContext, x + 2f, y + 2f, 16f, 36f, color1, 0, Pair(25f * getIdleProgress(), -38f + 38f * 2 * value), 0, 2.5f, 0f)
        }
        drawText(drawContext, fuelString(), x + 10f, y - 9f, color1, 1f, true, true)
    }
}