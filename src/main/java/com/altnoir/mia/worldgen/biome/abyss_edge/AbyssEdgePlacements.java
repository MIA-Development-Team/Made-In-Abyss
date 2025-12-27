package com.altnoir.mia.worldgen.biome.abyss_edge;

import com.altnoir.mia.worldgen.MiaPlacementUtils;
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

public class AbyssEdgePlacements {
    public static final ResourceKey<PlacedFeature> MONSTER_CHEAT = MiaPlacementUtils.abyssEdgeKey("monster_cheat");
    public static final ResourceKey<PlacedFeature> SLAB_RUINS = MiaPlacementUtils.abyssEdgeKey("slab_ruins");
    public static final ResourceKey<PlacedFeature> SPRING_WATER = MiaPlacementUtils.abyssEdgeKey("spring_water");
    public static final ResourceKey<PlacedFeature> LAKE_WATER = MiaPlacementUtils.abyssEdgeKey("lake_water");
    public static final ResourceKey<PlacedFeature> SUN_STONE = MiaPlacementUtils.abyssEdgeKey("sun_stone");
    public static final ResourceKey<PlacedFeature> VINES = MiaPlacementUtils.abyssEdgeKey("vines");
    public static final ResourceKey<PlacedFeature> GLOW_LICHEN = MiaPlacementUtils.abyssEdgeKey("glow_lichen");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_PLAIN = MiaPlacementUtils.abyssEdgeKey("patch_grass_plain");
    public static final ResourceKey<PlacedFeature> PATCH_LARGE_FERN = MiaPlacementUtils.abyssEdgeKey("patch_large_fern");
    public static final ResourceKey<PlacedFeature> PATCH_SUNFLOWER = MiaPlacementUtils.abyssEdgeKey("patch_sunflower");
    public static final ResourceKey<PlacedFeature> CAVE_VINES = MiaPlacementUtils.abyssEdgeKey("cave_vines");
    public static final ResourceKey<PlacedFeature> FLOWER_MEADOW = MiaPlacementUtils.abyssEdgeKey("flower_meadow");
    public static final ResourceKey<PlacedFeature> FOREST_FLOWERS = MiaPlacementUtils.abyssEdgeKey("forest_flowers");
    public static final ResourceKey<PlacedFeature> ABYSS_TREES = MiaPlacementUtils.abyssEdgeKey("abyss_trees");
    public static final ResourceKey<PlacedFeature> TREES_SKYFOG = MiaPlacementUtils.abyssEdgeKey("trees_skyfog");
    public static final ResourceKey<PlacedFeature> TREES_SKYFOG_AND_AZALEA = MiaPlacementUtils.abyssEdgeKey("trees_skyfog_and_azalea");
    public static final ResourceKey<PlacedFeature> PRASIOLITE_CLUSTER = MiaPlacementUtils.abyssEdgeKey("prasiolite_cluster");
    public static final ResourceKey<PlacedFeature> BIG_PRASIOLITE_CLUSTER = MiaPlacementUtils.abyssEdgeKey("big_prasiolite_cluster");
    public static final ResourceKey<PlacedFeature> PRASIOLITE_GEODE = MiaPlacementUtils.abyssEdgeKey("prasiolite_geode");
    public static final ResourceKey<PlacedFeature> CAVE_PILLAR = MiaPlacementUtils.abyssEdgeKey("cave_pillar");
    public static final ResourceKey<PlacedFeature> RAW_IRON = MiaPlacementUtils.abyssEdgeKey("raw_iron");

    private static final PlacementModifier ABYSS_BRINK_HEIGHT = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(360));
    private static final PlacementModifier ABYSS_BRINK_CAVE_HEIGHT = HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(200));

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> monster_cheat = holdergetter.getOrThrow(AbyssEdgeFeatures.MONSTER_CHEAT);
        Holder<ConfiguredFeature<?, ?>> slab_ruins = holdergetter.getOrThrow(AbyssEdgeFeatures.SLAB_RUINS);
        Holder<ConfiguredFeature<?, ?>> spring_water = holdergetter.getOrThrow(AbyssEdgeFeatures.SPRING_WATER);
        Holder<ConfiguredFeature<?, ?>> lake_water = holdergetter.getOrThrow(AbyssEdgeFeatures.LAKE_WATER);
        Holder<ConfiguredFeature<?, ?>> sun_stone = holdergetter.getOrThrow(AbyssEdgeFeatures.SUN_STONE);
        Holder<ConfiguredFeature<?, ?>> vines = holdergetter.getOrThrow(VegetationFeatures.VINES);
        Holder<ConfiguredFeature<?, ?>> glow_lichen = holdergetter.getOrThrow(AbyssEdgeFeatures.GLOW_LICHEN);
        Holder<ConfiguredFeature<?, ?>> grass = holdergetter.getOrThrow(VegetationFeatures.PATCH_GRASS);
        Holder<ConfiguredFeature<?, ?>> large_fern = holdergetter.getOrThrow(VegetationFeatures.PATCH_LARGE_FERN);
        Holder<ConfiguredFeature<?, ?>> cave_vine = holdergetter.getOrThrow(CaveFeatures.CAVE_VINE);
        Holder<ConfiguredFeature<?, ?>> sunflower = holdergetter.getOrThrow(VegetationFeatures.PATCH_SUNFLOWER);
        Holder<ConfiguredFeature<?, ?>> flower_meadow = holdergetter.getOrThrow(AbyssEdgeFeatures.FLOWER_MEADOW);
        Holder<ConfiguredFeature<?, ?>> forest_flowers = holdergetter.getOrThrow(AbyssEdgeFeatures.FOREST_FLOWERS);
        Holder<ConfiguredFeature<?, ?>> skyfog = holdergetter.getOrThrow(AbyssEdgeFeatures.TREES_SKYFOG);
        Holder<ConfiguredFeature<?, ?>> skyfog_and_azalea = holdergetter.getOrThrow(AbyssEdgeFeatures.TREES_SKYFOG_AND_AZALEA);
        Holder<ConfiguredFeature<?, ?>> prasiolite_cluster = holdergetter.getOrThrow(AbyssEdgeFeatures.PRASIOLITE_CLUSTER);
        Holder<ConfiguredFeature<?, ?>> big_prasiolite_cluster = holdergetter.getOrThrow(AbyssEdgeFeatures.BIG_PRASIOLITE_CLUSTER);
        Holder<ConfiguredFeature<?, ?>> prasiolite_geode = holdergetter.getOrThrow(AbyssEdgeFeatures.PRASIOLITE_GEODE);
        Holder<ConfiguredFeature<?, ?>> cave_pillar = holdergetter.getOrThrow(AbyssEdgeFeatures.CAVE_PILLAR);
        Holder<ConfiguredFeature<?, ?>> raw_iron = holdergetter.getOrThrow(AbyssEdgeFeatures.RAW_IRON);

        PlacementUtils.register(
                context, MONSTER_CHEAT, monster_cheat,
                RarityFilter.onAverageOnceEvery(3),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(280)),
                BiomeFilter.biome()
        );
        PlacementUtils.register(
                context, SLAB_RUINS, slab_ruins,
                RarityFilter.onAverageOnceEvery(2),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(320)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, SPRING_WATER, spring_water,
                CountPlacement.of(10),
                InSquarePlacement.spread(),
                ABYSS_BRINK_CAVE_HEIGHT,
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
                CountPlacement.of(UniformInt.of(104, 157)),
                ABYSS_BRINK_HEIGHT,
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
                context, PATCH_LARGE_FERN, large_fern,
                RarityFilter.onAverageOnceEvery(5),
                CountOnEveryLayerPlacement.of(1),
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
                RarityFilter.onAverageOnceEvery(3),
                CountPlacement.of(47),
                InSquarePlacement.spread(),
                ABYSS_BRINK_HEIGHT,
                EnvironmentScanPlacement.scanningFor(Direction.UP, BlockPredicate.hasSturdyFace(Direction.DOWN), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
                BiomeFilter.biome()
        );


        MiaPlacementUtils.register(context, FLOWER_MEADOW, flower_meadow, CountOnEveryLayerPlacement.of(1), BiomeFilter.biome());
        MiaPlacementUtils.register(
                context,
                FOREST_FLOWERS,
                forest_flowers,
                RarityFilter.onAverageOnceEvery(5),
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
                context, ABYSS_TREES, skyfog_and_azalea, abyssTreePlace(8, 5)
        );
        MiaPlacementUtils.register(
                context, TREES_SKYFOG, skyfog, abyssTreePlace(1, 3)
        );
        MiaPlacementUtils.register(
                context, TREES_SKYFOG_AND_AZALEA, skyfog_and_azalea, abyssTreePlace(8)
        );

        MiaPlacementUtils.register(
                context, PRASIOLITE_CLUSTER, prasiolite_cluster,
                CountPlacement.of(UniformInt.of(64, 128)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(300)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, BIG_PRASIOLITE_CLUSTER, big_prasiolite_cluster,
                CountPlacement.of(UniformInt.of(2, 4)),
                InSquarePlacement.spread(),
                ABYSS_BRINK_CAVE_HEIGHT,
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
                .add(CountOnEveryLayerPlacement.of(count))
                .add(BiomeFilter.biome());

    }

    private static ImmutableList.Builder<PlacementModifier> abyssTreePlacementBase(int count, int rarity) {
        return ImmutableList.<PlacementModifier>builder()
                .add(CountOnEveryLayerPlacement.of(count))
                .add(RarityFilter.onAverageOnceEvery(rarity))
                .add(BiomeFilter.biome());

    }

    public static List<PlacementModifier> abyssTreePlace(int count) {
        return abyssTreePlacementBase(count).build();
    }

    public static List<PlacementModifier> abyssTreePlace(int count, int rarity) {
        return abyssTreePlacementBase(count, rarity).build();
    }
}
