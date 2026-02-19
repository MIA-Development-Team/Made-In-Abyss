package com.altnoir.mia.worldgen.feature.tree;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.worldgen.MiaFeatures;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
import com.altnoir.mia.worldgen.feature.foliage.InvertedFoliagePlacer;
import com.altnoir.mia.worldgen.feature.foliage.MegaInvertedFoliagePlacer;
import com.altnoir.mia.worldgen.feature.trunk.InvertedForkingTrunkPlacer;
import com.altnoir.mia.worldgen.feature.trunk.InvertedGiantTrunkPlacer;
import com.google.common.collect.ImmutableList;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BushFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.CherryTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

import java.util.List;

public class MiaTreeFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_TREE = MiaFeatureUtils.treeKey("skyfog_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_TREE_BEES = MiaFeatureUtils.treeKey("skyfog_tree_bees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_SKYFOG_TREE_BEES = MiaFeatureUtils.treeKey("fancy_skyfog_tree_bees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_SKYFOG_TREE = MiaFeatureUtils.treeKey("mega_skyfog_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_BUSH = MiaFeatureUtils.treeKey("short_skyfog_tree");

    public static final ResourceKey<ConfiguredFeature<?, ?>> VERDANT_FUNGUS = MiaFeatureUtils.treeKey("verdant_fungus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> INVERTED_TREE = MiaFeatureUtils.treeKey("inverted_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_INVERTED_TREE = MiaFeatureUtils.treeKey("mega_inverted_tree");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {

        var bee0002 = new BeehiveDecorator(0.002F);
        var bee001 = new BeehiveDecorator(0.01F);
        var bee002 = new BeehiveDecorator(0.02F);
        var bee005 = new BeehiveDecorator(0.05F);
        var bee1 = new BeehiveDecorator(1.0F);

        MiaFeatureUtils.register(context, SKYFOG_TREE, Feature.TREE, skyfog().build());
        MiaFeatureUtils.register(context, SKYFOG_TREE_BEES, Feature.TREE, skyfog().decorators(List.of(bee0002)).build());
        MiaFeatureUtils.register(context, FANCY_SKYFOG_TREE_BEES, Feature.TREE, fancySkyfog().build());
        MiaFeatureUtils.register(
                context, MEGA_SKYFOG_TREE, Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                        new MegaJungleTrunkPlacer(10, 2, 19),

                        new WeightedStateProvider(
                                SimpleWeightedRandomList.<BlockState>builder()
                                        .add(MiaBlocks.SKYFOG_LEAVES.get().defaultBlockState(), 4)
                                        .add(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get().defaultBlockState(), 1)
                        ),
                        new FancyFoliagePlacer(ConstantInt.of(3), ConstantInt.of(1), 4),
                        new TwoLayersFeatureSize(1, 1, 2)
                ).decorators(
                        ImmutableList.of(
                                new AlterGroundDecorator(BlockStateProvider.simple(Blocks.ROOTED_DIRT)), TrunkVineDecorator.INSTANCE,
                                new LeaveVineDecorator(0.075F)
                        )
                ).ignoreVines().build()
        );
        MiaFeatureUtils.register(
                context, SKYFOG_BUSH, Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                        new StraightTrunkPlacer(1, 0, 0),

                        new WeightedStateProvider(
                                SimpleWeightedRandomList.<BlockState>builder()
                                        .add(MiaBlocks.SKYFOG_LEAVES.get().defaultBlockState(), 9)
                                        .add(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get().defaultBlockState(), 1)
                        ),
                        new BushFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 2),
                        new TwoLayersFeatureSize(0, 0, 0)
                ).dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT)).forceDirt().build()
        );

        MiaFeatureUtils.register(context, VERDANT_FUNGUS, Feature.HUGE_BROWN_MUSHROOM,
                new HugeMushroomFeatureConfiguration(
                        BlockStateProvider.simple(
                                MiaBlocks.VERDANT_LEAVES.get().defaultBlockState()
                        ),
                        BlockStateProvider.simple(
                                MiaBlocks.VERDANT_STEM.get().defaultBlockState()
                        ),
                        3
                )
        );
        MiaFeatureUtils.register(
                context, INVERTED_TREE, MiaFeatures.INVERTED_TREE.get(),
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MiaBlocks.INVERTED_LOG.get()),
                        new InvertedForkingTrunkPlacer(7, 2, 3),

                        BlockStateProvider.simple(MiaBlocks.INVERTED_LEAVES.get()),
                        new InvertedFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1)),
                        new TwoLayersFeatureSize(1, 0, 1)
                ).decorators(ImmutableList.of(new LeaveVineDecorator(0.05F))
                ).ignoreVines().build()
        );

        MiaFeatureUtils.register(
                context, MEGA_INVERTED_TREE, MiaFeatures.INVERTED_TREE.get(),
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MiaBlocks.INVERTED_LOG.get()),
                        new InvertedGiantTrunkPlacer(12, 2, 19),

                        BlockStateProvider.simple(MiaBlocks.INVERTED_LEAVES.get()),
                        new MegaInvertedFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2),
                        new TwoLayersFeatureSize(1, 1, 2)
                ).decorators(ImmutableList.of(new LeaveVineDecorator(0.05F))
                ).ignoreVines().build()
        );
    }

    private static TreeConfiguration.TreeConfigurationBuilder skyfog() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                new ForkingTrunkPlacer(6, 2, 2),

                new WeightedStateProvider(
                        SimpleWeightedRandomList.<BlockState>builder()
                                .add(MiaBlocks.SKYFOG_LEAVES.get().defaultBlockState(), 9)
                                .add(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get().defaultBlockState(), 1)
                ),
                new CherryFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(4), 0.25F, 0.5F, 0.16666667F, 0.33333334F),

                new TwoLayersFeatureSize(1, 0, 1)
        ).dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT)).forceDirt();
    }

    private static TreeConfiguration.TreeConfigurationBuilder fancySkyfog() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                new CherryTrunkPlacer(7, 1, 0,
                        new WeightedListInt(
                                SimpleWeightedRandomList.<IntProvider>builder().add(ConstantInt.of(1), 1).add(ConstantInt.of(2), 1).add(ConstantInt.of(3), 1).build()
                        ),
                        UniformInt.of(2, 4), UniformInt.of(-4, -3), UniformInt.of(-1, 0)
                ),

                new WeightedStateProvider(
                        SimpleWeightedRandomList.<BlockState>builder()
                                .add(MiaBlocks.SKYFOG_LEAVES.get().defaultBlockState(), 9)
                                .add(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get().defaultBlockState(), 1)
                ),
                new CherryFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), ConstantInt.of(5), 0.25F, 0.5F, 0.16666667F, 0.33333334F),
                new TwoLayersFeatureSize(1, 0, 2)
        )
                .dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT)).forceDirt()
                .decorators(ImmutableList.of(new BeehiveDecorator(0.002F), TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.05F)))
                .ignoreVines();
    }
}
