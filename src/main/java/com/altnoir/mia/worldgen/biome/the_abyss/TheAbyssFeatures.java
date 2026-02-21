package com.altnoir.mia.worldgen.biome.the_abyss;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.worldgen.MiaFeatures;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
import com.altnoir.mia.worldgen.feature.LakeFeature;
import com.altnoir.mia.worldgen.feature.configurations.*;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

public class TheAbyssFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> MONSTER_CHEAT = theAbyssKey("monster_cheat");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SLAB_RUINS = theAbyssKey("slab_ruins");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPRING_WATER = theAbyssKey("spring_water");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LAKE_WATER = theAbyssKey("lake_water");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SUN_STONE = theAbyssKey("sun_stone");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VINES = theAbyssKey("vines");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOW_LICHEN = theAbyssKey("glow_lichen");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_MEADOW = theAbyssKey("flower_meadow_layer1");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_MEADOW2 = theAbyssKey("flower_meadow_layer2");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FOREST_FLOWERS = theAbyssKey("forest_flowers");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_WATERLILY = theAbyssKey("patch_waterlily");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SINGLE_PIECE_OF_MARGINAL_WEED = theAbyssKey("single_piece_of_marginal_weed");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_SKYFOG = theAbyssKey("trees_skyfog");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_SKYFOG_AND_AZALEA = theAbyssKey("trees_skyfog_and_azalea");
    public static final ResourceKey<ConfiguredFeature<?, ?>> DENSE_TREES_SKYFOG = theAbyssKey("dense_trees_skyfog");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_FOSSILIZED = theAbyssKey("trees_fossilized");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_FOSSILIZED_UNDER = theAbyssKey("trees_fossilized_under");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_FOSSILIZED_UNDER2 = theAbyssKey("trees_fossilized_under2");
    public static final ResourceKey<ConfiguredFeature<?, ?>> REED = theAbyssKey("reed");
    public static final ResourceKey<ConfiguredFeature<?, ?>> POOL_WITH_REED = theAbyssKey("pool_with_reed");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_VERDANT_FUNGUS = theAbyssKey("trees_verdant_fungus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_INVERTED = theAbyssKey("trees_inverted");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PRASIOLITE_CLUSTER = theAbyssKey("prasiolite_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIG_PRASIOLITE_CLUSTER = theAbyssKey("big_prasiolite_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PRASIOLITE_GEODE = theAbyssKey("prasiolite_geode");
    public static final ResourceKey<ConfiguredFeature<?, ?>> CAVE_PILLAR = theAbyssKey("cave_pillar");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RAW_IRON = theAbyssKey("raw_iron");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);
        HolderGetter<PlacedFeature> holdergetter1 = context.lookup(Registries.PLACED_FEATURE);

        Holder<PlacedFeature> azalea = holdergetter1.getOrThrow(MiaTreePlacements.AZALEA_CHECKED);
        Holder<PlacedFeature> skyfog_bee = holdergetter1.getOrThrow(MiaTreePlacements.SKYFOG_BEES);
        Holder<PlacedFeature> fancy_skyfog_bee = holdergetter1.getOrThrow(MiaTreePlacements.FANCY_SKYFOG_BEES_002);
        Holder<PlacedFeature> maga_skyfog = holdergetter1.getOrThrow(MiaTreePlacements.MEGA_SKYFOG);
        Holder<PlacedFeature> skyfog_bush = holdergetter1.getOrThrow(MiaTreePlacements.SKYFOG_BUSH);
        Holder<PlacedFeature> verdant_fungus = holdergetter1.getOrThrow(MiaTreePlacements.VERDANT_FUNGUS);
        Holder<PlacedFeature> inverted = holdergetter1.getOrThrow(MiaTreePlacements.INVERTED);
        Holder<PlacedFeature> maga_inverted = holdergetter1.getOrThrow(MiaTreePlacements.MAGA_INVERTED);

        FeatureUtils.register(
                context, MONSTER_CHEAT, MiaFeatures.MONSTER_CHEAT.get(),
                new MonsterCheatConfiguration(
                        BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get().defaultBlockState()),
                        BlockStateProvider.simple(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get().defaultBlockState().setValue(BlockStateProperties.DISTANCE, 1)))
        );
        FeatureUtils.register(
                context, SLAB_RUINS, MiaFeatures.SLAB_RUINS.get(),
                new SlabRuinsConfiguration(
                        new WeightedStateProvider(
                                SimpleWeightedRandomList.<BlockState>builder()
                                        .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS_SLAB.get().defaultBlockState())
                                        .add(MiaBlocks.ABYSS_ANDESITE_BRICKS_SLAB.get().defaultBlockState())
                                        .build()),
                        new WeightedStateProvider(
                                SimpleWeightedRandomList.<BlockState>builder()
                                        .add(MiaBlocks.MOSSY_ABYSS_ANDESITE_BRICKS.get().defaultBlockState())
                                        .add(MiaBlocks.ABYSS_ANDESITE_BRICKS.get().defaultBlockState())
                                        .build())
                )
        );

        MiaFeatureUtils.register(
                context, SPRING_WATER, Feature.SPRING,
                new SpringConfiguration(
                        Fluids.WATER.defaultFluidState(),
                        true,
                        4,
                        1,
                        abyssEdgeStone()
                )
        );
        MiaFeatureUtils.register(
                context, LAKE_WATER, MiaFeatures.LAKE.get(),
                new LakeFeature.Configuration(
                        BlockStateProvider.simple(Blocks.WATER.defaultBlockState()), BlockStateProvider.simple(Blocks.SAND.defaultBlockState())
                )
        );
        MiaFeatureUtils.register(
                context, SUN_STONE, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(MiaBlocks.SUN_STONE.get().defaultBlockState()))
        );
        MiaFeatureUtils.register(context, VINES, MiaFeatures.LONG_VINES.get(), new LongVinesConfiguration(UniformInt.of(4, 16)));

        MultifaceBlock multifaceblock = (MultifaceBlock) Blocks.GLOW_LICHEN;
        MiaFeatureUtils.register(
                context, GLOW_LICHEN, Feature.MULTIFACE_GROWTH,
                new MultifaceGrowthConfiguration(
                        multifaceblock,
                        32,
                        false,
                        true,
                        true,
                        0.8F,
                        abyssEdgeStone(MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(), MiaBlocks.COVERGRASS_TUFF.get())
                )
        );

        MiaFeatureUtils.register(
                context, FLOWER_MEADOW, Feature.FLOWER,
                new RandomPatchConfiguration(
                        96,
                        6,
                        2,
                        PlacementUtils.onlyWhenEmpty(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(
                                        new DualNoiseProvider(
                                                new InclusiveRange<>(1, 3),
                                                new NormalNoise.NoiseParameters(-5, 1.0),
                                                1.0F,
                                                2345L,
                                                new NormalNoise.NoiseParameters(-3, 1.0),
                                                1.0F,
                                                List.of(
                                                        Blocks.BIG_DRIPLEAF.defaultBlockState(),
                                                        MiaBlocks.BALLOON_PLANT.get().defaultBlockState(),
                                                        MiaBlocks.GREEN_PERILLA.get().defaultBlockState(),
                                                        MiaBlocks.SCORCHLEAF.get().defaultBlockState(),
                                                        Blocks.TORCHFLOWER.defaultBlockState(),
                                                        MiaBlocks.MARGINAL_WEED.get().defaultBlockState()
                                                )
                                        )
                                )
                        )
                )
        );
        MiaFeatureUtils.register(
                context, FLOWER_MEADOW2, Feature.FLOWER,
                new RandomPatchConfiguration(
                        96,
                        6,
                        2,
                        PlacementUtils.onlyWhenEmpty(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(
                                        new DualNoiseProvider(
                                                new InclusiveRange<>(1, 3),
                                                new NormalNoise.NoiseParameters(-5, 1.0),
                                                1.0F,
                                                2345L,
                                                new NormalNoise.NoiseParameters(-3, 1.0),
                                                1.0F,
                                                List.of(
                                                        Blocks.BIG_DRIPLEAF.defaultBlockState(),
                                                        MiaBlocks.BALLOON_PLANT.get().defaultBlockState(),
                                                        MiaBlocks.SILVEAF_FUNGUS.get().defaultBlockState(),
                                                        MiaBlocks.KONJAC_ROOT.get().defaultBlockState(),
                                                        MiaBlocks.CRIMSON_VEILGRASS.get().defaultBlockState()
                                                )
                                        )
                                )
                        )
                )
        );

        SimpleWeightedRandomList.Builder<BlockState> builder = SimpleWeightedRandomList.builder();
        for (int i = 1; i <= 4; i++) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                builder.add(
                        MiaBlocks.FORTITUDE_FLOWER.get().defaultBlockState().setValue(PinkPetalsBlock.AMOUNT, Integer.valueOf(i)).setValue(PinkPetalsBlock.FACING, direction), 1
                );
            }
        }
        MiaFeatureUtils.register(
                context, FOREST_FLOWERS, Feature.FLOWER,
                new RandomPatchConfiguration(
                        96, 6, 2,
                        PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(new WeightedStateProvider(builder)))
                )
        );
        MiaFeatureUtils.register(
                context, PATCH_WATERLILY, Feature.RANDOM_PATCH,
                new RandomPatchConfiguration(
                        96, 7, 3,
                        PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILY_PAD)))
                )
        );
        MiaFeatureUtils.register(context, SINGLE_PIECE_OF_MARGINAL_WEED, Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(MiaBlocks.MARGINAL_WEED.get().defaultBlockState()))
        );

        MiaFeatureUtils.register(
                context, RAW_IRON, Feature.BLOCK_PILE,
                new BlockPileConfiguration(
                        new WeightedStateProvider(
                                SimpleWeightedRandomList.<BlockState>builder()
                                        .add(Blocks.RAW_IRON_BLOCK.defaultBlockState(), 2)
                                        .add(MiaBlocks.ABYSS_COBBLED_ANDESITE.get().defaultBlockState(), 1)
                                        .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get().defaultBlockState(), 1)
                                        .build()
                        )
                )
        );

        MiaFeatureUtils.register(
                context, TREES_SKYFOG, Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(skyfog_bee, 0.3F)), fancy_skyfog_bee)
        );
        MiaFeatureUtils.register(
                context, TREES_SKYFOG_AND_AZALEA, Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(
                        new WeightedPlacedFeature(maga_skyfog, 0.075F),
                        new WeightedPlacedFeature(azalea, 0.1F),
                        new WeightedPlacedFeature(skyfog_bee, 0.5F)
                ), fancy_skyfog_bee)
        );
        MiaFeatureUtils.register(
                context, DENSE_TREES_SKYFOG, Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(
                        new WeightedPlacedFeature(maga_skyfog, 0.33333334F),
                        new WeightedPlacedFeature(skyfog_bush, 0.5F),
                        new WeightedPlacedFeature(fancy_skyfog_bee, 0.1F)
                ), skyfog_bee)
        );
        MiaFeatureUtils.register(context, TREES_FOSSILIZED, Feature.SIMPLE_RANDOM_SELECTOR,
                new SimpleRandomFeatureConfiguration(
                        HolderSet.direct(
                                makeSmallPillar(MiaBlocks.FOSSILIZED_LOG.get(), MiaBlocks.MOSSY_FOSSILIZED_LOG.get(), Blocks.MOSS_CARPET, 2, Direction.UP)
                        )
                )
        );
        MiaFeatureUtils.register(context, TREES_FOSSILIZED_UNDER, Feature.SIMPLE_RANDOM_SELECTOR,
                new SimpleRandomFeatureConfiguration(
                        HolderSet.direct(
                                makeSmallPillar(MiaBlocks.FOSSILIZED_LOG.get(), MiaBlocks.MOSSY_FOSSILIZED_LOG.get(), Blocks.MOSS_CARPET, 8, Direction.UP)
                        )
                )
        );
        MiaFeatureUtils.register(context, TREES_FOSSILIZED_UNDER2, Feature.SIMPLE_RANDOM_SELECTOR,
                new SimpleRandomFeatureConfiguration(
                        HolderSet.direct(
                                makeSmallPillar(MiaBlocks.FOSSILIZED_LOG.get(), MiaBlocks.MOSSY_FOSSILIZED_LOG.get(), Blocks.MOSS_CARPET, 8, Direction.DOWN)
                        )
                )
        );
        MiaFeatureUtils.register(context, REED, Feature.SIMPLE_RANDOM_SELECTOR,
                new SimpleRandomFeatureConfiguration(
                        HolderSet.direct(
                                PlacementUtils.inlinePlaced(
                                        Feature.BLOCK_COLUMN,
                                        new BlockColumnConfiguration(
                                                List.of(
                                                        BlockColumnConfiguration.layer(
                                                                ConstantInt.of(1),
                                                                BlockStateProvider.simple(MiaBlocks.REED.get().defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER))
                                                        ),
                                                        BlockColumnConfiguration.layer(
                                                                ConstantInt.of(1),
                                                                BlockStateProvider.simple(MiaBlocks.REED.get().defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER))
                                                        )
                                                ),
                                                Direction.UP,
                                                BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE,
                                                true
                                        )
                                )
                        )
                )
        );
        MiaFeatureUtils.register(context, POOL_WITH_REED, Feature.WATERLOGGED_VEGETATION_PATCH,
                new VegetationPatchConfiguration(
                        BlockTags.MOSS_REPLACEABLE,
                        BlockStateProvider.simple(Blocks.MOSS_BLOCK),
                        PlacementUtils.inlinePlaced(holdergetter.getOrThrow(REED)),
                        CaveSurface.FLOOR,
                        ConstantInt.of(3),
                        1.0F,
                        5,
                        0.5F,
                        UniformInt.of(4, 9),
                        0.1F
                )
        );
        MiaFeatureUtils.register(
                context, TREES_VERDANT_FUNGUS, Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(fancy_skyfog_bee, 0.1F)), verdant_fungus)
        );

        MiaFeatureUtils.register(
                context, TREES_INVERTED, Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(maga_inverted, 0.2F)), inverted)
        );

        WeightedStateProvider prasiolite1 = new WeightedStateProvider(createPrasioliteStates(false).build());
        WeightedStateProvider prasiolite2 = new WeightedStateProvider(createPrasioliteStates(true).build());
        MiaFeatureUtils.register(
                context, PRASIOLITE_CLUSTER, Feature.SIMPLE_RANDOM_SELECTOR,
                new SimpleRandomFeatureConfiguration(
                        HolderSet.direct(
                                PlacementUtils.inlinePlaced(
                                        MiaFeatures.CLUSTER.get(),
                                        new ClusterConfiguration(
                                                BlockStateProvider.simple(MiaBlocks.PRASIOLITE_BLOCK.get().defaultBlockState()),
                                                prasiolite1, prasiolite2,
                                                0.5F, UniformInt.of(1, 2), ConstantInt.of(1)
                                        ),
                                        EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, 12),
                                        RandomOffsetPlacement.vertical(ConstantInt.of(1))
                                ),
                                PlacementUtils.inlinePlaced(
                                        MiaFeatures.CLUSTER.get(),
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
                context, BIG_PRASIOLITE_CLUSTER, MiaFeatures.BIG_CLUSTER.get(),
                new ClusterConfiguration(
                        BlockStateProvider.simple(MiaBlocks.PRASIOLITE_BLOCK.get().defaultBlockState()),
                        prasiolite1, prasiolite2,
                        0.5F, UniformInt.of(1, 5), UniformInt.of(16, 32))
        );

        MiaFeatureUtils.register(
                context, PRASIOLITE_GEODE, Feature.GEODE,
                new GeodeConfiguration(
                        new GeodeBlockSettings(
                                BlockStateProvider.simple(Blocks.AIR),
                                BlockStateProvider.simple(MiaBlocks.PRASIOLITE_BLOCK.get()),
                                BlockStateProvider.simple(MiaBlocks.BUDDING_PRASIOLITE.get()),
                                BlockStateProvider.simple(Blocks.CALCITE),
                                BlockStateProvider.simple(Blocks.SMOOTH_BASALT),
                                List.of(
                                        MiaBlocks.SMALL_PRASIOLITE_BUD.get().defaultBlockState(),
                                        MiaBlocks.MEDIUM_PRASIOLITE_BUD.get().defaultBlockState(),
                                        MiaBlocks.LARGE_PRASIOLITE_BUD.get().defaultBlockState(),
                                        MiaBlocks.PRASIOLITE_CLUSTER.get().defaultBlockState()
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

        MiaFeatureUtils.register(
                context, CAVE_PILLAR, MiaFeatures.ABYSS_CAVE_PILLAR.get(),
                new MiaCavePillarConfiguration(
                        188,
                        UniformInt.of(3, 15),
                        UniformFloat.of(0.5F, 3.0F),
                        0.5F,
                        UniformFloat.of(0.3F, 0.6F),
                        UniformFloat.of(0.4F, 1.0F),
                        UniformFloat.of(0.0F, 0.3F),
                        4,
                        0.6F
                )
        );
    }


    public static HolderSet<Block> abyssEdgeStone(Block... addBlocks) {
        return HolderSet.direct(
                Block::builtInRegistryHolder,
                Blocks.TUFF,
                Blocks.DEEPSLATE,
                MiaBlocks.ABYSS_ANDESITE.get()
        );
    }

    private static Holder<PlacedFeature> makeSmallPillar(Block block1, Block block2, Block blockTop, int l, Direction direction) {
        var list = direction == Direction.UP ?
                List.of(
                        BlockTrunkConfiguration.layer(
                                new WeightedListInt(
                                        SimpleWeightedRandomList.<IntProvider>builder()
                                                .add(UniformInt.of(10, 14), 7)
                                                .add(ConstantInt.of(8), 2)
                                                .add(UniformInt.of(16, 24), 1)
                                                .build()
                                ),
                                new WeightedStateProvider(
                                        SimpleWeightedRandomList.<BlockState>builder()
                                                .add(block1.defaultBlockState(), 1)
                                                .add(block2.defaultBlockState(), 3)
                                                .build()
                                )
                        ),
                        BlockTrunkConfiguration.layer(
                                ConstantInt.of(1),
                                BlockStateProvider.simple(blockTop.defaultBlockState())
                        )
                ) :
                List.of(
                        BlockTrunkConfiguration.layer(
                                new WeightedListInt(
                                        SimpleWeightedRandomList.<IntProvider>builder()
                                                .add(UniformInt.of(8, 12), 3)
                                                .add(ConstantInt.of(6), 1)
                                                .build()
                                ),
                                new WeightedStateProvider(
                                        SimpleWeightedRandomList.<BlockState>builder()
                                                .add(block1.defaultBlockState(), 1)
                                                .add(block2.defaultBlockState(), 3)
                                                .build()
                                )
                        )
                );

        return PlacementUtils.inlinePlaced(
                MiaFeatures.BLOCK_TRUNK.get(),
                new BlockTrunkConfiguration(list,
                        BlockTrunkConfiguration.layer(
                                new WeightedListInt(
                                        SimpleWeightedRandomList.<IntProvider>builder()
                                                .add(UniformInt.of(1, l), 1)
                                                .add(ConstantInt.of(1), 3)
                                                .build()
                                ),
                                new WeightedStateProvider(
                                        SimpleWeightedRandomList.<BlockState>builder()
                                                .add(block1.defaultBlockState(), 1)
                                                .add(block2.defaultBlockState(), 3)
                                                .build()
                                )
                        ),
                        BlockTrunkConfiguration.layer(BlockStateProvider.simple(blockTop.defaultBlockState())), 0.15F, direction,
                        BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE,
                        true
                )
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

    private static ResourceKey<ConfiguredFeature<?, ?>> theAbyssKey(String name) {
        return MiaFeatureUtils.resourceKey("the_abyss/" + name);
    }
}
