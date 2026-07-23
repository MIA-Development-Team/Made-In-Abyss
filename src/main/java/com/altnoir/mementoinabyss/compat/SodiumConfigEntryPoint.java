package com.altnoir.mementoinabyss.compat;

import com.altnoir.mementoinabyss.MementoInAbyss;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.StorageEventHandler;
import net.caffeinemc.mods.sodium.api.config.option.ControlValueFormatter;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.IntegerOptionBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.OptionGroupBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Exposes MIA's existing LOD settings in Sodium's video settings screen.
 *
 * <p>This class is only loaded through Sodium's optional config API entrypoint.
 * The option bindings remain backed by Fzzy Config, so there is only one source
 * of truth regardless of which config screen edits the values.</p>
 */
public final class SodiumConfigEntryPoint implements ConfigEntryPoint {
    private static final String CONFIG_KEY_PREFIX = "mementoinabyss.config.guiSection.";
    private static final String SODIUM_KEY_PREFIX = "mementoinabyss.sodium.";
    private static final StorageEventHandler STORAGE = MementoInAbyss.CONFIGS::save;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        var config = MementoInAbyss.CONFIGS.guiSection;
        Identifier enabledId = id("cross_dimension_lod_enabled");

        OptionGroupBuilder lodGroup = builder.createOptionGroup()
                .setName(Component.translatable(CONFIG_KEY_PREFIX + "crossDimensionLodGroup"))
                .addOption(builder.createBooleanOption(enabledId)
                        .setName(name("crossDimensionLodEnabled"))
                        .setTooltip(tooltip("cross_dimension_lod_enabled"))
                        .setStorageHandler(STORAGE)
                        .setBinding(
                                value -> config.crossDimensionLodEnabled.validateAndSet(value),
                                config.crossDimensionLodEnabled::get)
                        .setDefaultValue(true))
                .addOption(integerOption(
                        builder,
                        "cross_dimension_lod_view_distance",
                        "crossDimensionLodViewDistance",
                        config.crossDimensionLodViewDistance,
                        32,
                        16,
                        128,
                        units("chunks")))
                .addOption(integerOption(
                        builder,
                        "cross_dimension_lod_minimum_diameter",
                        "crossDimensionLodMinimumDiameter",
                        config.crossDimensionLodMinimumDiameter,
                        1024,
                        256,
                        8192,
                        units("blocks")))
                .addOption(integerOption(
                        builder,
                        "cross_dimension_lod_margin",
                        "crossDimensionLodMargin",
                        config.crossDimensionLodMargin,
                        192,
                        0,
                        1024,
                        units("blocks")))
                .addOption(integerOption(
                        builder,
                        "cross_dimension_lod_capture_queue_limit",
                        "crossDimensionLodCaptureQueueLimit",
                        config.crossDimensionLodCaptureQueueLimit,
                        1024,
                        64,
                        16384,
                        units("entries")));

        builder.registerOwnModOptions()
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable(SODIUM_KEY_PREFIX + "lod_page"))
                        .addOptionGroup(lodGroup));
    }

    private static IntegerOptionBuilder integerOption(
            ConfigBuilder builder,
            String idPath,
            String configField,
            ValidatedInt value,
            int defaultValue,
            int min,
            int max,
            ControlValueFormatter formatter) {
        return builder.createIntegerOption(id(idPath))
                .setName(name(configField))
                .setTooltip(tooltip(idPath))
                .setStorageHandler(STORAGE)
                .setBinding(input -> value.validateAndSet(input), value::get)
                .setDefaultValue(defaultValue)
                // A step of one preserves every value accepted by the existing Fzzy config.
                .setRange(min, max, 1)
                .setValueFormatter(formatter);
    }

    private static Identifier id(String path) {
        return MementoInAbyss.asResource(path);
    }

    private static Component name(String configField) {
        return Component.translatable(CONFIG_KEY_PREFIX + configField);
    }

    private static Component tooltip(String idPath) {
        return Component.translatable(SODIUM_KEY_PREFIX + idPath + ".tooltip");
    }

    private static ControlValueFormatter units(String unit) {
        return value -> Component.translatable(SODIUM_KEY_PREFIX + "value." + unit, value);
    }
}
