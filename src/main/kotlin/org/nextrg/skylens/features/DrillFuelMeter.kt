package org.nextrg.skylens.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.PlayerStats.fuel
import org.nextrg.skylens.features.HudEditor.Companion.hudEditor
import org.nextrg.skylens.features.PetOverlay.getIdleProgress
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.RenderUtil
import org.nextrg.skylens.helpers.RenderUtil.drawText
import org.nextrg.skylens.helpers.VariablesUtil.animateFloat
import org.nextrg.skylens.helpers.VariablesUtil.hexStringToInt
import org.nextrg.skylens.helpers.VariablesUtil.withAlpha
import org.nextrg.skylens.helpers.VariablesUtil.quad
import org.nextrg.skylens.pipelines.Renderables.roundFluidContainer
import org.nextrg.skylens.pipelines.Renderables.roundRectangleFloat
import java.lang.Math.clamp
import kotlin.math.ceil

object DrillFuelMeter {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var transition = 0f
    private var transitionX = 0f
    private var transitionY = 0f
    private var hidden = true
    private var transitionDuration = 300L

    private var color1 = 0; private var color2 = 0; private var color3 = 0
    private var theme_biofuel = Triple("77ff77", "224a22", "102210")
    private var theme_mithril = Triple("a7bfef", "354143", "152527")

    private var animatedFuel = 0f

    private val themes = mapOf(
        0 to (theme_biofuel),
        1 to (theme_mithril)
    )

    private fun updateTheme() {
        val colors = themes[ModConfig.drillFuelMeterTheme] ?: theme_biofuel
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
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            Identifier.of("skylens", "drill-fuel-meter"),
            DrillFuelMeter::prepareRender
        )
    }

    fun prepareRender(drawContext: DrawContext, renderTickCounter: RenderTickCounter) {
        render(drawContext)
    }

    fun highlight(context: DrawContext) {
        val (x, y) = getPosition()
        val scale = ModConfig.drillFuelMeterScale

        val margin = 1
        val baseWidth = 20 + margin * 2
        val baseHeight = 49 + margin * 2

        val scaledWidth = (baseWidth * scale).toInt()
        val scaledHeight = (baseHeight * scale).toInt()

        val intX = x.toInt() - (margin * scale).toInt()
        val intY = y.toInt() - (9 * scale).toInt() - (margin * scale).toInt()

        context.fill(intX, intY, intX + scaledWidth, intY + scaledHeight, 0x14FFFFFF)
    }

    fun getPosition(): Pair<Float, Float> {
        val scale = ModConfig.drillFuelMeterScale
        val (baseX, baseY) = RenderUtil.computePosition(
            RenderUtil.ElementPos(
                anchorKey = ModConfig.drillFuelMeterAnchor.toString(),
                offsetX = ModConfig.drillFuelMeterX.toFloat(),
                offsetY = ModConfig.drillFuelMeterY.toFloat(),
                clampX = { pos, screenW -> clamp(pos, 1f, screenW.toFloat() - (20f + 1f) * scale) },
                clampY = { pos, screenH -> clamp(pos, (1f + 9f) * scale, screenH.toFloat() - (40f + 1f) * scale) }
            )
        )

        val (screenW, screenH) = RenderUtil.getScaledWidthHeight()
        val (ax, ay) = RenderUtil.anchors[ModConfig.drillFuelMeterAnchor.toString()] ?: floatArrayOf(0.5f, 1f)

        val anchorX = screenW * ax - 50 * ax
        val anchorY = screenH * ay - 8 * ay
        val marginX = 2 * (1 - ax * 2)
        val marginY = 2 * (1 - ay * 2)

        fun map(value: Float): Float = (120f * (value - 0.5f)) * ModConfig.drillFuelMeterScale
        var offsetX = map(ax)
        var offsetY = map(ay)
        offsetX -= (baseX - anchorX - marginX) * (1 - ax * 2)
        offsetY += (baseY - anchorY - marginY) * (1 - ay * 2)

        if (ModConfig.drillFuelMeterAnchor.toString() in listOf("TopMiddle", "BottomMiddle")) {
            transitionX = 1f
        }

        val finalX = baseX + offsetX - offsetX * (if (hudEditor) 1f else transitionX)
        val finalY = baseY + offsetY - offsetY * (if (hudEditor) 1f else transitionY)

        return finalX to finalY
    }

    private fun hasFuel(): Boolean = fuel.split("/").getOrNull(0)?.toIntOrNull()?.let { it > 0 } ?: false

    private fun getFuel(): Float {
        try {
            val parts = fuel.split("/")
            return clamp(parts[0].toFloat() / parts[1].toFloat(), 0f, 1f)
        } catch (ignored: Exception) {
            return 0f
        }
    }

    private fun getFuelString(): String {
        val percentage = animatedFuel * 100
        val value = ceil(percentage * 10f) / 10f

        val format = "%." + (if (percentage > 9.9f) "0" else "1") + "f"
        val formatted = format.format(value).replace(",", ".")
        return "$formatted%"
    }

    fun render(drawContext: DrawContext, isHudEditor: Boolean = false) {
        if (!isHudEditor && (!ModConfig.drillFuelMeter || transition == 0f) || !onSkyblock()) return
        animatedFuel += (getFuel() - animatedFuel) * 0.09f
        animatedFuel = clamp(animatedFuel, 0f, 1f)

        val (x, y) = getPosition()

        val scale = ModConfig.drillFuelMeterScale
        drawContext.matrices.pushMatrix()
        drawContext.matrices.translate(x, y)
        drawContext.matrices.scale(scale, scale)
        drawContext.matrices.translate(-x, -y)

        draw(drawContext, x, y, animatedFuel)

        drawContext.matrices.popMatrix()
    }

    private fun draw(drawContext: DrawContext, x: Float, y: Float, fuel: Float) {
        val width = 20f; val height = 40f

        // Background
        roundRectangleFloat(drawContext, x, y, width, height, color3, 0, 5f, 0f)
        roundRectangleFloat(drawContext, x + 2f, y + 2f, width - 4f, height - 4f, color2, 0, 2.5f, 1f)

        // Fuel
        if (hasFuel()) {
            roundFluidContainer(drawContext, x + 2f, y + 2f, width - 4f, height - 4f, withAlpha(color1, 90), 0, Pair(-3.6f + 50f * getIdleProgress(2900.0), -26.5f + 32.5f * 2 * fuel), 0, 2.5f, 0f)
            roundFluidContainer(drawContext, x + 2f, y + 2f, width - 4f, height - 4f, color1, 0, Pair(50f * getIdleProgress(), -32.5f + 32.5f * 2 * fuel), 0, 2.5f, 0f)
        }

        drawText(drawContext, getFuelString(), x + width / 2, y - 9f, color1, 1f, true, true)
    }
}