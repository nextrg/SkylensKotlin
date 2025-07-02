package org.nextrg.skylens.helpers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.ProfileComponent
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.nextrg.skylens.ModConfig
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*


object OtherUtil {
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

    fun onSkyblock(): Boolean {
        if (ModConfig.onlySkyblock) {
            return MinecraftClient.getInstance().world != null || !MinecraftClient.getInstance().isInSingleplayer;
        } else {
            return true;
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

    fun errorMessage(message: String, exception: Exception) {
        System.err.println("[Skylens] $message error:\n$exception")
    }

    fun getTextureFromNeu(itemName: String, isPet: Boolean): ItemStack {
        var itemStack = ItemStack(Items.PLAYER_HEAD)
        val air = ItemStack(Items.AIR)
        if (itemName != "") {
            try {
                val expBoostItem = itemName.contains("Exp Boost")
                var rarity = ""; var behind = ""
                if (isPet) {
                    rarity = "%3B4"
                }
                if (expBoostItem) {
                    behind = "PET_ITEM_"
                    rarity = "_COMMON"
                }
                val path = "/f75fb6876c1cc0179b47546e273389a21f8968a7/items/$behind" +
                itemName.uppercase(Locale.getDefault()).replace(" ", "_").replace("EXP_", "SKILL_") + rarity + ".json"
                val petJson = jsonNeu(path)
                if (!petJson.toString().contains("SkullOwner")) {
                    val string: String = petJson.toString()
                    val itemString = string.substring(string.indexOf("itemid") + 9, string.indexOf("\",\"")).replace("minecraft:", "")
                    itemStack = ItemStack(Registries.ITEM.get(Identifier.ofVanilla(itemString)))
                } else {
                    val string: String = petJson.get("nbttag").asString
                        .replace("\\[\\d+:\\{", "[{")
                        .replace("\\[\\d+:\"", "[\"")
                        .replace(",\\d+:\"", ",\"")
                        .replace("\\\\\"", "\"")
                        .replace("([{,])([A-Za-z_][A-Za-z0-9_]*)\\:", "$1\"$2\":")
                    val texture = string.substring(string.indexOf("Value:") + 7, string.lastIndexOf("\"}]}},"))

                    val gameProfile = GameProfile(UUID.randomUUID(), "CustomHead")
                    gameProfile.properties.put("textures", Property("textures", texture))
                    itemStack.set(DataComponentTypes.PROFILE, ProfileComponent(gameProfile))
                }
            } catch (e: java.lang.Exception) {
                errorMessage("Caught an error setting item texture (fallback)", e)
                itemStack = air
            }
        } else {
            itemStack = air
        }
        return itemStack
    }
}