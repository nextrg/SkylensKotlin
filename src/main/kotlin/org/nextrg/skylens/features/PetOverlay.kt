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
import org.nextrg.skylens.helpers.Other.animateFloat
import org.nextrg.skylens.helpers.Other.quad
import org.nextrg.skylens.helpers.Pets
import org.nextrg.skylens.helpers.Pets.getCurrentPet
import org.nextrg.skylens.helpers.Pets.getPetLevel
import org.nextrg.skylens.helpers.Pets.getPetMaxLevel
import org.nextrg.skylens.helpers.Pets.getPetRarity
import org.nextrg.skylens.helpers.Pets.getPetRarityText
import org.nextrg.skylens.helpers.Pets.getPetXp
import kotlin.math.max

object PetOverlay {
    fun init() {
        PetOverlay.prepare()
    }

    private var currentPet: ItemStack = ItemStack(Items.BONE)

    private var level: Int = 1;
    private var maxLevel: Int = 100;
    private var xp: Float = 0f

    private var transitionY = 0f;
    private var transitionX = 0f;
    private var hidden: Boolean = true

    fun updatePet() {
        CoroutineScope(Dispatchers.Default).launch {
            delay(25L)
            if (!hidden) {
                delay(475L)
                currentPet = getCurrentPet()
            } else {
                currentPet = getCurrentPet()
            }
        }
    }

    private fun statisticsCoroutine() {
        level = getPetLevel()
        maxLevel = getPetMaxLevel()
        xp = getPetXp()
    }

    fun updatePetStatistics() {
        CoroutineScope(Dispatchers.Default).launch {
            delay(25L)
            if (!hidden) {
                delay(475L)
                statisticsCoroutine()
            } else {
                statisticsCoroutine()
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
        CoroutineScope(Dispatchers.Default).launch {
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
        val textRenderer = MinecraftClient.getInstance().textRenderer

        val level = getPetLevel()
        val maxLevel = getPetMaxLevel()
        val xp = getPetXp()

        val levelProgress = level / maxLevel

        val textLevel = level.toString()
        val textXp = (xp * 100).toString().replace(".0", "") + "%"

        val rarity = getPetRarity(getPetRarityText(getCurrentPet()))

        val globalX = -60 + 60 * transitionX
        val globalY = -60 + 60 * transitionY

        val x = (globalX + 10).toInt()
        val y = (globalY + 10).toInt()

        renderBar(drawContext, x, y, 50, 8, rarityColors[rarity]!![2], 4.5f)
        renderBar(drawContext, x, y, max(8, 50 * levelProgress), 8, rarityColors[rarity]!![1], 4.5f)
        renderBar(drawContext, x, y, max(2, (46 * xp).toInt()), 4, rarityColors[rarity]!![0], 2.5f)
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