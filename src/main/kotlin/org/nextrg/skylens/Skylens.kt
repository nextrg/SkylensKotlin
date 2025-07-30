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
import org.nextrg.skylens.api.Pets
import org.nextrg.skylens.api.PlayerStats
import org.nextrg.skylens.features.*


class Skylens : ClientModInitializer {
    override fun onInitializeClient() {
        getModContainer()
        ModConfig().init()
        registerCommands()

        // APIs
        Pets.init()
        PlayerStats.init()

        MissingEnchants.prepare()
        PetOverlay.prepare()
        PressureDisplay.prepare()
        DrillFuelBar.prepare()
        CompactPetLevel.prepare()
        LowHpIndicator.prepare()
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
                                    HudEditor.openScreen(null, "")
                                    1
                                }
                                .then(
                                    ClientCommandManager.literal("pet_overlay")
                                        .executes {
                                            HudEditor.openScreen(null, "Pet Overlay")
                                            1
                                        }
                                )
                                .then(
                                    ClientCommandManager.literal("pressure_display")
                                        .executes {
                                            HudEditor.openScreen(null, "Pressure Display")
                                            1
                                        }
                                )
                                .then(
                                    ClientCommandManager.literal("drill_fuel_bar")
                                        .executes {
                                            HudEditor.openScreen(null, "Drill Fuel Bar")
                                            1
                                        }
                                )
                        )
                )
            })
        }
    }
}