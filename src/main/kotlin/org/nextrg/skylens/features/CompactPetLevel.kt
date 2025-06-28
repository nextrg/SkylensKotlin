package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.text.Text
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.Pets.getPetRarity
import org.nextrg.skylens.api.Pets.isGoldenDragon
import org.nextrg.skylens.helpers.Other.onSkyblock
import org.nextrg.skylens.helpers.Strings.codeFromName


object CompactPetLevel {
    fun prepare() {
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            main(stack, lines)
        }
    }

    private fun main(stack: ItemStack, lines: MutableList<Text>) {
        if (!ModConfig.compactPetLevel || !onSkyblock()) return

        val itemName = stack.customName ?: return
        if (stack.item != Items.PLAYER_HEAD || !itemName.string.contains("[Lvl ") || stack.name.siblings.size <= 1) return

        val petRarity = codeFromName(getPetRarity(itemName.siblings[1]))
        val maxLevel = if (isGoldenDragon(itemName.string)) 200 else 100

        val hasSkinColor = runCatching {
            itemName.siblings.last().style.color.toString()
        }.getOrDefault("")

        val displayText = itemName.string
            .replace("\\[(\\d+)(✦)]".toRegex(), "§8[$petRarity$1§4$2§8]")
            .replace("[Lvl $maxLevel", "§8[$petRarity$maxLevel")
            .replace("[Lvl ", "§8[§7")
            .replace("]", "§8]§r$petRarity")
            .replace(" ✦", codeFromName(hasSkinColor) + " ✦")

        lines.removeFirst()
        lines.addFirst(Text.literal(displayText))
    }
}