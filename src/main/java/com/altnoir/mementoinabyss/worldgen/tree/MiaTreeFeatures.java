package com.altnoir.mementoinabyss.worldgen.tree;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.CherryFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.BeehiveDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.altnoir.mementoinabyss.init.MiaWorldgenFeatures;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

import java.util.List;
import com.altnoir.mementoinabyss.worldgen.feature.MiaAbyssFeatures;

public final class MiaTreeFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_TREE = key("skyfog_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_TREE_BEES = key("skyfog_tree_bees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_SKYFOG_TREE = key("mega_skyfog_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VERDANT_FUNGUS = key("verdant_fungus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> INVERTED_TREE = key("inverted_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_INVERTED_TREE = key("mega_inverted_tree");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(SKYFOG_TREE, new ConfiguredFeature<>(Feature.TREE, skyfog().build()));
        context.register(SKYFOG_TREE_BEES, new ConfiguredFeature<>(Feature.TREE,
                skyfog().decorators(List.of(new BeehiveDecorator(0.002F))).build()));
        context.register(MEGA_SKYFOG_TREE, new ConfiguredFeature<>(Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                        new MegaJungleTrunkPlacer(10, 2, 19),
                        skyfogLeaves(4, 1),
                        new FancyFoliagePlacer(ConstantInt.of(3), ConstantInt.of(1), 4),
                        new TwoLayersFeatureSize(1, 1, 2))
                        .belowTrunkProvider(BlockStateProvider.simple(Blocks.ROOTED_DIRT)).ignoreVines().build()));
        context.register(VERDANT_FUNGUS, new ConfiguredFeature<>(Feature.HUGE_BROWN_MUSHROOM,
                new HugeMushroomFeatureConfiguration(
                        BlockStateProvider.simple(MiaBlocks.VERDANT_LEAVES.get()),
                        BlockStateProvider.simple(MiaBlocks.VERDANT_STEM.get()), 3,
                        BlockPredicate.matchesTag(BlockTags.HUGE_BROWN_MUSHROOM_CAN_PLACE_ON))));
        context.register(INVERTED_TREE, new ConfiguredFeature<>(MiaWorldgenFeatures.INVERTED_TREE.get(),
                inverted(10, 2, 3, 3)));
        context.register(MEGA_INVERTED_TREE, new ConfiguredFeature<>(MiaWorldgenFeatures.INVERTED_TREE.get(),
                inverted(16, 3, 6, 5)));
    }

    private static TreeConfiguration.TreeConfigurationBuilder skyfog() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                new ForkingTrunkPlacer(6, 2, 2),
                skyfogLeaves(9, 1),
                new CherryFoliagePlacer(ConstantInt.of(3), ConstantInt.of(0), ConstantInt.of(4),
                        0.25F, 0.5F, 0.16666667F, 0.33333334F),
                new TwoLayersFeatureSize(1, 0, 1))
                .belowTrunkProvider(BlockStateProvider.simple(Blocks.ROOTED_DIRT));
    }

    private static WeightedStateProvider skyfogLeaves(int normalWeight, int fruitWeight) {
        return new WeightedStateProvider(WeightedList.<BlockState>builder()
                .add(MiaBlocks.SKYFOG_LEAVES.get().defaultBlockState(), normalWeight)
                .add(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get().defaultBlockState(), fruitWeight));
    }

    private static TreeConfiguration inverted(int baseHeight, int randomA, int randomB, int radius) {
        return new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(MiaBlocks.INVERTED_LOG.get()),
                new StraightTrunkPlacer(baseHeight, randomA, randomB),
                BlockStateProvider.simple(MiaBlocks.INVERTED_LEAVES.get()),
                new BlobFoliagePlacer(ConstantInt.of(radius), ConstantInt.of(1), 3),
                new TwoLayersFeatureSize(1, 0, 1)).ignoreVines().build();
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, MementoInAbyss.asResource("tree/" + name));
    }

    private MiaTreeFeatures() {}
}
