package org.nextrg.skylens.features

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.ChatFormatting
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.helpers.ItemsUtil.getItemEnchants
import org.nextrg.skylens.helpers.ItemsUtil.getItemType
import org.nextrg.skylens.helpers.ItemsUtil.getTooltipMiddle
import org.nextrg.skylens.helpers.OtherUtil.errorMessage
import org.nextrg.skylens.helpers.OtherUtil.isShiftDown
import org.nextrg.skylens.helpers.OtherUtil.jsonNeu
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.StringsUtil.getFormatCode
import org.nextrg.skylens.helpers.StringsUtil.nameToColorCode
import org.nextrg.skylens.helpers.StringsUtil.titleCase
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
        val enchantData = enchants ?: return Pair(mutableListOf(), mutableListOf())
        val neuEnchants = enchantData["enchants"].asJsonObject
        val neuEnchantPools: JsonArray = enchants!!["enchant_pools"].asJsonArray

        try {
            if (neuEnchants.get(itemType) != null) {
                for (encElement in neuEnchants.get(itemType).getAsJsonArray()) {
                    val enc: String = encElement.asString.lowercase()
                    var conflictFound = false
                    for (conflictGroup in neuEnchantPools) {
                        val array = conflictGroup.asJsonArray
                        if (array.any { it.asString.lowercase() == enc }) {
                            if (array.any { itemEnchants.contains(it.asString.lowercase()) }) {
                                conflictFound = true
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
            errorMessage("Failed to get missing enchants list", e)
        }

        return Pair(missingEnchants, ultimateEnchants)
    }

    private fun display(index: Int, list: List<String>, lines: MutableList<Component>) {
        if (index == 1) return
        val hasShift = isShiftDown()

        val targetIndex = index + 2

        val symbol = if (hasShift) "✦" else "✧"
        val color = (if (hasShift)
                Color(85, 255, 255) else Color(0, 170, 170)).rgb

        lines.add(targetIndex, Component.literal(" "))

        if (hasShift) {
            val displayList: MutableList<Component> = ArrayList()
            var i = list.size - 1
            while (i >= 0) {
                val group = buildString {
                    for (j in i downTo max(0, i - 2)) {
                        append(list[j])
                        if (j != max(0, i - 2)) {
                            append(nameToColorCode("gray"))
                            append(getFormatCode("reset"))
                            append(", ")
                        }
                    }
                }
                displayList.add(Component.literal("⋗ ${group.trim()}").withStyle(ChatFormatting.GRAY))
                i -= 3
            }
            lines.addAll(targetIndex, displayList)
        } else {
            lines.add(targetIndex, Component.literal("⋗ Press [SHIFT] to see").withStyle(ChatFormatting.GRAY))
        }

        lines.add(targetIndex, Component.literal("$symbol Missing enchantments:").withColor(color))
    }

    private fun main(stack: ItemStack, lines: MutableList<Component>) {
        if (!ModConfig.missingEnchants || !onSkyblock() || enchants == null) return

        try {
            val gauntlet = stack.customName.toString().contains("Gemstone Gauntlet")
            val itemType = if (gauntlet) "gauntlet" else getItemType(lines).replace("dungeon ", "")
            val data = stack.components.get(DataComponents.CUSTOM_DATA)

            if (itemType != "other" && data !== null) {
                val itemEnchants = getItemEnchants(data)
                val (missingEnchants, ultimateEnchants) = getMissingEnchants(itemType.uppercase(), itemEnchants)

                if (missingEnchants.isNotEmpty() && itemEnchants.isNotEmpty()) {
                    if (ultimateEnchants.isNotEmpty()) {
                        missingEnchants.add(getFormatCode("bold") + "Any Ultimate")
                    }
                    display(getTooltipMiddle(lines, itemEnchants), missingEnchants, lines)
                }
            }
        } catch (e: Exception) {
            errorMessage("Failed to show missing enchants on an item", e)
        }
    }

    fun prepare() {
        getEnchantJson()
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            main(stack, lines)
        }
    }
}
