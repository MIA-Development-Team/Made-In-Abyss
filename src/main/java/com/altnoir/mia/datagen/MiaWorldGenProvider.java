package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaPaintingVariants;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
import com.altnoir.mia.worldgen.MiaPlacementUtils;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import com.altnoir.mia.worldgen.dimension.MiaDimensionTypes;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import com.altnoir.mia.worldgen.noise_setting.MiaDensityFunction;
import com.altnoir.mia.worldgen.noise_setting.MiaNoiseGeneratorSettings;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MiaWorldGenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, MiaDimensionTypes::bootstrapType)
            .add(Registries.CONFIGURED_FEATURE, MiaFeatureUtils::bootstrap)
            .add(Registries.PLACED_FEATURE, MiaPlacementUtils::bootstrap)
            .add(Registries.BIOME, MiaBiomes::boostrap)
            .add(Registries.LEVEL_STEM, MiaDimensions::bootstrapStem)
            .add(Registries.DENSITY_FUNCTION, MiaDensityFunction::bootstrap)
            .add(Registries.NOISE_SETTINGS, MiaNoiseGeneratorSettings::bootstrap)
            .add(Registries.PAINTING_VARIANT, MiaPaintingVariants::bootstrap);

    public MiaWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(MIA.MOD_ID));
    }
}
