package org.nextrg.skylens

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.ModContainer
import net.minecraft.util.Identifier
import org.nextrg.skylens.features.MissingEnchants
import org.nextrg.skylens.features.PetOverlay

class Skylens : ClientModInitializer {
    override fun onInitializeClient() {
        getModContainer()
        MissingEnchants.init()
        PetOverlay.init()
    }

    companion object {
        private var mod: ModContainer? = null

        fun getModContainer() {
            mod = FabricLoader.getInstance().getModContainer("skylens").get();
        }

        fun id(path: String?): Identifier {
            return Identifier.of(mod?.metadata?.id ?: "", path)
        }
    }
}