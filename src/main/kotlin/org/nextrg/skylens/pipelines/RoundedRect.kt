package org.nextrg.skylens.pipelines

import earth.terrarium.olympus.client.elements.BaseGuiElement
import net.minecraft.client.gui.ScreenRect
import net.minecraft.client.render.VertexConsumer
import org.nextrg.skylens.helpers.RenderUtil.floatToIntScreenRect
import java.lang.Math.clamp
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RoundRectGuiElement(private val xy: Pair<Float, Float>, private val wh: Pair<Float, Float>, private val color: Int, private val radius: Float) : BaseGuiElement(Pipelines.GUI_TRIANGLE_FAN) {
    override fun bounds(): ScreenRect = floatToIntScreenRect(xy.first, xy.second, wh.first, wh.second)

    override fun setupVertices(consumer: VertexConsumer, z: Float) {
        // this is broken for some reason im gonna fix this tomorrow i guess
        val x = xy.first; val y = xy.second
        val w = wh.first; val h = wh.second

        val r = clamp(radius, 1f, min(w, h) / 2)
        val corners = arrayOf(
            floatArrayOf(x + w - r, y + r),
            floatArrayOf(x + w - r, y + h - r),
            floatArrayOf(x + r, y + h - r),
            floatArrayOf(x + r, y + r)
        )

        consumer.vertex(x + w / 2f, y + h / 2f, z).color(color)
        for (corner in 0..3) {
            val cornerStart = (corner - 1) * 90
            val cornerEnd = cornerStart + 90
            var i = cornerStart
            while (i <= cornerEnd) {
                val angle = Math.toRadians(i.toDouble()).toFloat()
                val rx = corners[corner][0] + (cos(angle) * r)
                val ry = corners[corner][1] + (sin(angle) * r)
                consumer.vertex(rx, ry, z).color(color)
                i += 10
            }
        }
        consumer.vertex(corners[0][0], y, z).color(color)
    }
}
