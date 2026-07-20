package com.altnoir.mementoinabyss.worldgen.feature;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import com.altnoir.mementoinabyss.worldgen.placement.FastCountOnEveryLayerPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;

import java.util.List;
import com.altnoir.mementoinabyss.worldgen.tree.MiaTreeFeatures;
import com.altnoir.mementoinabyss.worldgen.placement.TreeOnEveryLayerPlacement;
import com.altnoir.mementoinabyss.worldgen.placement.InvertedCountOnEveryLayerPlacement;
import com.altnoir.mementoinabyss.worldgen.placement.WaterOnEveryLayerPlacement;

public final class MiaAbyssPlacements {
    public static final ResourceKey<PlacedFeature> PATCH_MARGINAL_WEED = key("patch_marginal_weed");
    public static final ResourceKey<PlacedFeature> PATCH_BALLOON_PLANT = key("patch_balloon_plant");
    public static final ResourceKey<PlacedFeature> PATCH_LANTERN_PLANT = key("patch_lantern_plant");
    public static final ResourceKey<PlacedFeature> PATCH_GREEN_PERILLA = key("patch_green_perilla");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_PLAIN = key("patch_grass_plain");
    public static final ResourceKey<PlacedFeature> PATCH_GRASS_FERN = key("patch_grass_fern");
    public static final ResourceKey<PlacedFeature> PATCH_LARGE_FERN = key("patch_large_fern");
    public static final ResourceKey<PlacedFeature> PATCH_DENSE_LARGE_FERN = key("patch_dense_large_fern");
    public static final ResourceKey<PlacedFeature> PATCH_SUNFLOWER = key("patch_sunflower");
    public static final ResourceKey<PlacedFeature> CAVE_VINES = key("cave_vines");
    public static final ResourceKey<PlacedFeature> SPORE_BLOSSOM = key("spore_blossom");
    public static final ResourceKey<PlacedFeature> FLOWER_MEADOW_LAYER1 = key("flower_meadow_layer1");
    public static final ResourceKey<PlacedFeature> FLOWER_MEADOW_LAYER2 = key("flower_meadow_layer2");
    public static final ResourceKey<PlacedFeature> FOREST_FLOWERS = key("forest_flowers");
    public static final ResourceKey<PlacedFeature> LONG_VINES = key("vines");
    public static final ResourceKey<PlacedFeature> GLOW_LICHEN = key("glow_lichen");
    public static final ResourceKey<PlacedFeature> PATCH_WATERLILY = key("patch_waterlily");
    public static final ResourceKey<PlacedFeature> PATCH_GLOOM_BERRY = key("patch_gloom_berry_plant");
    public static final ResourceKey<PlacedFeature> CAVES_CEILING_VEGETATION = key("caves_ceiling_vegetation");
    public static final ResourceKey<PlacedFeature> POOL_WITH_REED = key("pool_with_reed");
    public static final ResourceKey<PlacedFeature> SPRING_WATER = key("spring_water");
    public static final ResourceKey<PlacedFeature> LAKE_WATER = key("lake_water");
    public static final ResourceKey<PlacedFeature> RAW_IRON = key("raw_iron");
    public static final ResourceKey<PlacedFeature> SUN_STONE = key("sun_stone");
    public static final ResourceKey<PlacedFeature> PRASIOLITE_GEODE = key("prasiolite_geode");
    public static final ResourceKey<PlacedFeature> PRASIOLITE_CLUSTER = key("prasiolite_cluster");
    public static final ResourceKey<PlacedFeature> BIG_PRASIOLITE_CLUSTER = key("big_prasiolite_cluster");
    public static final ResourceKey<PlacedFeature> ORE_DIRT = key("ore_dirt");
    public static final ResourceKey<PlacedFeature> ORE_GRAVEL = key("ore_gravel");
    public static final ResourceKey<PlacedFeature> ORE_IRON = key("ore_iron");
    public static final ResourceKey<PlacedFeature> ORE_COPPER = key("ore_copper");
    public static final ResourceKey<PlacedFeature> ORE_GOLD = key("ore_gold");
    public static final ResourceKey<PlacedFeature> ORE_LAPIS = key("ore_lapis");
    public static final ResourceKey<PlacedFeature> ORE_REDSTONE = key("ore_redstone");
    public static final ResourceKey<PlacedFeature> ORE_DIAMOND = key("ore_diamond");
    public static final ResourceKey<PlacedFeature> ORE_EMERALD = key("ore_emerald");
    public static final ResourceKey<PlacedFeature> ORE_QUARTZ = key("ore_quartz");
    public static final ResourceKey<PlacedFeature> ORE_CHLOROPHYTE = key("ore_chlorophyte");
    public static final ResourceKey<PlacedFeature> TREES_SKYFOG = key("trees_skyfog");
    public static final ResourceKey<PlacedFeature> DENSE_TREES_SKYFOG = key("dense_trees_skyfog");
    public static final ResourceKey<PlacedFeature> TREES_VERDANT_FUNGUS = key("trees_verdant_fungus");
    public static final ResourceKey<PlacedFeature> TREES_INVERTED = key("trees_inverted");
    public static final ResourceKey<PlacedFeature> TREES_FOSSILIZED = key("trees_fossilized");
    public static final ResourceKey<PlacedFeature> TREES_FOSSILIZED_UNDER = key("trees_fossilized_under");
    public static final ResourceKey<PlacedFeature> TREES_FOSSILIZED_UNDER_CEILING = key("trees_fossilized_under_ceiling");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        plant(context, PATCH_MARGINAL_WEED, configured.getOrThrow(MiaAbyssFeatures.PATCH_MARGINAL_WEED), 10);
        plant(context, PATCH_BALLOON_PLANT, configured.getOrThrow(MiaAbyssFeatures.PATCH_BALLOON_PLANT), 5);
        plant(context, PATCH_LANTERN_PLANT, configured.getOrThrow(MiaAbyssFeatures.PATCH_LANTERN_PLANT), 3);
        plant(context, PATCH_GREEN_PERILLA, configured.getOrThrow(MiaAbyssFeatures.PATCH_GREEN_PERILLA), 5);
        everyLayer(context, PATCH_GRASS_PLAIN, configured.getOrThrow(vanillaConfigured("grass")), 1);
        everyLayer(context, PATCH_GRASS_FERN, configured.getOrThrow(vanillaConfigured("taiga_grass")), 1);
        context.register(PATCH_LARGE_FERN, new PlacedFeature(configured.getOrThrow(vanillaConfigured("large_fern")),
                List.of(RarityFilter.onAverageOnceEvery(5), FastCountOnEveryLayerPlacement.of(1), BiomeFilter.biome())));
        everyLayer(context, PATCH_DENSE_LARGE_FERN, configured.getOrThrow(vanillaConfigured("large_fern")), 2);
        context.register(PATCH_SUNFLOWER, new PlacedFeature(configured.getOrThrow(vanillaConfigured("sunflower")),
                List.of(RarityFilter.onAverageOnceEvery(2), InSquarePlacement.spread(), BiomeFilter.biome())));
        ceilingPlant(context, CAVE_VINES, configured.getOrThrow(vanillaConfigured("cave_vine")), 47, 12);
        ceilingPlant(context, SPORE_BLOSSOM, configured.getOrThrow(vanillaConfigured("spore_blossom")), 25, 12);
        everyLayer(context, FLOWER_MEADOW_LAYER1, configured.getOrThrow(MiaAbyssFeatures.FLOWER_MEADOW_LAYER1), 24);
        everyLayer(context, FLOWER_MEADOW_LAYER2, configured.getOrThrow(MiaAbyssFeatures.FLOWER_MEADOW_LAYER2), 24);
        context.register(FOREST_FLOWERS, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.FOREST_FLOWERS),
                List.of(RarityFilter.onAverageOnceEvery(2), FastCountOnEveryLayerPlacement.of(16), BiomeFilter.biome())));
        context.register(LONG_VINES, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.LONG_VINES), List.of(
                CountPlacement.of(127), CountPlacement.of(5), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(128), VerticalAnchor.belowTop(8)),
                BiomeFilter.biome())));
        context.register(GLOW_LICHEN, new PlacedFeature(configured.getOrThrow(vanillaConfigured("glow_lichen")), List.of(
                CountPlacement.of(UniformInt.of(157, 250)),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(128), VerticalAnchor.belowTop(64)),
                InSquarePlacement.spread(), BiomeFilter.biome())));
        context.register(PATCH_WATERLILY, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.PATCH_WATERLILY),
                List.of(WaterOnEveryLayerPlacement.of(2), BiomeFilter.biome())));
        everyLayer(context, PATCH_GLOOM_BERRY, configured.getOrThrow(MiaAbyssFeatures.PATCH_GLOOM_BERRY), 2);
        ceilingPlant(context, CAVES_CEILING_VEGETATION,
                configured.getOrThrow(vanillaConfigured("moss_patch_ceiling")), 125, 12);
        context.register(POOL_WITH_REED, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.POOL_WITH_REED), List.of(
                CountPlacement.of(62), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(128), VerticalAnchor.belowTop(8)),
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, BlockPredicate.solid(),
                        BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(1)), BiomeFilter.biome())));
        context.register(SPRING_WATER, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.SPRING_WATER), List.of(
                CountPlacement.of(10), InSquarePlacement.spread(),
                HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.top()),
                BiomeFilter.biome())));
        context.register(LAKE_WATER, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.LAKE_WATER), List.of(
                RarityFilter.onAverageOnceEvery(4), InSquarePlacement.spread(),
                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG), BiomeFilter.biome())));
        context.register(RAW_IRON, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.RAW_IRON), List.of(
                RarityFilter.onAverageOnceEvery(5), FastCountOnEveryLayerPlacement.of(1), BiomeFilter.biome())));
        ceilingPlant(context, SUN_STONE, configured.getOrThrow(MiaAbyssFeatures.SUN_STONE), 2, 12);
        context.register(PRASIOLITE_GEODE, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.PRASIOLITE_GEODE), List.of(
                RarityFilter.onAverageOnceEvery(24), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(128), VerticalAnchor.belowTop(64)),
                BiomeFilter.biome())));
        context.register(PRASIOLITE_CLUSTER, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.PRASIOLITE_CLUSTER), List.of(
                CountPlacement.of(UniformInt.of(64, 128)), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(0), VerticalAnchor.absolute(450)),
                BiomeFilter.biome())));
        context.register(BIG_PRASIOLITE_CLUSTER, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.BIG_PRASIOLITE_CLUSTER), List.of(
                CountPlacement.of(UniformInt.of(2, 4)), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(16), VerticalAnchor.absolute(400)),
                BiomeFilter.biome())));
        ore(context, ORE_DIRT, configured.getOrThrow(MiaAbyssFeatures.ORE_DIRT), 7);
        ore(context, ORE_GRAVEL, configured.getOrThrow(MiaAbyssFeatures.ORE_GRAVEL), 7);
        ore(context, ORE_IRON, configured.getOrThrow(MiaAbyssFeatures.ORE_IRON), 12);
        ore(context, ORE_COPPER, configured.getOrThrow(MiaAbyssFeatures.ORE_COPPER), 10);
        ore(context, ORE_GOLD, configured.getOrThrow(MiaAbyssFeatures.ORE_GOLD), 6);
        ore(context, ORE_LAPIS, configured.getOrThrow(MiaAbyssFeatures.ORE_LAPIS), 4);
        ore(context, ORE_REDSTONE, configured.getOrThrow(MiaAbyssFeatures.ORE_REDSTONE), 8);
        ore(context, ORE_DIAMOND, configured.getOrThrow(MiaAbyssFeatures.ORE_DIAMOND), 3);
        ore(context, ORE_EMERALD, configured.getOrThrow(MiaAbyssFeatures.ORE_EMERALD), 2);
        ore(context, ORE_QUARTZ, configured.getOrThrow(MiaAbyssFeatures.ORE_QUARTZ), 7);
        ore(context, ORE_CHLOROPHYTE, configured.getOrThrow(MiaAbyssFeatures.ORE_CHLOROPHYTE), 4);
        context.register(TREES_SKYFOG, new PlacedFeature(configured.getOrThrow(MiaTreeFeatures.SKYFOG_TREE),
                List.of(TreeOnEveryLayerPlacement.of(8), BiomeFilter.biome())));
        context.register(DENSE_TREES_SKYFOG, new PlacedFeature(configured.getOrThrow(MiaTreeFeatures.SKYFOG_TREE),
                List.of(TreeOnEveryLayerPlacement.of(16), BiomeFilter.biome())));
        context.register(TREES_VERDANT_FUNGUS, new PlacedFeature(configured.getOrThrow(MiaTreeFeatures.VERDANT_FUNGUS),
                List.of(TreeOnEveryLayerPlacement.of(8), BiomeFilter.biome())));
        context.register(TREES_INVERTED, new PlacedFeature(configured.getOrThrow(MiaTreeFeatures.INVERTED_TREE),
                List.of(InvertedCountOnEveryLayerPlacement.of(6), BiomeFilter.biome())));
        context.register(TREES_FOSSILIZED, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.TREES_FOSSILIZED),
                List.of(TreeOnEveryLayerPlacement.of(8), BiomeFilter.biome())));
        context.register(TREES_FOSSILIZED_UNDER, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.TREES_FOSSILIZED_UNDER),
                List.of(TreeOnEveryLayerPlacement.of(12), BiomeFilter.biome())));
        context.register(TREES_FOSSILIZED_UNDER_CEILING, new PlacedFeature(configured.getOrThrow(MiaAbyssFeatures.TREES_FOSSILIZED_UNDER_CEILING),
                List.of(InvertedCountOnEveryLayerPlacement.of(6), BiomeFilter.biome())));
    }

    private static void everyLayer(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                   Holder<ConfiguredFeature<?, ?>> configured, int count) {
        context.register(key, new PlacedFeature(configured,
                List.of(FastCountOnEveryLayerPlacement.of(count), BiomeFilter.biome())));
    }

    private static void ceilingPlant(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                     Holder<ConfiguredFeature<?, ?>> configured, int count, int scanDistance) {
        context.register(key, new PlacedFeature(configured, List.of(
                CountPlacement.of(count), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(128), VerticalAnchor.belowTop(8)),
                EnvironmentScanPlacement.scanningFor(Direction.UP,
                        BlockPredicate.hasSturdyFace(Direction.DOWN), BlockPredicate.ONLY_IN_AIR_PREDICATE, scanDistance),
                RandomOffsetPlacement.vertical(ConstantInt.of(-1)), BiomeFilter.biome())));
    }

    private static void plant(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                              Holder<ConfiguredFeature<?, ?>> configured, int count) {
        context.register(key, new PlacedFeature(configured, List.of(
                CountPlacement.of(count), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-240), VerticalAnchor.absolute(360)),
                BiomeFilter.biome())));
    }

    private static void ore(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                            Holder<ConfiguredFeature<?, ?>> configured, int count) {
        context.register(key, new PlacedFeature(configured, List.of(
                CountPlacement.of(count), InSquarePlacement.spread(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-240), VerticalAnchor.absolute(360)),
                BiomeFilter.biome())));
    }

    private static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, MementoInAbyss.asResource("the_abyss/" + name));
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> vanillaConfigured(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE,
                net.minecraft.resources.Identifier.withDefaultNamespace(name));
    }

    private MiaAbyssPlacements() {}
}
