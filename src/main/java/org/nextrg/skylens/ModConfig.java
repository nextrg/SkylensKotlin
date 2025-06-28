package org.nextrg.skylens;

import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModConfig implements ModMenuApi {
    public static ConfigClassHandler<ModConfig> HANDLER = ConfigClassHandler.createBuilder(ModConfig.class)
            .id(Identifier.of("skylens"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("skylens.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();
    
    private static Option<Boolean> createBooleanOption(Boolean value, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Enable"))
                .binding(value, getter, setter)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                        .coloured(true))
                .build();
    }
    
    private static Option<Integer> createPositionOption(int variable, String type, Supplier<Integer> getter, Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(Text.literal(type + " Position"))
                .description(OptionDescription.of(Text.literal("Offsets from the " + type + " anchor. Automatically adjusts when positioned off-screen.")))
                .binding(variable, getter, setter)
                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                        .range(-500, 500)
                        .step(1)
                        .formatValue(val -> Text.literal(val + "px")))
                .build();
    }
    
    private static Option<Color> createColorOption(Color color, String type, Supplier<Color> getter, Consumer<Color> setter) {
        return Option.<Color>createBuilder()
                .name(Text.literal(type + " Color"))
                .binding(color, getter, setter)
                .controller(opt -> ColorControllerBuilder.create(opt)
                        .allowAlpha(true))
                .build();
    }
    
    public static Text title() {
        var aprilFools = LocalDate.now().getMonthValue() == 4 && LocalDate.now().getDayOfMonth() == 1;
        return Text.literal((aprilFools ? "Skibidi" : "Sky") + "lens");
    }
    
    public static LabelOption label(String string) {
        return LabelOption.create(Text.literal(string));
    }
    
    public enum Anchor implements NameableEnum {
        TopLeft,
        MiddleLeft,
        BottomLeft,
        TopRight,
        MiddleRight,
        BottomRight,
        TopMiddle,
        BottomMiddle;
        
        @Override
        public Text getDisplayName() {
            return Text.literal(name()
                    .replace("Left", " Left")
                    .replace("Right", " Right")
                    .replace("Middle", " Middle"));
        }
    }
    
    @SerialEntry
    public static boolean petOverlay = true;
    @SerialEntry
    public static Anchor petOverlayAnchor = Anchor.BottomMiddle;
    @SerialEntry
    public static int petOverlayX = 0;
    @SerialEntry
    public static int petOverlayY = 0;
    @SerialEntry
    public static Color petOverlayColor1 = Color.WHITE;
    @SerialEntry
    public static Color petOverlayColor2 = Color.GRAY;
    @SerialEntry
    public static Color petOverlayColor3 = Color.DARK_GRAY;
    
    @SerialEntry
    public static boolean missingEnchants = true;
    
    public Screen config(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Skylens"))
                .category(
                        ConfigCategory.createBuilder()
                                .name(title())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.literal("Pet Overlay"))
                                        .description(OptionDescription.of(Text.literal("Displays the progress to max level and next level of current pet.")))
                                        .collapsed(true)
                                        .option(createBooleanOption(petOverlay, () -> petOverlay, newValue -> petOverlay = newValue))
                                        
                                        .option(label("Position"))
                                        .option(Option.<Anchor>createBuilder()
                                                .name(Text.literal("Anchor"))
                                                .description(OptionDescription.of(Text.literal("Sets the anchor of the overlay to given positions.")))
                                                .binding(
                                                        Anchor.BottomMiddle,
                                                        () -> petOverlayAnchor,
                                                        newVal -> petOverlayAnchor = newVal
                                                )
                                                .controller(opt -> EnumControllerBuilder.create(opt)
                                                        .enumClass(Anchor.class))
                                                .build())
                                        .option(createPositionOption(121, "X", () -> petOverlayX, newValue -> petOverlayX = newValue))
                                        .option(createPositionOption(-3, "Y", () -> petOverlayY, newValue -> petOverlayY = newValue))
                                        
                                        .option(label("Themes"))
                                        .option(createColorOption(Color.WHITE, "Level", () -> petOverlayColor1, newValue -> petOverlayColor1 = newValue))
                                        .option(createColorOption(Color.GRAY, "XP", () -> petOverlayColor2, newValue -> petOverlayColor2 = newValue))
                                        .option(createColorOption(Color.DARK_GRAY, "Background", () -> petOverlayColor3, newValue -> petOverlayColor3 = newValue))
                                        
                                        .build())
                                .group(OptionGroup.createBuilder()
                                        .name(Text.literal("Missing Enchants"))
                                        .description(OptionDescription.of(Text.literal("Shows a list of missing enchantments on items.")))
                                        .collapsed(true)
                                        .option(createBooleanOption(missingEnchants, () -> missingEnchants, newValue -> missingEnchants = newValue))
                                        .build())
                                .build())
                .save(this::update)
                .build()
                .generateScreen(parent);
    }
    
    public void update() {
        ModConfig.HANDLER.save();
    }
    
    public static ModConfig get() {
        return HANDLER.instance();
    }
    
    public static void openConfig() {
        final boolean[] open = {false};
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!open[0]) {
                open[0] = true;
                client.setScreen(new ModConfig().config(null));
            }
        });
    }
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::config;
    }
    
    public void init() {
        ModConfig.HANDLER.load();
    }
}