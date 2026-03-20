package com.altnoir.mia.worldgen.biome.great_fault;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class GreatFaultFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ABYSS_LIGHT = greatFaultKey("abyss_light");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAERULITE_GEODE = greatFaultKey("caerulite_geode");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);
        HolderGetter<PlacedFeature> holdergetter1 = context.lookup(Registries.PLACED_FEATURE);

        MiaFeatureUtils.register(context, ABYSS_LIGHT, Feature.SIMPLE_RANDOM_SELECTOR,
                new SimpleRandomFeatureConfiguration(HolderSet.direct(makeLight()))
        );
        MiaFeatureUtils.register(
                context, CAERULITE_GEODE, Feature.GEODE,
                new GeodeConfiguration(
                        new GeodeBlockSettings(
                                BlockStateProvider.simple(Blocks.AIR),
                                BlockStateProvider.simple(MiaBlocks.CAERULITE_BLOCK.get()),
                                BlockStateProvider.simple(MiaBlocks.BUDDING_CAERULITE.get()),
                                BlockStateProvider.simple(Blocks.CALCITE),
                                BlockStateProvider.simple(Blocks.SMOOTH_BASALT),
                                List.of(
                                        MiaBlocks.SMALL_CAERULITE_BUD.get().defaultBlockState(),
                                        MiaBlocks.MEDIUM_CAERULITE_BUD.get().defaultBlockState(),
                                        MiaBlocks.LARGE_CAERULITE_BUD.get().defaultBlockState(),
                                        MiaBlocks.CAERULITE_CLUSTER.get().defaultBlockState()
                                ),
                                BlockTags.FEATURES_CANNOT_REPLACE,
                                BlockTags.GEODE_INVALID_BLOCKS
                        ),
                        new GeodeLayerSettings(1.7, 2.2, 3.2, 4.2),
                        new GeodeCrackSettings(0.95, 2.0, 2),
                        0.35,
                        0.083,
                        true,
                        UniformInt.of(4, 6),
                        UniformInt.of(3, 4),
                        UniformInt.of(1, 2),
                        -16,
                        16,
                        0.05,
                        1
                )
        );

    }

    private static Holder<PlacedFeature> makeLight() {
        return PlacementUtils.inlinePlaced(
                Feature.BLOCK_COLUMN,
                new BlockColumnConfiguration(
                        List.of(
                                BlockColumnConfiguration.layer(ConstantInt.of(12), BlockStateProvider.simple(Blocks.LIGHT.defaultBlockState()))
                        ),
                        Direction.UP,
                        BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE,
                        true
                )
        );
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> greatFaultKey(String name) {
        return MiaFeatureUtils.resourceKey("great_fault/" + name);
    }
}
