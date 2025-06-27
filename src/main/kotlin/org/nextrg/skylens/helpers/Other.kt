package org.nextrg.skylens.helpers

import kotlinx.coroutines.delay
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.text.Text
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

object Other {
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

    fun colorToARGB(color: Color): Int {
        return (color.alpha shl 24) or (color.red shl 16) or (color.green shl 8) or color.blue
    }

    fun hexTransparent(hex: Int, alpha: Int): Int {
        return (alpha shl 24) or (hex and 0x00FFFFFF)
    }

    fun hexOpaque(hexa: Int): Int {
        return (0xFF shl 24) or (hexa and 0x00FFFFFF)
    }

    fun sToMs(seconds: Float): Long {
        return (seconds * 1000).toLong()
    }

    fun errorMessage(message: String, exception: Exception) {
        System.err.println("[Skylens] $message error:\n$exception")
    }

    fun animateFloat(
        start: Float,
        end: Float,
        durationMs: Long,
        easing: (Float) -> Float = ::linear
    ): Flow<Float> = flow {
        val startTime = System.currentTimeMillis()
        var fraction: Float
        do {
            val now = System.currentTimeMillis()
            val elapsed = now - startTime
            fraction = (elapsed.toFloat() / durationMs).coerceIn(0f, 1f)

            val easedFraction = easing(fraction)

            val value = start + (end - start) * easedFraction
            emit(value)
            delay(10)
        } while (fraction < 1f)
    }

    fun linear(t: Float) = t

    fun quad(t: Float) =
        if (t < 0.5f) 2 * t * t else -1 + (4 - 2 * t) * t

}