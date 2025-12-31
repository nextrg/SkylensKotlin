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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.nextrg.skylens.features.*;
import org.nextrg.skylens.helpers.StringsUtil;

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
        BarALT,
        Circular,
        CircularALT;
        
        @Override
        public Text getDisplayName() {
            return Text.literal(name()
                    .replace("ALT", " (alt)"));
        }
    }
    
    public enum PetOverlayTheme implements NameableEnum {
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
            return Text.literal(StringsUtil.INSTANCE.nameToColorCode(name().toLowerCase()) + name()
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
    public static boolean hideDrillFuel = false;
    @SerialEntry
    public static boolean onlySkyblock = true;
    
    @SerialEntry
    public static boolean pressureDisplay = true;
    @SerialEntry
    public static Float pressureDisplayShowAt = 0.05f;
    @SerialEntry
    public static Anchor pressureDisplayAnchor = Anchor.BottomMiddle;
    @SerialEntry
    public static int pressureDisplayX = -81;
    @SerialEntry
    public static int pressureDisplayY = -14;
    @SerialEntry
    public static int pressureDisplayTheme = 0;
    
    @SerialEntry
    public static boolean drillFuelMeter = true;
    @SerialEntry
    public static Anchor drillFuelMeterAnchor = Anchor.BottomMiddle;
    @SerialEntry
    public static int drillFuelMeterX = -88;
    @SerialEntry
    public static int drillFuelMeterY = -32;
    @SerialEntry
    public static int drillFuelMeterTheme = 0;
    
    @SerialEntry
    public static boolean dungeonScoreMeter = true;
    @SerialEntry
    public static Anchor dungeonScoreMeterAnchor = Anchor.BottomMiddle;
    @SerialEntry
    public static int dungeonScoreMeterX = -116;
    @SerialEntry
    public static int dungeonScoreMeterY = -9;
    @SerialEntry
    public static int dungeonScoreMeterTheme = 0;
    @SerialEntry
    public static Color dungeonScoreMeterColor1 = new Color(111, 152, 150);
    @SerialEntry
    public static Color dungeonScoreMeterColor2 = new Color(166, 219, 131);
    @SerialEntry
    public static float dungeonScoreMeterGradientRotate = 0.9f;
    
    @SerialEntry
    public static boolean petOverlay = true;
    @SerialEntry
    public static Anchor petOverlayAnchor = Anchor.BottomMiddle;
    @SerialEntry
    public static int petOverlayX = 119;
    @SerialEntry
    public static int petOverlayY = -2;
    @SerialEntry
    public static Type petOverlayType = Type.Bar;
    @SerialEntry
    public static PetOverlayTheme petOverlayTheme = PetOverlayTheme.Pet;
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
    public static boolean petOverlayAnimation_IdlePulse = true;
    @SerialEntry
    public static boolean petOverlayAnimation_IdleHover = true;
    @SerialEntry
    public static boolean petOverlayAnimation_LevelUp = true;
    @SerialEntry
    public static boolean petOverlayAnimation_LevelXp = true;
    @SerialEntry
    public static boolean petOverlayRainbowLvl = false;
    @SerialEntry
    public static boolean petOverlayRainbowXp = false;
    @SerialEntry
    public static boolean petOverlayRainbowBg = false;
    
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
                .option(createBooleanOption(false, "Show Pet Item", "Renders item directly above pet's icon.", () -> petOverlayShowItem, newValue -> petOverlayShowItem = newValue))
                .option(createBooleanOption(false, "Invert Level/XP Color", "", () -> petOverlayInvert, newValue -> petOverlayInvert = newValue))
                .option(createBooleanOption(false, "Flip Icon Position", "Available only using the bar style.", () -> petOverlayFlip, newValue -> petOverlayFlip = newValue))
                
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
                .option(Option.<PetOverlayTheme>createBuilder()
                        .name(Text.literal("Theme"))
                        .binding(PetOverlayTheme.Pet, () -> petOverlayTheme, newVal -> petOverlayTheme = newVal)
                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(PetOverlayTheme.class))
                        .build())
                .option(createColorOption(Color.WHITE, "Level", () -> petOverlayColor1, newValue -> petOverlayColor1 = newValue))
                .option(createColorOption(Color.GRAY, "XP", () -> petOverlayColor2, newValue -> petOverlayColor2 = newValue))
                .option(createColorOption(Color.DARK_GRAY, "Background", () -> petOverlayColor3, newValue -> petOverlayColor3 = newValue))
                
                .option(label("Animations"))
                .option(createBooleanOption(true, "Idle: Pulse", "", () -> petOverlayAnimation_IdlePulse, newValue -> petOverlayAnimation_IdlePulse = newValue))
                .option(createBooleanOption(true, "Idle: Hover", "", () -> petOverlayAnimation_IdleHover, newValue -> petOverlayAnimation_IdleHover = newValue))
                .option(createBooleanOption(true, "Level Up", "", () -> petOverlayAnimation_LevelUp, newValue -> petOverlayAnimation_LevelUp = newValue))
                .option(createBooleanOption(true, "Level/XP Change", "", () -> petOverlayAnimation_LevelXp, newValue -> petOverlayAnimation_LevelXp = newValue))
               
                .option(label("Other"))
                .option(createBooleanOption(false, "Rainbow Level", "Overrides level color.", () -> petOverlayRainbowLvl, newValue -> petOverlayRainbowLvl = newValue))
                .option(createBooleanOption(false, "Rainbow XP", "Overrides XP color.", () -> petOverlayRainbowXp, newValue -> petOverlayRainbowXp = newValue))
                .option(createBooleanOption(false, "Rainbow Background", "Overrides background color.", () -> petOverlayRainbowBg, newValue -> petOverlayRainbowBg = newValue))
                .build();
    }
    
    public static OptionGroup pressureDisplayGroup() {
        return OptionGroup.createBuilder()
                .name(Text.literal("Pressure Display"))
                .description(OptionDescription.of(Text.literal("Displays the pressure percentage caused by waters in Galatea.")))
                .collapsed(true)
                .option(createBooleanEnableOption(pressureDisplay, () -> pressureDisplay, newValue -> pressureDisplay = newValue))
                .option(Option.<Float>createBuilder()
                        .name(Text.literal("Show at"))
                        .binding(0.05f, () -> pressureDisplayShowAt, newVal -> pressureDisplayShowAt = newVal)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0.01f, 0.99f)
                                .step(0.01f)
                                .formatValue(val -> Text.literal(String.format("❍ %d%% Pressure", Math.round(val * 100))).withColor(0xFFB5B5F4)))
                        .build())
                
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
                
                .option(label("Themes"))
                .option(Option.<Integer>createBuilder()
                        .name(Text.literal("Theme"))
                        .binding(0, () -> pressureDisplayTheme, newVal -> pressureDisplayTheme = newVal)
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(0, 1)
                                .step(1)
                                .formatValue(val -> Text.literal(switch(val) {
                                    case 1 -> "\uD83C\uDF51 Peach"; default -> "\uD83C\uDF03 Nighttime";
                                }).withColor(switch(val) {
                                    case 1 -> new Color(255, 193, 124).getRGB(); default -> new Color(147, 156, 177).getRGB();
                                })))
                        .build())
                .build();
    }
    
    public static OptionGroup drillFuelMeterGroup() {
        return OptionGroup.createBuilder()
                .name(Text.literal("Drill Fuel Meter"))
                .description(OptionDescription.of(Text.literal("Displays the current value of fuel in the drill.")))
                .collapsed(true)
                .option(createBooleanEnableOption(drillFuelMeter, () -> drillFuelMeter, newValue -> drillFuelMeter = newValue))
                
                .option(label("Position"))
                .option(Option.<Anchor>createBuilder()
                        .name(Text.literal("Anchor"))
                        .description(OptionDescription.of(Text.literal("Sets the anchor of the overlay to given positions.")))
                        .binding(Anchor.BottomMiddle, () -> drillFuelMeterAnchor, newValue -> drillFuelMeterAnchor = newValue)
                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Anchor.class))
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(Text.literal("Open HUD Editor"))
                        .text(Text.literal("→"))
                        .action((yaclScreen, thisOption) -> HudEditor.Companion.openScreen(MinecraftClient.getInstance().currentScreen, "Drill Fuel Meter"))
                        .build())
                
                .option(label("Themes"))
                .option(Option.<Integer>createBuilder()
                        .name(Text.literal("Theme"))
                        .binding(0, () -> drillFuelMeterTheme, newVal -> drillFuelMeterTheme = newVal)
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(0, 1)
                                .step(1)
                                .formatValue(val -> Text.literal(switch(val) {
                                    case 1 -> "✨ Mithril"; default -> "♻ Biofuel";
                                }).withColor(switch(val) {
                                    case 1 -> new Color(159, 187, 207).getRGB(); default -> new Color(140, 255, 144).getRGB();
                                })))
                        .build())
                .build();
    }
    
    public static OptionGroup dungeonScoreMeterGroup() {
        return OptionGroup.createBuilder()
                .name(Text.literal("Dungeon Score Meter"))
                .description(OptionDescription.of(Text.literal("")))
                .collapsed(true)
                .option(createBooleanEnableOption(dungeonScoreMeter, () -> dungeonScoreMeter, newValue -> dungeonScoreMeter = newValue))
                
                .option(label("Position"))
                .option(Option.<Anchor>createBuilder()
                        .name(Text.literal("Anchor"))
                        .description(OptionDescription.of(Text.literal("Sets the anchor of the overlay to given positions.")))
                        .binding(Anchor.BottomMiddle, () -> dungeonScoreMeterAnchor, newValue -> dungeonScoreMeterAnchor = newValue)
                        .controller(opt -> EnumControllerBuilder.create(opt).enumClass(Anchor.class))
                        .build())
                .option(ButtonOption.createBuilder()
                        .name(Text.literal("Open HUD Editor"))
                        .text(Text.literal("→"))
                        .action((yaclScreen, thisOption) -> HudEditor.Companion.openScreen(MinecraftClient.getInstance().currentScreen, "Dungeon Score Meter"))
                        .build())
        
                .option(label("Themes"))
                .option(Option.<Integer>createBuilder()
                        .name(Text.literal("Theme"))
                        .binding(0, () -> dungeonScoreMeterTheme, newVal -> dungeonScoreMeterTheme = newVal)
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                .range(0, 1)
                                .step(1)
                                .formatValue(val -> Text.literal(switch(val) {
                                    case 1 -> "Gradient"; default -> "Rank";
                                })))
                        .build())
                .option(createColorOption(new Color(111, 152, 150), "Gradient 1st", () -> dungeonScoreMeterColor1, newValue -> dungeonScoreMeterColor1 = newValue))
                .option(createColorOption(new Color(166, 219, 131), "Gradient 2nd", () -> dungeonScoreMeterColor2, newValue -> dungeonScoreMeterColor2 = newValue))
                .option(Option.<Float>createBuilder()
                        .name(Text.literal("Gradient Rotation"))
                        .binding(0.9f, () -> dungeonScoreMeterGradientRotate, newVal -> dungeonScoreMeterGradientRotate = newVal)
                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                .range(0f, 1f)
                                .step(0.01f)
                                .formatValue(val -> Text.literal(String.format("%d°§7/%d%%", Math.round(val * 360), Math.round(val * 100)))))
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
                                    float scaled = (val - 0.675f) / 0.16f;
                                    int warning = Math.max(0, (int) Math.ceil(scaled));
                                    
                                    int defaultColor = Math.clamp(255 - warning * 45L, 0, 255);
                                    Color color = new Color(255, defaultColor, defaultColor);
                                    String percentage = String.format("%.1f%%", Math.floor(val * 1000) / 10);
                                    
                                    return Text.literal("⚠".repeat(warning) + " " + percentage)
                                            .withColor(color.getRGB());
                                }))
                        .build())
                .option(createBooleanOption(lowHpIndicatorHeartbeat, "Pulse Animation", "", () -> lowHpIndicatorHeartbeat, newValue -> lowHpIndicatorHeartbeat = newValue))
                .build();
    }
    
    public static OptionGroup otherFeaturesGroup() {
        var randomLevel = Math.round(15 + Math.random() * 25);
        var randomLevel2 = Math.round(45 + Math.random() * 35);
        return OptionGroup.createBuilder()
                .name(Text.literal("Other"))
                .collapsed(true)
                .option(createBooleanOption(true, "Show Missing Enchants",
                        "Displays a list of missing enchants the hovered item has.",
                        () -> missingEnchants, newValue -> missingEnchants = newValue))
                .option(createBooleanOption(true, "Compact Pet Level", "Shortens pet level display on tooltip.\nExamples:\n"
                                + "§7[Lvl " + randomLevel + "] §aPet §f→ §8[§7" + randomLevel + "§8] §aPet\n"
                                + "§7[Lvl " + randomLevel2 + "] §6Pet §f→ §8[§7" + randomLevel2 + "§8] §6Pet\n"
                                + "§7[Lvl 100] §6Pet §f→ §8[§6100§8] §6Pet",
                        () -> compactPetLevel, newValue -> compactPetLevel = newValue))
                .option(createBooleanOption(false, "Hide Pressure in Action Bar", "", () -> hidePressure, newValue -> hidePressure = newValue))
                .option(createBooleanOption(false, "Hide Drill Fuel in Action Bar", "", () -> hideDrillFuel, newValue -> hideDrillFuel = newValue))
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
                                .group(drillFuelMeterGroup())
                                .group(dungeonScoreMeterGroup())
                                .group(lowHpIndicatorGroup())
                                .group(otherFeaturesGroup())
                                .build())
                .save(this::update)
                .build()
                .generateScreen(parent);
    }
    
    public void update() {
        ModConfig.HANDLER.save();
        PressureDisplay.INSTANCE.updateConfigValues();
        PetOverlay.INSTANCE.updateConfigValues();
        DrillFuelMeter.INSTANCE.updateConfigValues();
        DungeonScoreMeter.INSTANCE.updateConfigValues();
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
        PressureDisplay.INSTANCE.updateConfigValues();
        PetOverlay.INSTANCE.updateConfigValues();
        DrillFuelMeter.INSTANCE.updateConfigValues();
        DungeonScoreMeter.INSTANCE.updateConfigValues();
    }
}