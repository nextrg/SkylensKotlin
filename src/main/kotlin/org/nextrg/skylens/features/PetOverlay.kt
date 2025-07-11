package org.nextrg.skylens.features

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.RenderTickCounter
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import net.minecraft.util.Util
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.Pets.getCurrentPet
import org.nextrg.skylens.api.Pets.getPetHeldItem
import org.nextrg.skylens.api.Pets.getPetLevel
import org.nextrg.skylens.api.Pets.getPetMaxLevel
import org.nextrg.skylens.api.Pets.getPetRarity
import org.nextrg.skylens.api.Pets.getPetRarityText
import org.nextrg.skylens.api.Pets.getPetXp
import org.nextrg.skylens.helpers.OtherUtil.getTextureFromNeu
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.RenderUtil
import org.nextrg.skylens.helpers.RenderUtil.drawItem
import org.nextrg.skylens.helpers.RenderUtil.drawText
import org.nextrg.skylens.helpers.RenderUtil.legacyRoundRectangle
import org.nextrg.skylens.helpers.VariablesUtil.animateFloat
import org.nextrg.skylens.helpers.VariablesUtil.colorToARGB
import org.nextrg.skylens.helpers.VariablesUtil.getAlphaProgress
import org.nextrg.skylens.helpers.VariablesUtil.hexTransparent
import org.nextrg.skylens.helpers.VariablesUtil.quad
import org.nextrg.skylens.helpers.VariablesUtil.sToMs
import org.nextrg.skylens.renderables.Renderables.drawPie
import org.nextrg.skylens.renderables.Renderables.roundRectangleFloat
import java.lang.Math.clamp
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

    private var cacheColor1 = colorToARGB(ModConfig.petOverlayColor2)
    private var cacheColor2 = colorToARGB(ModConfig.petOverlayColor1)
    private var cacheColor3 = colorToARGB(ModConfig.petOverlayColor3)

    private var isBarType = false
    private var altStyle = false
    private var flipped = false
    private var invertColor = false
    private var showItem = false
    private var idleAnim = false
    private var levelUpAnim = false
    private var valueChangeAnim = false

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

    fun updatePet() {
        scope.launch {
            delay((transition * transitionDuration).toLong())

            val pet = getCurrentPet()
            currentPet = pet
            rarity = getPetRarity(getPetRarityText(pet))
            updateTheme()
        }
    }

    fun levelUp() {
        if (!levelUpAnim) return
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

    fun prepare() {
        updateTheme()
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

    fun highlight(context: DrawContext) {
        val (x, y) = getPosition()

        val margin = 4
        val intX = x.toInt() - margin
        val intY = y.toInt() - 18 - margin

        if (isBarType) {
            context.fill(intX, intY + 3, intX + 51 + margin * 2, intY + 26 + margin * 2, 0x14FFFFFF)
        } else {
            context.fill(intX, intY - 14, intX + 24 + margin * 2, intY + 26 + margin * 2, 0x14FFFFFF)
        }
    }

    private fun getAnimationOffset(x: Float, y: Float): Pair<Float, Float> {
        fun map(value: Float): Float = 120f * (value - 0.5f)
        return map(x) to map(y)
    }

    fun getPosition(): Pair<Float, Float> {
        val anchorKey = ModConfig.petOverlayAnchor.toString()
        val offsetX = ModConfig.petOverlayX.toFloat()
        val offsetY = ModConfig.petOverlayY.toFloat()

        val (baseX, baseY) = RenderUtil.computePosition(
            RenderUtil.ElementPos(
                anchorKey = anchorKey,
                offsetX = offsetX,
                offsetY = offsetY,
                isBar = isBarType,
                clampX = { pos, screenW ->
                    clamp(pos, 4f, screenW.toFloat() - 2 - 26 - if (isBarType) 27 else 0)
                },
                clampY = { pos, screenH ->
                    clamp(pos, 19f + if (!isBarType) 17 else 0, screenH.toFloat() - 12)
                }
            )
        )

        val (screenW, screenH) = RenderUtil.getScaledWidthHeight()
        val (ax, ay) = RenderUtil.anchors[anchorKey] ?: floatArrayOf(0.5f, 1f)

        val anchorX = screenW * ax - 50 * ax
        val anchorY = screenH * ay - 8 * ay
        val marginX = 2 * (1 - ax * 2)
        val marginY = 2 * (1 - ay * 2)

        var (offsetAnimX, offsetAnimY) = getAnimationOffset(ax, ay)
        offsetAnimX -= (baseX - anchorX - marginX) * (1 - ax * 2)
        offsetAnimY += (baseY - anchorY - marginY) * (1 - ay * 2)

        if (anchorKey == "TopMiddle" || anchorKey == "BottomMiddle") {
            transitionX = 1f
        }

        val finalX = baseX + offsetAnimX - offsetAnimX * (if (hudEditor) 1f else transitionX)
        val finalY = baseY + offsetAnimY - offsetAnimY * (if (hudEditor) 1f else transitionY)

        return finalX to finalY
    }

    fun updateConfigValues() {
        isBarType = ModConfig.petOverlayType == ModConfig.Type.Bar
        flipped = ModConfig.petOverlayFlip
        altStyle = ModConfig.petOverlayType == ModConfig.Type.CircularALT
        invertColor = ModConfig.petOverlayInvert
        showItem = ModConfig.petOverlayShowItem
        idleAnim = ModConfig.petOverlayAnimation_Idle
        valueChangeAnim = ModConfig.petOverlayAnimation_LevelXp
        levelUpAnim = ModConfig.petOverlayAnimation_LevelUp
        updateTheme()
    }

    private fun updateTheme() {
        val configTheme = ModConfig.petOverlayTheme.toString()
        val isCustom = configTheme == "Custom"

        val displayTheme = when {
            configTheme == "Pet" -> rarity
            isCustom -> rarity
            else -> configTheme
        }

        return if (!isCustom) {
            val colors = rarityColors[displayTheme.lowercase()]
            if (colors != null) {
                cacheColor1 = colors[0]
                cacheColor2 = colors[1]
                cacheColor3 = colors[2]
            } else { }
        } else {
            cacheColor1 = colorToARGB(ModConfig.petOverlayColor2)
            cacheColor2 = colorToARGB(ModConfig.petOverlayColor1)
            cacheColor3 = colorToARGB(ModConfig.petOverlayColor3)
        }
    }

    fun render(drawContext: DrawContext) {
        if ((!ModConfig.petOverlay && !hudEditor) || !onSkyblock()) return

        val (x, y) = getPosition()
        var color1 = cacheColor1; var color2 = cacheColor2; var color3 = cacheColor3

        val textColor = color2
        if (invertColor) {
            color1 = color2.also { color2 = color1 }
        }

        if (isBarType) {
            renderBars(drawContext, x, y, animatedLevelProgress, color1, color2, color3)
        } else {
            renderCircles(drawContext, x, y, animatedLevelProgress, color1, color2, color3)
        }
        renderText(drawContext, x, y, textColor)
    }

    private fun renderText(drawContext: DrawContext, x: Float, y: Float, color: Int) {
        val isLevelMax = level == maxLevel

        val iconX = x + 3 + (if (!isBarType) 1 else 0) + if (isBarType && flipped) 29 else 0
        val iconY = y - 17 + (if (!isBarType) 4.5f else 0f)

        val textX = x + 34 - (if (!isBarType) 21.5f else 0f) - if (isBarType && flipped) 16 else 0
        val textY = y - 10 - (if (!isBarType) 15.5f else 0f)

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

        val isPlayerHead = heldItem.itemName.toString().contains("player_head")
        drawItem(drawContext, currentPet, iconX, iconY + if (!isBarType && showItem && isPlayerHead) 2 else 0, 1.0f)
        if (showItem) {
            val offsetX = if (isPlayerHead) 0 else 7
            val offsetY = -3 - (if (isBarType) 2 else 0) + (if (isPlayerHead) 0 else 8)
            val scale = 0.8f - if (isPlayerHead) 0f else 0.3f

            drawItem(drawContext, heldItem, iconX + offsetX, iconY + offsetY, scale)
        }
    }

    private fun renderBars(drawContext: DrawContext, x: Float, y: Float, levelProgress: Float, color1: Int, color2: Int, color3: Int) {
        if (idleAnim) {
            val idleProgress = (Util.getMeasuringTimeMs() / 1700.0).toFloat() % 1
            legacyRoundRectangle(
                drawContext, x + 2 - idleProgress * 6, y + 2 - idleProgress * 6,
                46 + (idleProgress * 13), 4 + (idleProgress * 12),
                12f, hexTransparent(color2, 255 - getAlphaProgress(idleProgress))
            )
        }

        // Background
        roundRectangleFloat(drawContext, x, y, 51f, 8f, color3, 0, 4.5f, 0)
        // Level
        roundRectangleFloat(drawContext, x, y, max(8f, (51 * levelProgress)), 8f, color2, 0, 4.5f, 0)
        // XP
        roundRectangleFloat(drawContext, x + 2, y + 2, max(2f, (47 * animatedXp)), 4f, color1, 0,2.5f, 0)
    }

    private fun renderCircles(drawContext: DrawContext, x: Float, y: Float, levelProgress: Float, color1: Int, color2: Int, color3: Int) {
        if (idleAnim) {
            val idleProgress = (Util.getMeasuringTimeMs() / 1700.0).toFloat() % 1
            val color = hexTransparent(color2, 255 - getAlphaProgress(idleProgress))
            drawPie(drawContext, x + 12f, y - 4f, 1.01f, 11f + 5f * idleProgress, color, 0f, 0f, false, false)
        }

        // Background
        drawPie(drawContext, x + 12f, y - 4f, 1.01f, 12.5f, color2, 0f, 0f, false, false)
        // Level
        drawPie(drawContext, x + 12f, y - 4f, levelProgress * 1.01f, 12.7f, color3, Math.PI.toFloat() / 2, 0f, true, false)
        drawPie(drawContext, x + 12f, y - 4f, 1.01f, 10.54f, color3, 0f, 0f, false, false)
        // XP
        drawPie(drawContext, x + 12f, y - 4f, animatedXp * 1.01f, 10.52f - if (altStyle) 1.5f else 0f, color1, Math.PI.toFloat() / 2, 0f, false, false)
    }
}