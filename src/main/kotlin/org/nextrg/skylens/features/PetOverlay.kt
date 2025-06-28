package org.nextrg.skylens.features

import earth.terrarium.olympus.client.pipelines.RoundedRectangle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.Pets
import org.nextrg.skylens.api.Pets.getCurrentPet
import org.nextrg.skylens.api.Pets.getPetLevel
import org.nextrg.skylens.api.Pets.getPetMaxLevel
import org.nextrg.skylens.api.Pets.getPetRarity
import org.nextrg.skylens.api.Pets.getPetRarityText
import org.nextrg.skylens.api.Pets.getPetXp
import org.nextrg.skylens.helpers.Variables.animateFloat
import org.nextrg.skylens.helpers.Variables.quad
import kotlin.math.max

object PetOverlay {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var currentPet: ItemStack = ItemStack(Items.BONE)

    private var level: Int = 1
    private var maxLevel: Int = 100
    private var xp: Float = 0f
    private var rarity: String = "common"

    private var transitionX = 0f
    private var transitionY = 0f
    private var hidden: Boolean = true

    private fun isTransitionComplete() =
        (transitionX == 1f && transitionY == 1f) || (transitionX == 0f && transitionY == 0f)

    fun updatePet() {
        scope.launch {
            if (!hidden) {
                delay(25L)
            }
            val update = {
                val pet = getCurrentPet()
                currentPet = pet
                rarity = getPetRarity(getPetRarityText(pet))
            }

            if (isTransitionComplete()) {
                update()
            } else {
                delay(475L)
                update()
            }
        }
    }

    fun updateStats() {
        scope.launch {
            if (!hidden) {
                delay(25L)
            }
            val update = {
                level = getPetLevel()
                maxLevel = getPetMaxLevel()
                xp = getPetXp()
            }

            if (isTransitionComplete()) {
                update()
            } else {
                delay(475L)
                update()
            }
        }
    }

    private suspend fun transition(show: Boolean) {
        animateFloat(if (show) 0f else 1f, if (show) 1f else 0f, 500L, ::quad).collect { value ->
            transitionX = value
            transitionY = value
        }
    }

    private fun show(show: Boolean, offset: Boolean) {
        scope.launch {
            if (offset) {
                delay(500L)
            }
            hidden = !show
            transition(show)
        }
    }

    fun showOverlay() {
        if (hidden) {
            show(true, false)
        } else {
            hideOverlay()
            show(true, true)
        }
    }

    fun hideOverlay() {
        if (!hidden) {
            show(false, false)
        }
    }

    private val rarityColors: Map<String, IntArray> = java.util.Map.of(
        "special", intArrayOf(-0x55dedf, -0xcdce, -0x88eaeb),
        "divine", intArrayOf(-0xf7aa67, -0xee5523, -0xfac99a),
        "mythic", intArrayOf(-0x88dd97, -0xaa01, -0xaeeebf),
        "legendary", intArrayOf(-0x4c8900, -0x35800, -0x9fcb00),
        "epic", intArrayOf(-0xa8fe49, -0x56cd27, -0xdbfeac),
        "rare", intArrayOf(-0xcdcd5d, -0xadad0d, -0xeeeecb),
        "uncommon", intArrayOf(-0xea75eb, -0xab02ac, -0xebc5ec),
        "common", intArrayOf(0xFF9A9A9A.toInt(), 0xFFFFFFFF.toInt(), 0xFF636363.toInt())
    )

    private val anchors: Map<String, FloatArray> = java.util.Map.of(
        "TopLeft", floatArrayOf(0f, 0f), // (X, Y)
        "MiddleLeft", floatArrayOf(0f, 0.5f),
        "BottomLeft", floatArrayOf(0f, 1f),
        "TopRight", floatArrayOf(1f, 0f),
        "MiddleRight", floatArrayOf(1f, 0.5f),
        "BottomRight", floatArrayOf(1f, 1f),
        "TopMiddle", floatArrayOf(0.5f, 0f),
        "BottomMiddle", floatArrayOf(0.5f, 1f)
    )

    fun prepare() {
        Pets.init()
        HudLayerRegistrationCallback.EVENT.register(HudLayerRegistrationCallback { wrap: LayeredDrawerWrapper ->
            wrap.attachLayerAfter(
                IdentifiedLayer.HOTBAR_AND_BARS,
                Identifier.of("skylens", "pet-overlay"),
                PetOverlay::prepareRender
            )
        })
    }

    private fun prepareRender(drawContext: DrawContext, renderTickCounter: RenderTickCounter) {
        render(drawContext)
    }

    private fun getAnimationOffset(x: Float, y: Float): Pair<Float, Float> {
        fun map(value: Float): Float = 120f * (value - 0.5f)
        return map(x) to map(y)
    }

    private fun getPosition(): Pair<Float, Float> {
        val client = MinecraftClient.getInstance()
        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        val selectedAnchor = ModConfig.petOverlayAnchor.toString()

        val anchor = anchors[selectedAnchor] ?: floatArrayOf(0.5f, 1f)
        val (anchorsX, anchorsY) = anchor

        val anchorX = screenWidth * anchorsX - 50 * anchorsX
        val anchorY = screenHeight * anchorsY - 8 * anchorsY

        val marginX = 2 * (1 - anchorsX * 2)
        val marginY = 2 * (1 - anchorsY * 2)

        val x = Math.clamp(anchorX + marginX + ModConfig.petOverlayX, 2f, screenWidth.toFloat() - 2 - 50)
        val y = Math.clamp(anchorY + marginY + ModConfig.petOverlayY, 2f, screenHeight.toFloat() - 10)

        var (offsetX, offsetY) = getAnimationOffset(anchorsX, anchorsY)
        offsetX += x - anchorX - marginX
        offsetY += (y - anchorY - marginY) * (1 - anchorsY * 2)

        if (selectedAnchor == "TopMiddle" || selectedAnchor == "BottomMiddle") {
            transitionX = 1f
        }

        val finalX = x + offsetX - offsetX * transitionX
        val finalY = y + offsetY - offsetY * transitionY

        return finalX to finalY
    }

    fun render(drawContext: DrawContext) {
        if (!ModConfig.petOverlay) return

        var textRenderer = MinecraftClient.getInstance().textRenderer
        val (x, y) = getPosition()

        val textLevel = "Lvl $level"
        val textXp = (xp * 100).toString().replace(".0", "") + "%"
        val levelProgress: Float = level.toFloat() / maxLevel.toFloat()

        renderBars(drawContext, x.toInt(), y.toInt(), levelProgress)
        // circular display: to be done
        drawContext.drawItem(currentPet, x.toInt() + 5, y.toInt() - 17)
        drawContext.drawCenteredTextWithShadow(textRenderer, textLevel, x.toInt() + 32, y.toInt() - 12, rarityColors[rarity]!![1])
        drawContext.drawCenteredTextWithShadow(textRenderer, textXp, x.toInt() + 32, y.toInt() - 12, rarityColors[rarity]!![1])
    }

    private fun renderBars(drawContext: DrawContext, x: Int, y: Int, maxLevelProgress: Float) {
        renderBar(drawContext, x - 2, y - 2, 51 + 4, 8 + 4, 0x34000000, 6.5f)
        renderBar(drawContext, x - 1, y - 1, 51 + 2, 8 + 2, 0x64000000, 6.5f)
        renderBar(drawContext, x, y, 51, 8, rarityColors[rarity]!![2], 4.5f)
        renderBar(drawContext, x, y, max(8, (51 * maxLevelProgress).toInt()), 8, rarityColors[rarity]!![1], 4.5f)
        renderBar(drawContext, x + 2, y + 2, max(2, (47 * xp).toInt()), 4, rarityColors[rarity]!![0], 2.5f)
    }

    private fun renderCircles(drawContext: DrawContext, x: Int, y: Int, maxLevelProgress: Float) {

    }

    private fun renderBar(
        drawContext: DrawContext,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Int,
        radius: Float
    ) {
        RoundedRectangle.draw(drawContext, x, y, width, height, color, 0, radius, 0)
    }
}