package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.worldgen.MIAConfigureFeatures;
import com.altnoir.mia.worldgen.MIAPlacedFeatures;
import com.altnoir.mia.worldgen.biome.MIABiomes;
import com.altnoir.mia.worldgen.dimension.MIADimensionTypes;
import com.altnoir.mia.worldgen.dimension.MIADimensions;
import com.altnoir.mia.worldgen.noisesetting.MIADensityFunction;
import com.altnoir.mia.worldgen.noisesetting.MIANoiseGeneratorSettings;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MIAWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, MIADimensionTypes::bootstrapType)
            .add(Registries.CONFIGURED_FEATURE, MIAConfigureFeatures::bootstrap)
            .add(Registries.PLACED_FEATURE, MIAPlacedFeatures::bootstrap)
            .add(Registries.BIOME, MIABiomes::boostrap)
            .add(Registries.LEVEL_STEM, MIADimensions::bootstrapStem)
            .add(Registries.DENSITY_FUNCTION, MIADensityFunction::bootstrap)
            .add(Registries.NOISE_SETTINGS, MIANoiseGeneratorSettings::bootstrap);

    public MIAWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(MIA.MOD_ID));
    }
}
