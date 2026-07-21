package com.altnoir.mementoinabyss.worldgen.noise;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.worldgen.MiaHeight;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;

import java.util.List;

public final class MiaNoiseGeneratorSettings {
    private static final NoiseSettings THE_ABYSS_NOISE_SETTINGS = NoiseSettings.create(
            MiaHeight.THE_ABYSS.minY(), MiaHeight.THE_ABYSS.height(), 2, 1);
    private static final NoiseSettings GREAT_FAULT_NOISE_SETTINGS = NoiseSettings.create(
            MiaHeight.GREAT_FAULT.minY(), MiaHeight.GREAT_FAULT.height(), 2, 1);

    public static final ResourceKey<NoiseGeneratorSettings> THE_ABYSS = ResourceKey.create(
            Registries.NOISE_SETTINGS, MementoInAbyss.asResource("the_abyss"));
    public static final ResourceKey<NoiseGeneratorSettings> GREAT_FAULT = ResourceKey.create(
            Registries.NOISE_SETTINGS, MementoInAbyss.asResource("great_fault"));

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        context.register(THE_ABYSS, new NoiseGeneratorSettings(
                THE_ABYSS_NOISE_SETTINGS,
                MiaBlocks.ABYSS_ANDESITE.get().defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                MiaNoiseRouterData.theAbyss(
                        context.lookup(Registries.DENSITY_FUNCTION),
                        context.lookup(Registries.NOISE)),
                MiaSurfaceRules.theAbyss(),
                List.of(),
                MiaHeight.THE_ABYSS.minY(),
                false,
                true,
                false,
                false));
        context.register(GREAT_FAULT, new NoiseGeneratorSettings(
                GREAT_FAULT_NOISE_SETTINGS,
                MiaBlocks.MARLITH.get().defaultBlockState(),
                Blocks.WATER.defaultBlockState(),
                MiaNoiseRouterData.greatFault(
                        context.lookup(Registries.DENSITY_FUNCTION),
                        context.lookup(Registries.NOISE)),
                MiaSurfaceRules.greatFault(),
                List.of(),
                MiaHeight.GREAT_FAULT.minY(),
                false,
                false,
                false,
                false));
    }

    private MiaNoiseGeneratorSettings() {
    }
}
