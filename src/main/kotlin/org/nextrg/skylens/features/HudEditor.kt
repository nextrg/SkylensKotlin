package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.features.PetOverlay.anchors
import org.nextrg.skylens.renderables.Rendering.drawText

class HudEditor(private var parent: Screen?, title: Text = Text.literal("HudEditor")) : Screen(title) {
    private var petOverlayHovered: Boolean = false

    override fun init() {

    }

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        val screenX = context.scaledWindowWidth
        val screenY = context.scaledWindowHeight
        super.render(context, mouseX, mouseY, deltaTicks)
        val line = textRenderer.fontHeight + 2

        val displayX = ModConfig.petOverlayX
        val displayY = ModConfig.petOverlayY
        val anchor = ModConfig.petOverlayAnchor.toString()
            .replace("Left", " Left")
            .replace("Right", " Right")
            .replace("Middle", " Middle")
        drawText(context, "Press left-click on an element to move it.", (screenX / 2).toFloat(), (screenY / 2).toFloat() - line * 3, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Press right/middle-click to reset its position.", (screenX / 2).toFloat(), (screenY / 2).toFloat() - line * 2, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Currently changing: Pet Overlay", (screenX / 2).toFloat(), (screenY / 2).toFloat(), 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Current position: [$displayX, $displayY]  Current anchor: $anchor", (screenX / 2).toFloat(), (screenY / 2).toFloat() + line, 0xFFFFFFFF.toInt(), 1.0f, true, true)

        val petPos = PetOverlay.getPosition()
        PetOverlay.render(context)
        highlightPetOverlay(context, petPos)
    }

    private fun highlightPetOverlay(context: DrawContext, position: Pair<Float, Float>) {
        val (x, y) = position

        val margin = 4
        val intX = x.toInt() - margin
        val intY = y.toInt() - 18 - margin

        if (ModConfig.petOverlayType == ModConfig.Type.Bar) {
            context.fill(intX, intY + 3, intX + 51 + margin * 2, intY + 26 + margin * 2, 0x14FFFFFF)
        } else {
            context.fill(intX, intY - 14, intX + 24 + margin * 2, intY + 26 + margin * 2, 0x14FFFFFF)
        }
    }

    private fun isOverPetOverlay(mouseX: Double, mouseY: Double, position: Pair<Float, Float>): Boolean {
        val (x, y) = position
        val ifX: Boolean; val ifY: Boolean
        val rightMost = mouseX - x >= -4.0; val bottomMost = mouseY - y <= 11.0
        if (ModConfig.petOverlayType == ModConfig.Type.Bar) {
            ifX = mouseX - x <= 54.0 && rightMost
            ifY = bottomMost && mouseY - y >= -19
        } else {
            ifX = mouseX - x <= 27.0 && rightMost
            ifY = bottomMost && mouseY - y >= -36.0
        }
        return ifX && ifY
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isOverPetOverlay(mouseX, mouseY, PetOverlay.getPosition())) {
            if (button != 0) {
                petOverlayHovered = false
                setPetOverlayMargin(-9999.0, 0.0)
            } else {
                petOverlayHovered = true
                setPetOverlayMargin(mouseX, mouseY)
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (petOverlayHovered) {
            setPetOverlayMargin(mouseX, mouseY)
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (petOverlayHovered) {
            petOverlayHovered = false
            setPetOverlayMargin(mouseX, mouseY)
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun setPetOverlayMargin(x: Double, y: Double) {
        if (x <= -9998.0) {
            ModConfig.petOverlayX = 0
            ModConfig.petOverlayY = 0
        } else {
            val client = MinecraftClient.getInstance()
            val screenX = client.window.scaledWidth
            val screenY = client.window.scaledHeight

            val anchor = anchors[ModConfig.petOverlayAnchor.toString()] ?: floatArrayOf(0.5f, 1f)
            val (anchorX, anchorY) = anchor
            ModConfig.petOverlayX = x.toInt() - (screenX * anchorX).toInt() + (27 * -1 * (1 - anchorX * 2)).toInt() + if (ModConfig.petOverlayType == ModConfig.Type.Bar) 0 else 14
            ModConfig.petOverlayY = y.toInt() - (screenY * anchorY).toInt() + 3 * (5 * anchorY).toInt() + if (ModConfig.petOverlayType == ModConfig.Type.Bar) 0 else 8
        }
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
}