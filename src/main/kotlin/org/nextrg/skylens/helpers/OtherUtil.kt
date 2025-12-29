package org.nextrg.skylens.helpers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ProfileComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import org.nextrg.skylens.ModConfig
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*


object OtherUtil {
    private val air = ItemStack(Items.AIR)
    private val bone = ItemStack(Items.BONE)
    private const val BASE_NEU_PATH = "https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO"
    private const val BASE_NEU_ITEM_PATH = "/refs/heads/master/items/"

    fun jsonNeu(path: String): JsonObject {
        return json("$BASE_NEU_PATH$path")
    }

    private fun json(path: String): JsonObject {
        return try {
            val url = URI(path).toURL()
            val jsonString = url.openStream().bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            JsonParser.parseString(jsonString).asJsonObject
        } catch (ignored: Exception) {
            JsonObject()
        }
    }

    fun onSkyblock(): Boolean {
        return if (ModConfig.onlySkyblock) {
            MinecraftClient.getInstance().world != null || !MinecraftClient.getInstance().isInSingleplayer
        } else {
            true
        }
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

    fun getScoreboardData(player: ClientPlayerEntity): List<String?> {
        val scoreboardData: MutableList<String?> = ArrayList()
        val scoreboard = player.scoreboard
        val title = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.FROM_ID.apply(1))
        if (title != null) {
            scoreboardData.addFirst(title.displayName.copy().string)
        }
        for (lines in scoreboard.knownScoreHolders) {
            if (scoreboard.getScoreHolderObjectives(lines).containsKey(title)) {
                val team = scoreboard.getScoreHolderTeam(lines.nameForScoreboard)
                if (team != null) {
                    val strLine = team.prefix.string + team.suffix.string
                    if (strLine.trim { it <= ' ' }.isNotEmpty()) {
                        scoreboardData.add(Formatting.strip(strLine))
                    }
                }
            }
        }
        return scoreboardData
    }

    fun errorMessage(message: String, exception: Exception) {
        System.err.println("[Skylens] $message error:\n$exception")
    }

    fun getTextureFromNeu(itemName: String, isPet: Boolean): ItemStack {
        val itemStack: ItemStack
        if (itemName.isEmpty()) return if (isPet) bone else air
        try {
            val (behind, rarity) = when {
                itemName.contains("Exp Boost") -> "PET_ITEM_" to "_COMMON"
                isPet -> "" to "%3B4"
                else -> "" to ""
            }
            val jsonName = itemName.uppercase(Locale.getDefault()).replace(" ", "_").replace("EXP_", "SKILL_")
            val path = "$BASE_NEU_ITEM_PATH$behind$jsonName$rarity.json"
            val petJson = jsonNeu(path)
            if (!petJson.toString().contains("SkullOwner")) {
                itemStack = getItemFromJson(petJson)
            } else {
                itemStack = ItemStack(Items.PLAYER_HEAD)
                val string: String = petJson.get("nbttag").asString
                    .replace("\\[\\d+:\\{", "[{")
                    .replace("\\[\\d+:\"", "[\"")
                    .replace(",\\d+:\"", ",\"")
                    .replace("\\\\\"", "\"")
                    .replace("([{,])([A-Za-z_][A-Za-z0-9_]*)\\:", "$1\"$2\":")
                if (string.contains("Value:")) {
                    applyTextureToHeadItem(string, itemStack)
                }
            }
        } catch (e: java.lang.Exception) {
            // errorMessage("Caught an error setting item texture (fallback)", e) <- fix later
            return if (isPet) bone else air
        }
        return itemStack
    }

    private fun getItemFromJson(petJson: JsonObject): ItemStack {
        val itemId = petJson.get("itemid").asString.removePrefix("minecraft:")
        return ItemStack(Registries.ITEM.get(Identifier.ofVanilla(itemId)))
    }

    private fun applyTextureToHeadItem(string: String, itemStack: ItemStack) {
        val end = if (string.contains("\"}]},")) "\"}]}," else "\"}]}},"
        val texture = string.substring(string.indexOf("Value:") + 7, string.lastIndexOf(end))
        val gameProfile = GameProfile(UUID.randomUUID(), "CustomHead")
        gameProfile.properties.put("textures", Property("textures", texture))
        itemStack.set(DataComponentTypes.PROFILE, ProfileComponent(gameProfile))
    }
}