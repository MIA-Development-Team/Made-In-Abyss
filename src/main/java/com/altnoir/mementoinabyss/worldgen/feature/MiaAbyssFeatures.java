package com.altnoir.mementoinabyss.worldgen.feature;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaTags;
import com.altnoir.mementoinabyss.init.MiaWorldgenFeatures;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.material.Fluids;
import com.altnoir.mementoinabyss.content.block.plant.DoubleBerryBlock;

import java.util.List;

public final class MiaAbyssFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_MARGINAL_WEED = key("patch_marginal_weed");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_BALLOON_PLANT = key("patch_balloon_plant");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_LANTERN_PLANT = key("patch_lantern_plant");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_GREEN_PERILLA = key("patch_green_perilla");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_MEADOW_LAYER1 = key("flower_meadow_layer1");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FLOWER_MEADOW_LAYER2 = key("flower_meadow_layer2");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FOREST_FLOWERS = key("forest_flowers");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LONG_VINES = key("vines");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_WATERLILY = key("patch_waterlily");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_GLOOM_BERRY = key("patch_gloom_berry_plant");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_REED = key("patch_reed");
    public static final ResourceKey<ConfiguredFeature<?, ?>> POOL_WITH_REED = key("pool_with_reed");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPRING_WATER = key("spring_water");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LAKE_WATER = key("lake_water");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RAW_IRON = key("raw_iron");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SUN_STONE = key("sun_stone");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PRASIOLITE_GEODE = key("prasiolite_geode");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PRASIOLITE_CLUSTER = key("prasiolite_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> BIG_PRASIOLITE_CLUSTER = key("big_prasiolite_cluster");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_DIRT = key("ore_dirt");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_GRAVEL = key("ore_gravel");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_IRON = key("ore_iron");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_COPPER = key("ore_copper");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_GOLD = key("ore_gold");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_LAPIS = key("ore_lapis");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_REDSTONE = key("ore_redstone");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_DIAMOND = key("ore_diamond");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_EMERALD = key("ore_emerald");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_QUARTZ = key("ore_quartz");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_CHLOROPHYTE = key("ore_chlorophyte");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_FOSSILIZED = key("trees_fossilized");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_FOSSILIZED_UNDER = key("trees_fossilized_under");
    public static final ResourceKey<ConfiguredFeature<?, ?>> TREES_FOSSILIZED_UNDER_CEILING = key("trees_fossilized_under_ceiling");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        patch(context, PATCH_MARGINAL_WEED, MiaBlocks.MARGINAL_WEED.get(), 64);
        patch(context, PATCH_BALLOON_PLANT, MiaBlocks.BALLOON_PLANT.get(), 32);
        patch(context, PATCH_LANTERN_PLANT, MiaBlocks.LANTERN_PLANT.get(), 24);
        patch(context, PATCH_GREEN_PERILLA, MiaBlocks.GREEN_PERILLA.get(), 32);
        flowerPatch(context, FLOWER_MEADOW_LAYER1, WeightedList.<BlockState>builder()
                .add(Blocks.BIG_DRIPLEAF.defaultBlockState(), 1)
                .add(MiaBlocks.BALLOON_PLANT.get().defaultBlockState(), 1)
                .add(MiaBlocks.LANTERN_PLANT.get().defaultBlockState(), 1)
                .add(MiaBlocks.GREEN_PERILLA.get().defaultBlockState(), 1)
                .add(MiaBlocks.SCORCHLEAF.get().defaultBlockState(), 1)
                .add(Blocks.TORCHFLOWER.defaultBlockState(), 1)
                .add(MiaBlocks.MARGINAL_WEED.get().defaultBlockState(), 1).build());
        flowerPatch(context, FLOWER_MEADOW_LAYER2, WeightedList.<BlockState>builder()
                .add(Blocks.BIG_DRIPLEAF.defaultBlockState(), 1)
                .add(MiaBlocks.BALLOON_PLANT.get().defaultBlockState(), 1)
                .add(MiaBlocks.LANTERN_PLANT.get().defaultBlockState(), 1)
                .add(MiaBlocks.SILVEAF_FUNGUS.get().defaultBlockState(), 1)
                .add(MiaBlocks.KONJAC_ROOT.get().defaultBlockState(), 1)
                .add(MiaBlocks.CRIMSON_VEILGRASS.get().defaultBlockState(), 1).build());
        WeightedList.Builder<BlockState> fortitude = WeightedList.builder();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            for (int amount = 1; amount <= 4; amount++) {
                fortitude.add(MiaBlocks.FORTITUDE_FLOWER.get().defaultBlockState()
                        .setValue(FlowerBedBlock.FACING, direction)
                        .setValue(FlowerBedBlock.AMOUNT, amount), 1);
            }
        }
        flowerPatch(context, FOREST_FLOWERS, fortitude.build());
        context.register(LONG_VINES, new ConfiguredFeature<>(MiaWorldgenFeatures.LONG_VINES.get(),
                new LongVinesConfiguration(UniformInt.of(4, 16))));
        context.register(PATCH_WATERLILY, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.LILY_PAD))));
        context.register(PATCH_GLOOM_BERRY, new ConfiguredFeature<>(Feature.BLOCK_COLUMN,
                new BlockColumnConfiguration(List.of(
                        BlockColumnConfiguration.layer(ConstantInt.of(1), BlockStateProvider.simple(
                                MiaBlocks.GLOOM_BERRY_PLANT.get().defaultBlockState()
                                        .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
                                        .setValue(DoubleBerryBlock.AGE, DoubleBerryBlock.MAX_AGE))),
                        BlockColumnConfiguration.layer(ConstantInt.of(1), BlockStateProvider.simple(
                                MiaBlocks.GLOOM_BERRY_PLANT.get().defaultBlockState()
                                        .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
                                        .setValue(DoubleBerryBlock.AGE, DoubleBerryBlock.MAX_AGE)))
                ), Direction.UP, BlockPredicate.ONLY_IN_AIR_PREDICATE, true)));
        context.register(PATCH_REED, new ConfiguredFeature<>(Feature.BLOCK_COLUMN,
                new BlockColumnConfiguration(List.of(
                        BlockColumnConfiguration.layer(ConstantInt.of(1), BlockStateProvider.simple(
                                MiaBlocks.REED.get().defaultBlockState()
                                        .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER))),
                        BlockColumnConfiguration.layer(ConstantInt.of(1), BlockStateProvider.simple(
                                MiaBlocks.REED.get().defaultBlockState()
                                        .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)))
                ), Direction.UP, BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, true)));
        Holder<PlacedFeature> reed = Holder.direct(new PlacedFeature(context.lookup(Registries.CONFIGURED_FEATURE)
                .getOrThrow(PATCH_REED), List.of()));
        context.register(POOL_WITH_REED, new ConfiguredFeature<>(Feature.WATERLOGGED_VEGETATION_PATCH,
                new VegetationPatchConfiguration(BlockTags.MOSS_REPLACEABLE,
                        BlockStateProvider.simple(Blocks.MOSS_BLOCK), reed, CaveSurface.FLOOR,
                        ConstantInt.of(3), 1.0F, 5, 0.5F, UniformInt.of(4, 9), 0.1F)));
        context.register(SPRING_WATER, new ConfiguredFeature<>(Feature.SPRING,
                new SpringConfiguration(Fluids.WATER.defaultFluidState(), true, 4, 1,
                        HolderSet.direct(net.minecraft.world.level.block.Block::builtInRegistryHolder,
                                Blocks.TUFF, Blocks.DEEPSLATE, MiaBlocks.ABYSS_ANDESITE.get()))));
        context.register(LAKE_WATER, new ConfiguredFeature<>(Feature.LAKE,
                new LakeFeature.Configuration(BlockStateProvider.simple(Blocks.WATER),
                        BlockStateProvider.simple(Blocks.SAND))));
        context.register(RAW_IRON, new ConfiguredFeature<>(Feature.BLOCK_PILE,
                new BlockPileConfiguration(new WeightedStateProvider(WeightedList.<BlockState>builder()
                        .add(Blocks.RAW_IRON_BLOCK.defaultBlockState(), 2)
                        .add(MiaBlocks.ABYSS_COBBLED_ANDESITE.get().defaultBlockState(), 1)
                        .add(MiaBlocks.MOSSY_ABYSS_COBBLED_ANDESITE.get().defaultBlockState(), 1).build()))));
        context.register(SUN_STONE, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(MiaBlocks.SUN_STONE.get()))));
        context.register(PRASIOLITE_GEODE, new ConfiguredFeature<>(Feature.GEODE,
                new GeodeConfiguration(new GeodeBlockSettings(
                        BlockStateProvider.simple(Blocks.AIR),
                        BlockStateProvider.simple(MiaBlocks.PRASIOLITE_BLOCK.get()),
                        BlockStateProvider.simple(MiaBlocks.BUDDING_PRASIOLITE.get()),
                        BlockStateProvider.simple(Blocks.CALCITE),
                        BlockStateProvider.simple(Blocks.SMOOTH_BASALT),
                        List.of(MiaBlocks.SMALL_PRASIOLITE_BUD.get().defaultBlockState(),
                                MiaBlocks.MEDIUM_PRASIOLITE_BUD.get().defaultBlockState(),
                                MiaBlocks.LARGE_PRASIOLITE_BUD.get().defaultBlockState(),
                                MiaBlocks.PRASIOLITE_CLUSTER.get().defaultBlockState()),
                        BlockTags.FEATURES_CANNOT_REPLACE, BlockTags.GEODE_INVALID_BLOCKS),
                        new GeodeLayerSettings(1.7, 2.2, 3.2, 4.2),
                        new GeodeCrackSettings(0.95, 2.0, 2),
                        0.35, 0.083, true, UniformInt.of(4, 6), UniformInt.of(3, 4),
                        UniformInt.of(1, 2), -16, 16, 0.05, 1)));
        WeightedStateProvider crystalsUp = prasioliteCrystals(Direction.UP);
        WeightedStateProvider crystalsDown = prasioliteCrystals(Direction.DOWN);
        context.register(PRASIOLITE_CLUSTER, new ConfiguredFeature<>(MiaWorldgenFeatures.CLUSTER.get(),
                new ClusterConfiguration(BlockStateProvider.simple(MiaBlocks.PRASIOLITE_BLOCK.get()),
                        crystalsUp, crystalsDown, 0.5F, UniformInt.of(1, 2), ConstantInt.of(1))));
        context.register(BIG_PRASIOLITE_CLUSTER, new ConfiguredFeature<>(MiaWorldgenFeatures.BIG_CLUSTER.get(),
                new ClusterConfiguration(BlockStateProvider.simple(MiaBlocks.PRASIOLITE_BLOCK.get()),
                        crystalsUp, crystalsDown, 0.5F, UniformInt.of(1, 5), UniformInt.of(16, 32))));

        TagMatchTest stone = new TagMatchTest(MiaTags.BlockTags.ABYSS_ANDESITE_ORE_REPLACEABLE.tag);
        ore(context, ORE_DIRT, stone, Blocks.DIRT.defaultBlockState(), 33, 1.0F);
        ore(context, ORE_GRAVEL, stone, Blocks.GRAVEL.defaultBlockState(), 33, 0.0F);
        ore(context, ORE_IRON, stone, MiaBlocks.ABYSS_IRON_ORE.get().defaultBlockState(), 4, 0.0F);
        ore(context, ORE_COPPER, stone, MiaBlocks.ABYSS_COPPER_ORE.get().defaultBlockState(), 6, 0.0F);
        ore(context, ORE_GOLD, stone, MiaBlocks.ABYSS_GOLD_ORE.get().defaultBlockState(), 9, 0.5F);
        ore(context, ORE_LAPIS, stone, MiaBlocks.ABYSS_LAPIS_ORE.get().defaultBlockState(), 10, 1.0F);
        ore(context, ORE_REDSTONE, stone, MiaBlocks.ABYSS_REDSTONE_ORE.get().defaultBlockState(), 12, 1.0F);
        ore(context, ORE_DIAMOND, stone, MiaBlocks.ABYSS_DIAMOND_ORE.get().defaultBlockState(), 8, 1.0F);
        ore(context, ORE_EMERALD, stone, MiaBlocks.ABYSS_EMERALD_ORE.get().defaultBlockState(), 3, 1.0F);
        ore(context, ORE_QUARTZ, stone, MiaBlocks.ABYSS_QUARTZ_ORE.get().defaultBlockState(), 14, 0.0F);
        ore(context, ORE_CHLOROPHYTE, stone, MiaBlocks.ABYSS_CHLOROPHYTE_ORE.get().defaultBlockState(), 9, 1.0F);
        fossilTrunk(context, TREES_FOSSILIZED, 2, Direction.UP);
        fossilTrunk(context, TREES_FOSSILIZED_UNDER, 8, Direction.UP);
        fossilTrunk(context, TREES_FOSSILIZED_UNDER_CEILING, 8, Direction.DOWN);
    }

    private static void patch(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key,
                              net.minecraft.world.level.block.Block block, int tries) {
        context.register(key, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(BlockStateProvider.simple(block))));
    }

    private static void flowerPatch(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                    ResourceKey<ConfiguredFeature<?, ?>> key, WeightedList<BlockState> states) {
        context.register(key, new ConfiguredFeature<>(Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(new WeightedStateProvider(states))));
    }

    private static WeightedStateProvider prasioliteCrystals(Direction facing) {
        return new WeightedStateProvider(WeightedList.<BlockState>builder()
                .add(MiaBlocks.SMALL_PRASIOLITE_BUD.get().defaultBlockState().setValue(BlockStateProperties.FACING, facing), 1)
                .add(MiaBlocks.MEDIUM_PRASIOLITE_BUD.get().defaultBlockState().setValue(BlockStateProperties.FACING, facing), 1)
                .add(MiaBlocks.LARGE_PRASIOLITE_BUD.get().defaultBlockState().setValue(BlockStateProperties.FACING, facing), 1)
                .add(MiaBlocks.PRASIOLITE_CLUSTER.get().defaultBlockState().setValue(BlockStateProperties.FACING, facing), 1)
                .build());
    }

    private static void ore(BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key,
                            TagMatchTest rule, net.minecraft.world.level.block.state.BlockState state, int size, float discard) {
        context.register(key, new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(rule, state, size, discard)));
    }

    private static void fossilTrunk(BootstrapContext<ConfiguredFeature<?, ?>> context,
                                    ResourceKey<ConfiguredFeature<?, ?>> key, int branchLength,
                                    Direction direction) {
        WeightedStateProvider logs = new WeightedStateProvider(WeightedList.<BlockState>builder()
                .add(MiaBlocks.FOSSILIZED_LOG.get().defaultBlockState(), 1)
                .add(MiaBlocks.MOSSY_FOSSILIZED_LOG.get().defaultBlockState(), 3));
        IntProvider trunkHeight = direction == Direction.UP
                ? new WeightedListInt(WeightedList.<IntProvider>builder()
                        .add(UniformInt.of(10, 14), 7).add(ConstantInt.of(8), 2).add(UniformInt.of(16, 24), 1).build())
                : new WeightedListInt(WeightedList.<IntProvider>builder()
                        .add(UniformInt.of(8, 12), 3).add(ConstantInt.of(6), 1).build());
        List<BlockTrunkConfiguration.Layer> layers = direction == Direction.UP
                ? List.of(BlockTrunkConfiguration.layer(trunkHeight, logs),
                        BlockTrunkConfiguration.layer(BlockStateProvider.simple(Blocks.MOSS_CARPET)))
                : List.of(BlockTrunkConfiguration.layer(trunkHeight, logs));
        BlockTrunkConfiguration.Layer branch = BlockTrunkConfiguration.layer(
                new WeightedListInt(WeightedList.<IntProvider>builder()
                        .add(UniformInt.of(1, branchLength), 1).add(ConstantInt.of(1), 3).build()), logs);
        BlockTrunkConfiguration.Layer branchTop = BlockTrunkConfiguration.layer(
                BlockStateProvider.simple(Blocks.MOSS_CARPET));
        context.register(key, new ConfiguredFeature<>(MiaWorldgenFeatures.BLOCK_TRUNK.get(),
                new BlockTrunkConfiguration(layers, branch, branchTop, 0.15F, direction,
                        BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE, true)));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, MementoInAbyss.asResource("the_abyss/" + name));
    }

    private MiaAbyssFeatures() {}
}
