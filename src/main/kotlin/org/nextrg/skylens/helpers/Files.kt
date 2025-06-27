package org.nextrg.skylens.helpers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets

class Files {
    companion object {
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
            } catch (ignored: Exception) {}
            return json
        }
    }
}