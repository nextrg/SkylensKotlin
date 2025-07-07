package org.nextrg.skylens.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;
import org.nextrg.skylens.ModConfig;
import org.nextrg.skylens.api.PlayerStats;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

@Mixin(value = MessageHandler.class, priority = 100)
public abstract class MessageHandlerMixin {
    @Shadow @Final private MinecraftClient client;
    
    @Inject(order = 500, method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessage(Text text, boolean isActionBar, CallbackInfo callbackInfo) {
        if (isActionBar) {
            PlayerStats.INSTANCE.readActionBar(text);
            var hidePressure = ModConfig.hidePressure;
            if (hidePressure) {
                var string = text.getString();
                var array = new ArrayList<>(Arrays.asList(string.split(" {5}")));
                if (array.get(1).contains("Pressure")) {
                    array.remove(1);
                }
                var display = array.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining("     "));
                text = Text.literal(display);
                callbackInfo.cancel();
                client.inGameHud.setOverlayMessage(text, false);
            }
        }
    }
}
