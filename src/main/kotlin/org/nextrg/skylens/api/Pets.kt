package org.nextrg.skylens.api

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.nextrg.skylens.features.PetOverlay.hideOverlay
import org.nextrg.skylens.features.PetOverlay.levelUp
import org.nextrg.skylens.features.PetOverlay.showOverlay
import org.nextrg.skylens.features.PetOverlay.updatePet
import org.nextrg.skylens.features.PetOverlay.updateStats
import org.nextrg.skylens.helpers.ItemsUtil.tooltipFromItemStack
import org.nextrg.skylens.helpers.OtherUtil.getTabData
import org.nextrg.skylens.helpers.OtherUtil.getTextureFromNeu
import org.nextrg.skylens.helpers.StringsUtil.colorFromCode
import org.nextrg.skylens.helpers.StringsUtil.colorToRarity
import org.nextrg.skylens.helpers.VariablesUtil.sToMs
import org.nextrg.skylens.helpers.VariablesUtil.toFixed
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object Pets {
    private val AUTOPET_PATTERN: Pattern = Pattern.compile("^Autopet equipped your \\[Lvl (\\d+)] (.+)! VIEW RULE$")
    private val SUMMON_PATTERN: Pattern = Pattern.compile("You (summoned|despawned) your (.+?)!")
    private val LEVELUP_PATTERN: Pattern = Pattern.compile("Your (.+?) leveled up to level (\\d+)!")

    private val scheduler = Executors.newScheduledThreadPool(1)
    private var scheduledResetTask: ScheduledFuture<*>? = null

    private var cachedPets: MutableList<ItemStack> = mutableListOf()
    private var isPetMenu = false
    private var currentPetScreen: GenericContainerScreen? = null
    private var hasCached = false

    private var noCacheMode = true
    private var noCacheLevel = 1
    private var noCacheMaxLevel = 100
    private var noCacheRarity = "common"

    private var lastUpdate = System.currentTimeMillis()
    private var updateByTab = true

    private var currentPet: ItemStack = ItemStack(Items.BONE)
    private var level: Int = 1
    private var maxLevel: Int = 100
    private var xp: Float = 0f
    private var heldItem: String = ""

    fun init() {
        ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { message, _ ->
            messageEvents(message)
        })
        ScreenEvents.BEFORE_INIT.register(ScreenEvents.BeforeInit { _, screen, _, _ ->
            if (screen is GenericContainerScreen && screen.title.string.startsWith("Pets")) {
                readInventory(screen)
            }
        })
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            checkPetScreen(client)
            readTab(client, true)
        })
    }

    fun getCurrentPet(): ItemStack = currentPet

    fun getPetLevel(): Int = if (noCacheMode) noCacheLevel else level
    fun getPetMaxLevel(): Int = if (noCacheMode) noCacheMaxLevel else maxLevel
    fun getPetXp(): Float = if (noCacheMode && noCacheLevel == noCacheMaxLevel) 1f else xp
    fun getPetHeldItem(): String = heldItem

    fun getPetRarity(element: Text): String {
        if (noCacheMode) {
            return noCacheRarity
        }
        return colorToRarity(element.style.color.toString())
    }

    fun getPetRarityText(customName: Text?): Text {
        val siblings = customName?.siblings
        return if (siblings != null && siblings.size > 1) {
            if (siblings[1].string.contains("[")) {
                return siblings[2]
            } else {
                return siblings[1]
            }
        } else {
            Text.empty()
        }
    }

    fun isGoldenDragon(string: String): Boolean {
        return string.contains(Regex("(Golden|Jade) Dragon"))
    }

    private fun checkPetScreen(client: MinecraftClient) {
        val screen = client.currentScreen
        if (isPetMenu && (screen !is GenericContainerScreen || screen.title.string?.startsWith("Pets") != true)) {
            isPetMenu = false
            currentPetScreen = null
        }
    }

    private fun getPetSlots(screen: GenericContainerScreen): List<Slot> {
        return screen.screenHandler.slots
            .filter { it.id in 10..43 && it.id % 9 in 1..7 }
    }

    private fun getPetStats(pet: ItemStack) {
        val tooltip = tooltipFromItemStack(pet)
        val petName = pet.customName ?: return
        level = parseLevel(petName, 0)
        maxLevel = if (isGoldenDragon(petName.string)) 200 else 100
        heldItem = ""

        for (line in tooltip) {
            val string = line.toString()

            if (string.contains("Progress to") && string.contains("%")) {
                if (line.siblings.size > 1) {
                    val displayXp = line.siblings[1].string.replace("%", "")
                    xp = (displayXp.toFloat() / 100f).toFixed(3)
                }
            }

            if (string.contains("MAX LEVEL")) {
                xp = 1f
                level = maxLevel
            }

            if (string.contains("Held Item:")) {
                if (line.siblings.size > 1) {
                    heldItem = line.siblings[1].string
                }
            }
        }

        updateStats()
    }

    private fun findTabIndices(list: List<Text>): Pair<Int, Int> {
        return list.withIndex().fold(3 to 45) { (lvlIdx, xpIdx), (i, text) ->
            val s = text.toString()
            val newLvl = if ("[Lvl" in s) i else lvlIdx
            val newXp = if ("XP" in s && "/" in s && "%" in s) i else xpIdx
            newLvl to newXp
        }
    }

    private fun parseLevel(text: Text?, index: Int): Int {
        return text?.siblings?.getOrNull(index)?.string
            ?.replace(Regex("""\[Lvl (\d+)]"""), "$1")
            ?.trim()
            ?.toIntOrNull() ?: 1
    }

    private fun parseXp(text: Text?): Float {
        return text?.siblings?.getOrNull(4)?.string
            ?.removePrefix("(")
            ?.removeSuffix("%)")
            ?.trim()
            ?.toFloatOrNull()
            ?.div(100) ?: 0f
    }

    // Real-time updating values based on tab list, updates every 2.5 seconds
    private fun readTab(client: MinecraftClient, cooldown: Boolean) {
        if (System.currentTimeMillis() - lastUpdate < 2500 && cooldown) return
        lastUpdate = System.currentTimeMillis()

        if (!updateByTab) return

        val currentPetText = getPetRarityText(currentPet.customName)
        if (currentPetText == Text.empty() && !noCacheMode) return

        val currentRarity = getPetRarity(currentPetText)

        val list = getTabData(client)
        val (levelIndex, xpIndex) = findTabIndices(list)

        val tabPet = list.getOrNull(levelIndex) ?: return
        if (tabPet.siblings.size < 3) return

        val tabName = tabPet.siblings[2]
        val tabRarity = getPetRarity(tabName)
        val tabPetName = tabName.string

        if (currentPetText.string != tabPetName && currentRarity != tabRarity && !noCacheMode) return

        level = parseLevel(tabPet, 1)
        maxLevel = if (isGoldenDragon(tabPetName)) 200 else 100
        xp = if (!tooltipFromItemStack(currentPet).toString().contains("MAX LEVEL")) {
            parseXp(list.getOrNull(xpIndex))
        } else {
            1f
        }

        updateStats()
    }

    private fun readInventory(screen: GenericContainerScreen) {
        if (screen != currentPetScreen) {
            currentPetScreen = screen
            hasCached = false
        }

        isPetMenu = true
        noCacheMode = false

        var ticks = 0
        ScreenEvents.afterTick(screen).register(ScreenEvents.AfterTick { _ ->
            if (!isPetMenu || hasCached) return@AfterTick

            if (ticks < 2) {
                ticks++
                return@AfterTick
            }

            cachedPets.clear()

            var equippedPet = ""
            var rarity = "common"

            getPetSlots(screen).forEach { slot ->
                val stack = slot.stack
                if (!stack.isEmpty && stack.item == Items.PLAYER_HEAD) {
                    cachedPets.add(stack)
                    val content = tooltipFromItemStack(stack).toString()
                    if ("Click to despawn!" in content && stack.customName != null) {
                        equippedPet = stack.customName!!.string
                        rarity = getPetRarity(getPetRarityText(stack.customName!!))
                    }
                }
            }

            findPetFromInventory(equippedPet, rarity)
            hasCached = true
        })
    }

    private fun preventTabUpdate() {
        updateByTab = false
        scheduledResetTask?.cancel(false)

        scheduledResetTask = scheduler.schedule({
            updateByTab = true; scheduledResetTask = null
        }, sToMs(2.5f), TimeUnit.MILLISECONDS)
    }

    private fun removeLevel(petName: String): String {
        return petName.substringAfter("] ", petName).trim()
    }

    private fun findPetFromInventory(petName: String, rarity: String) {
        val noSymbol = petName.replace(" ✦", "").replace("⭐ ", "")
        val petNameWithoutLvl = removeLevel(noSymbol)
        if (noCacheMode) {
            setPet(getTextureFromNeu(noSymbol, true))
            return
        }
        for (pet in cachedPets) {
            val cachedPetName = pet.customName
            val nameWithoutFormat = Formatting.strip(cachedPetName?.string)
            val petRarity = getPetRarity(getPetRarityText(pet.customName))
            if (nameWithoutFormat?.contains(petNameWithoutLvl) == true && petRarity == rarity) {
                setPet(pet)
                break
            }
        }
    }

    private fun setPet(pet: ItemStack) {
        preventTabUpdate()
        currentPet = pet
        updatePet()
        getPetStats(pet)
    }

    private fun messageEvents(message: Text) {
        val string = message.string
        val content = Formatting.strip(string).toString()
        if (content.contains("You summoned your") || content.contains("You despawned your")) {
            val matcher = SUMMON_PATTERN.matcher(content)
            if (matcher.find()) {
                if (matcher.group(1) == "summoned") {
                    val rarity = colorToRarity(message.siblings[1].style.color.toString())
                    findPetFromInventory(matcher.group(2), rarity)
                    showOverlay()
                }
                if (matcher.group(1) == "despawned") {
                    hideOverlay()
                }
            }
        }

        if (content.contains("Autopet")) {
            val matcher = AUTOPET_PATTERN.matcher(content)
            if (matcher.find()) {
                val autopetLevel = matcher.group(1)
                val autopetPet = matcher.group(2)
                val matchIndex = string.indexOf(autopetLevel) + autopetLevel.length - 2
                var rarity = "common"

                if (matchIndex != -1 && matchIndex + 6 <= string.length) {
                    val input = string.substring(matchIndex + 4, matchIndex + 6)
                    val color = colorFromCode(input)
                    if (color.size > 1) {
                        rarity = color[1]
                    }
                }

                noCacheLevel = autopetLevel.toIntOrNull() ?: 1
                noCacheRarity = rarity
                noCacheMaxLevel = if (isGoldenDragon(autopetPet)) 200 else 100

                findPetFromInventory(autopetPet, rarity)
                showOverlay()
            }
        }

        if (content.contains("Welcome to Hypixel Skyblock!")) {
            scheduler.schedule({
                readTab(MinecraftClient.getInstance(), false)
            }, sToMs(1.25f), TimeUnit.MILLISECONDS)
        }

        if (content.contains("leveled up to level")) {
            val matcher = LEVELUP_PATTERN.matcher(content)
            if (matcher.find()) {
                level = matcher.group(2)?.toIntOrNull() ?: level
                xp = 0f
                updateStats()
                levelUp()
                preventTabUpdate()
            }
        }
    }
}