package com.altnoir.mia.worldgen.biome.abyss_brink;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
import com.altnoir.mia.worldgen.tree.MiaTreePlacements;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class AbyssBrinkFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPRING_WATER = MiaFeatureUtils.resourceKey("spring_water");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_SKYFOG_AND_AZALEA = MiaFeatureUtils.resourceKey("trees_skyfog_and_azalea");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<PlacedFeature> holdergetter1 = context.lookup(Registries.PLACED_FEATURE);
        Holder<PlacedFeature> azalea = holdergetter1.getOrThrow(MiaTreePlacements.AZALEA_CHECKED);
        Holder<PlacedFeature> skyfog_bee = holdergetter1.getOrThrow(MiaTreePlacements.SKYFOG_BEES_002);
        Holder<PlacedFeature> fancy_skyfog_bee = holdergetter1.getOrThrow(MiaTreePlacements.FANCY_SKYFOG_BEES_005);
        MiaFeatureUtils.register(context, SPRING_WATER, Feature.SPRING, new SpringConfiguration(
                        Fluids.WATER.defaultFluidState(),
                        true,
                        4,
                        1,
                        HolderSet.direct(
                                Block::builtInRegistryHolder,
                                MiaBlocks.ABYSS_ANDESITE.get(),
                                Blocks.STONE,
                                Blocks.GRANITE,
                                Blocks.DIORITE,
                                Blocks.ANDESITE,
                                Blocks.DEEPSLATE,
                                Blocks.TUFF,
                                Blocks.CALCITE,
                                Blocks.DIRT
                        )
                )
        );
        MiaFeatureUtils.register(
                context,
                TREES_SKYFOG_AND_AZALEA,
                Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(skyfog_bee, 0.4F), new WeightedPlacedFeature(azalea, 0.1F)), fancy_skyfog_bee)
        );
    }
}
