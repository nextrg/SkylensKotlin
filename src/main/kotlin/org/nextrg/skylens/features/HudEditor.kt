package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import org.joml.Math.clamp
import org.lwjgl.glfw.GLFW
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.helpers.RenderUtil.anchors
import org.nextrg.skylens.helpers.RenderUtil.drawText
import org.nextrg.skylens.helpers.RenderUtil.getScaledWidthHeight
import java.util.regex.Pattern

class HudEditor(private var parent: Screen?, title: Component = Component.literal("HudEditor")) : Screen(title) {
    private val ANCHOR_PATTERN: Pattern = Pattern.compile("(Left|Right|Middle)")
    private var petOverlayHovered: Boolean = false
    private var pressureDisplayHovered: Boolean = false
    private var drillFuelBarHovered: Boolean = false
    private var dungeonScoreMeterHovered: Boolean = false
    private var currentFeature = ""
    private val features = mutableListOf("Pet Overlay", "Pressure Display", "Drill Fuel Meter", "Dungeon Score Meter")

    override fun init() {}

    override fun keyPressed(input: KeyEvent): Boolean {
        var move = features.indexOf(currentFeature)
        when (input.input()) {
            GLFW.GLFW_KEY_A -> move = if (move - 1 < 0) features.size - 1 else move - 1
            GLFW.GLFW_KEY_D -> move = (move + 1) % features.size
        }
        companionFeature = features[clamp(move, 0, features.size)]

        return super.keyPressed(input)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.render(context, mouseX, mouseY, deltaTicks)
        this.currentFeature = companionFeature

        drawHotbar(context)
        drawTextInfo(context)

        if (currentFeature == features[0]) {
            PetOverlay.render(context, true)
            PetOverlay.highlight(context)
        }
        if (currentFeature == features[1]) {
            PressureDisplay.render(context, true)
            PressureDisplay.highlight(context)
        }
        if (currentFeature == features[2]) {
            DrillFuelMeter.render(context, true)
            DrillFuelMeter.highlight(context)
        }
        if (currentFeature == features[3]) {
            DungeonScoreMeter.render(context, true)
            DungeonScoreMeter.highlight(context)
        }
    }

    private fun drawHotbar(context: GuiGraphics) {
        val (screenX, screenY) = getScaledWidthHeight()
        val width = 182
        val height = 22
        val x = screenX / 2 - width / 2
        val y = screenY - height
        context.fill(x, y, x + width, y + height, 0x2a000000)
        context.fillGradient(x, y, x + width, y + height, 0, 0xBA000000.toInt())
        drawText(context, "Hotbar", x.toFloat() + width / 2, y.toFloat() + height / 2 - font.lineHeight / 2, 0x99FFFFFF.toInt(), 1.0f, true, false)
    }

    private fun drawTextInfo(context: GuiGraphics) {
        val (screenX, screenY) = getScaledWidthHeight()
        val line = font.lineHeight + 2

        val empty = currentFeature == ""
        var displayX = 0; var displayY = 0; var anchorSource = ""
        val displayFeature = if (empty) "nothing!" else currentFeature

        val featureMap = mapOf(
            "Pet Overlay" to Triple(ModConfig.petOverlayX, ModConfig.petOverlayY, ModConfig.petOverlayAnchor.toString()),
            "Pressure Display" to Triple(ModConfig.pressureDisplayX, ModConfig.pressureDisplayY, ModConfig.pressureDisplayAnchor.toString()),
            "Drill Fuel Meter" to Triple(ModConfig.drillFuelMeterX, ModConfig.drillFuelMeterY, ModConfig.drillFuelMeterAnchor.toString()),
            "Dungeon Score Meter" to Triple(ModConfig.dungeonScoreMeterX, ModConfig.dungeonScoreMeterY, ModConfig.dungeonScoreMeterAnchor.toString())
        )

        featureMap.entries.firstOrNull { currentFeature.contains(it.key) }?.let { (_, triple) ->
            displayX = triple.first
            displayY = triple.second
            anchorSource = triple.third
        }

        val anchor = ANCHOR_PATTERN.matcher(anchorSource).replaceAll(" $1")

        drawText(context, "Press left-click on an element to move it.", (screenX / 2).toFloat(), (screenY / 2).toFloat() - line * 3, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Press right/middle-click to reset its position.", (screenX / 2).toFloat(), (screenY / 2).toFloat() - line * 2, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Press A/D to switch elements.", (screenX / 2).toFloat(), (screenY / 2).toFloat() - line, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        drawText(context, "Currently changing: $displayFeature", (screenX / 2).toFloat(), (screenY / 2).toFloat(), 0xFFFFFFFF.toInt(), 1.0f, true, true)
        if (!empty) {
            drawText(context, "Current position: [$displayX, $displayY]  Current anchor: $anchor", (screenX / 2).toFloat(), (screenY / 2).toFloat() + line, 0xFFFFFFFF.toInt(), 1.0f, true, true)
        }
    }

    private fun isOverPetOverlay(mouseX: Double, mouseY: Double, position: Pair<Float, Float>): Boolean {
        val (x, y) = position
        val dx = mouseX - x; val dy = mouseY - y

        val left = -1.0; val bottom = 11.0
        val right = (if (ModConfig.petOverlayType.toString().contains("Bar")) 27.0 else 0.0) + 25.0
        val top = if (ModConfig.petOverlayType.toString().contains("Bar")) -19.0 else -36.0

        return dx in left..right && dy in top..bottom
    }

    private fun isOverPressureDisplay(mouseX: Double, mouseY: Double, position: Pair<Float, Float>): Boolean {
        val (x, y) = position
        val dx = mouseX - x; val dy = mouseY - y

        val left = -15.0; val right = 15.0
        val top = -15.0; val bottom = 24.0

        return dx in left..right && dy in top..bottom
    }

    private fun isOverDrillFuelBar(mouseX: Double, mouseY: Double, position: Pair<Float, Float>): Boolean {
        val (x, y) = position
        val dx = mouseX - x; val dy = mouseY - y

        val left = -1.0; val right = 21.0
        val top = -1.0; val bottom = 41.0

        return dx in left..right && dy in top..bottom
    }

    private fun isOverDungeonScoreMeter(mouseX: Double, mouseY: Double, position: Pair<Float, Float>): Boolean {
        val (x, y) = position
        val dx = mouseX - x; val dy = mouseY - y

        val left = -16.0; val right = 16.0
        val top = -17.0; val bottom = 16.0

        return dx in left..right && dy in top..bottom
    }

    override fun mouseClicked(click: MouseButtonEvent, doubled: Boolean): Boolean {
        val mouseX = click.x
        val mouseY = click.y
        val button = click.button()
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
        if (isOverDrillFuelBar(mouseX, mouseY, DrillFuelMeter.getPosition()) && currentFeature.contains("Drill Fuel Meter")) {
            if (button != 0) {
                drillFuelBarHovered = false
                setDrillFuelBarMargin(-9999.0, 0.0)
            } else {
                drillFuelBarHovered = true
                setDrillFuelBarMargin(mouseX, mouseY)
            }
        }
        if (isOverDungeonScoreMeter(mouseX, mouseY, DungeonScoreMeter.getPosition()) && currentFeature.contains("Dungeon Score Meter")) {
            if (button != 0) {
                dungeonScoreMeterHovered = false
                setDungeonScoreMeterMargin(-9999.0, 0.0)
            } else {
                dungeonScoreMeterHovered = true
                setDungeonScoreMeterMargin(mouseX, mouseY)
            }
        }
        return super.mouseClicked(click, doubled)
    }

    override fun mouseDragged(click: MouseButtonEvent, mouseX: Double, mouseY: Double): Boolean {
        val mouseX = click.x
        val mouseY = click.y
        if (petOverlayHovered) {
            setPetOverlayMargin(mouseX, mouseY)
        }
        if (pressureDisplayHovered) {
            setPressureDisplayMargin(mouseX, mouseY)
        }
        if (drillFuelBarHovered) {
            setDrillFuelBarMargin(mouseX, mouseY)
        }
        if (dungeonScoreMeterHovered) {
            setDungeonScoreMeterMargin(mouseX, mouseY)
        }
        return super.mouseDragged(click, mouseX, mouseY)
    }

    override fun mouseReleased(click: MouseButtonEvent): Boolean {
        val mouseX = click.x
        val mouseY = click.y
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
        if (dungeonScoreMeterHovered) {
            dungeonScoreMeterHovered = false
            setDungeonScoreMeterMargin(mouseX, mouseY)
        }
        return super.mouseReleased(click)
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
            anchorKey = ModConfig.drillFuelMeterAnchor.toString(), overlayType = null,
            defaultX = { ModConfig.drillFuelMeterX = it }, defaultY = { ModConfig.drillFuelMeterY = it },
            isDrillBar = true
        )
    }

    private fun setDungeonScoreMeterMargin(x: Double, y: Double) {
        setOverlayMargin(x, y,
            anchorKey = ModConfig.dungeonScoreMeterAnchor.toString(), overlayType = null,
            defaultX = { ModConfig.dungeonScoreMeterX = it }, defaultY = { ModConfig.dungeonScoreMeterY = it },
            isDungeonScoreMeter = true
        )
    }

    private fun setOverlayMargin(x: Double, y: Double, anchorKey: String, overlayType: ModConfig.Type?,
                                 defaultX: (Int) -> Unit, defaultY: (Int) -> Unit,
                                 isPetOverlay: Boolean = false, isDrillBar: Boolean = false, isDungeonScoreMeter: Boolean = false) {
        if (x <= -9998.0) {
            val (configDefaultX, configDefaultY) = when {
                isPetOverlay -> 119 to -2
                isDrillBar -> -88 to -32
                isDungeonScoreMeter -> -116 to -9
                else -> -81 to -14
            }
            defaultX(configDefaultX)
            defaultY(configDefaultY)
        } else {
            val (screenX, screenY) = getScaledWidthHeight()
            val anchor = anchors[anchorKey] ?: floatArrayOf(0.5f, 1f)
            val (anchorX, anchorY) = anchor

            val offsetX = if (isPetOverlay) {
                (27 * -1 * (1 - anchorX * 2)).toInt() + if (overlayType.toString().contains("Bar")) 0 else 14
            } else if (isDrillBar) {
                (27 * anchorX * 2).toInt() - 10
            } else {
                (27 * anchorX * 2).toInt()
            }

            val offsetY = if (isPetOverlay) {
                3 * (5 * anchorY).toInt() + if (overlayType.toString().contains("Bar")) 0 else 8
            } else if (isDrillBar) {
                2 * (5 * anchorY).toInt() - 16
            } else if (isDungeonScoreMeter) {
                2 * (5 * anchorY).toInt()
            } else {
                3 * (5 * anchorY).toInt() - 8
            }

            defaultX(x.toInt() - (screenX * anchorX).toInt() + offsetX)
            defaultY(y.toInt() - (screenY * anchorY).toInt() + offsetY)
        }
    }

    override fun onClose() {
        ModConfig.get().update()
        hudEditor = false
        val open = booleanArrayOf(false)
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: Minecraft ->
            if (!open[0]) {
                open[0] = true
                client.setScreen(parent)
                parent = null
            }
        })
    }

    companion object {
        var hudEditor = false
        var companionFeature = ""

        fun openScreen(screen: Screen?, name: String) {
            hudEditor = true
            val open = booleanArrayOf(false)
            ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: Minecraft ->
                if (!open[0]) {
                    open[0] = true
                    client.setScreen(HudEditor(screen))
                    companionFeature = name
                }
            })
        }
    }
}
