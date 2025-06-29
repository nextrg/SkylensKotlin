package org.nextrg.skylens

import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.util.Identifier
import org.nextrg.skylens.ModConfig.openConfig
import org.nextrg.skylens.features.CompactPetLevel
import org.nextrg.skylens.features.HudEditor
import org.nextrg.skylens.features.MissingEnchants
import org.nextrg.skylens.features.PetOverlay


class Skylens : ClientModInitializer {
    override fun onInitializeClient() {
        getModContainer()
        ModConfig().init()
        MissingEnchants.prepare()
        PetOverlay.prepare()
        CompactPetLevel.prepare()
        registerCommands()
    }

    companion object {
        private var mod: ModContainer? = null

        fun getModContainer() {
            mod = FabricLoader.getInstance().getModContainer("skylens").get()
        }

        fun id(path: String?): Identifier {
            return Identifier.of(mod?.metadata?.id ?: "", path)
        }

        fun registerCommands() {
            ClientCommandRegistrationCallback.EVENT.register(ClientCommandRegistrationCallback { dispatcher: CommandDispatcher<FabricClientCommandSource?>, registryAccess: CommandRegistryAccess? ->
                dispatcher.register(
                    ClientCommandManager.literal("skylens")
                        .executes {
                            openConfig()
                            1
                        }
                        .then(
                            ClientCommandManager.literal("hudedit")
                                .executes {
                                    HudEditor.openScreen(null)
                                    1
                                }
                        )
                )
            })
        }
    }
}