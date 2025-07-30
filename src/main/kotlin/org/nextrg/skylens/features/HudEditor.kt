package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.helpers.RenderUtil.anchors
import org.nextrg.skylens.helpers.RenderUtil.drawText
import org.nextrg.skylens.helpers.RenderUtil.getScaledWidthHeight

class HudEditor(private var parent: Screen?, title: Text = Text.literal("HudEditor")) : Screen(title) {
    private var petOverlayHovered: Boolean = false
    private var pressureDisplayHovered: Boolean = false
    private var drillFuelBarHovered: Boolean = false
    private var currentFeature = ""

    override fun init() {}

    override fun render(context: DrawContext, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.render(context, mouseX, mouseY, deltaTicks)
        this.currentFeature = companionFeature

        drawTextInfo(context)

        if (currentFeature == "Pet Overlay") {
            PetOverlay.render(context)
            PetOverlay.highlight(context)
        }

        if (currentFeature == "Pressure Display") {
            PressureDisplay.render(context)
            PressureDisplay.highlight(context)
        }

        if (currentFeature == "Drill Fuel Bar") {
            DrillFuelBar.render(context)
            DrillFuelBar.highlight(context)
        }
    }

    private fun drawTextInfo(context: DrawContext) {
        val (screenX, screenY) = getScaledWidthHeight()
        val line = textRenderer.fontHeight + 2

        val empty = currentFeature == ""
        var displayX = 0; var displayY = 0; var anchorSource = "";
        val displayFeature = if (empty) "nothing!" else currentFeature
        if (currentFeature.contains("Pet Overlay")) {
            displayX = ModConfig.petOverlayX
            displayY = ModConfig.petOverlayY
            anchorSource = ModConfig.petOverlayAnchor.toString()
        } else if (currentFeature.contains("Pressure Display")) {
            displayX = ModConfig.pressureDisplayX
            displayY = ModConfig.pressureDisplayY
            anchorSource = ModConfig.pressureDisplayAnchor.toString()
        } else if (currentFeature.contains("Drill Fuel Bar")) {
            displayX = ModConfig.drillFuelBarX
            displayY = ModConfig.drillFuelBarY
            anchorSource = ModConfig.drillFuelBarAnchor.toString()
        }
        val anchor = anchorSource
            .replace("Left", " Left")
            .replace("Right", " Right")
            .replace("Middle", " Middle")

        drawText(context, "Press left-click on an element to move it.", (screenX / 2).toFloat(), (screenY / 2).toFloat() - line * 3, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Press right/middle-click to reset its position.", (screenX / 2).toFloat(), (screenY / 2).toFloat() - line * 2, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Currently changing: $displayFeature", (screenX / 2).toFloat(), (screenY / 2).toFloat(), 0xFFFFFFFF.toInt(), 1.0f, true, true)
        if (!empty) {
            drawText(context, "Current position: [$displayX, $displayY]  Current anchor: $anchor", (screenX / 2).toFloat(), (screenY / 2).toFloat() + line, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        }
    }

    private fun isOverPetOverlay(mouseX: Double, mouseY: Double, position: Pair<Float, Float>): Boolean {
        val (x, y) = position
        val dx = mouseX - x; val dy = mouseY - y

        val left = -4.0; val bottom = 11.0
        val right = if (ModConfig.petOverlayType.toString().contains("Bar")) 54.0 else 27.0
        val top = if (ModConfig.petOverlayType.toString().contains("Bar")) -19.0 else -36.0

        return dx in left..right && dy in top..bottom
    }

    private fun isOverPressureDisplay(mouseX: Double, mouseY: Double, position: Pair<Float, Float>): Boolean {
        val (x, y) = position
        val dx = mouseX - x; val dy = mouseY - y

        val left = -15.0; val right = 14.5
        val top = -15.0; val bottom = 24.0

        return dx in left..right && dy in top..bottom
    }

    private fun isOverDrillFuelBar(mouseX: Double, mouseY: Double, position: Pair<Float, Float>): Boolean {
        val (x, y) = position
        val dx = mouseX - x; val dy = mouseY - y

        val left = -1.0; val right = 91.0
        val top = -1.0; val bottom = 23.0

        return dx in left..right && dy in top..bottom
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (isOverPetOverlay(mouseX, mouseY, PetOverlay.getPosition()) && currentFeature.contains("Pet Overlay")) {
            if (button != 0) {
                petOverlayHovered = false
                setPetOverlayMargin(-9999.0, 0.0)
            } else {
                petOverlayHovered = true
                setPetOverlayMargin(mouseX, mouseY)
            }
        }
        if (isOverPressureDisplay(mouseX, mouseY, PressureDisplay.getPosition()) && currentFeature.contains("Pressure Display")) {
            if (button != 0) {
                pressureDisplayHovered = false
                setPressureDisplayMargin(-9999.0, 0.0)
            } else {
                pressureDisplayHovered = true
                setPressureDisplayMargin(mouseX, mouseY)
            }
        }
        if (isOverDrillFuelBar(mouseX, mouseY, DrillFuelBar.getPosition()) && currentFeature.contains("Drill Fuel Bar")) {
            if (button != 0) {
                drillFuelBarHovered = false
                setDrillFuelBarMargin(-9999.0, 0.0)
            } else {
                drillFuelBarHovered = true
                setDrillFuelBarMargin(mouseX, mouseY)
            }
        }
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (petOverlayHovered) {
            setPetOverlayMargin(mouseX, mouseY)
        }
        if (pressureDisplayHovered) {
            setPressureDisplayMargin(mouseX, mouseY)
        }
        if (drillFuelBarHovered) {
            setDrillFuelBarMargin(mouseX, mouseY)
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (petOverlayHovered) {
            petOverlayHovered = false
            setPetOverlayMargin(mouseX, mouseY)
        }
        if (pressureDisplayHovered) {
            pressureDisplayHovered = false
            setPressureDisplayMargin(mouseX, mouseY)
        }
        if (drillFuelBarHovered) {
            drillFuelBarHovered = false
            setDrillFuelBarMargin(mouseX, mouseY)
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    private fun setPetOverlayMargin(x: Double, y: Double) {
        setOverlayMargin(x, y,
            anchorKey = ModConfig.petOverlayAnchor.toString(), overlayType = ModConfig.petOverlayType,
            defaultX = { ModConfig.petOverlayX = it }, defaultY = { ModConfig.petOverlayY = it },
            isPetOverlay = true
        )
    }

    private fun setPressureDisplayMargin(x: Double, y: Double) {
        setOverlayMargin(x, y,
            anchorKey = ModConfig.pressureDisplayAnchor.toString(), overlayType = null,
            defaultX = { ModConfig.pressureDisplayX = it }, defaultY = { ModConfig.pressureDisplayY = it }
        )
    }

    private fun setDrillFuelBarMargin(x: Double, y: Double) {
        setOverlayMargin(x, y,
            anchorKey = ModConfig.drillFuelBarAnchor.toString(), overlayType = null,
            defaultX = { ModConfig.drillFuelBarX = it }, defaultY = { ModConfig.drillFuelBarY = it },
            isDrillBar = true
        )
    }

    private fun setOverlayMargin(x: Double, y: Double, anchorKey: String, overlayType: ModConfig.Type?, defaultX: (Int) -> Unit, defaultY: (Int) -> Unit, isPetOverlay: Boolean = false, isDrillBar: Boolean = false) {
        if (x <= -9998.0) {
            defaultX(0)
            defaultY(0)
        } else {
            val (screenX, screenY) = getScaledWidthHeight()
            val anchor = anchors[anchorKey] ?: floatArrayOf(0.5f, 1f)
            val (anchorX, anchorY) = anchor

            val offsetX = if (isPetOverlay) {
                (27 * -1 * (1 - anchorX * 2)).toInt() + if (overlayType.toString().contains("Bar")) 0 else 14
            } else if (isDrillBar) {
                (27 * -1 * (1 - anchorX * 2)).toInt() - 17
            } else {
                (27 * anchorX * 2).toInt()
            }

            val offsetY = if (isPetOverlay) {
                3 * (5 * anchorY).toInt() + if (overlayType.toString().contains("Bar")) 0 else 8
            } else if (isDrillBar) {
                3 * (5 * anchorY).toInt() - 8
            } else {
                2 * (5 * anchorY).toInt()
            }

            defaultX(x.toInt() - (screenX * anchorX).toInt() + offsetX)
            defaultY(y.toInt() - (screenY * anchorY).toInt() + offsetY)
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
        var companionFeature = ""
        fun openScreen(screen: Screen?, name: String) {
            val open = booleanArrayOf(false)
            ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
                if (!open[0]) {
                    PetOverlay.hudEditor = true
                    open[0] = true
                    client.setScreen(HudEditor(screen))
                    companionFeature = name
                }
            })
        }
    }
}