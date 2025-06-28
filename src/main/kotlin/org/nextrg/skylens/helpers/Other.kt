package org.nextrg.skylens.helpers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.Text
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

object Other {
    fun jsonNeu(path: String): JsonObject {
        return json("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO$path");
    }

    private fun json(path: String): JsonObject {
        var json = JsonObject();
        try {
            val url: URL = URI(path).toURL()
            val reader = BufferedReader(InputStreamReader(url.openStream(), StandardCharsets.UTF_8))
            val sb = StringBuilder()
            var cp: Int
            while ((reader.read().also { cp = it }) != -1) {
                sb.append(cp.toChar())
            }
            json = JsonParser.parseString(sb.toString()).getAsJsonObject()
        } catch (ignored: Exception) {
        }
        return json
    }

    fun getTabData(client: MinecraftClient): List<Text> {
        var text: List<Text> = ArrayList()
        val networkHandler: ClientPlayNetworkHandler? = client.networkHandler
        if (networkHandler != null) {
            text = networkHandler.playerList
                .stream()
                .map<Text> { obj: PlayerListEntry -> obj.displayName }
                .filter(Objects::nonNull)
                .toList()
        }
        return text
    }

    fun errorMessage(message: String, exception: Exception) {
        System.err.println("[Skylens] $message error:\n$exception")
    }
}