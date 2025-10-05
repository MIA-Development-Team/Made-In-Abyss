package com.altnoir.mia.worldgen.biome.abyss_brink;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.worldgen.MiaFeature;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
import com.altnoir.mia.worldgen.feature.LakeFeature;
import com.altnoir.mia.worldgen.feature.configurations.ClusterConfiguration;
import com.altnoir.mia.worldgen.feature.tree.MiaTreePlacements;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class AbyssBrinkFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPRING_WATER = MiaFeatureUtils.abyssBrinkKey("spring_water");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LAKE_WATER = MiaFeatureUtils.abyssBrinkKey("lake_water");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_MEADOW = MiaFeatureUtils.abyssBrinkKey("flower_meadow");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FOREST_FLOWERS = MiaFeatureUtils.abyssBrinkKey("forest_flowers");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_SKYFOG = MiaFeatureUtils.abyssBrinkKey("trees_skyfog");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_SKYFOG_AND_AZALEA = MiaFeatureUtils.abyssBrinkKey("trees_skyfog_and_azalea");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PRASIOLITE_CLUSTER = MiaFeatureUtils.abyssBrinkKey("prasiolite_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIG_PRASIOLITE_CLUSTER = MiaFeatureUtils.abyssBrinkKey("big_prasiolite_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RAW_IRON = MiaFeatureUtils.abyssBrinkKey("raw_iron");

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
                LAKE_WATER,
                MiaFeature.LAKE.get(),
                new LakeFeature.Configuration(
                        BlockStateProvider.simple(Blocks.WATER.defaultBlockState()), BlockStateProvider.simple(Blocks.SAND.defaultBlockState())
                )
        );

        MiaFeatureUtils.register(
                context,
                FLOWER_MEADOW,
                Feature.FLOWER,
                new RandomPatchConfiguration(
                        96,
                        6,
                        2,
                        PlacementUtils.onlyWhenEmpty(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(
                                        new DualNoiseProvider(
                                                new InclusiveRange<>(1, 3),
                                                new NormalNoise.NoiseParameters(-10, 1.0),
                                                1.0F,
                                                2345L,
                                                new NormalNoise.NoiseParameters(-3, 1.0),
                                                1.0F,
                                                List.of(
                                                        Blocks.TALL_GRASS.defaultBlockState(),
                                                        Blocks.AZURE_BLUET.defaultBlockState(),
                                                        Blocks.OXEYE_DAISY.defaultBlockState(),
                                                        Blocks.WHITE_TULIP.defaultBlockState(),
                                                        Blocks.LILY_OF_THE_VALLEY.defaultBlockState(),
                                                        Blocks.TORCHFLOWER.defaultBlockState(),
                                                        Blocks.SHORT_GRASS.defaultBlockState()
                                                )
                                        )
                                )
                        )
                )
        );
        MiaFeatureUtils.register(
                context,
                FOREST_FLOWERS,
                Feature.SIMPLE_RANDOM_SELECTOR,
                new SimpleRandomFeatureConfiguration(
                        HolderSet.direct(
                                PlacementUtils.inlinePlaced(
                                        Feature.NO_BONEMEAL_FLOWER,
                                        FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(MiaBlocks.FORTITUDE_FLOWER.get())))
                                )
                        )
                )
        );
        MiaFeatureUtils.register(context, RAW_IRON, Feature.BLOCK_PILE, new BlockPileConfiguration(BlockStateProvider.simple(Blocks.RAW_IRON_BLOCK)));

        MiaFeatureUtils.register(
                context,
                TREES_SKYFOG,
                Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(skyfog_bee, 0.3F)), fancy_skyfog_bee)
        );
        MiaFeatureUtils.register(
                context,
                TREES_SKYFOG_AND_AZALEA,
                Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(skyfog_bee, 0.4F), new WeightedPlacedFeature(azalea, 0.1F)), fancy_skyfog_bee)
        );

        WeightedStateProvider prasiolite1 = new WeightedStateProvider(createPrasioliteStates(false).build());
        WeightedStateProvider prasiolite2 = new WeightedStateProvider(createPrasioliteStates(true).build());
        MiaFeatureUtils.register(
                context,
                PRASIOLITE_CLUSTER,
                Feature.SIMPLE_RANDOM_SELECTOR,
                new SimpleRandomFeatureConfiguration(
                        HolderSet.direct(
                                PlacementUtils.inlinePlaced(
                                        MiaFeature.CLUSTER.get(),
                                        new ClusterConfiguration(
                                                BlockStateProvider.simple(MiaBlocks.PRASIOLITE_BLOCK.get().defaultBlockState()),
                                                prasiolite1, prasiolite2,
                                                0.5F, UniformInt.of(1, 2), ConstantInt.of(1)
                                        ),
                                        EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
                                        RandomOffsetPlacement.vertical(ConstantInt.of(1))
                                ),
                                PlacementUtils.inlinePlaced(
                                        MiaFeature.CLUSTER.get(),
                                        new ClusterConfiguration(
                                                BlockStateProvider.simple(MiaBlocks.PRASIOLITE_BLOCK.get().defaultBlockState()),
                                                prasiolite1, prasiolite2,
                                                0.5F, UniformInt.of(1, 2), ConstantInt.of(1)
                                        ),
                                        EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
                                        RandomOffsetPlacement.vertical(ConstantInt.of(-1))
                                )
                        )
                )
        );
        MiaFeatureUtils.register(
                context,
                BIG_PRASIOLITE_CLUSTER,
                MiaFeature.BIG_CLUSTER.get(),
                new ClusterConfiguration(
                        BlockStateProvider.simple(MiaBlocks.PRASIOLITE_BLOCK.get().defaultBlockState()),
                        prasiolite1, prasiolite2,
                        0.5F, UniformInt.of(1, 5), UniformInt.of(16, 32))
        );
    }

    private static SimpleWeightedRandomList.Builder<BlockState> createPrasioliteStates(boolean facingDown) {
        SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();

        if (facingDown) {
            builder.add(MiaBlocks.PRASIOLITE_CLUSTER.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN), 1)
                    .add(MiaBlocks.LARGE_PRASIOLITE_BUD.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN), 1)
                    .add(MiaBlocks.MEDIUM_PRASIOLITE_BUD.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN), 1)
                    .add(MiaBlocks.SMALL_PRASIOLITE_BUD.get().defaultBlockState().setValue(BlockStateProperties.FACING, Direction.DOWN), 1);
        } else {
            builder.add(MiaBlocks.PRASIOLITE_CLUSTER.get().defaultBlockState(), 1)
                    .add(MiaBlocks.LARGE_PRASIOLITE_BUD.get().defaultBlockState(), 1)
                    .add(MiaBlocks.MEDIUM_PRASIOLITE_BUD.get().defaultBlockState(), 1)
                    .add(MiaBlocks.SMALL_PRASIOLITE_BUD.get().defaultBlockState(), 1);
        }
        return builder;
    }

}
