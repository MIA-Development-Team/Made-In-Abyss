package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.GreatFaultPlacements;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.MiaAbyssPlacements;
import com.altnoir.mementoinabyss.init.MiaSoundEvents;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.FloatModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class MiaBiomes {
    public static final ResourceKey<Biome> OVERWORLD_ABYSS =
            ResourceKey.create(Registries.BIOME, MementoInAbyss.asResource("overworld/the_abyss"));

    public static final ResourceKey<Biome> THE_ABYSS = key("the_abyss");
    public static final ResourceKey<Biome> ABYSS_PLAINS = key("abyss_plains");
    public static final ResourceKey<Biome> PRASIOLITE_CAVES = key("prasiolite_caves");
    public static final ResourceKey<Biome> ABYSS_LUSH_CAVES = key("abyss_lush_caves");
    public static final ResourceKey<Biome> ABYSS_TUFF_CAVES = key("abyss_tuff_caves");
    public static final ResourceKey<Biome> SKYFOG_FOREST = key("skyfog_forest");
    public static final ResourceKey<Biome> DENSE_SKYFOG_FOREST = key("dense_skyfog_forest");
    public static final ResourceKey<Biome> FOSSILIZED_FOREST = key("fossilized_forest");

    public static final ResourceKey<Biome> ABYSS_RIVER = key("abyss_river");
    public static final ResourceKey<Biome> GIANT_FOSSILIZED_FOREST = key("giant_fossilized_forest");
    public static final ResourceKey<Biome> SPARSE_GIANT_FOSSILIZED_FOREST =
            key("sparse_giant_fossilized_forest");
    public static final ResourceKey<Biome> RICH_FOSSILIZED_FOREST = key("rich_fossilized_forest");
    public static final ResourceKey<Biome> UNDER_FOSSILIZED_FOREST = key("under_fossilized_forest");
    public static final ResourceKey<Biome> UNDER_RICH_FOSSILIZED_FOREST =
            key("under_rich_fossilized_forest");
    public static final ResourceKey<Biome> UNDER_WATER_FOSSILIZED_FOREST =
            key("under_water_fossilized_forest");
    public static final ResourceKey<Biome> TEMPTATION_FOREST = key("temptation_forest");
    public static final ResourceKey<Biome> TEMPTATION_PLAINS = key("temptation_plains");
    public static final ResourceKey<Biome> TEMPTATION_OCEAN = key("temptation_ocean");
    public static final ResourceKey<Biome> TEMPTATION_WARM_OCEAN = key("temptation_warm_ocean");
    public static final ResourceKey<Biome> DUSKVEIL_GARDEN = key("duskveil_garden");
    public static final ResourceKey<Biome> ABYSS_MANGROVE_SWAMP = key("abyss_mangrove_swamp");
    public static final ResourceKey<Biome> PRIMO_FOREST = key("primo_forest");
    public static final ResourceKey<Biome> PRIMO_PLAINS = key("primo_plains");

    public static final ResourceKey<Biome> INVERTED_FOREST = key("inverted_forest");

    private static final BiomeSpecialEffects ABYSS_EFFECTS =
            new BiomeSpecialEffects.Builder()
                    .waterColor(6141935)
                    .foliageColorOverride(11335504)
                    .grassColorOverride(11335504)
                    .build();
    private static final List<ResourceKey<PlacedFeature>> DUSKVEIL_GROUND_VEGETATION =
            List.of(
                    VegetationPlacements.PALE_MOSS_PATCH,
                    VegetationPlacements.PATCH_GRASS_FOREST,
                    VegetationPlacements.PATCH_PUMPKIN,
                    VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER);
    private static final List<ResourceKey<PlacedFeature>> MANGROVE_SWAMP_VEGETATION =
            List.of(
                    VegetationPlacements.TREES_MANGROVE,
                    VegetationPlacements.PATCH_GRASS_NORMAL,
                    VegetationPlacements.PATCH_DEAD_BUSH,
                    VegetationPlacements.PATCH_WATERLILY,
                    AquaticPlacements.SEAGRASS_SWAMP,
                    VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER);
    private static final List<ResourceKey<PlacedFeature>> OCEAN_SHORE_VEGETATION =
            List.of(
                    VegetationPlacements.TREES_WATER,
                    VegetationPlacements.FLOWER_DEFAULT,
                    VegetationPlacements.PATCH_GRASS_BADLANDS,
                    VegetationPlacements.PATCH_SUGAR_CANE,
                    VegetationPlacements.PATCH_FIREFLY_BUSH_NEAR_WATER);
    private static final List<ResourceKey<PlacedFeature>> WARM_OCEAN_VEGETATION =
            List.of(
                    AquaticPlacements.WARM_OCEAN_VEGETATION,
                    AquaticPlacements.SEAGRASS_WARM,
                    AquaticPlacements.SEA_PICKLE,
                    AquaticPlacements.KELP_WARM);
    private static final List<ResourceKey<PlacedFeature>> TEMPTATION_OCEAN_VEGETATION =
            List.of(AquaticPlacements.SEAGRASS_NORMAL, AquaticPlacements.KELP_COLD);

    public static final List<ResourceKey<PlacedFeature>> LAYER2_VANILLA_VEGETATION =
            Stream.of(
                            DUSKVEIL_GROUND_VEGETATION,
                            MANGROVE_SWAMP_VEGETATION,
                            OCEAN_SHORE_VEGETATION,
                            WARM_OCEAN_VEGETATION,
                            TEMPTATION_OCEAN_VEGETATION)
                    .flatMap(List::stream)
                    .distinct()
                    .toList();

    public static final ResourceKey<Biome> THE_GREAT_FAULT = greatFaultKey("the_great_fault");
    public static final ResourceKey<Biome> GREAT_FAULT = greatFaultKey("great_fault");

    public static final List<ResourceKey<Biome>> FIRST_LAYERS =
            List.of(
                    THE_ABYSS,
                    SKYFOG_FOREST,
                    DENSE_SKYFOG_FOREST,
                    FOSSILIZED_FOREST,
                    GIANT_FOSSILIZED_FOREST,
                    SPARSE_GIANT_FOSSILIZED_FOREST,
                    RICH_FOSSILIZED_FOREST,
                    UNDER_FOSSILIZED_FOREST,
                    UNDER_RICH_FOSSILIZED_FOREST,
                    UNDER_WATER_FOSSILIZED_FOREST,
                    ABYSS_PLAINS,
                    PRASIOLITE_CAVES,
                    ABYSS_LUSH_CAVES,
                    ABYSS_TUFF_CAVES,
                    TEMPTATION_FOREST,
                    TEMPTATION_PLAINS,
                    PRIMO_FOREST,
                    PRIMO_PLAINS,
                    INVERTED_FOREST,
                    ABYSS_RIVER,
                    ABYSS_MANGROVE_SWAMP,
                    DUSKVEIL_GARDEN,
                    TEMPTATION_WARM_OCEAN,
                    TEMPTATION_OCEAN);

    public static void bootstrap(BootstrapContext<Biome> context) {
        for (ResourceKey<Biome> biome : FIRST_LAYERS) {
            context.register(biome, create(context, biome, false));
        }
        context.register(OVERWORLD_ABYSS, create(context, THE_ABYSS, true));
        context.register(THE_GREAT_FAULT, createGreatFault(context, true));
        context.register(GREAT_FAULT, createGreatFault(context, false));
    }

    private static Biome create(
            BootstrapContext<Biome> context, ResourceKey<Biome> biome, boolean overworld) {
        BiomeGenerationSettings.Builder generationBuilder =
                new BiomeGenerationSettings.Builder(
                        context.lookup(Registries.PLACED_FEATURE),
                        context.lookup(Registries.CONFIGURED_CARVER));
        if (overworld) {
            generationBuilder
                    .addCarver(Carvers.CAVE)
                    .addCarver(Carvers.CAVE_EXTRA_UNDERGROUND)
                    .addCarver(Carvers.CANYON);
        }
        generationBuilder
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_DIRT)
                .addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_GRAVEL)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_IRON)
                .addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_COPPER)
                .addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_GOLD)
                .addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_LAPIS)
                .addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_REDSTONE)
                .addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_DIAMOND)
                .addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_EMERALD)
                .addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES, MiaAbyssPlacements.ORE_QUARTZ)
                .addFeature(
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        MiaAbyssPlacements.ORE_CHLOROPHYTE);
        if (biome.equals(DUSKVEIL_GARDEN)) return createDuskveilGarden(generationBuilder);
        AbyssBiome kind = AbyssBiome.from(biome);
        if (kind.defaultVegetation) {
            generationBuilder
                    .addFeature(
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            MiaAbyssPlacements.LONG_VINES)
                    .addFeature(
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            MiaAbyssPlacements.GLOW_LICHEN)
                    .addFeature(
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            MiaAbyssPlacements.PATCH_WATERLILY);
        }
        var mobs = new MobSpawnSettings.Builder();
        switch (kind) {
            case THE_ABYSS -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                if (overworld)
                    addTree(generationBuilder, MiaAbyssPlacements.OVERWORLD_TREES_SKYFOG);
                addLayerOneMeadow(generationBuilder);
                addLargeFern(generationBuilder);
                generationBuilder
                        .addFeature(GenerationStep.Decoration.LAKES, MiaAbyssPlacements.LAKE_WATER)
                        .addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                MiaAbyssPlacements.PATCH_SUNFLOWER);
                if (!overworld) {
                    addTree(generationBuilder, MiaAbyssPlacements.ABYSS_TREES_SKYFOG);
                    addTree(generationBuilder, MiaAbyssPlacements.ABYSS_FALLEN_SKYFOG_LOG);
                }
            }
            case SKYFOG_FOREST -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                addLayerOneMeadow(generationBuilder);
                addRawIron(generationBuilder);
                addLargeFern(generationBuilder);
                addCaveVines(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.TREES_SKYFOG);
                addTree(generationBuilder, MiaAbyssPlacements.FALLEN_SKYFOG_LOG);
            }
            case DENSE_SKYFOG_FOREST -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                addDenseMeadow(generationBuilder);
                addCaveVines(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.DENSE_TREES_SKYFOG);
                addTree(generationBuilder, MiaAbyssPlacements.FALLEN_SKYFOG_LOG);
            }
            case FOSSILIZED_FOREST, GIANT_FOSSILIZED_FOREST, SPARSE_GIANT_FOSSILIZED_FOREST -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                addLayerOneMeadow(generationBuilder);
                addRawIron(generationBuilder);
                addLargeFern(generationBuilder);
                addCaveVines(generationBuilder);
                addTree(
                        generationBuilder,
                        switch (kind) {
                            case GIANT_FOSSILIZED_FOREST ->
                                    MiaAbyssPlacements.GIANT_FOSSILIZED_STUMPS;
                            case SPARSE_GIANT_FOSSILIZED_FOREST ->
                                    MiaAbyssPlacements.SPARSE_GIANT_FOSSILIZED_STUMPS;
                            default -> MiaAbyssPlacements.TREES_FOSSILIZED;
                        });
                if (biome.equals(GIANT_FOSSILIZED_FOREST)
                        || biome.equals(SPARSE_GIANT_FOSSILIZED_FOREST)) {
                    addTree(generationBuilder, MiaAbyssPlacements.GIANT_FOREST_TREES_SKYFOG);
                    addTree(generationBuilder, MiaAbyssPlacements.GIANT_FOREST_TREES_FOSSILIZED);
                    addTree(generationBuilder, MiaAbyssPlacements.FALLEN_FOSSILIZED_LOG);
                }
            }
            case RICH_FOSSILIZED_FOREST, UNDER_RICH_FOSSILIZED_FOREST -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                addPlainGrass(generationBuilder);
                addCaveVines(generationBuilder);
                if (biome.equals(UNDER_RICH_FOSSILIZED_FOREST)) {
                    addTree(generationBuilder, MiaAbyssPlacements.LAYER2_TREES_FOSSILIZED);
                    addTree(generationBuilder, MiaAbyssPlacements.LAYER2_TREES_FOSSILIZED_CEILING);
                } else addTree(generationBuilder, MiaAbyssPlacements.TREES_FOSSILIZED);
                generationBuilder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.POOL_WITH_REED);
            }
            case UNDER_FOSSILIZED_FOREST, UNDER_WATER_FOSSILIZED_FOREST -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                addLayerOneMeadow(generationBuilder);
                addRawIron(generationBuilder);
                addLargeFern(generationBuilder);
                addCaveVines(generationBuilder);
                generationBuilder
                        .addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                MiaAbyssPlacements.CAVES_CEILING_VEGETATION)
                        .addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                MiaAbyssPlacements.SPORE_BLOSSOM)
                        .addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                biome.equals(UNDER_WATER_FOSSILIZED_FOREST)
                                        ? MiaAbyssPlacements.LAYER2_TREES_FOSSILIZED_WATER
                                        : MiaAbyssPlacements.TREES_FOSSILIZED_UNDER)
                        .addFeature(
                                GenerationStep.Decoration.VEGETAL_DECORATION,
                                biome.equals(UNDER_WATER_FOSSILIZED_FOREST)
                                        ? MiaAbyssPlacements.LAYER2_TREES_FOSSILIZED_WATER_CEILING
                                        : MiaAbyssPlacements.TREES_FOSSILIZED_UNDER_CEILING);
                if (biome.equals(UNDER_WATER_FOSSILIZED_FOREST)) {
                    generationBuilder.addFeature(
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            MiaAbyssPlacements.LAYER2_UNDERWATER_VEGETATION);
                }
            }
            case ABYSS_PLAINS -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                addLayerOneMeadow(generationBuilder);
                addRawIron(generationBuilder);
                addLargeFern(generationBuilder);
                addCaveVines(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.FALLEN_FOSSILIZED_LOG);
            }
            case PRASIOLITE_CAVES -> {
                addSlabRuins(generationBuilder);
                addLayerOneMeadow(generationBuilder);
                addLargeFern(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.TREES_SKYFOG);
                generationBuilder
                        .addFeature(
                                GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                                MiaAbyssPlacements.PRASIOLITE_CLUSTER)
                        .addFeature(
                                GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                                MiaAbyssPlacements.BIG_PRASIOLITE_CLUSTER);
            }
            case ABYSS_LUSH_CAVES -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                addLayerOneMeadow(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.TREES_SKYFOG);
            }
            case ABYSS_TUFF_CAVES -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                addLayerOneMeadow(generationBuilder);
            }
            case TEMPTATION_FOREST, TEMPTATION_PLAINS -> {
                addSlabRuins(generationBuilder);
                addGeode(generationBuilder);
                if (biome.equals(TEMPTATION_FOREST)) addLayerTwoMeadow(generationBuilder);
                else
                    generationBuilder.addFeature(
                            GenerationStep.Decoration.VEGETAL_DECORATION,
                            MiaAbyssPlacements.TEMPTATION_PLAINS_FLOWERS);
                addRawIron(generationBuilder);
                if (biome.equals(TEMPTATION_FOREST)) addLargeFern(generationBuilder);
                addCaveVines(generationBuilder);
                if (biome.equals(TEMPTATION_FOREST)) {
                    addTree(generationBuilder, MiaAbyssPlacements.TREES_VERDANT_FUNGUS);
                    addTree(generationBuilder, MiaAbyssPlacements.FALLEN_VERDANT_STEM);
                }
            }
            case TEMPTATION_WARM_OCEAN, TEMPTATION_OCEAN -> {
                addLayer2Vegetation(generationBuilder, OCEAN_SHORE_VEGETATION);
                if (biome.equals(TEMPTATION_WARM_OCEAN)) {
                    addLayer2Vegetation(generationBuilder, WARM_OCEAN_VEGETATION);
                    BiomeDefaultFeatures.warmOceanSpawns(mobs, 10, 4);
                } else {
                    addLayer2Vegetation(generationBuilder, TEMPTATION_OCEAN_VEGETATION);
                    BiomeDefaultFeatures.oceanSpawns(mobs, 1, 4, 10);
                }
                mobs.addSpawn(
                        MobCategory.WATER_CREATURE,
                        5,
                        new MobSpawnSettings.SpawnerData(EntityType.NAUTILUS, 1, 1));
            }
            case PRIMO_FOREST, PRIMO_PLAINS -> {
                addSlabRuins(generationBuilder);
                addLargeFern(generationBuilder);
                addCaveVines(generationBuilder);
                if (biome.equals(PRIMO_FOREST))
                    addTree(generationBuilder, MiaAbyssPlacements.TREES_PRIMO_FUNGUS);
                generationBuilder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.MYCELIUM_PATCH);
            }
            case ABYSS_MANGROVE_SWAMP -> {
                addLayer2Vegetation(generationBuilder, MANGROVE_SWAMP_VEGETATION);
                BiomeDefaultFeatures.swampSpawns(mobs, 70);
                mobs.addSpawn(
                        MobCategory.WATER_AMBIENT,
                        25,
                        new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 8, 8));
                return abyssBiome(generationBuilder, mobs)
                        .modifyAttribute(
                                EnvironmentAttributes.WATER_FOG_END_DISTANCE,
                                FloatModifier.MULTIPLY,
                                0.85F)
                        .setAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT, true)
                        .build();
            }
            case INVERTED_FOREST -> {
                addGeode(generationBuilder);
                addLayerTwoMeadow(generationBuilder);
                addCaveVines(generationBuilder);
                addTree(generationBuilder, MiaAbyssPlacements.TREES_INVERTED);
            }
            case ABYSS_RIVER -> {}
        }
        return abyssBiome(generationBuilder, mobs).build();
    }

    private static Biome createDuskveilGarden(BiomeGenerationSettings.Builder generation) {
        generation.addFeature(
                GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.TREES_DUSKVEIL);
        addTree(generation, MiaAbyssPlacements.FALLEN_DUSKVEIL_LOG);
        addLayer2Vegetation(generation, DUSKVEIL_GROUND_VEGETATION);
        var mobs = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(mobs);
        return abyssBiome(generation, mobs)
                .temperature(0.7F)
                .downfall(0.8F)
                .specialEffects(
                        new BiomeSpecialEffects.Builder()
                                .waterColor(0x5946B9)
                                .foliageColorOverride(0x619BD1)
                                .dryFoliageColorOverride(0x6E8FA8)
                                .grassColorOverride(0x6B97C7)
                                .build())
                .setAttribute(EnvironmentAttributes.FOG_COLOR, 0xFF364C67)
                .setAttribute(EnvironmentAttributes.SKY_COLOR, 0xFFB9B9B9)
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, 0xFF473894)
                .setAttribute(EnvironmentAttributes.CREAKING_ACTIVE, true)
                .setAttribute(EnvironmentAttributes.MUSIC_VOLUME, 1.0F)
                .build();
    }

    private static Biome.BiomeBuilder abyssBiome(
            BiomeGenerationSettings.Builder generation, MobSpawnSettings.Builder mobs) {
        generation.addFeature(
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES, MiaAbyssPlacements.SUN_STONE);
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(2.0F)
                .downfall(0.8F)
                .mobSpawnSettings(mobs.build())
                .generationSettings(generation.build())
                .specialEffects(ABYSS_EFFECTS)
                .setAttribute(
                        EnvironmentAttributes.BACKGROUND_MUSIC,
                        new BackgroundMusic(MiaSoundEvents.MUSIC_THE_FIRST_LAYER));
    }

    private static void addLayer2Vegetation(
            BiomeGenerationSettings.Builder generation, List<ResourceKey<PlacedFeature>> features) {
        for (var feature : features) {
            generation.addFeature(
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    MiaAbyssPlacements.layer2VegetationKey(feature));
        }
    }

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, MementoInAbyss.asResource("the_abyss/" + path));
    }

    private static ResourceKey<Biome> greatFaultKey(String path) {
        return ResourceKey.create(
                Registries.BIOME, MementoInAbyss.asResource("great_fault/" + path));
    }

    private static Biome createGreatFault(BootstrapContext<Biome> context, boolean inside) {
        BiomeGenerationSettings.Builder generationBuilder =
                new BiomeGenerationSettings.Builder(
                        context.lookup(Registries.PLACED_FEATURE),
                        context.lookup(Registries.CONFIGURED_CARVER));
        if (!inside) {
            generationBuilder.addFeature(
                    GenerationStep.Decoration.LOCAL_MODIFICATIONS,
                    GreatFaultPlacements.CAERULITE_GEODE);
            generationBuilder.addFeature(
                    GenerationStep.Decoration.VEGETAL_DECORATION,
                    MiaAbyssPlacements.PATCH_LARGE_FERN);
        }
        return new Biome.BiomeBuilder()
                .hasPrecipitation(false)
                .temperature(0.8F)
                .downfall(0.8F)
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(generationBuilder.build())
                .specialEffects(
                        new BiomeSpecialEffects.Builder()
                                .waterColor(6141935)
                                .foliageColorOverride(11335504)
                                .grassColorOverride(11335504)
                                .build())
                .build();
    }

    private static void addGeode(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(
                GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiaAbyssPlacements.PRASIOLITE_GEODE);
    }

    private static void addSlabRuins(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES, MiaAbyssPlacements.SLAB_RUINS);
    }

    private static void addRawIron(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(
                GenerationStep.Decoration.LOCAL_MODIFICATIONS, MiaAbyssPlacements.RAW_IRON);
    }

    private static void addPlainGrass(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(
                GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_GRASS_PLAIN);
    }

    private static void addLayerOneMeadow(BiomeGenerationSettings.Builder builder) {
        addPlainGrass(builder);
        builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.PATCH_GLOOM_BERRY)
                .addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.FLOWER_MEADOW_LAYER1)
                .addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.FOREST_FLOWERS);
    }

    private static void addLayerTwoMeadow(BiomeGenerationSettings.Builder builder) {
        addPlainGrass(builder);
        builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.FLOWER_MEADOW_LAYER2)
                .addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.FOREST_FLOWERS);
    }

    private static void addDenseMeadow(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.PATCH_GRASS_FERN)
                .addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.PATCH_DENSE_LARGE_FERN)
                .addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.PATCH_GLOOM_BERRY)
                .addFeature(
                        GenerationStep.Decoration.VEGETAL_DECORATION,
                        MiaAbyssPlacements.FOREST_FLOWERS);
    }

    private static void addLargeFern(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(
                GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.PATCH_LARGE_FERN);
    }

    private static void addCaveVines(BiomeGenerationSettings.Builder builder) {
        builder.addFeature(
                GenerationStep.Decoration.VEGETAL_DECORATION, MiaAbyssPlacements.CAVE_VINES);
    }

    private static void addTree(
            BiomeGenerationSettings.Builder builder, ResourceKey<PlacedFeature> tree) {
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, tree);
    }

    private enum AbyssBiome {
        THE_ABYSS(MiaBiomes.THE_ABYSS),
        SKYFOG_FOREST(MiaBiomes.SKYFOG_FOREST),
        DENSE_SKYFOG_FOREST(MiaBiomes.DENSE_SKYFOG_FOREST),
        FOSSILIZED_FOREST(MiaBiomes.FOSSILIZED_FOREST),
        GIANT_FOSSILIZED_FOREST(MiaBiomes.GIANT_FOSSILIZED_FOREST),
        SPARSE_GIANT_FOSSILIZED_FOREST(MiaBiomes.SPARSE_GIANT_FOSSILIZED_FOREST),
        RICH_FOSSILIZED_FOREST(MiaBiomes.RICH_FOSSILIZED_FOREST),
        UNDER_FOSSILIZED_FOREST(MiaBiomes.UNDER_FOSSILIZED_FOREST),
        UNDER_RICH_FOSSILIZED_FOREST(MiaBiomes.UNDER_RICH_FOSSILIZED_FOREST),
        UNDER_WATER_FOSSILIZED_FOREST(MiaBiomes.UNDER_WATER_FOSSILIZED_FOREST),
        ABYSS_PLAINS(MiaBiomes.ABYSS_PLAINS),
        PRASIOLITE_CAVES(MiaBiomes.PRASIOLITE_CAVES),
        ABYSS_LUSH_CAVES(MiaBiomes.ABYSS_LUSH_CAVES),
        ABYSS_TUFF_CAVES(MiaBiomes.ABYSS_TUFF_CAVES),
        TEMPTATION_FOREST(MiaBiomes.TEMPTATION_FOREST),
        TEMPTATION_PLAINS(MiaBiomes.TEMPTATION_PLAINS),
        PRIMO_FOREST(MiaBiomes.PRIMO_FOREST),
        PRIMO_PLAINS(MiaBiomes.PRIMO_PLAINS),
        INVERTED_FOREST(MiaBiomes.INVERTED_FOREST),
        ABYSS_RIVER(MiaBiomes.ABYSS_RIVER),
        ABYSS_MANGROVE_SWAMP(MiaBiomes.ABYSS_MANGROVE_SWAMP, false),
        TEMPTATION_WARM_OCEAN(MiaBiomes.TEMPTATION_WARM_OCEAN, false),
        TEMPTATION_OCEAN(MiaBiomes.TEMPTATION_OCEAN, false);

        private final ResourceKey<Biome> key;
        private final boolean defaultVegetation;

        AbyssBiome(ResourceKey<Biome> key) {
            this(key, true);
        }

        AbyssBiome(ResourceKey<Biome> key, boolean defaultVegetation) {
            this.key = key;
            this.defaultVegetation = defaultVegetation;
        }

        private static AbyssBiome from(ResourceKey<Biome> key) {
            for (AbyssBiome biome : values()) {
                if (biome.key.equals(key)) return biome;
            }
            throw new IllegalArgumentException("Unknown abyss biome: " + key.identifier());
        }
    }

    private MiaBiomes() {}
}
