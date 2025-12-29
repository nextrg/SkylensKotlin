package org.nextrg.skylens.helpers

import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.ItemLore
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component

object ItemsUtil {
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

    fun getItemEnchants(data: CustomData): MutableList<String> {
        val itemEnchants: MutableList<String> = ArrayList(emptyList())
        data.copyTag().getCompound("enchantments").map { obj: CompoundTag -> obj.keySet() }
            .orElse(emptySet()).forEach { e -> itemEnchants.add(e.lowercase()) }
        return itemEnchants
    }

    private fun hasRarity(string: String): Boolean {
        return itemRarities.any { rarity -> string.contains(rarity) }
    }

    fun getItemType(lines: MutableList<Component>): String {
        var type = "other"
        for (line in lines) {
            if (hasRarity(line.toString())) {
                for (sibling in line.siblings) {
                    val string = sibling.string
                    if (hasRarity(string) && string.contains(" ")) {
                        type = string.substring(string.indexOf(" ") + 1).lowercase().trim()
                    }
                }
            }
        }
        return type
    }

    fun tooltipFromItemStack(stack: ItemStack): List<Component> {
        return stack.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).styledLines()
    }

    fun getTooltipMiddle(lines: MutableList<Component>, itemEnch: MutableList<String>): Int {
        var index = 1
        lines.forEachIndexed { i, line ->
            val lineLowercase = line.toString().lowercase()
            if (itemEnch.any { enchant -> lineLowercase.contains(enchant
                .replace("ultimate_", "")
                .replace("turbo_", "turbo-")
                .replace("_", " "))
            }) {
                index = index.coerceAtLeast(i)
            }
        }
        itemEnch.remove("telekinesis")
        for (offset in 0..4) {
            val offseted = index + offset
            if (lines[offseted].string.isEmpty()) {
                index = offseted - 1
                break
            }
        }
        return index
    }
}
