package com.altnoir.mia.worldgen.biome.tree;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
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
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
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

import java.util.List;

public class MiaTreeFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_TREE = MiaFeatureUtils.treeKey("skyfog_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_TREE_BEES = MiaFeatureUtils.treeKey("skyfog_tree_bees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_SKYFOG_TREE_BEES = MiaFeatureUtils.treeKey("fancy_skyfog_tree_bees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_SKYFOG_TREE = MiaFeatureUtils.treeKey("mega_skyfog_tree");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {

        var bee0002 = new BeehiveDecorator(0.002F);
        var bee001 = new BeehiveDecorator(0.01F);
        var bee002 = new BeehiveDecorator(0.02F);
        var bee005 = new BeehiveDecorator(0.05F);
        var bee1 = new BeehiveDecorator(1.0F);

        MiaFeatureUtils.register(context, SKYFOG_TREE, Feature.TREE, skyfog().build());
        MiaFeatureUtils.register(context, SKYFOG_TREE_BEES, Feature.TREE, skyfog().decorators(List.of(bee002)).build());
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
                ).decorators(ImmutableList.of(
                        new AlterGroundDecorator(BlockStateProvider.simple(Blocks.ROOTED_DIRT)),
                        TrunkVineDecorator.INSTANCE,
                        new LeaveVineDecorator(0.15F))
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
                new CherryFoliagePlacer(ConstantInt.of(4), ConstantInt.of(0), ConstantInt.of(4), 0.25F, 0.5F, 0.16666667F, 0.33333334F),

                new TwoLayersFeatureSize(1, 0, 1)
        ).dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT)).forceDirt().ignoreVines();
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
                .decorators(ImmutableList.of(new BeehiveDecorator(0.05F), TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.075F)))
                .ignoreVines();
    }
}
