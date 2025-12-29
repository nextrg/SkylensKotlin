package org.nextrg.skylens.features

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.network.chat.Component
import org.nextrg.skylens.ModConfig
import org.nextrg.skylens.api.Pets.isGoldenDragon
import org.nextrg.skylens.helpers.OtherUtil.onSkyblock
import org.nextrg.skylens.helpers.StringsUtil.textToString
import java.util.regex.Pattern


object CompactPetLevel {
    private val PATTERN = Pattern.compile("""\[Lvl (\d+)]\s*(§[0-9a-fk-or])""")
    fun prepare() {
        ItemTooltipCallback.EVENT.register { stack, _, _, lines ->
            main(stack, lines)
        }
    }

    private fun main(stack: ItemStack, lines: MutableList<Component>) {
        if (!ModConfig.compactPetLevel || !onSkyblock()) return

        val itemName = stack.customName?.string ?: return
        if (stack.item != Items.PLAYER_HEAD || !itemName.contains("[Lvl ") || stack.hoverName.siblings.size <= 1) return

        val original = textToString(stack.customName!!)
        var level = 1; var rarity = "§7"
        if (!original.contains("[Lvl 1 → 100]")) {
            val match = PATTERN.matcher(original).takeIf { it.find() } ?: return
            level = match.group(1).toInt()
            if ((isGoldenDragon(itemName) && level == 200) || level == 100) {
                rarity = match.group(2)
            }
        }

        val displayText = original
            .replace("[Lvl 1 → 100]", "§8[$rarity"+"1 §8→$rarity 100§8]")
            .replace("[Lvl $level]", "§8[$rarity$level§8]")
            .replace("Lvl ", "")

        lines.removeFirst()
        lines.addFirst(Component.literal(displayText))
    }
}
