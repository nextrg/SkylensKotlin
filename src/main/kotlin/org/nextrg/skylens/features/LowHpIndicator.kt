package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.DeltaTracker
import net.minecraft.resources.Identifier
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.PlayerStats
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.RenderUtil.getScaledWidthHeight
import org.nextrg.skylens.helpers.VariablesUtil.withAlpha
import org.nextrg.skylens.helpers.VariablesUtil.quad
import org.nextrg.skylens.pipelines.Renderables.roundGradient

object LowHpIndicator {
    private const val COLOR = 0xFFFF0000.toInt()
    private var animatedHealth = 1f

    fun prepare() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CROSSHAIR,
            Identifier.fromNamespaceAndPath("skylens", "low-hp-indicator"),
            LowHpIndicator::prepareRender
        )
    }

    fun prepareRender(guiGraphics: GuiGraphics, renderTickCounter: DeltaTracker) {
        render(guiGraphics)
    }

    private fun render(guiGraphics: GuiGraphics) {
        if (!onSkyblock() || !ModConfig.lowHpIndicator) return

        animatedHealth += (PlayerStats.health - animatedHealth) * 0.09f
        val displayHealth = quad(animatedHealth)
        if (displayHealth >= 0.5f) return

        val (screenX, screenY) = getScaledWidthHeight()
        val maxBaseAlpha = 170 * ModConfig.lowHpIndicatorTransparency
        val healthFactor = (0.5f - displayHealth) / 0.5f
        val baseAlpha = (healthFactor * maxBaseAlpha).toInt()

        val pulse = if (ModConfig.lowHpIndicatorHeartbeat) pulse() else 1f
        val finalAlpha = (baseAlpha + pulse * baseAlpha * 0.5f).toInt().coerceIn(0, 255)

        val color1 = withAlpha(COLOR, finalAlpha)
        val color2 = withAlpha(COLOR, finalAlpha / 3)

        for (direction in 0..1) {
            roundGradient(guiGraphics, 0f, 0f, screenX.toFloat(), screenY.toFloat(),
                mutableListOf(color1, color2), direction, 0f, 0, 0f, 0f)
        }
    }

    private fun pulse(): Float {
        val progress = (System.currentTimeMillis() % 1000) / 1000f
        return 1f - (progress * progress)
    }
}
