package com.altnoir.mia.worldgen.biome.the_abyss;

import com.altnoir.mia.worldgen.MiaPlacementUtils;
import com.altnoir.mia.worldgen.place.InvertedCountOnEveryLayerPlacement;
import com.altnoir.mia.worldgen.place.TreeOnEveryLayerPlacement;
import com.altnoir.mia.worldgen.place.WaterOnEveryLayerPlacement;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class TheAbyssPlacements {
    public static final ResourceKey<PlacedFeature> MONSTER_CHEAT = theAbyssKey("monster_cheat");
    public static final ResourceKey<PlacedFeature> SLAB_RUINS = theAbyssKey("slab_ruins");
    public static final ResourceKey<PlacedFeature> SPRING_WATER = theAbyssKey("spring_water");
    public static final ResourceKey<PlacedFeature> LAKE_WATER = theAbyssKey("lake_water");
    public static final ResourceKey<PlacedFeature> SUN_STONE = theAbyssKey("sun_stone");
    public static final ResourceKey<PlacedFeature> VINES = theAbyssKey("vines");
    public static final ResourceKey<PlacedFeature> GLOW_LICHEN = theAbyssKey("glow_lichen");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_PLAIN = theAbyssKey("patch_grass_plain");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_FERN = theAbyssKey("patch_grass_fern");
    public static final ResourceKey<PlacedFeature> PATCH_LARGE_FERN = theAbyssKey("patch_large_fern");
    public static final ResourceKey<PlacedFeature> PATCH_DENSE_LARGE_FERN = theAbyssKey("patch_dense_large_fern");
    public static final ResourceKey<PlacedFeature> PATCH_SUNFLOWER = theAbyssKey("patch_sunflower");
    public static final ResourceKey<PlacedFeature> CAVE_VINES = theAbyssKey("cave_vines");
    public static final ResourceKey<PlacedFeature> PATCH_WATERLILY = theAbyssKey("patch_waterlily");
    public static final ResourceKey<PlacedFeature> MARGINAL_WEED_BONEMEAL = theAbyssKey("marginal_weed_bonemeal");
    public static final ResourceKey<PlacedFeature> FLOWER_MEADOW = theAbyssKey("flower_meadow_layer1");
    public static final ResourceKey<PlacedFeature> FLOWER_MEADOW2 = theAbyssKey("flower_meadow_layer2");
    public static final ResourceKey<PlacedFeature> FOREST_FLOWERS = theAbyssKey("forest_flowers");
    public static final ResourceKey<PlacedFeature> ABYSS_TREES = theAbyssKey("abyss_trees");
    public static final ResourceKey<PlacedFeature> TREES_SKYFOG = theAbyssKey("trees_skyfog");
    public static final ResourceKey<PlacedFeature> TREES_SKYFOG_AND_AZALEA = theAbyssKey("trees_skyfog_and_azalea");
    public static final ResourceKey<PlacedFeature> DENSE_TREES_SKYFOG = theAbyssKey("dense_trees_skyfog");
    public static final ResourceKey<PlacedFeature> TREES_FOSSILIZED = theAbyssKey("trees_fossilized");
    public static final ResourceKey<PlacedFeature> TREES_FOSSILIZED_UNDER = theAbyssKey("trees_fossilized_under");
    public static final ResourceKey<PlacedFeature> TREES_FOSSILIZED_UNDER2 = theAbyssKey("trees_fossilized_under2");
    public static final ResourceKey<PlacedFeature> CAVES_CEILING_VEGETATION = theAbyssKey("caves_ceiling_vegetation");
    public static final ResourceKey<PlacedFeature> SPORE_BLOSSOM = theAbyssKey("spore_blossom");
    public static final ResourceKey<PlacedFeature> POOL_WITH_REED = theAbyssKey("pool_with_reed");
    public static final ResourceKey<PlacedFeature> TREES_VERDANT_FUNGUS = theAbyssKey("trees_verdant_fungus");
    public static final ResourceKey<PlacedFeature> TREES_INVERTED = theAbyssKey("trees_inverted");
    public static final ResourceKey<PlacedFeature> PRASIOLITE_CLUSTER = theAbyssKey("prasiolite_cluster");
    public static final ResourceKey<PlacedFeature> BIG_PRASIOLITE_CLUSTER = theAbyssKey("big_prasiolite_cluster");
    public static final ResourceKey<PlacedFeature> PRASIOLITE_GEODE = theAbyssKey("prasiolite_geode");
    public static final ResourceKey<PlacedFeature> CAVE_PILLAR = theAbyssKey("cave_pillar");
    public static final ResourceKey<PlacedFeature> RAW_IRON = theAbyssKey("raw_iron");

    private static final PlacementModifier ABYSS_BRINK_HEIGHT = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(128), VerticalAnchor.belowTop(8));
    private static final PlacementModifier ABYSS_BRINK_CAVE_HEIGHT = HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(128), VerticalAnchor.belowTop(64));

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> monster_cheat = holdergetter.getOrThrow(TheAbyssFeatures.MONSTER_CHEAT);
        Holder<ConfiguredFeature<?, ?>> slab_ruins = holdergetter.getOrThrow(TheAbyssFeatures.SLAB_RUINS);
        Holder<ConfiguredFeature<?, ?>> spring_water = holdergetter.getOrThrow(TheAbyssFeatures.SPRING_WATER);
        Holder<ConfiguredFeature<?, ?>> lake_water = holdergetter.getOrThrow(TheAbyssFeatures.LAKE_WATER);
        Holder<ConfiguredFeature<?, ?>> sun_stone = holdergetter.getOrThrow(TheAbyssFeatures.SUN_STONE);
        Holder<ConfiguredFeature<?, ?>> vines = holdergetter.getOrThrow(VegetationFeatures.VINES);
        Holder<ConfiguredFeature<?, ?>> glow_lichen = holdergetter.getOrThrow(TheAbyssFeatures.GLOW_LICHEN);
        Holder<ConfiguredFeature<?, ?>> grass = holdergetter.getOrThrow(VegetationFeatures.PATCH_GRASS);
        Holder<ConfiguredFeature<?, ?>> fern = holdergetter.getOrThrow(VegetationFeatures.PATCH_TAIGA_GRASS);
        Holder<ConfiguredFeature<?, ?>> large_fern = holdergetter.getOrThrow(VegetationFeatures.PATCH_LARGE_FERN);
        Holder<ConfiguredFeature<?, ?>> cave_vine = holdergetter.getOrThrow(CaveFeatures.CAVE_VINE);
        Holder<ConfiguredFeature<?, ?>> waterlily = holdergetter.getOrThrow(TheAbyssFeatures.PATCH_WATERLILY);
        Holder<ConfiguredFeature<?, ?>> single_piece_of_marginal_weed = holdergetter.getOrThrow(TheAbyssFeatures.SINGLE_PIECE_OF_MARGINAL_WEED);
        Holder<ConfiguredFeature<?, ?>> sunflower = holdergetter.getOrThrow(VegetationFeatures.PATCH_SUNFLOWER);
        Holder<ConfiguredFeature<?, ?>> flower_meadow = holdergetter.getOrThrow(TheAbyssFeatures.FLOWER_MEADOW);
        Holder<ConfiguredFeature<?, ?>> flower_meadow2 = holdergetter.getOrThrow(TheAbyssFeatures.FLOWER_MEADOW2);
        Holder<ConfiguredFeature<?, ?>> forest_flowers = holdergetter.getOrThrow(TheAbyssFeatures.FOREST_FLOWERS);
        Holder<ConfiguredFeature<?, ?>> skyfog = holdergetter.getOrThrow(TheAbyssFeatures.TREES_SKYFOG);
        Holder<ConfiguredFeature<?, ?>> skyfog_and_azalea = holdergetter.getOrThrow(TheAbyssFeatures.TREES_SKYFOG_AND_AZALEA);
        Holder<ConfiguredFeature<?, ?>> dense_skyfog = holdergetter.getOrThrow(TheAbyssFeatures.DENSE_TREES_SKYFOG);
        Holder<ConfiguredFeature<?, ?>> fossilized_tree = holdergetter.getOrThrow(TheAbyssFeatures.TREES_FOSSILIZED);
        Holder<ConfiguredFeature<?, ?>> fossilized_tree_under = holdergetter.getOrThrow(TheAbyssFeatures.TREES_FOSSILIZED_UNDER);
        Holder<ConfiguredFeature<?, ?>> fossilized_tree_under2 = holdergetter.getOrThrow(TheAbyssFeatures.TREES_FOSSILIZED_UNDER2);
        Holder<ConfiguredFeature<?, ?>> cave_vine_ceiling = holdergetter.getOrThrow(CaveFeatures.MOSS_PATCH_CEILING);
        Holder<ConfiguredFeature<?, ?>> spore_blossom = holdergetter.getOrThrow(CaveFeatures.SPORE_BLOSSOM);
        Holder<ConfiguredFeature<?, ?>> pool_with_reed = holdergetter.getOrThrow(TheAbyssFeatures.POOL_WITH_REED);
        Holder<ConfiguredFeature<?, ?>> verdant_fungus = holdergetter.getOrThrow(TheAbyssFeatures.TREES_VERDANT_FUNGUS);
        Holder<ConfiguredFeature<?, ?>> inverted = holdergetter.getOrThrow(TheAbyssFeatures.TREES_INVERTED);
        Holder<ConfiguredFeature<?, ?>> prasiolite_cluster = holdergetter.getOrThrow(TheAbyssFeatures.PRASIOLITE_CLUSTER);
        Holder<ConfiguredFeature<?, ?>> big_prasiolite_cluster = holdergetter.getOrThrow(TheAbyssFeatures.BIG_PRASIOLITE_CLUSTER);
        Holder<ConfiguredFeature<?, ?>> prasiolite_geode = holdergetter.getOrThrow(TheAbyssFeatures.PRASIOLITE_GEODE);
        Holder<ConfiguredFeature<?, ?>> cave_pillar = holdergetter.getOrThrow(TheAbyssFeatures.CAVE_PILLAR);
        Holder<ConfiguredFeature<?, ?>> raw_iron = holdergetter.getOrThrow(TheAbyssFeatures.RAW_IRON);

        MiaPlacementUtils.register(
                context, MONSTER_CHEAT, monster_cheat,
                RarityFilter.onAverageOnceEvery(3),
                InSquarePlacement.spread(),
                ABYSS_BRINK_CAVE_HEIGHT,
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, SLAB_RUINS, slab_ruins,
                RarityFilter.onAverageOnceEvery(2),
                InSquarePlacement.spread(),
                ABYSS_BRINK_CAVE_HEIGHT,
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, SPRING_WATER, spring_water,
                CountPlacement.of(10),
                InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.top()),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, LAKE_WATER, lake_water,
                RarityFilter.onAverageOnceEvery(4),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, SUN_STONE, sun_stone,
                CountPlacement.of(2),
                InSquarePlacement.spread(),
                ABYSS_BRINK_HEIGHT,
                EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.hasSturdyFace(Direction.DOWN), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                context, VINES, vines,
                CountPlacement.of(127),
                InSquarePlacement.spread(),
                ABYSS_BRINK_HEIGHT,
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, GLOW_LICHEN, glow_lichen,
                CountPlacement.of(UniformInt.of(157, 250)),
                ABYSS_BRINK_CAVE_HEIGHT,
                InSquarePlacement.spread(),
                //SurfaceRelativeThresholdFilter.of(Heightmap.Types.OCEAN_FLOOR_WG, Integer.MIN_VALUE, -13),
                BiomeFilter.biome()
        );

        MiaPlacementUtils.register(
                context, PATCH_GRASS_PLAIN, grass,
                NoiseThresholdCountPlacement.of(-0.8, 5, 10),
                CountOnEveryLayerPlacement.of(1),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, PATCH_GRASS_FERN, fern,
                NoiseThresholdCountPlacement.of(-0.8, 5, 10),
                CountOnEveryLayerPlacement.of(1),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, PATCH_LARGE_FERN, large_fern,
                RarityFilter.onAverageOnceEvery(5),
                CountOnEveryLayerPlacement.of(1),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, PATCH_DENSE_LARGE_FERN, large_fern,
                CountOnEveryLayerPlacement.of(2),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, PATCH_SUNFLOWER, sunflower,
                RarityFilter.onAverageOnceEvery(2),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP,
                BiomeFilter.biome()
        );

        MiaPlacementUtils.register(
                context, CAVE_VINES, cave_vine,
                CountPlacement.of(47),
                InSquarePlacement.spread(),
                ABYSS_BRINK_HEIGHT,
                EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.hasSturdyFace(Direction.DOWN), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(context, PATCH_WATERLILY, waterlily,
                WaterOnEveryLayerPlacement.of(2),
                BiomeFilter.biome());
        MiaPlacementUtils.register(context, MARGINAL_WEED_BONEMEAL, single_piece_of_marginal_weed, PlacementUtils.isEmpty());
        MiaPlacementUtils.register(
                context, FLOWER_MEADOW, flower_meadow,
                CountOnEveryLayerPlacement.of(1),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, FLOWER_MEADOW2, flower_meadow2,
                CountOnEveryLayerPlacement.of(1),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, FOREST_FLOWERS, forest_flowers,
                RarityFilter.onAverageOnceEvery(2),
                CountOnEveryLayerPlacement.of(1),
                CountPlacement.of(ClampedInt.of(UniformInt.of(-3, 1), 0, 1)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, RAW_IRON, raw_iron,
                RarityFilter.onAverageOnceEvery(5),
                CountOnEveryLayerPlacement.of(1),
                BiomeFilter.biome()
        );

        MiaPlacementUtils.register(
                context, ABYSS_TREES, skyfog_and_azalea,
                abyssTreePlace(8, 5)
        );
        MiaPlacementUtils.register(
                context, TREES_SKYFOG, skyfog,
                abyssTreePlace(1, 3)
        );
        MiaPlacementUtils.register(
                context, TREES_SKYFOG_AND_AZALEA, skyfog_and_azalea,
                abyssTreePlace(8)
        );
        MiaPlacementUtils.register(
                context, DENSE_TREES_SKYFOG, dense_skyfog,
                abyssTreePlace(16)
        );
        MiaPlacementUtils.register(
                context, TREES_FOSSILIZED, fossilized_tree,
                abyssTreePlace(8)
        );

        MiaPlacementUtils.register(
                context, TREES_FOSSILIZED_UNDER, fossilized_tree_under,
                abyssTreePlace(12)
        );
        MiaPlacementUtils.register(
                context, TREES_FOSSILIZED_UNDER2, fossilized_tree_under2,
                abyssInvertedTreePlace(6)
        );
        MiaPlacementUtils.register(context, CAVES_CEILING_VEGETATION, cave_vine_ceiling,
                CountPlacement.of(125),
                InSquarePlacement.spread(),
                ABYSS_BRINK_CAVE_HEIGHT,
                EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(context, SPORE_BLOSSOM, spore_blossom,
                CountPlacement.of(25),
                InSquarePlacement.spread(),
                ABYSS_BRINK_CAVE_HEIGHT,
                EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(context, POOL_WITH_REED, pool_with_reed,
                CountPlacement.of(62),
                InSquarePlacement.spread(),
                ABYSS_BRINK_HEIGHT,
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(1)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, TREES_VERDANT_FUNGUS, verdant_fungus,
                abyssTreePlace(8)
        );
        MiaPlacementUtils.register(
                context, TREES_INVERTED, inverted,
                abyssInvertedTreePlace(6)
        );

        MiaPlacementUtils.register(
                context, PRASIOLITE_CLUSTER, prasiolite_cluster,
                CountPlacement.of(UniformInt.of(64, 128)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(450)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, BIG_PRASIOLITE_CLUSTER, big_prasiolite_cluster,
                CountPlacement.of(UniformInt.of(2, 4)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(16), VerticalAnchor.absolute(400)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, PRASIOLITE_GEODE, prasiolite_geode,
                RarityFilter.onAverageOnceEvery(24),
                InSquarePlacement.spread(),
                ABYSS_BRINK_CAVE_HEIGHT,
                BiomeFilter.biome()
        );

        MiaPlacementUtils.register(
                context, CAVE_PILLAR, cave_pillar,
                CountPlacement.of(UniformInt.of(5, 24)),
                InSquarePlacement.spread(),
                ABYSS_BRINK_HEIGHT,
                BiomeFilter.biome()
        );
    }


    private static ImmutableList.Builder<PlacementModifier> abyssTreePlacementBase(int count) {
        return ImmutableList.<PlacementModifier>builder()
                .add(TreeOnEveryLayerPlacement.of(count))
                .add(BiomeFilter.biome());
    }

    private static ImmutableList.Builder<PlacementModifier> abyssTreePlacementBase(int count, int rarity) {
        return ImmutableList.<PlacementModifier>builder()
                .add(TreeOnEveryLayerPlacement.of(count))
                .add(RarityFilter.onAverageOnceEvery(rarity))
                .add(BiomeFilter.biome());
    }

    private static ImmutableList.Builder<PlacementModifier> abyssInvertedTreePlacementBase(int count) {
        return ImmutableList.<PlacementModifier>builder()
                .add(InvertedCountOnEveryLayerPlacement.of(count))
                .add(BiomeFilter.biome());
    }


    public static List<PlacementModifier> abyssTreePlace(int count) {
        return abyssTreePlacementBase(count).build();
    }

    public static List<PlacementModifier> abyssTreePlace(int count, int rarity) {
        return abyssTreePlacementBase(count, rarity).build();
    }

    public static List<PlacementModifier> abyssInvertedTreePlace(int count) {
        return abyssInvertedTreePlacementBase(count).build();
    }

    private static ResourceKey<PlacedFeature> theAbyssKey(String name) {
        return MiaPlacementUtils.resourceKey("the_abyss/" + name);
    }
}
