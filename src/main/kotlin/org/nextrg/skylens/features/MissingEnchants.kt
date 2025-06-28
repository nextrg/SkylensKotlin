package org.nextrg.skylens.features

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.client.gui.screen.Screen
import net.minecraft.component.DataComponentTypes
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.helpers.Items.getItemEnchants
import org.nextrg.skylens.helpers.Items.getItemType
import org.nextrg.skylens.helpers.Items.getTooltipMiddle
import org.nextrg.skylens.helpers.Other.errorMessage
import org.nextrg.skylens.helpers.Other.jsonNeu
import org.nextrg.skylens.helpers.Strings.codeFromName
import org.nextrg.skylens.helpers.Strings.getFormatCode
import org.nextrg.skylens.helpers.Strings.titleCase
import org.nextrg.skylens.helpers.Variables.colorToARGB
import java.awt.Color
import kotlin.math.max

object MissingEnchants {
    private var enchants: JsonObject? = null

    private fun getEnchantJson() {
        enchants = jsonNeu("/refs/heads/master/constants/enchants.json")
    }

    private fun getMissingEnchants(
        itemType: String,
        itemEnchants: MutableList<String>
    ): Pair<MutableList<String>, MutableList<String>> {
        val missingEnchants = mutableListOf<String>()
        val ultimateEnchants = mutableListOf<String>()
        val neuEnchants: JsonObject = enchants!!["enchants"].asJsonObject
        val neuEnchantPools: JsonArray = enchants!!["enchant_pools"].asJsonArray

        try {
            if (neuEnchants.get(itemType) != null) {
                for (encElement in neuEnchants.get(itemType).getAsJsonArray()) {
                    val enc: String = encElement.asString.lowercase()
                    var conflictFound = false
                    for (conflictGroup in neuEnchantPools) {
                        if (conflictGroup.toString().lowercase().contains(enc.lowercase())) {
                            val array: JsonArray? = conflictGroup.getAsJsonArray()
                            if (array != null) {
                                for (i in 0..<array.size()) {
                                    if (itemEnchants.contains(array.get(i).asString.lowercase())) {
                                        conflictFound = true
                                        break
                                    }
                                }
                            }
                        }
                        if (conflictFound) break
                    }
                    if (!conflictFound && !itemEnchants.contains(enc.lowercase())
                        && !itemEnchants.contains("one_for_all")
                    ) {
                        var result = enc.replace("_".toRegex(), " ")
                        result = titleCase(result.replace("pristine", "prismatic"))
                        if (!enc.contains("ultimate")) {
                            missingEnchants.add(result)
                        } else {
                            ultimateEnchants.add(result)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage("Missing Enchants", e)
        }

        return Pair(missingEnchants, ultimateEnchants)
    }

    private fun display(index: Int, list: List<String>, lines: MutableList<Text>) {
        if (index == 1) return

        val targetIndex = index + 2
        val symbol = if (Screen.hasShiftDown()) "✦" else "✧"
        val color = colorToARGB(
            if (Screen.hasShiftDown())
                Color(85, 255, 255) else Color(0, 170, 170)
        )

        lines.add(targetIndex, Text.literal(" "))

        if (Screen.hasShiftDown()) {
            val displayList: MutableList<Text> = ArrayList()
            var i = list.size - 1
            while (i >= 0) {
                val sb = StringBuilder()
                for (j in i downTo max(0.0, (i - 2).toDouble()).toInt()) {
                    sb.append(list[j])
                    if (j != 0) {
                        sb.append(codeFromName("gray")).append(getFormatCode("reset")).append(", ")
                    }
                }
                displayList.add(Text.literal("⋗ " + sb.toString().trim { it <= ' ' }).formatted(Formatting.GRAY))
                i -= 3
            }
            lines.addAll(targetIndex, displayList)
        } else {
            lines.add(targetIndex, Text.literal("⋗ Press [SHIFT] to see").formatted(Formatting.GRAY))
        }

        lines.add(targetIndex, Text.literal("$symbol Missing enchantments:").withColor(color))
    }

    private fun main(stack: ItemStack, lines: MutableList<Text>) {
        if (!ModConfig.missingEnchants || enchants == null) return

        val gauntlet = stack.customName.toString().contains("Gemstone Gauntlet")
        val itemType = if (gauntlet) "gauntlet" else getItemType(lines).replace("dungeon ", "")
        val data = stack.components.get(DataComponentTypes.CUSTOM_DATA)

        if (itemType != "other" && data != null) {
            val itemEnchants = getItemEnchants(data)
            val (missingEnchants, ultimateEnchants) = getMissingEnchants(itemType.uppercase(), itemEnchants)

            if (missingEnchants.isNotEmpty() && itemEnchants.isNotEmpty()) {
                if (ultimateEnchants.isNotEmpty()) {
                    missingEnchants.add(getFormatCode("bold") + "Any Ultimate")
                }
                display(getTooltipMiddle(lines, itemEnchants), missingEnchants, lines)
            }
        }
    }

    fun prepare() {
        getEnchantJson()
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            main(stack, lines)
        }
    }
}