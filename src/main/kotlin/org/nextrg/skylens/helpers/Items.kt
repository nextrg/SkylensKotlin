package org.nextrg.skylens.helpers

import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import kotlin.math.abs

object Items {
    private val itemRarities = arrayOf(
        "COMMON",
        "UNCOMMON",
        "RARE",
        "EPIC",
        "LEGENDARY",
        "MYTHIC",
        "DIVINE",
        "SPECIAL",
        "VERY SPECIAL",
        "ULTIMATE",
        "ADMIN"
    )

    fun getItemEnchants(data: NbtComponent): MutableList<String> {
        val itemEnchants: MutableList<String> = ArrayList(emptyList())
        data.copyNbt().getCompound("enchantments").map { obj: NbtCompound -> obj.keys }
            .orElse(emptySet()).forEach { e -> itemEnchants.add(e.lowercase()) }
        return itemEnchants
    }

    private fun hasRarity(string: String): Boolean {
        return itemRarities.any { rarity -> string.contains(rarity) }
    }

    fun getItemType(lines: MutableList<Text>): String {
        var type = "other"
        for (line in lines) {
            if (hasRarity(line.toString())) {
                for (sibling in line.siblings) {
                    val string = sibling.string
                    if (hasRarity(string) && string.contains(" ")) {
                        type = string.substring(string.indexOf(" ") + 1).lowercase()
                    }
                }
            }
        }
        return type
    }

    fun tooltipFromItemStack(stack: ItemStack): List<Text> {
        return stack.getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).styledLines()
    }

    fun getTooltipMiddle(lines: MutableList<Text>, itemEnch: MutableList<String>): Int {
        var index = 1
        lines.forEachIndexed { i, line ->
            if (itemEnch.any { enchant ->
                    line.toString().lowercase().contains(enchant.replace("ultimate_", "").replace("_", " "))
                }) {
                index = index.coerceAtLeast(i)
            }
        }
        itemEnch.remove("telekinesis")
        if (itemEnch.size <= 3) {
            for (i in index until index + 3) {
                if (lines[i].content.toString() == "empty") {
                    val offset = if (itemEnch.size >= 2) 1 else 0
                    index += abs(index - i) - offset
                }
            }
        }
        if (itemEnch.size == 4) {
            index += 1
        }
        if (itemEnch.size == 5) {
            index += 2
        }
        return index
    }
}