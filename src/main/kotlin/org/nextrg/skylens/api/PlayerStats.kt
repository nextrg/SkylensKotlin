package org.nextrg.skylens.api

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.features.DrillFuelMeter
import org.nextrg.skylens.features.DungeonScoreMeter
import org.nextrg.skylens.features.PressureDisplay
import org.nextrg.skylens.helpers.OtherUtil.getScoreboardData
import org.nextrg.skylens.helpers.StringsUtil.parseSuffix
import java.util.regex.Pattern

object PlayerStats {
    private val PRESSURE_PATTERN: Pattern = Pattern.compile("(?<=Pressure: ❍)(\\d+)(?=%)")
    private val DRILL_FUEL_PATTERN: Pattern = Pattern.compile("(\\d+)/([^\\s]+)\\s+Drill Fuel$")
    private val DUNGEON_SCORE_PATTERN: Pattern = Pattern.compile("Cleared:\\s*\\d+%\\s*\\((\\d+)\\)")

    private var lastCheckTime = 0L
    private var wasInWater = false
    private var wasInDungeon = false

    var pressure = 0f
    var health = 0f
    var dungeonScore = 0f
    var fuel = "0/3000"

    fun init() {
        ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { message, _ ->
            messageEvents(message)
        })
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            val player = client.player ?: return@EndTick

            updateHealth(player)

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastCheckTime >= 500L) {
                lastCheckTime = currentTime
                checkInWater(player); checkDungeon(player)
            }
        })
    }

    private fun checkDungeon(player: ClientPlayerEntity) {
        val scoreboardData = getScoreboardData(player)
        val inDungeon = scoreboardData.toString().contains("The Catacombs")

        if (inDungeon) {
            for (lines in scoreboardData) {
                val string = lines.toString()
                if (string.contains("Cleared:")) {
                    val matcher = DUNGEON_SCORE_PATTERN.matcher(string)
                    if (matcher.find()) {
                        dungeonScore = matcher.group(1).toFloat()
                    }
                }
            }
        } else {
            dungeonScore = 0f
        }

        if (inDungeon && !wasInDungeon) {
            DungeonScoreMeter.show()
        } else if (!inDungeon) {
            DungeonScoreMeter.hide()
        }

        wasInDungeon = inDungeon
    }

    private fun checkInWater(player: ClientPlayerEntity) {
        val inWater = player.world.getFluidState(player.blockPos).fluid == Fluids.WATER
        val showAt = ModConfig.pressureDisplayShowAt

        if (!inWater) {
            pressure = 0f
        }

        if (pressure >= showAt && inWater && !wasInWater) {
            PressureDisplay.show()
        } else if (pressure < showAt || !inWater) {
            PressureDisplay.hide()
        }

        wasInWater = inWater && pressure >= showAt
    }

    private fun updateHealth(player: ClientPlayerEntity) {
        health = player.health / player.maxHealth
    }

    fun readActionBar(text: Text) {
        val string = text.string
        if (string.contains("Pressure:")) {
            val matcher = PRESSURE_PATTERN.matcher(string)
            if (matcher.find()) {
                pressure = matcher.group(1).toFloat() / 100
            }
        }
        if (string.contains("Drill Fuel")) {
            val noFormatting = Formatting.strip(string)
            val matcher = DRILL_FUEL_PATTERN.matcher(noFormatting.toString())
            if (matcher.find()) {
                fuel = "${matcher.group(1)}/${parseSuffix(matcher.group(2))}"
                DrillFuelMeter.show()
            }
        } else {
            DrillFuelMeter.hide()
        }
    }

    private fun messageEvents(message: Text) {
        val string = message.string
        val content = Formatting.strip(string).toString()
        if (content.contains("You fainted from pressure")) {
            pressure = 0f
        }
    }
}