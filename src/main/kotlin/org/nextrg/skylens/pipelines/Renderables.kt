package org.nextrg.skylens.pipelines

import net.minecraft.client.gui.GuiGraphics
import org.joml.Vector2f
import org.nextrg.skylens.helpers.VariablesUtil.intToVector4f
import org.nextrg.skylens.helpers.VariablesUtil.listToVector4fArray

object Renderables {
    fun drawPie(
        graphics: GuiGraphics,
        x: Float,
        y: Float,
        progress: Float,
        outerRadius: Float,
        innerRadius: Float,
        color: Int,
        startAngle: Float,
        time: Float,
        invert: Boolean = false,
        reverse: Boolean = false
    ) {
        CircleChart.draw(graphics, x, y, Pair(outerRadius, innerRadius), listToVector4fArray(listOf(color)), progress, time, startAngle, invert, reverse)
    }

    fun drawPieGradient(
        graphics: GuiGraphics,
        x: Float,
        y: Float,
        progress: Float,
        outerRadius: Float,
        innerRadius: Float,
        color: List<Int>,
        startAngle: Float,
        time: Float,
        invert: Boolean = false,
        reverse: Boolean = false
    ) {
        CircleChart.draw(graphics, x, y, Pair(outerRadius, innerRadius), listToVector4fArray(color), progress, time, startAngle, invert, reverse)
    }

    fun drawLine(
        graphics: GuiGraphics,
        x: Float,
        y: Float,
        startAngle: Float,
        radius: Float,
        lineColor: Int,
        angleThickness: Float,
        lineThickness: Float,
        fadeSoftness: Float,
        mode: Int
    ) {
        RadialLine.draw(graphics, x, y, intToVector4f(lineColor), radius, startAngle, angleThickness, lineThickness, fadeSoftness, mode)
    }

    fun roundRectangleFloat(
        graphics: GuiGraphics,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        backgroundColor: Int,
        borderColor: Int,
        borderRadius: Float,
        borderWidth: Float
    ) {
        RoundRectangleFloat.draw(graphics, x, y, width, height, backgroundColor, borderColor, borderRadius, borderWidth)
    }

    fun roundGradient(
        graphics: GuiGraphics,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: MutableList<Int>,
        gradientDirection: Int,
        time: Float,
        borderColor: Int,
        borderRadius: Float,
        borderWidth: Float
    ) {
        RoundGradient.draw(graphics, x, y, width, height, listToVector4fArray(color), time, gradientDirection, borderColor, borderRadius, borderWidth)
    }

    fun roundFluidContainer(
        graphics: GuiGraphics,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        color: Int,
        waveDirection: Int,
        offset: Pair<Float, Float>,
        borderColor: Int, // UNUSED
        borderRadius: Float,
        borderWidth: Float // UNUSED
    ) {
        FluidContainer.draw(graphics, x, y, width, height, intToVector4f(color), waveDirection, Vector2f(offset.first, offset.second), borderRadius)
    }
}
