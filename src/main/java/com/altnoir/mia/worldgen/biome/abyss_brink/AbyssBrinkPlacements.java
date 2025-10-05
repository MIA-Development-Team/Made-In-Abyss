package com.altnoir.mia.worldgen.biome.abyss_brink;

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

public class AbyssBrinkPlacements {
    public static final ResourceKey<PlacedFeature> SPRING_WATER = MiaPlacementUtils.abyssBrinkKey("spring_water");
    public static final ResourceKey<PlacedFeature> LAKE_WATER = MiaPlacementUtils.abyssBrinkKey("lake_water");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_PLAIN = MiaPlacementUtils.abyssBrinkKey("patch_grass_plain");
    public static final ResourceKey<PlacedFeature> PATCH_LARGE_FERN = MiaPlacementUtils.abyssBrinkKey("patch_large_fern");
    public static final ResourceKey<PlacedFeature> PATCH_SUNFLOWER = MiaPlacementUtils.abyssBrinkKey("patch_sunflower");
    public static final ResourceKey<PlacedFeature> CAVE_VINES = MiaPlacementUtils.abyssBrinkKey("cave_vines");
    public static final ResourceKey<PlacedFeature> FLOWER_MEADOW = MiaPlacementUtils.abyssBrinkKey("flower_meadow");
    public static final ResourceKey<PlacedFeature> FOREST_FLOWERS = MiaPlacementUtils.abyssBrinkKey("forest_flowers");
    public static final ResourceKey<PlacedFeature> TREES_SKYFOG = MiaPlacementUtils.abyssBrinkKey("trees_skyfog");
    public static final ResourceKey<PlacedFeature> TREES_SKYFOG_AND_AZALEA = MiaPlacementUtils.abyssBrinkKey("trees_skyfog_and_azalea");
    public static final ResourceKey<PlacedFeature> PRASIOLITE_CLUSTER = MiaPlacementUtils.abyssBrinkKey("prasiolite_cluster");
    public static final ResourceKey<PlacedFeature> BIG_PRASIOLITE_CLUSTER = MiaPlacementUtils.abyssBrinkKey("big_prasiolite_cluster");
    public static final ResourceKey<PlacedFeature> RAW_IRON = MiaPlacementUtils.abyssBrinkKey("raw_iron");

    public static final PlacementModifier ABYSS_BRINK_HEIGHT = HeightRangePlacement.uniform(
            VerticalAnchor.bottom(), VerticalAnchor.absolute(360)
    );

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> holdergetter = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> spring_water = holdergetter.getOrThrow(AbyssBrinkFeatures.SPRING_WATER);
        Holder<ConfiguredFeature<?, ?>> lake_water = holdergetter.getOrThrow(AbyssBrinkFeatures.LAKE_WATER);
        Holder<ConfiguredFeature<?, ?>> grass = holdergetter.getOrThrow(VegetationFeatures.PATCH_GRASS);
        Holder<ConfiguredFeature<?, ?>> large_fern = holdergetter.getOrThrow(VegetationFeatures.PATCH_LARGE_FERN);
        Holder<ConfiguredFeature<?, ?>> cave_vine = holdergetter.getOrThrow(CaveFeatures.CAVE_VINE);
        Holder<ConfiguredFeature<?, ?>> sunflower = holdergetter.getOrThrow(VegetationFeatures.PATCH_SUNFLOWER);
        Holder<ConfiguredFeature<?, ?>> flower_meadow = holdergetter.getOrThrow(AbyssBrinkFeatures.FLOWER_MEADOW);
        Holder<ConfiguredFeature<?, ?>> forest_flowers = holdergetter.getOrThrow(AbyssBrinkFeatures.FOREST_FLOWERS);
        Holder<ConfiguredFeature<?, ?>> skyfog = holdergetter.getOrThrow(AbyssBrinkFeatures.TREES_SKYFOG);
        Holder<ConfiguredFeature<?, ?>> skyfog_and_azalea = holdergetter.getOrThrow(AbyssBrinkFeatures.TREES_SKYFOG_AND_AZALEA);
        Holder<ConfiguredFeature<?, ?>> prasiolite_cluster = holdergetter.getOrThrow(AbyssBrinkFeatures.PRASIOLITE_CLUSTER);
        Holder<ConfiguredFeature<?, ?>> big_prasiolite_cluster = holdergetter.getOrThrow(AbyssBrinkFeatures.BIG_PRASIOLITE_CLUSTER);
        Holder<ConfiguredFeature<?, ?>> raw_iron = holdergetter.getOrThrow(AbyssBrinkFeatures.RAW_IRON);

        MiaPlacementUtils.register(
                context, SPRING_WATER, spring_water,
                CountPlacement.of(10),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.absolute(200)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context,
                LAKE_WATER,
                lake_water,
                RarityFilter.onAverageOnceEvery(2),
                InSquarePlacement.spread(),
                PlacementUtils.HEIGHTMAP_WORLD_SURFACE,
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
                context, TREES_SKYFOG, skyfog, abyssTreePlace(1, 3)
        );
        MiaPlacementUtils.register(
                context, TREES_SKYFOG_AND_AZALEA, skyfog_and_azalea, abyssTreePlace(8)
        );

        MiaPlacementUtils.register(
                context, PRASIOLITE_CLUSTER, prasiolite_cluster,
                CountPlacement.of(UniformInt.of(64, 128)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(300)),
                BiomeFilter.biome()
        );
        MiaPlacementUtils.register(
                context, BIG_PRASIOLITE_CLUSTER, big_prasiolite_cluster,
                CountPlacement.of(UniformInt.of(2, 4)),
                InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(300)),
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

    private static ImmutableList.Builder<PlacementModifier> treePlacementBase() {
        return ImmutableList.<PlacementModifier>builder()
                .add(InSquarePlacement.spread())
                .add(ABYSS_BRINK_HEIGHT)
                .add(RandomOffsetPlacement.vertical(ConstantInt.of(-1)))
                .add(BiomeFilter.biome());
    }

    public static List<PlacementModifier> treePlace() {
        return treePlacementBase().build();
    }

    public static List<PlacementModifier> treePlace(int count) {
        return treePlacementBase().add(RarityFilter.onAverageOnceEvery(count)).build();
    }
}
