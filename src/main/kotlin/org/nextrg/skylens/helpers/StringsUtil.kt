package org.nextrg.skylens.helpers

import net.minecraft.text.Text

object StringsUtil {
    fun codeFromName(input: String): String {
        return when (input.replace("_".toRegex(), "")) {
            "darkblue" -> "§1"
            "darkgreen" -> "§2"
            "darkaqua" -> "§3"
            "darkred" -> "§4"
            "darkpurple", "epic" -> "§5"
            "gold", "legendary" -> "§6"
            "gray" -> "§7"
            "darkgray" -> "§8"
            "blue", "rare" -> "§9"
            "green", "uncommon" -> "§a"
            "aqua", "divine" -> "§b"
            "red", "special" -> "§c"
            "lightpurple", "mythic" -> "§d"
            "yellow" -> "§e"
            else -> "§f"
        }
    }

    fun colorFromCode(code: String): List<String> {
        return when (code) {
            "§1" -> listOf("dark_blue")
            "§2" -> listOf("dark_green")
            "§3" -> listOf("dark_aqua")
            "§4" -> listOf("dark_red")
            "§5" -> listOf("dark_purple", "epic")
            "§6" -> listOf("gold", "legendary")
            "§7" -> listOf("gray")
            "§8" -> listOf("dark_gray")
            "§9" -> listOf("blue", "rare")
            "§a" -> listOf("green", "uncommon")
            "§b" -> listOf("aqua", "divine")
            "§c" -> listOf("red", "special")
            "§d" -> listOf("light_purple", "mythic")
            "§e" -> listOf("yellow")
            else -> listOf("white")
        }
    }

    fun colorToRarity(input: String): String {
        return when (input) {
            "light_purple" -> "mythic"
            "gold" -> "legendary"
            "dark_purple" -> "epic"
            "blue" -> "rare"
            "green" -> "uncommon"
            else -> "common"
        }
    }

    fun getFormatCode(input: String): String {
        return when (input) {
            "obfuscated" -> "§k"
            "bold" -> "§l"
            "strikethrough" -> "§m"
            "underline" -> "§n"
            "italic" -> "§o"
            "reset" -> "§r"
            else -> ""
        }
    }

    fun titleCase(text: String): String {
        return text
            .split(" ")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

    fun textToString(text: Text): String {
        var name = ""
        for (sibling in text.siblings) {
            name += codeFromName(sibling.style.color.toString()) + sibling.string
        }
        return name
    }

    fun parseSuffix(input: String): Int {
        val lower = input.lowercase()
        return when {
            lower.endsWith("k") -> {
                val number = lower.removeSuffix("k").toFloatOrNull() ?: return 0
                (number * 1_000).toInt()
            }
            lower.endsWith("m") -> {
                val number = lower.removeSuffix("m").toFloatOrNull() ?: return 0
                (number * 1_000_000).toInt()
            }
            else -> input.toIntOrNull() ?: 0
        }
    }

    fun formatSuffix(value: Int): String {
        return when {
            value >= 1_000_000 -> {
                val formatted = (value / 1_000_000f).let {
                    if (it % 1 == 0f) it.toInt().toString() else String.format("%.1f", it).replace(",", ".")
                }
                "${formatted}m"
            }
            value >= 1_000 -> {
                val formatted = (value / 1_000f).let {
                    if (it % 1 == 0f) it.toInt().toString() else String.format("%.1f", it).replace(",", ".")
                }
                "${formatted}k"
            }
            else -> value.toString()
        }
    }
}