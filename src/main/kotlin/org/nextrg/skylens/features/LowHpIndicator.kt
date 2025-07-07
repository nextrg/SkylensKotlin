package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.util.Identifier
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.PlayerStats
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.RenderUtil.getScaledWidthHeight
import org.nextrg.skylens.helpers.VariablesUtil.hexTransparent
import org.nextrg.skylens.helpers.VariablesUtil.quad

object LowHpIndicator {
    private var animatedHealth = 0f

    fun prepare() {
        HudLayerRegistrationCallback.EVENT.register(HudLayerRegistrationCallback { wrap: LayeredDrawerWrapper ->
            wrap.attachLayerBefore(
                IdentifiedLayer.HOTBAR_AND_BARS,
                Identifier.of("skylens", "low-hp-indicator"),
                LowHpIndicator::prepareRender
            )
        })
    }

    fun prepareRender(drawContext: DrawContext, renderTickCounter: RenderTickCounter) {
        render(drawContext)
    }

    private fun render(drawContext: DrawContext) {
        if (!onSkyblock() || !ModConfig.lowHpIndicator) return
        animatedHealth += (PlayerStats.health - animatedHealth) * 0.09f

        val displayHealth = quad(animatedHealth)
        val (screenX, screenY) = getScaledWidthHeight()

        if (displayHealth < 0.5f) {
            val maxBaseAlpha = 90
            val healthFactor = (0.5f - displayHealth) / 0.5f
            val baseAlpha = (healthFactor * maxBaseAlpha).toInt()

            val progress = (System.currentTimeMillis() % 1000) / 1000f
            val pulse = 1f - (progress * progress)

            val finalAlpha = (baseAlpha + pulse * baseAlpha * 0.5f).toInt().coerceIn(0, 255)

            drawContext.fill(0, 0, screenX, screenY, hexTransparent(0xFFFF0000.toInt(), finalAlpha))
        }
    }
}