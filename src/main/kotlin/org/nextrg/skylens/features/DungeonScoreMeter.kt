package org.nextrg.skylens.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.DeltaTracker
import net.minecraft.resources.Identifier
import net.minecraft.util.ARGB
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.PlayerStats.dungeonScore
import org.nextrg.skylens.features.HudEditor.Companion.hudEditor
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.RenderUtil
import org.nextrg.skylens.helpers.RenderUtil.drawText
import org.nextrg.skylens.helpers.StringsUtil.getFormatCode
import org.nextrg.skylens.helpers.VariablesUtil.animateFloat
import org.nextrg.skylens.helpers.VariablesUtil.degreesToRadians
import org.nextrg.skylens.helpers.VariablesUtil.getGradient
import org.nextrg.skylens.helpers.VariablesUtil.quad
import org.nextrg.skylens.pipelines.Renderables.drawPie
import org.nextrg.skylens.pipelines.Renderables.drawPieGradient
import java.lang.Math.clamp

object DungeonScoreMeter {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var transition = 0f
    private var transitionX = 0f
    private var transitionY = 0f
    private var hidden = true
    private var transitionDuration = 300L

    private val slices = listOf(
        Triple("D", 0xFFFC0000.toInt(), 99f),
        Triple("C", 0xFF3F3FFD.toInt(), 60f),
        Triple("B", 0xFF7FCC19.toInt(), 70f),
        Triple( "A", 0xFF7F3FB2.toInt(), 40.4f),
        Triple("S", 0xFFBEBE22.toInt(),29.6f),
        Triple("S+", 0xFFF9ED4C.toInt(), 6f)
    )
    private var animatedScore = 0f
    private var animatedColor = slices.first().second
    private var displayLabel = slices.first().first

    private var gradient = false
    private var gradientC1 = ModConfig.dungeonScoreMeterColor1
    private var gradientC2 = ModConfig.dungeonScoreMeterColor2
    private var gradientRotate = ModConfig.dungeonScoreMeterGradientRotate

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
        gradient = ModConfig.dungeonScoreMeterTheme == 1
        gradientC1 = ModConfig.dungeonScoreMeterColor1
        gradientC2 = ModConfig.dungeonScoreMeterColor2
        gradientRotate = ModConfig.dungeonScoreMeterGradientRotate
    }

    fun prepare() {
        updateConfigValues()
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            Identifier.fromNamespaceAndPath("skylens", "dungeon-score-meter"),
            DungeonScoreMeter::prepareRender
        )
    }

    fun prepareRender(drawContext: GuiGraphics, renderTickCounter: DeltaTracker) {
        render(drawContext)
    }

    fun highlight(context: GuiGraphics) {
        val (x, y) = getPosition()

        val margin = 1
        val intX = x.toInt() - margin - 15
        val intY = y.toInt() - margin - 15

        context.fill(intX, intY, intX + 30 + margin * 2, intY + 30 + margin * 2, 0x14FFFFFF)
    }

    fun getPosition(): Pair<Float, Float> {
        val (baseX, baseY) = RenderUtil.computePosition(
            RenderUtil.ElementPos(
                anchorKey = ModConfig.dungeonScoreMeterAnchor.toString(),
                offsetX = ModConfig.dungeonScoreMeterX.toFloat(),
                offsetY = ModConfig.dungeonScoreMeterY.toFloat(),
                clampX = { pos, screenW -> clamp(pos, 15f + 1f, screenW.toFloat() - (15f + 1f)) },
                clampY = { pos, screenH -> clamp(pos, 15f + 1f, screenH.toFloat() - (15f + 1f)) }
            )
        )

        val (screenW, screenH) = RenderUtil.getScaledWidthHeight()
        val (ax, ay) = RenderUtil.anchors[ModConfig.dungeonScoreMeterAnchor.toString()] ?: floatArrayOf(0.5f, 1f)

        val anchorX = screenW * ax - 50 * ax
        val anchorY = screenH * ay - 8 * ay
        val marginX = 2 * (1 - ax * 2)
        val marginY = 2 * (1 - ay * 2)

        fun map(value: Float): Float = 120f * (value - 0.5f)
        var offsetX = map(ax)
        var offsetY = map(ay)
        offsetX -= (baseX - anchorX - marginX) * (1 - ax * 2)
        offsetY += (baseY - anchorY - marginY) * (1 - ay * 2)

        if (ModConfig.dungeonScoreMeterAnchor.toString() in listOf("TopMiddle", "BottomMiddle")) {
            transitionX = 1f
        }

        val finalX = baseX + offsetX - offsetX * (if (hudEditor) 1f else transitionX)
        val finalY = baseY + offsetY - offsetY * (if (hudEditor) 1f else transitionY)

        return finalX to finalY
    }

    private fun getActiveSlice(score: Float): Pair<String, Int> {
        var total = 0f
        var label = "D"
        var color = slices.first().second

        for ((sLabel, sColor, sValue) in slices) {
            val remaining = score - total
            val sliceValue = minOf(sValue, remaining)
            if (sliceValue > 0f) {
                label = sLabel
                color = sColor
            }
            total += sValue
            if (total >= score) break
        }
        return label to color
    }

    private fun getScore() {
        animatedScore += (dungeonScore - animatedScore) * 0.09f
        animatedScore = clamp(animatedScore, 0f, 305f)

        val (label, color) = getActiveSlice(animatedScore)
        displayLabel = label

        animatedColor = ARGB.srgbLerp(0.25f, animatedColor, color)
    }

    private fun getScoreString(): String {
        return "%.0f".format(animatedScore).replace(",", ".")
    }

    fun render(drawContext: GuiGraphics, isHudEditor: Boolean = false) {
        if (!isHudEditor && (!ModConfig.dungeonScoreMeter || transition == 0f) || !onSkyblock()) return
        getScore()

        val (x, y) = getPosition()
        draw(drawContext, x, y)
    }

    private fun draw(drawContext: GuiGraphics, x: Float, y: Float) {
        // Background
        drawPie(drawContext, x, y, 1.01f, 16.5f, -1f, 0x8F000000.toInt(), 0f, 0f)

        // Progress
        drawSlices(drawContext, x, y)

        drawText(drawContext, getFormatCode("bold") + displayLabel, x, y - 8.5f, animatedColor, 1f, true, false)
        drawText(drawContext, getScoreString(), x, y + 0.5f, 0x8FFFFFFF.toInt(), 1f, true, false)
    }

    private fun drawSlices(drawContext: GuiGraphics, x: Float, y: Float) {
        val maxScore = 305f
        val gapDegrees = 1f
        var startAngle = 0f

        for ((label, color, value) in slices) {
            val currentAngle = value / maxScore * 360f
            val gapAngle = currentAngle - gapDegrees
            val score = value * (gapAngle / currentAngle)

            drawPie(
                drawContext, x, y,
                score / maxScore,
                13.8f, 12.2f,
                color,
                degreesToRadians(startAngle),
                0f
            )

            startAngle += currentAngle
        }

        val score = if (hudEditor && animatedScore == 0f) 305f else animatedScore
        if (gradient) {
            val gradColor = getGradient(gradientC1.rgb, gradientC2.rgb, 8, gradientRotate)
            drawPieGradient(drawContext, x, y, clamp(score / 305f, 0f, 1f) * 1.01f, 16.5f, 14f, gradColor, 0f, 0f)
        } else {
            drawPie(drawContext, x, y, clamp(score / 305f, 0f, 1f) * 1.01f, 16.5f, 14f, animatedColor, 0f, 0f)
        }
    }
}
