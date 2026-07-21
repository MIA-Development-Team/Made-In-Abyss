package com.altnoir.mementoinabyss.worldgen.biome;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import com.altnoir.mementoinabyss.worldgen.feature.MiaAbyssPlacements;

import java.util.List;

public final class MiaBiomes {
    public static final ResourceKey<Biome> THE_ABYSS = key("the_abyss");
    public static final ResourceKey<Biome> SKYFOG_FOREST = key("skyfog_forest");
    public static final ResourceKey<Biome> DENSE_SKYFOG_FOREST = key("dense_skyfog_forest");
    public static final ResourceKey<Biome> FOSSILIZED_FOREST = key("fossilized_forest");
    public static final ResourceKey<Biome> RICH_FOSSILIZED_FOREST = key("rich_fossilized_forest");
    public static final ResourceKey<Biome> UNDER_FOSSILIZED_FOREST = key("under_fossilized_forest");
    public static final ResourceKey<Biome> ABYSS_PLAINS = key("abyss_plains");
    public static final ResourceKey<Biome> PRASIOLITE_CAVES = key("prasiolite_caves");
    public static final ResourceKey<Biome> ABYSS_LUSH_CAVES = key("abyss_lush_caves");
    public static final ResourceKey<Biome> ABYSS_DRIPSTONE_CAVES = key("abyss_dripstone_caves");
    public static final ResourceKey<Biome> TEMPTATION_FOREST = key("temptation_forest");
    public static final ResourceKey<Biome> INVERTED_FOREST = key("inverted_forest");
    public static final ResourceKey<Biome> THE_GREAT_FAULT = greatFaultKey("the_great_fault");
    public static final ResourceKey<Biome> GREAT_FAULT = greatFaultKey("great_fault");

    public static final List<ResourceKey<Biome>> FIRST_LAYERS = List.of(
            THE_ABYSS, SKYFOG_FOREST, DENSE_SKYFOG_FOREST, FOSSILIZED_FOREST,
            RICH_FOSSILIZED_FOREST, UNDER_FOSSILIZED_FOREST, ABYSS_PLAINS,
            PRASIOLITE_CAVES, ABYSS_LUSH_CAVES, ABYSS_DRIPSTONE_CAVES,
            TEMPTATION_FOREST, INVERTED_FOREST);

    public static void bootstrap(BootstrapContext<Biome> context) {
        for (ResourceKey<Biome> biome : FIRST_LAYERS) {
            context.register(biome, create(context, biome));
        }
        context.register(THE_GREAT_FAULT, createGreatFault(context, true));
        context.register(GREAT_FAULT, createGreatFault(context, false));
    }

    private static Biome create(BootstrapContext<Biome> context, ResourceKey<Biome> biome) {
        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE),
                context.lookup(Registries.CONFIGURED_CARVER));
        generationBuilder
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_DIRT)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_GRAVEL)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_IRON)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_COPPER)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_GOLD)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_LAPIS)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_REDSTONE)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_DIAMOND)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_EMERALD)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_QUARTZ)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_CHLOROPHYTE)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.LONG_VINES)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.GLOW_LICHEN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_WATERLILY)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_STRUCTURES, MiaAbyssPlacements.SUN_STONE);
        switch (AbyssBiome.from(biome)) {
            case THE_ABYSS -> {
                addGeode(generationBuilder);
                addLayerOneMeadow(generationBuilder);
                addLargeFern(generationBuilder);
                generationBuilder
                        .addFeature(GenerationStep.Decoration.LAKES, MiaAbyssPlacements.LAKE_WATER)
                        .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_SUNFLOWER);
            }
            case SKYFOG_FOREST -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addLayerOneMeadow(generationBuilder);
                addRawIron(generationBuilder); addLargeFern(generationBuilder); addCaveVines(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.TREES_SKYFOG);
            }
            case DENSE_SKYFOG_FOREST -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addDenseMeadow(generationBuilder);
                addCaveVines(generationBuilder); addTree(generationBuilder, MiaAbyssPlacements.DENSE_TREES_SKYFOG);
            }
            case FOSSILIZED_FOREST -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addLayerOneMeadow(generationBuilder);
                addRawIron(generationBuilder); addLargeFern(generationBuilder); addCaveVines(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.TREES_FOSSILIZED);
            }
            case RICH_FOSSILIZED_FOREST -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addPlainGrass(generationBuilder);
                addCaveVines(generationBuilder); addTree(generationBuilder, MiaAbyssPlacements.TREES_FOSSILIZED);
                generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.POOL_WITH_REED);
            }
            case UNDER_FOSSILIZED_FOREST -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addLayerOneMeadow(generationBuilder);
                addRawIron(generationBuilder); addLargeFern(generationBuilder); addCaveVines(generationBuilder);
                generationBuilder
                        .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.CAVES_CEILING_VEGETATION)
                        .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.SPORE_BLOSSOM)
                        .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.TREES_FOSSILIZED_UNDER)
                        .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.TREES_FOSSILIZED_UNDER_CEILING);
            }
            case ABYSS_PLAINS -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addLayerOneMeadow(generationBuilder);
                addRawIron(generationBuilder); addLargeFern(generationBuilder); addCaveVines(generationBuilder);
            }
            case PRASIOLITE_CAVES -> {
                addCustomPlants(generationBuilder); addLayerOneMeadow(generationBuilder); addLargeFern(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.TREES_SKYFOG);
                generationBuilder
                        .addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiaAbyssPlacements.PRASIOLITE_CLUSTER)
                        .addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiaAbyssPlacements.BIG_PRASIOLITE_CLUSTER);
            }
            case ABYSS_LUSH_CAVES -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addLayerOneMeadow(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.TREES_SKYFOG);
            }
            case ABYSS_DRIPSTONE_CAVES -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addLayerOneMeadow(generationBuilder);
            }
            case TEMPTATION_FOREST -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addLayerTwoMeadow(generationBuilder);
                addRawIron(generationBuilder); addLargeFern(generationBuilder); addCaveVines(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.TREES_VERDANT_FUNGUS);
            }
            case INVERTED_FOREST -> {
                addGeode(generationBuilder); addCustomPlants(generationBuilder); addLayerTwoMeadow(generationBuilder);
                addCaveVines(generationBuilder); addTree(generationBuilder, MiaAbyssPlacements.TREES_INVERTED);
            }
        }
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(2.0F)
                .downfall(0.8F)
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(generationBuilder.build())
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(6141935)
                        .foliageColorOverride(11335504)
                        .grassColorOverride(11335504)
                        .build())
                .build();
    }

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, MementoInAbyss.asResource("the_abyss/" + path));
    }

    private static ResourceKey<Biome> greatFaultKey(String path) {
        return ResourceKey.create(Registries.BIOME, MementoInAbyss.asResource("great_fault/" + path));
    }

    private static Biome createGreatFault(BootstrapContext<Biome> context, boolean inside) {
        BiomeGenerationSettings.Builder generationBuilder = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
        if (inside) {
            generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                    com.altnoir.mementoinabyss.worldgen.feature.GreatFaultPlacements.ABYSS_LIGHT);
        } else {
            generationBuilder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                    com.altnoir.mementoinabyss.worldgen.feature.GreatFaultPlacements.CAERULITE_GEODE);
            generationBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION,
                    MiaAbyssPlacements.PATCH_LARGE_FERN);
        }
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(0.8F)
                .downfall(0.8F)
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(generationBuilder.build())
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .waterColor(6141935)
                        .foliageColorOverride(11335504)
                        .grassColorOverride(11335504)
                        .build())
                .build();
    }

    private static void addGeode(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiaAbyssPlacements.PRASIOLITE_GEODE);
    }

    private static void addRawIron(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiaAbyssPlacements.RAW_IRON);
    }

    private static void addCustomPlants(BiomeGenerationSettings.Builder builder) {
        builder
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_MARGINAL_WEED)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_BALLOON_PLANT)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_LANTERN_PLANT)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_GREEN_PERILLA);
    }

    private static void addPlainGrass(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_GRASS_PLAIN);
    }

    private static void addLayerOneMeadow(BiomeGenerationSettings.Builder builder) {
        addPlainGrass(builder);
        builder
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.FLOWER_MEADOW_LAYER1)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.FOREST_FLOWERS)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_GLOOM_BERRY);
    }

    private static void addLayerTwoMeadow(BiomeGenerationSettings.Builder builder) {
        addPlainGrass(builder);
        builder
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.FLOWER_MEADOW_LAYER2)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.FOREST_FLOWERS);
    }

    private static void addDenseMeadow(BiomeGenerationSettings.Builder builder) {
        builder
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_GRASS_FERN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_DENSE_LARGE_FERN)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.FLOWER_MEADOW_LAYER1)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.FOREST_FLOWERS)
                .addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_GLOOM_BERRY);
    }

    private static void addLargeFern(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_LARGE_FERN);
    }

    private static void addCaveVines(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.CAVE_VINES);
    }

    private static void addTree(BiomeGenerationSettings.Builder builder, ResourceKey<PlacedFeature> tree) {
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, tree);
    }

    private enum AbyssBiome {
        THE_ABYSS(MiaBiomes.THE_ABYSS),
        SKYFOG_FOREST(MiaBiomes.SKYFOG_FOREST),
        DENSE_SKYFOG_FOREST(MiaBiomes.DENSE_SKYFOG_FOREST),
        FOSSILIZED_FOREST(MiaBiomes.FOSSILIZED_FOREST),
        RICH_FOSSILIZED_FOREST(MiaBiomes.RICH_FOSSILIZED_FOREST),
        UNDER_FOSSILIZED_FOREST(MiaBiomes.UNDER_FOSSILIZED_FOREST),
        ABYSS_PLAINS(MiaBiomes.ABYSS_PLAINS),
        PRASIOLITE_CAVES(MiaBiomes.PRASIOLITE_CAVES),
        ABYSS_LUSH_CAVES(MiaBiomes.ABYSS_LUSH_CAVES),
        ABYSS_DRIPSTONE_CAVES(MiaBiomes.ABYSS_DRIPSTONE_CAVES),
        TEMPTATION_FOREST(MiaBiomes.TEMPTATION_FOREST),
        INVERTED_FOREST(MiaBiomes.INVERTED_FOREST);

        private final ResourceKey<Biome> key;

        AbyssBiome(ResourceKey<Biome> key) {
            this.key = key;
        }

        private static AbyssBiome from(ResourceKey<Biome> key) {
            for (AbyssBiome biome : values()) {
                if (biome.key.equals(key)) return biome;
            }
            throw new IllegalArgumentException("Unknown abyss biome: " + key.identifier());
        }
    }

    private MiaBiomes() {
    }
}
