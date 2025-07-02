package org.nextrg.skylens.api

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.regex.Pattern

object PlayerStats {
    private var PRESSURE_PATTERN: Pattern = Pattern.compile("(?<=Pressure: ❍)(\\d+)(?=%)")
    var pressure = 0f

    fun init() {
        ClientReceiveMessageEvents.GAME.register(ClientReceiveMessageEvents.Game { message, _ ->
            messageEvents(message)
        })
    }

    fun readActionBar(text: Text) {
        val string = text.string
        if (string.contains("Pressure:")) {
            val matcher = PRESSURE_PATTERN.matcher(string)
            if (matcher.find()) {
                pressure = matcher.group(1).toFloat() / 100
            }
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