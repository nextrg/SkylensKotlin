package org.nextrg.skylens.helpers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.PropertyMap
import com.mojang.serialization.JsonOps
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.multiplayer.PlayerInfo
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.scores.DisplaySlot
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
            Minecraft.getInstance().level != null || !Minecraft.getInstance().isLocalServer
        } else {
            true
        }
    }

    fun getTabData(client: Minecraft): List<Component> {
        var text: List<Component> = ArrayList()
        val networkHandler: ClientPacketListener? = client.connection
        if (networkHandler != null) {
            text = networkHandler.onlinePlayers
                .stream()
                .map<Component> { obj: PlayerInfo -> obj.tabListDisplayName }
                .filter(Objects::nonNull)
                .toList()
        }
        return text
    }

    fun getScoreboardData(player: LocalPlayer): List<String?> {
        val scoreboardData: MutableList<String?> = ArrayList()
        val scoreboard = player.level().scoreboard
        val title = scoreboard.getDisplayObjective(DisplaySlot.BY_ID.apply(1))
        if (title != null) {
            scoreboardData.addFirst(title.displayName.copy().string)
        }
        for (lines in scoreboard.trackedPlayers) {
            if (scoreboard.listPlayerScores(lines).containsKey(title)) {
                val team = scoreboard.getPlayersTeam(lines.scoreboardName)
                if (team != null) {
                    val strLine = team.playerPrefix.string + team.playerSuffix.string
                    if (strLine.trim { it <= ' ' }.isNotEmpty()) {
                        scoreboardData.add(ChatFormatting.stripFormatting(strLine))
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
            errorMessage("Caught an error setting item texture (fallback)", e)
            return if (isPet) bone else air
        }
        return itemStack
    }

    fun isShiftDown(): Boolean {
        return Minecraft.getInstance().hasShiftDown()
    }

    private fun getItemFromJson(petJson: JsonObject): ItemStack {
        val itemId = petJson.get("itemid").asString.removePrefix("minecraft:")
        return ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.withDefaultNamespace(itemId)))
    }

    private fun applyTextureToHeadItem(string: String, itemStack: ItemStack) {
        val end = if (string.contains("\"}]},")) "\"}]}," else "\"}]}},"
        val texture = string.substring(string.indexOf("Value:") + 7, string.lastIndexOf(end))

        val gameProfile = createGameProfile(UUID.randomUUID(), ExtraCodecs.PROPERTY_MAP.parse(JsonOps.INSTANCE, JsonParser.parseString(
            "[{\"name\":\"textures\",\"value\":\"$texture\"}]"
        )).getOrThrow())

        itemStack.set(DataComponents.PROFILE, ResolvableProfile.createResolved(gameProfile))
    }

    private fun createGameProfile(uuid: UUID, properties: PropertyMap): GameProfile {
        return GameProfile(uuid, "", properties)
    }
}
