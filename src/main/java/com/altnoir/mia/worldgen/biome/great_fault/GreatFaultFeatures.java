package com.altnoir.mia.worldgen.biome.great_fault;

import com.altnoir.mia.worldgen.MiaFeatureUtils;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public class GreatFaultFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ABYSS_LIGHT = greatFaultKey("abyss_light");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);
        HolderGetter<PlacedFeature> holdergetter1 = context.lookup(Registries.PLACED_FEATURE);

        MiaFeatureUtils.register(context, ABYSS_LIGHT, Feature.SIMPLE_RANDOM_SELECTOR,
                new SimpleRandomFeatureConfiguration(HolderSet.direct(makeLight()))
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
