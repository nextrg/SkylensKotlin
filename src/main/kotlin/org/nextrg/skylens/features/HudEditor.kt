package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.features.PetOverlay.anchors
import org.nextrg.skylens.features.PetOverlay.getPosition
import org.nextrg.skylens.renderables.Rendering.drawText

class HudEditor(private var parent: Screen?, title: Text = Text.literal("HudEditor")) : Screen(title) {
    companion object {
        fun openScreen(screen: Screen?) {
            val open = booleanArrayOf(false)
            ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
                if (!open[0]) {
                    PetOverlay.hudEditor = true
                    open[0] = true
                    client.setScreen(HudEditor(screen))
                }
            })
        }
    }

    private var hovered: Boolean = false

    override fun init() {

    }

    override fun renderBackground(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        val (x, y) = getPosition()

        val margin = 4
        val intX = x.toInt() - margin
        val intY = y.toInt() - 18 - margin

        if (ModConfig.petOverlayType == ModConfig.Type.Bar) {
            context.fill(intX, intY + 3, intX + 51 + margin * 2, intY + 26 + margin * 2, 0x14FFFFFF)
        } else {
            context.fill(intX, intY - 14, intX + 24 + margin * 2, intY + 26 + margin * 2, 0x14FFFFFF)
        }

        super.renderBackground(context, mouseX, mouseY, deltaTicks)
    }

    override fun mouseClicked(a: Double, b: Double, c: Int): Boolean {
        if (c != 0) {
            hovered = false
            setMargin(-9999.0, 0.0)
        } else {
            hovered = true
            setMargin(a, b)
        }
        return super.mouseClicked(a, b, c)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (hovered) {
            setMargin(mouseX, mouseY)
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (hovered) {
            hovered = false
            setMargin(mouseX, mouseY)
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun setMargin(x: Double, y: Double) {
        if (x <= -9998.0) {
            ModConfig.petOverlayX = 0
            ModConfig.petOverlayY = 0
        } else {
            val client = MinecraftClient.getInstance()
            val screenX = client.window.scaledWidth
            val screenY = client.window.scaledHeight

            val anchor = anchors[ModConfig.petOverlayAnchor.toString()] ?: floatArrayOf(0.5f, 1f)
            val (anchorX, anchorY) = anchor
            ModConfig.petOverlayX = x.toInt() - (screenX * anchorX).toInt() + (27 * -1 * (1 - anchorX * 2)).toInt()
            ModConfig.petOverlayY = y.toInt() - (screenY * anchorY).toInt() + 3 * (4 * anchorX).toInt()
        }
    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        val screenX = context.scaledWindowWidth
        val screenY = context.scaledWindowHeight
        super.render(context, mouseX, mouseY, deltaTicks)
        val line = textRenderer.fontHeight

        val displayX = ModConfig.petOverlayX
        val displayY = ModConfig.petOverlayY
        val anchor = ModConfig.petOverlayAnchor.toString()
            .replace("Left", " Left")
            .replace("Right", " Right")
            .replace("Middle", " Middle")
        drawText(context, "Press left-click to move the Pet Overlay.", (screenX / 2).toFloat(), (screenY / 2).toFloat() - line * 2, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Right/Middle-click will reset its position to default.", (screenX / 2).toFloat(), (screenY / 2).toFloat() - line + 2, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Current position: [$displayX, $displayY]  Current anchor: $anchor", (screenX / 2).toFloat(), (screenY / 2).toFloat() + 4, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        PetOverlay.render(context)
    }

    override fun close() {
        ModConfig.get().update()
        val open = booleanArrayOf(false)
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            if (!open[0]) {
                open[0] = true
                client.setScreen(parent)
                PetOverlay.hudEditor = false
                parent = null
            }
        })
    }
}