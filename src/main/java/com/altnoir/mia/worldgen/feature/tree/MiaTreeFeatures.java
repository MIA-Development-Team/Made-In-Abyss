package com.altnoir.mia.worldgen.feature.tree;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.worldgen.MiaFeatures;
import com.altnoir.mia.worldgen.MiaFeatureUtils;
import com.altnoir.mia.worldgen.feature.configurations.PrimoHugeFungusConfiguration;
import com.altnoir.mia.worldgen.feature.foliage.InvertedFoliagePlacer;
import com.altnoir.mia.worldgen.feature.foliage.MegaInvertedFoliagePlacer;
import com.altnoir.mia.worldgen.feature.trunk.InvertedForkingTrunkPlacer;
import com.altnoir.mia.worldgen.feature.trunk.InvertedGiantTrunkPlacer;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.util.valueproviders.WeightedListInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
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

public class MiaTreeFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_TREE = treeKey("skyfog_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_TREE_BEES =
            treeKey("skyfog_tree_bees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FANCY_SKYFOG_TREE_BEES =
            treeKey("fancy_skyfog_tree_bees");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_SKYFOG_TREE =
            treeKey("mega_skyfog_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SKYFOG_BUSH =
            treeKey("short_skyfog_tree");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PRIMO_FUNGUS = treeKey("primo_fungus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GLOW_PRIMO_FUNGUS =
            treeKey("glow_primo_fungus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> VERDANT_FUNGUS =
            treeKey("verdant_fungus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> INVERTED_TREE =
            treeKey("inverted_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MEGA_INVERTED_TREE =
            treeKey("mega_inverted_tree");

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {

        var bee0002 = new BeehiveDecorator(0.002F);
        var bee001 = new BeehiveDecorator(0.01F);
        var bee002 = new BeehiveDecorator(0.02F);
        var bee005 = new BeehiveDecorator(0.05F);
        var bee1 = new BeehiveDecorator(1.0F);

        // 巨型菌生长时可以顶掉的方块：原版那套"可替换植物"清单，外加本模组自己的三种。
        BlockPredicate replaceablePlants =
                BlockPredicate.matchesBlocks(
                        Blocks.OAK_SAPLING,
                        Blocks.SPRUCE_SAPLING,
                        Blocks.BIRCH_SAPLING,
                        Blocks.JUNGLE_SAPLING,
                        Blocks.ACACIA_SAPLING,
                        Blocks.CHERRY_SAPLING,
                        Blocks.DARK_OAK_SAPLING,
                        Blocks.MANGROVE_PROPAGULE,
                        Blocks.DANDELION,
                        Blocks.TORCHFLOWER,
                        Blocks.POPPY,
                        Blocks.BLUE_ORCHID,
                        Blocks.ALLIUM,
                        Blocks.AZURE_BLUET,
                        Blocks.RED_TULIP,
                        Blocks.ORANGE_TULIP,
                        Blocks.WHITE_TULIP,
                        Blocks.PINK_TULIP,
                        Blocks.OXEYE_DAISY,
                        Blocks.CORNFLOWER,
                        Blocks.WITHER_ROSE,
                        Blocks.LILY_OF_THE_VALLEY,
                        Blocks.BROWN_MUSHROOM,
                        Blocks.RED_MUSHROOM,
                        Blocks.WHEAT,
                        Blocks.SUGAR_CANE,
                        Blocks.ATTACHED_PUMPKIN_STEM,
                        Blocks.ATTACHED_MELON_STEM,
                        Blocks.PUMPKIN_STEM,
                        Blocks.MELON_STEM,
                        Blocks.LILY_PAD,
                        Blocks.NETHER_WART,
                        Blocks.COCOA,
                        Blocks.CARROTS,
                        Blocks.POTATOES,
                        Blocks.CHORUS_PLANT,
                        Blocks.CHORUS_FLOWER,
                        Blocks.TORCHFLOWER_CROP,
                        Blocks.PITCHER_CROP,
                        Blocks.BEETROOTS,
                        Blocks.SWEET_BERRY_BUSH,
                        Blocks.WARPED_FUNGUS,
                        Blocks.CRIMSON_FUNGUS,
                        Blocks.WEEPING_VINES,
                        Blocks.WEEPING_VINES_PLANT,
                        Blocks.TWISTING_VINES,
                        Blocks.TWISTING_VINES_PLANT,
                        Blocks.CAVE_VINES,
                        Blocks.CAVE_VINES_PLANT,
                        Blocks.SPORE_BLOSSOM,
                        Blocks.AZALEA,
                        Blocks.FLOWERING_AZALEA,
                        Blocks.MOSS_CARPET,
                        Blocks.PINK_PETALS,
                        Blocks.BIG_DRIPLEAF,
                        Blocks.BIG_DRIPLEAF_STEM,
                        Blocks.SMALL_DRIPLEAF,
                        MiaBlocks.PRIMO_FUNGUS.get(),
                        MiaBlocks.GLOW_PRIMO_FUNGUS.get(),
                        MiaBlocks.MUSHROOM_BED.get());

        MiaFeatureUtils.register(context, SKYFOG_TREE, Feature.TREE, skyfog().build());
        MiaFeatureUtils.register(
                context,
                SKYFOG_TREE_BEES,
                Feature.TREE,
                skyfog().decorators(List.of(bee0002)).build());
        MiaFeatureUtils.register(
                context, FANCY_SKYFOG_TREE_BEES, Feature.TREE, fancySkyfog().build());
        MiaFeatureUtils.register(
                context,
                MEGA_SKYFOG_TREE,
                Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                                BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                                new MegaJungleTrunkPlacer(10, 2, 19),
                                new WeightedStateProvider(
                                        SimpleWeightedRandomList.<BlockState>builder()
                                                .add(
                                                        MiaBlocks.SKYFOG_LEAVES
                                                                .get()
                                                                .defaultBlockState(),
                                                        4)
                                                .add(
                                                        MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS
                                                                .get()
                                                                .defaultBlockState(),
                                                        1)),
                                new FancyFoliagePlacer(ConstantInt.of(3), ConstantInt.of(1), 4),
                                new TwoLayersFeatureSize(1, 1, 2))
                        .decorators(
                                ImmutableList.of(
                                        new AlterGroundDecorator(
                                                BlockStateProvider.simple(Blocks.ROOTED_DIRT)),
                                        TrunkVineDecorator.INSTANCE,
                                        new LeaveVineDecorator(0.075F)))
                        .ignoreVines()
                        .build());
        MiaFeatureUtils.register(
                context,
                SKYFOG_BUSH,
                Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                                BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                                new StraightTrunkPlacer(1, 0, 0),
                                new WeightedStateProvider(
                                        SimpleWeightedRandomList.<BlockState>builder()
                                                .add(
                                                        MiaBlocks.SKYFOG_LEAVES
                                                                .get()
                                                                .defaultBlockState(),
                                                        9)
                                                .add(
                                                        MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS
                                                                .get()
                                                                .defaultBlockState(),
                                                        1)),
                                new BushFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1), 2),
                                new TwoLayersFeatureSize(0, 0, 0))
                        .dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT))
                        .forceDirt()
                        .build());

        MiaFeatureUtils.register(
                context,
                PRIMO_FUNGUS,
                MiaFeatures.HUGE_PRIMO_FUNGUS.get(),
                new PrimoHugeFungusConfiguration(
                        BlockTags.DIRT,
                        MiaBlocks.PRIMO_STEM.get().defaultBlockState(),
                        MiaBlocks.PRIMO_CAP.get().defaultBlockState(),
                        Optional.of(Blocks.BUDDING_AMETHYST.defaultBlockState()),
                        replaceablePlants));

        MiaFeatureUtils.register(
                context,
                GLOW_PRIMO_FUNGUS,
                MiaFeatures.HUGE_PRIMO_FUNGUS.get(),
                new PrimoHugeFungusConfiguration(
                        BlockTags.DIRT,
                        MiaBlocks.PRIMO_STEM.get().defaultBlockState(),
                        MiaBlocks.GLOW_PRIMO_CAP.get().defaultBlockState(),
                        Optional.empty(),
                        replaceablePlants));
        MiaFeatureUtils.register(
                context,
                VERDANT_FUNGUS,
                Feature.HUGE_BROWN_MUSHROOM,
                new HugeMushroomFeatureConfiguration(
                        BlockStateProvider.simple(
                                MiaBlocks.VERDANT_LEAVES.get().defaultBlockState()),
                        BlockStateProvider.simple(MiaBlocks.VERDANT_STEM.get().defaultBlockState()),
                        3));
        MiaFeatureUtils.register(
                context,
                INVERTED_TREE,
                MiaFeatures.INVERTED_TREE.get(),
                new TreeConfiguration.TreeConfigurationBuilder(
                                BlockStateProvider.simple(MiaBlocks.INVERTED_LOG.get()),
                                new InvertedForkingTrunkPlacer(7, 2, 3),
                                BlockStateProvider.simple(MiaBlocks.INVERTED_LEAVES.get()),
                                new InvertedFoliagePlacer(ConstantInt.of(2), ConstantInt.of(1)),
                                new TwoLayersFeatureSize(1, 0, 1))
                        .decorators(ImmutableList.of(new LeaveVineDecorator(0.05F)))
                        .ignoreVines()
                        .build());

        MiaFeatureUtils.register(
                context,
                MEGA_INVERTED_TREE,
                MiaFeatures.INVERTED_TREE.get(),
                new TreeConfiguration.TreeConfigurationBuilder(
                                BlockStateProvider.simple(MiaBlocks.INVERTED_LOG.get()),
                                new InvertedGiantTrunkPlacer(12, 2, 19),
                                BlockStateProvider.simple(MiaBlocks.INVERTED_LEAVES.get()),
                                new MegaInvertedFoliagePlacer(
                                        ConstantInt.of(2), ConstantInt.of(0), 2),
                                new TwoLayersFeatureSize(1, 1, 2))
                        .decorators(ImmutableList.of(new LeaveVineDecorator(0.05F)))
                        .ignoreVines()
                        .build());
    }

    private static TreeConfiguration.TreeConfigurationBuilder skyfog() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                        new ForkingTrunkPlacer(6, 2, 2),
                        new WeightedStateProvider(
                                SimpleWeightedRandomList.<BlockState>builder()
                                        .add(MiaBlocks.SKYFOG_LEAVES.get().defaultBlockState(), 9)
                                        .add(
                                                MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS
                                                        .get()
                                                        .defaultBlockState(),
                                                1)),
                        new CherryFoliagePlacer(
                                ConstantInt.of(3),
                                ConstantInt.of(0),
                                ConstantInt.of(4),
                                0.25F,
                                0.5F,
                                0.16666667F,
                                0.33333334F),
                        new TwoLayersFeatureSize(1, 0, 1))
                .dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT))
                .forceDirt();
    }

    private static TreeConfiguration.TreeConfigurationBuilder fancySkyfog() {
        return new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(MiaBlocks.SKYFOG_LOG.get()),
                        new CherryTrunkPlacer(
                                7,
                                1,
                                0,
                                new WeightedListInt(
                                        SimpleWeightedRandomList.<IntProvider>builder()
                                                .add(ConstantInt.of(1), 1)
                                                .add(ConstantInt.of(2), 1)
                                                .add(ConstantInt.of(3), 1)
                                                .build()),
                                UniformInt.of(2, 4),
                                UniformInt.of(-4, -3),
                                UniformInt.of(-1, 0)),
                        new WeightedStateProvider(
                                SimpleWeightedRandomList.<BlockState>builder()
                                        .add(MiaBlocks.SKYFOG_LEAVES.get().defaultBlockState(), 9)
                                        .add(
                                                MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS
                                                        .get()
                                                        .defaultBlockState(),
                                                1)),
                        new CherryFoliagePlacer(
                                ConstantInt.of(4),
                                ConstantInt.of(0),
                                ConstantInt.of(5),
                                0.25F,
                                0.5F,
                                0.16666667F,
                                0.33333334F),
                        new TwoLayersFeatureSize(1, 0, 2))
                .dirt(BlockStateProvider.simple(Blocks.ROOTED_DIRT))
                .forceDirt()
                .decorators(
                        ImmutableList.of(
                                new BeehiveDecorator(0.002F),
                                TrunkVineDecorator.INSTANCE,
                                new LeaveVineDecorator(0.05F)))
                .ignoreVines();
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> treeKey(String name) {
        return MiaFeatureUtils.resourceKey("tree/" + name);
    }
}
