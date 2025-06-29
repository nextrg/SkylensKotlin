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
import net.minecraft.util.Util
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.Pets
import org.nextrg.skylens.api.Pets.getCurrentPet
import org.nextrg.skylens.api.Pets.getPetHeldItem
import org.nextrg.skylens.api.Pets.getPetLevel
import org.nextrg.skylens.api.Pets.getPetMaxLevel
import org.nextrg.skylens.api.Pets.getPetRarity
import org.nextrg.skylens.api.Pets.getPetRarityText
import org.nextrg.skylens.api.Pets.getPetXp
import org.nextrg.skylens.helpers.OtherUtil.getTextureFromNeu
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.VariablesUtil.animateFloat
import org.nextrg.skylens.helpers.VariablesUtil.colorToARGB
import org.nextrg.skylens.helpers.VariablesUtil.getAlphaProgress
import org.nextrg.skylens.helpers.VariablesUtil.hexTransparent
import org.nextrg.skylens.helpers.VariablesUtil.quad
import org.nextrg.skylens.helpers.VariablesUtil.sToMs
import org.nextrg.skylens.renderables.CircleChart
import org.nextrg.skylens.renderables.Rendering.drawItem
import org.nextrg.skylens.renderables.Rendering.drawText
import org.nextrg.skylens.renderables.Rendering.legacyRoundRectangle
import java.util.*
import kotlin.math.max

object PetOverlay {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var currentPet: ItemStack = ItemStack(Items.BONE)
    private var heldItem: ItemStack = ItemStack(Items.AIR)

    private var level: Int = 1
    private var maxLevel: Int = 100
    private var xp: Float = 0f
    private var rarity: String = "common"

    private var animatedXp: Float = 0f
    private var animatedLevelProgress: Float = 0f
    private var animatedLevelUp: Float = 0f

    private var transitionDuration = 300L
    private var levelUpDelay = sToMs(2f)

    var hudEditor = false
    private var transition = 0f
    private var transitionX = 0f
    private var transitionY = 0f
    private var hidden: Boolean = true
    private var cachedBase: Pair<Float, Float>? = null
    private var lastState = ""

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

    val anchors: Map<String, FloatArray> = java.util.Map.of(
        "TopLeft", floatArrayOf(0f, 0f), // (X, Y)
        "MiddleLeft", floatArrayOf(0f, 0.5f),
        "BottomLeft", floatArrayOf(0f, 1f),
        "TopRight", floatArrayOf(1f, 0f),
        "MiddleRight", floatArrayOf(1f, 0.5f),
        "BottomRight", floatArrayOf(1f, 1f),
        "TopMiddle", floatArrayOf(0.5f, 0f),
        "BottomMiddle", floatArrayOf(0.5f, 1f)
    )

    fun updatePet() {
        scope.launch {
            delay((transition * transitionDuration).toLong())

            val pet = getCurrentPet()
            currentPet = pet
            rarity = getPetRarity(getPetRarityText(pet))
        }
    }

    fun levelUp() {
        if (!ModConfig.petOverlayAnimation_LevelUp) return
        scope.launch {
            animateFloat(1f, 0f, transitionDuration * 2, ::quad).collect { animatedXp = it }
            animateFloat(0f, 1f, transitionDuration, ::quad).collect { animatedLevelUp = it }
            delay(levelUpDelay)
            xp = 0f
            animateFloat(1f, 0f, transitionDuration, ::quad).collect { animatedLevelUp = it }
        }
    }

    fun updateStats() {
        if (animatedLevelUp > 0f) return

        scope.launch {
            delay((transition * transitionDuration).toLong())
            heldItem = getTextureFromNeu(getPetHeldItem(), false)

            val newLevel = getPetLevel()
            val newMaxLevel = getPetMaxLevel()
            val newXp = getPetXp()

            level = newLevel
            maxLevel = newMaxLevel
            xp = newXp

            if (ModConfig.petOverlayAnimation_LevelXp) {
                scope.launch {
                    animateFloat(
                        animatedLevelProgress,
                        newLevel.toFloat() / newMaxLevel,
                        transitionDuration,
                        ::quad
                    ).collect {
                        animatedLevelProgress = it
                    }
                }
                scope.launch {
                    animateFloat(animatedXp, newXp, transitionDuration, ::quad).collect {
                        animatedXp = it
                    }
                }
            } else {
                animatedLevelProgress = newLevel.toFloat() / newMaxLevel
                animatedXp = newXp
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

    private fun show(show: Boolean, offset: Boolean) {
        scope.launch {
            if (offset) {
                delay(transitionDuration)
                if (show) {
                    updatePet()
                }
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

    private fun getAnimationOffset(x: Float, y: Float): Pair<Float, Float> {
        fun map(value: Float): Float = 120f * (value - 0.5f)
        return map(x) to map(y)
    }

    fun getPosition(): Pair<Float, Float> {
        val client = MinecraftClient.getInstance()
        val screenW = client.window.scaledWidth
        val screenH = client.window.scaledHeight

        val anchorKey = ModConfig.petOverlayAnchor.toString()
        val isBar = ModConfig.petOverlayType == ModConfig.Type.Bar
        val configState = "$anchorKey|$screenW|$screenH|${ModConfig.petOverlayX}|${ModConfig.petOverlayY}|$isBar"

        if (configState != lastState) {
            val (ax, ay) = anchors[anchorKey] ?: floatArrayOf(0.5f, 1f)
            val anchorX = screenW * ax - 50 * ax
            val anchorY = screenH * ay - 8 * ay
            val marginX = 2 * (1 - ax * 2)
            val marginY = 2 * (1 - ay * 2)

            val x = Math.clamp(anchorX + marginX + ModConfig.petOverlayX, 4f, screenW.toFloat() - 2 - 26 - if (isBar) 27 else 0)
            val y = Math.clamp(anchorY + marginY + ModConfig.petOverlayY, 19f + if (!isBar) 17 else 0, screenH.toFloat() - 12)

            cachedBase = x to y
            lastState = configState
        }

        val (ax, ay) = anchors[anchorKey] ?: floatArrayOf(0.5f, 1f)
        val (baseX, baseY) = cachedBase!!
        val anchorX = screenW * ax - 50 * ax
        val anchorY = screenH * ay - 8 * ay
        val marginX = 2 * (1 - ax * 2)
        val marginY = 2 * (1 - ay * 2)

        var (offsetX, offsetY) = getAnimationOffset(ax, ay)
        offsetX -= (baseX - anchorX - marginX) * (1 - ax * 2)
        offsetY += (baseY - anchorY - marginY) * (1 - ay * 2)

        if (anchorKey == "TopMiddle" || anchorKey == "BottomMiddle") transitionX = 1f

        val finalX = baseX + offsetX - offsetX * (if (hudEditor) 1f else transitionX)
        val finalY = baseY + offsetY - offsetY * (if (hudEditor) 1f else transitionY)
        return finalX to finalY
    }

    private fun getColors(rarity: String): Triple<Int, Int, Int> {
        val configTheme = ModConfig.petOverlayTheme.toString()
        val isCustom = configTheme == "Custom"

        val displayTheme = when {
            configTheme == "Pet" -> rarity
            isCustom -> rarity
            else -> configTheme
        }

        return if (!isCustom) {
            val colors = rarityColors[displayTheme.lowercase()]
                ?: error("[Skylens] Missing theme: $displayTheme")
            Triple(colors[0], colors[1], colors[2])
        } else {
            Triple(
                colorToARGB(ModConfig.petOverlayColor2),
                colorToARGB(ModConfig.petOverlayColor1),
                colorToARGB(ModConfig.petOverlayColor3)
            )
        }
    }

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

    fun render(drawContext: DrawContext) {
        if ((!ModConfig.petOverlay && !hudEditor) || !onSkyblock()) return

        val (x, y) = getPosition()
        var (color1, color2, color3) = getColors(rarity)

        val textColor = color2
        if (ModConfig.petOverlayInvert) {
            color1 = color2.also { color2 = color1 }
        }

        if (ModConfig.petOverlayType == ModConfig.Type.Bar) {
            renderBars(drawContext, x.toInt(), y.toInt(), animatedLevelProgress, color1, color2, color3)
        } else {
            renderCircles(drawContext, x.toInt(), y.toInt(), animatedLevelProgress, color1, color2, color3)
        }
        renderText(drawContext, x, y, textColor)
    }

    private fun renderText(drawContext: DrawContext, x: Float, y: Float, color: Int) {
        val isLevelMax = level == maxLevel
        val barStyle = ModConfig.petOverlayType == ModConfig.Type.Bar
        val flipped = ModConfig.petOverlayFlip

        val iconX = x + 3 + (if (!barStyle) 1 else 0) + if (barStyle && flipped) 29 else 0
        val iconY = y - 17 + (if (!barStyle) 4.5f else 0f)

        val textX = x + 34 - (if (!barStyle) 21.5f else 0f) - if (barStyle && flipped) 16 else 0
        val textY = y - 10 - (if (!barStyle) 15.5f else 0f)

        val displayLevel = if (isLevelMax) "LV MX" else "Lvl $level"
        if (!isLevelMax) {
            val displayXp = String.format(Locale.US, (if (animatedXp >= 0.1) "%.1f%%" else "%.2f%%"), animatedXp * 100)
            val levelTextY = textY - (3 * animatedLevelUp)
            drawText(drawContext, displayXp, textX, levelTextY,
                hexTransparent(color, 10.coerceAtLeast(255 - (animatedLevelUp * 255).toInt())),
                1f, true, true)
            drawText(drawContext, "LV UP", textX, levelTextY,
                hexTransparent(color, 10.coerceAtLeast((animatedLevelUp * 255).toInt())),
                1f, true, true)
        }

        drawText(drawContext, displayLevel, textX - if (isLevelMax) 0.5f else 0f, textY - 6 + if (isLevelMax) 3.5f else 0f,
            hexTransparent(color, 10.coerceAtLeast(255 - (animatedLevelUp * 255).toInt())),
            if (isLevelMax) 0.9f else 0.8f, true, true)

        val showItem = ModConfig.petOverlayShowItem
        drawItem(drawContext, currentPet, iconX, iconY + if (!barStyle && showItem) 2 else 0, 1.0f)
        if (showItem) {
            drawItem(drawContext, heldItem, iconX, iconY - 3 - if (barStyle) 2 else 0, 0.8f)
        }
    }

    private fun renderBars(drawContext: DrawContext, x: Int, y: Int, levelProgress: Float, color1: Int, color2: Int, color3: Int) {
        if (ModConfig.petOverlayAnimation_Idle) {
            val idleProgress = (Util.getMeasuringTimeMs() / 1700.0).toFloat() % 1
            legacyRoundRectangle(
                drawContext, x + 2 - idleProgress * 6, y + 2 - idleProgress * 6,
                46 + (idleProgress * 13), 4 + (idleProgress * 12),
                12f, hexTransparent(color2, 255 - getAlphaProgress(idleProgress))
            )
        }

        // Background
        RoundedRectangle.draw(drawContext, x, y, 51, 8, color3, 0, 4.5f, 0)
        // Level
        RoundedRectangle.draw(drawContext, x, y, max(8, (51 * levelProgress).toInt()), 8, color2, 0, 4.5f, 0)
        // XP
        RoundedRectangle.draw(drawContext, x + 2, y + 2, max(2, (47 * animatedXp).toInt()), 4, color1, 0,2.5f, 0)
    }

    private fun renderCircles(drawContext: DrawContext, x: Int, y: Int, levelProgress: Float, color1: Int, color2: Int, color3: Int) {
        if (ModConfig.petOverlayAnimation_Idle) {
            val idleProgress = (Util.getMeasuringTimeMs() / 1700.0).toFloat() % 1
            val color = hexTransparent(color2, 255 - getAlphaProgress(idleProgress))
            CircleChart.draw(drawContext, x + 12, y - 4, 1.01f, 11f + 5f * idleProgress, color, color, 0f, 0f, false, false)
        }
        val alt = ModConfig.petOverlayType == ModConfig.Type.CircularALT

        // Background
        CircleChart.draw(drawContext, x + 12, y - 4, 1.01f, 12.5f, color2, color2, 0f, 0f, false, false)
        // Level
        CircleChart.draw(drawContext, x + 12, y - 4, levelProgress * 1.01f, 12.7f, color3, color3, Math.PI.toFloat() / 2, 0f, true, false)
        CircleChart.draw(drawContext, x + 12, y - 4, 1.01f, 10.54f, color3, color3, 0f, 0f, false, false)
        // XP
        CircleChart.draw(drawContext, x + 12, y - 4, animatedXp * 1.01f, 10.52f - if (alt) 1.5f else 0f, color1, color1, Math.PI.toFloat() / 2, 0f, false, false)
    }
}