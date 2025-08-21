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
import org.nextrg.skylens.api.PlayerStats.dungeonScore
import org.nextrg.skylens.features.HudEditor.Companion.hudEditor
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.RenderUtil
import org.nextrg.skylens.helpers.RenderUtil.drawText
import org.nextrg.skylens.helpers.StringsUtil.getFormatCode
import org.nextrg.skylens.helpers.VariablesUtil.animateFloat
import org.nextrg.skylens.helpers.VariablesUtil.degreesToRadians
import org.nextrg.skylens.helpers.VariablesUtil.quad
import org.nextrg.skylens.pipelines.Renderables.drawPie
import java.lang.Math.clamp
import kotlin.math.floor

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

    fun prepare() {
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            Identifier.of("skylens", "dungeon-score-meter"),
            DungeonScoreMeter::prepareRender
        )
    }

    fun prepareRender(drawContext: DrawContext, renderTickCounter: RenderTickCounter) {
        render(drawContext)
    }

    fun highlight(context: DrawContext) {
        val (x, y) = getPosition()

        val margin = 1
        val intX = x.toInt() - margin - 13
        val intY = y.toInt() - margin - 14

        context.fill(intX, intY, intX + 27 + margin * 2, intY + 28 + margin * 2, 0x14FFFFFF)
    }

    fun getPosition(): Pair<Float, Float> {
        val (baseX, baseY) = RenderUtil.computePosition(
            RenderUtil.ElementPos(
                anchorKey = ModConfig.dungeonScoreMeterAnchor.toString(),
                offsetX = ModConfig.dungeonScoreMeterX.toFloat(),
                offsetY = ModConfig.dungeonScoreMeterY.toFloat(),
                clampX = { pos, screenW -> clamp(pos, 14f, screenW.toFloat() - (14f + 1f)) },
                clampY = { pos, screenH -> clamp(pos, 14f + 1f, screenH.toFloat() - (14f + 1f)) }
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

    fun render(drawContext: DrawContext, isHudEditor: Boolean = false) {
        if (!isHudEditor && (!ModConfig.dungeonScoreMeter || transition == 0f) || !onSkyblock()) return
        animatedScore += (dungeonScore - animatedScore) * 0.09f
        animatedScore = clamp(animatedScore, 0f, 305f)

        val (x, y) = getPosition()
        draw(drawContext, x, y, animatedScore)
    }

    private fun draw(drawContext: DrawContext, x: Float, y: Float, score: Float) {
        var startAngle = 0f
        var total = 0f
        var defaultColor = 0xFFFC0000.toInt()
        var defaultLabel = "D"

        drawPie(drawContext, x, y, 1.01f, 13.95f, -1f, 0x8F000000.toInt(), 0f, 0f)
        for ((label, color, value) in slices) {
            if (total >= score) break

            val remaining = score - total
            val sliceValue = minOf(value, remaining)

            val angle = sliceValue / 305f * 360f
            drawPie(drawContext, x, y, sliceValue * 1.09f / 305f, 14f, 10.7f, color, degreesToRadians(startAngle), 0f)

            startAngle += angle
            total += sliceValue
            defaultLabel = label
            defaultColor = color
        }
        drawText(drawContext, getFormatCode("bold") + defaultLabel, x, y - 7.5f, defaultColor, 1f, true, true)
        drawText(drawContext, floor(dungeonScore).toString().replace(".0", ""), x, y + 1.5f, 0x8FFFFFFF.toInt(), 0.75f, true, true)
    }
}