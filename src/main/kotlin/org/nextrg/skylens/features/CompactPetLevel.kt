package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.Pets.isGoldenDragon
import org.nextrg.skylens.helpers.Other.onSkyblock
import org.nextrg.skylens.helpers.Strings.textToString
import java.util.regex.Pattern


object CompactPetLevel {
    private val PATTERN = Pattern.compile("""\[Lvl (\d+)]\s*(§[0-9a-fk-or])""")
    fun prepare() {
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            main(stack, lines)
        }
    }

    private fun main(stack: ItemStack, lines: MutableList<Text>) {
        if (!ModConfig.compactPetLevel || !onSkyblock()) return

        val itemName = stack.customName?.string ?: return
        if (stack.item != Items.PLAYER_HEAD || !itemName.contains("[Lvl ") || stack.name.siblings.size <= 1) return

        val original = textToString(stack.customName!!)
        val match = PATTERN.matcher(original).takeIf { it.find() } ?: return

        val level = match.group(1).toInt()
        val rarity = if ((isGoldenDragon(itemName) && level == 200) || level == 100) match.group(2) else "§7"

        val displayText = original
            .replace("[Lvl $level]", "§8[$rarity$level§8]")
            .replace("Lvl ", "")

        lines.removeFirst()
        lines.addFirst(Text.literal(displayText))
    }
}