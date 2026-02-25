package com.altnoir.mia.datagen;

import com.altnoir.mia.MIA;
import com.altnoir.mia.init.MiaPaintingVariants;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
import com.altnoir.mia.worldgen.MiaPlacementUtils;
import com.altnoir.mia.worldgen.biome.MiaBiomes;
import com.altnoir.mia.worldgen.dimension.MiaDimensionTypes;
import com.altnoir.mia.worldgen.dimension.MiaDimensions;
import com.altnoir.mia.worldgen.noise.MiaNoiseData;
import com.altnoir.mia.worldgen.noise_setting.MiaDensityFunctions;
import com.altnoir.mia.worldgen.noise_setting.MiaNoiseGeneratorSettings;
import com.altnoir.mia.worldgen.structure.MiaStructureSets;
import com.altnoir.mia.worldgen.structure.MiaStructures;
import com.altnoir.mia.worldgen.structure.pools.MiaPools;
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
            .add(Registries.STRUCTURE, MiaStructures::bootstrap)
            .add(Registries.STRUCTURE_SET, MiaStructureSets::bootstrap)
            .add(Registries.TEMPLATE_POOL, MiaPools::bootstrap)
            .add(Registries.BIOME, MiaBiomes::boostrap)
            .add(Registries.LEVEL_STEM, MiaDimensions::bootstrapStem)
            .add(Registries.NOISE, MiaNoiseData::bootstrap)
            .add(Registries.DENSITY_FUNCTION, MiaDensityFunctions::bootstrap)
            .add(Registries.NOISE_SETTINGS, MiaNoiseGeneratorSettings::bootstrap)
            .add(Registries.PAINTING_VARIANT, MiaPaintingVariants::bootstrap);

    public MiaWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(MIA.MOD_ID));
    }
}
