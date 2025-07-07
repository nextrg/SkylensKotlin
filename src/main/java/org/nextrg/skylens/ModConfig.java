package org.nextrg.skylens;

import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.nextrg.skylens.features.HudEditor;
import org.nextrg.skylens.helpers.StringsUtil;
import org.nextrg.skylens.helpers.VariablesUtil;

import java.awt.*;
import java.time.LocalDate;
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
    
    public enum Type implements NameableEnum {
        Bar,
        Circular,
        CircularALT;
        
        @Override
        public Text getDisplayName() {
            return Text.literal(name()
                    .replace("ALT", " (alt)"));
        }
    }
    
    public enum Theme implements NameableEnum {
        Pet,
        Custom,
        Special,
        Divine,
        Mythic,
        Legendary,
        Epic,
        Rare,
        Uncommon,
        Common;
        
        @Override
        public Text getDisplayName() {
            return Text.literal(StringsUtil.INSTANCE.codeFromName(name().toLowerCase()) + name()
                    .replace("Pet", "Pet Rarity"));
        }
    }
    
    private static Option<Boolean> createBooleanEnableOption(Boolean value, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Text.literal("Enable"))
                .binding(value, getter, setter)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                        .coloured(true))
                .build();
    }
    
    private static Option<Boolean> createBooleanOption(Boolean value, String name, String description, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Text.literal(name))
                .description(OptionDescription.of(Text.literal(description)))
                .binding(value, getter, setter)
                .controller(opt -> BooleanControllerBuilder.create(opt)
                        .formatValue(val -> val ? Text.literal("Yes") : Text.literal("No"))
                        .coloured(true))
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
    
    @SerialEntry
    public static boolean missingEnchants = true;
    @SerialEntry
    public static boolean compactPetLevel = true;
    @SerialEntry
    public static boolean hidePressure = false;
    @SerialEntry
    public static boolean onlySkyblock = true;
    
    @SerialEntry
    public static boolean pressureDisplay = true;
    @SerialEntry
    public static Anchor pressureDisplayAnchor = Anchor.BottomMiddle;
    @SerialEntry
    public static int pressureDisplayX = 0;
    @SerialEntry
    public static int pressureDisplayY = 0;
    
    @SerialEntry
    public static boolean petOverlay = true;
    @SerialEntry
    public static Anchor petOverlayAnchor = Anchor.BottomMiddle;
    @SerialEntry
    public static int petOverlayX = 0;
    @SerialEntry
    public static int petOverlayY = 0;
    @SerialEntry
    public static Type petOverlayType = Type.Bar;
    @SerialEntry
    public static Theme petOverlayTheme = Theme.Pet;
    @SerialEntry
    public static boolean petOverlayInvert = false;
    @SerialEntry
    public static boolean petOverlayFlip = false;
    @SerialEntry
    public static boolean petOverlayShowItem = false;
    @SerialEntry
    public static Color petOverlayColor1 = Color.WHITE;
    @SerialEntry
    public static Color petOverlayColor2 = Color.GRAY;
    @SerialEntry
    public static Color petOverlayColor3 = Color.DARK_GRAY;
    @SerialEntry
    public static boolean petOverlayAnimation_Idle = true;
    @SerialEntry
    public static boolean petOverlayAnimation_LevelUp = true;
    @SerialEntry
    public static boolean petOverlayAnimation_LevelXp = true;
    
    @SerialEntry
    public static boolean lowHpIndicator = true;
    @SerialEntry
    public static boolean lowHpIndicatorHeartbeat = true;
    @SerialEntry
    public static float lowHpIndicatorTransparency = 0.4f;
    
    public static OptionGroup petOverlayGroup() {
        return OptionGroup.createBuilder()
                .name(Text.literal("Pet Overlay"))
                .description(OptionDescription.of(Text.literal("Displays the progress to max level and next level of current pet.")))
                .collapsed(true)
                .option(createBooleanEnableOption(petOverlay, () -> petOverlay, newValue -> petOverlay = newValue))
                
                .option(label("Styling"))
                .option(Option.<Type>createBuilder()
                        .name(Text.literal("Type"))
                        .binding(
                                Type.Bar,
                                () -> petOverlayType,
                                newVal -> petOverlayType = newVal
                        )
                        .controller(opt -> EnumControllerBuilder.create(opt)
                                .enumClass(Type.class))
                        .build())
                .option(createBooleanOption(petOverlayShowItem, "Show Pet Item", "Renders item directly above pet's icon.", () -> petOverlayShowItem, newValue -> petOverlayShowItem = newValue))
                .option(createBooleanOption(petOverlayInvert, "Invert Level/XP Color", "", () -> petOverlayInvert, newValue -> petOverlayInvert = newValue))
                .option(createBooleanOption(petOverlayFlip, "Flip Icon Position", "Available only using the bar style.", () -> petOverlayFlip, newValue -> petOverlayFlip = newValue))
                
                .option(label("Position"))
                .option(Option.<Anchor>createBuilder()
                        .name(Text.literal("Anchor"))
                        .description(OptionDescription.of(Text.literal("Sets the anchor of the overlay to given positions.")))
                        .binding(Anchor.BottomMiddle, () -> petOverlayAnchor, newValue -> petOverlayAnchor = newValue)
                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Anchor.class))
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(Text.literal("Open HUD Editor"))
                        .text(Text.literal("→"))
                        .action((yaclScreen, thisOption) -> HudEditor.Companion.openScreen(MinecraftClient.getInstance().currentScreen, "Pet Overlay"))
                        .build())
                
                .option(label("Themes"))
                .option(Option.<Theme>createBuilder()
                        .name(Text.literal("Theme"))
                        .binding(Theme.Pet, () -> petOverlayTheme, newVal -> petOverlayTheme = newVal)
                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Theme.class))
                        .build())
                .option(createColorOption(Color.WHITE, "Level", () -> petOverlayColor1, newValue -> petOverlayColor1 = newValue))
                .option(createColorOption(Color.GRAY, "XP", () -> petOverlayColor2, newValue -> petOverlayColor2 = newValue))
                .option(createColorOption(Color.DARK_GRAY, "Background", () -> petOverlayColor3, newValue -> petOverlayColor3 = newValue))
                
                .option(label("Animations"))
                .option(createBooleanOption(petOverlayAnimation_Idle, "Idle", "", () -> petOverlayAnimation_Idle, newValue -> petOverlayAnimation_Idle = newValue))
                .option(createBooleanOption(petOverlayAnimation_LevelUp, "Level Up", "", () -> petOverlayAnimation_LevelUp, newValue -> petOverlayAnimation_LevelUp = newValue))
                .option(createBooleanOption(petOverlayAnimation_LevelXp, "Level/XP Change", "", () -> petOverlayAnimation_LevelXp, newValue -> petOverlayAnimation_LevelXp = newValue))
                .build();
    }
    
    public static OptionGroup pressureDisplayGroup() {
        return OptionGroup.createBuilder()
                .name(Text.literal("Pressure Display"))
                .description(OptionDescription.of(Text.literal("Displays the pressure percentage caused by waters in Galatea.")))
                .collapsed(true)
                .option(createBooleanEnableOption(pressureDisplay, () -> pressureDisplay, newValue -> pressureDisplay = newValue))
                
                .option(label("Position"))
                .option(Option.<Anchor>createBuilder()
                        .name(Text.literal("Anchor"))
                        .description(OptionDescription.of(Text.literal("Sets the anchor of the overlay to given positions.")))
                        .binding(Anchor.BottomMiddle, () -> pressureDisplayAnchor, newValue -> pressureDisplayAnchor = newValue)
                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Anchor.class))
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(Text.literal("Open HUD Editor"))
                        .text(Text.literal("→"))
                        .action((yaclScreen, thisOption) -> HudEditor.Companion.openScreen(MinecraftClient.getInstance().currentScreen, "Pressure Display"))
                        .build())
                .build();
    }
    
    public static OptionGroup lowHpIndicatorGroup() {
        return OptionGroup.createBuilder()
                .name(Text.literal("Low HP Indicator"))
                .description(OptionDescription.of(Text.literal("Reddens the screen the lower player's HP is.")))
                .collapsed(true)
                .option(createBooleanEnableOption(lowHpIndicator, () -> lowHpIndicator, newValue -> lowHpIndicator = newValue))
                .option(Option.<Float>createBuilder()
                        .name(Text.literal("Transparency"))
                        .binding(0.4f, () -> lowHpIndicatorTransparency, newVal -> lowHpIndicatorTransparency = newVal)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.2f, 1f)
                                .step(0.01f)
                                .formatValue(val -> {
                                    float red = Math.max(0f, Math.min(1f, (val - 0.71f) / (1f - 0.71f)));
                                    Color color = new Color(255, (int) Math.clamp(255 - red * 115, 0, 255), (int) Math.clamp(255 - red * 115, 0, 255));
                                    return Text.literal(Math.floor(val * 100) + "%").withColor(VariablesUtil.INSTANCE.colorToARGB(color));
                                }))
                        .build())
                .option(createBooleanOption(lowHpIndicatorHeartbeat, "Pulse Animation", "", () -> lowHpIndicatorHeartbeat, newValue -> lowHpIndicatorHeartbeat = newValue))
                .build();
    }
    
    public static OptionGroup otherFeaturesGroup() {
        var randomLevel = Math.round(15 + Math.random() * 75);
        return OptionGroup.createBuilder()
                .name(Text.literal("Other"))
                .collapsed(false)
                .option(createBooleanOption(missingEnchants, "Show Missing Enchants",
                        "Displays a list of missing enchants the hovered item has.",
                        () -> missingEnchants, newValue -> missingEnchants = newValue))
                .option(createBooleanOption(compactPetLevel, "Compact Pet Level", "Shortens pet level display on tooltip.\nExamples:\n§7[Lvl " +
                                randomLevel + "] §6Pet §f→ §8[§7" +
                                randomLevel + "§8] §6Pet\n§7[Lvl 100] §6Pet §f→ §8[§6100§8] §6Pet",
                        () -> compactPetLevel, newValue -> compactPetLevel = newValue))
                .option(createBooleanOption(false, "Hide Pressure in Action Bar", "", () -> hidePressure, newValue -> hidePressure = newValue))
                .build();
    }
    
    public Screen config(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Skylens"))
                .category(
                        ConfigCategory.createBuilder()
                                .name(title())
                                .option(createBooleanOption(onlySkyblock, "Only in Skyblock", "", () -> onlySkyblock, newValue -> onlySkyblock = newValue))
                                .group(petOverlayGroup())
                                .group(pressureDisplayGroup())
                                .group(lowHpIndicatorGroup())
                                .group(otherFeaturesGroup())
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