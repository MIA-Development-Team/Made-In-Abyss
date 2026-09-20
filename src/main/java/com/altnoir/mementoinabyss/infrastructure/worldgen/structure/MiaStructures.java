package com.altnoir.mementoinabyss.infrastructure.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaTags;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public final class MiaStructures {
    public static final ResourceKey<Structure> STAR_COMPASS_RUINS = key("star_compass_ruins");
    public static final ResourceKey<Structure> ABYSS_STRONGHOLD = key("abyss_stronghold");
    public static final ResourceKey<Structure> ABYSSAL_RUINS_01 = key("abyssal_ruins_01");
    public static final ResourceKey<Structure> ABYSSAL_RUINS_02 = key("abyssal_ruins_02");
    public static final ResourceKey<Structure> ABYSSAL_RUINS_03 = key("abyssal_ruins_03");
    public static final ResourceKey<Structure> ABYSSAL_RUINS_04 = key("abyssal_ruins_04");
    public static final ResourceKey<Structure> ABYSSAL_RUINS_05 = key("abyssal_ruins_05");
    public static final ResourceKey<Structure> ABYSSAL_RUINS_06 = key("abyssal_ruins_06");
    public static final ResourceKey<Structure> CAVE_RAIDER_HUT = key("cave_raider_hut");
    public static final ResourceKey<Structure> RUINED_CAVE_RAIDER_HUT =
            key("ruined_cave_raider_hut");
    public static final ResourceKey<Structure> FISHERMAN_HUT = key("fisherman_hut");

    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        context.register(
                STAR_COMPASS_RUINS,
                new JigsawStructure(
                        new Structure.StructureSettings.Builder(
                                        biomes.getOrThrow(
                                                MiaTags.BiomeTags.HAS_STAR_COMPASS_TEMPLE.tag))
                                .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                                .spawnOverrides(
                                        Map.of(
                                                MobCategory.MONSTER,
                                                new StructureSpawnOverride(
                                                        StructureSpawnOverride.BoundingBoxType
                                                                .STRUCTURE,
                                                        WeightedList
                                                                .<MobSpawnSettings.SpawnerData>
                                                                        builder()
                                                                .add(
                                                                        new MobSpawnSettings
                                                                                .SpawnerData(
                                                                                EntityType.ZOMBIE,
                                                                                1,
                                                                                1),
                                                                        1)
                                                                .add(
                                                                        new MobSpawnSettings
                                                                                .SpawnerData(
                                                                                EntityType.SKELETON,
                                                                                1,
                                                                                1),
                                                                        1)
                                                                .add(
                                                                        new MobSpawnSettings
                                                                                .SpawnerData(
                                                                                EntityType.SLIME,
                                                                                1,
                                                                                1),
                                                                        3)
                                                                .build())))
                                .build(),
                        pools.getOrThrow(MiaStructurePools.STAR_COMPASS_RUINS),
                        1,
                        ConstantHeight.of(VerticalAnchor.absolute(0)),
                        true,
                        Heightmap.Types.WORLD_SURFACE_WG));

        context.register(
                ABYSS_STRONGHOLD,
                new MiaJigsawStructure(
                        new Structure.StructureSettings.Builder(
                                        biomes.getOrThrow(MiaTags.BiomeTags.HAS_ISLAND.tag))
                                .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                                .terrainAdapation(TerrainAdjustment.ENCAPSULATE)
                                .spawnOverrides(
                                        Arrays.stream(MobCategory.values())
                                                .collect(
                                                        Collectors.toMap(
                                                                category -> category,
                                                                category ->
                                                                        new StructureSpawnOverride(
                                                                                StructureSpawnOverride
                                                                                        .BoundingBoxType
                                                                                        .PIECE,
                                                                                WeightedList
                                                                                        .of()))))
                                .build(),
                        pools.getOrThrow(MiaStructurePools.ABYSS_STRONGHOLD),
                        Optional.empty(),
                        14,
                        ConstantHeight.of(VerticalAnchor.absolute(-50)),
                        false,
                        Optional.empty(),
                        new JigsawStructure.MaxDistance(230),
                        List.of(),
                        new DimensionPadding(10),
                        LiquidSettings.IGNORE_WATERLOGGING));

        registerCaveFloorStructure(
                context,
                biomes,
                pools,
                ABYSSAL_RUINS_01,
                MiaTags.BiomeTags.HAS_ABYSSAL_RUINS.tag,
                MiaStructurePools.ABYSSAL_RUINS_01,
                9);
        registerCaveFloorStructure(
                context,
                biomes,
                pools,
                ABYSSAL_RUINS_02,
                MiaTags.BiomeTags.HAS_ABYSSAL_RUINS.tag,
                MiaStructurePools.ABYSSAL_RUINS_02,
                7);
        registerCaveFloorStructure(
                context,
                biomes,
                pools,
                ABYSSAL_RUINS_03,
                MiaTags.BiomeTags.HAS_ABYSSAL_RUINS.tag,
                MiaStructurePools.ABYSSAL_RUINS_03,
                9);
        registerCaveFloorStructure(
                context,
                biomes,
                pools,
                ABYSSAL_RUINS_04,
                MiaTags.BiomeTags.HAS_ABYSSAL_RUINS.tag,
                MiaStructurePools.ABYSSAL_RUINS_04,
                8);
        registerCaveFloorStructure(
                context,
                biomes,
                pools,
                ABYSSAL_RUINS_05,
                MiaTags.BiomeTags.HAS_ABYSSAL_RUINS.tag,
                MiaStructurePools.ABYSSAL_RUINS_05,
                9);
        registerCaveFloorStructure(
                context,
                biomes,
                pools,
                ABYSSAL_RUINS_06,
                MiaTags.BiomeTags.HAS_ABYSSAL_RUINS.tag,
                MiaStructurePools.ABYSSAL_RUINS_06,
                10);
        registerCaveFloorStructure(
                context,
                biomes,
                pools,
                CAVE_RAIDER_HUT,
                MiaTags.BiomeTags.HAS_CAVE_RAIDER_HUT.tag,
                MiaStructurePools.CAVE_RAIDER_HUT_START,
                10);
        registerHeightCappedSurfaceStructure(
                context,
                biomes,
                pools,
                RUINED_CAVE_RAIDER_HUT,
                MiaTags.BiomeTags.HAS_RUINED_CAVE_RAIDER_HUT.tag,
                MiaStructurePools.RUINED_CAVE_RAIDER_HUT);
        registerHeightCappedSurfaceStructure(
                context,
                biomes,
                pools,
                FISHERMAN_HUT,
                MiaTags.BiomeTags.HAS_FISHERMAN_HUT.tag,
                MiaStructurePools.FISHERMAN_HUT);
    }

    private static void registerCaveFloorStructure(
            BootstrapContext<Structure> context,
            HolderGetter<Biome> biomes,
            HolderGetter<StructureTemplatePool> templatePools,
            ResourceKey<Structure> structureKey,
            TagKey<Biome> biomeTag,
            ResourceKey<StructureTemplatePool> startPool,
            int clearance) {
        context.register(
                structureKey,
                new MiaJigsawStructure(
                        new Structure.StructureSettings.Builder(biomes.getOrThrow(biomeTag))
                                .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                                .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                                .build(),
                        templatePools.getOrThrow(startPool),
                        Optional.empty(),
                        1,
                        ConstantHeight.of(VerticalAnchor.absolute(374)),
                        true,
                        Optional.empty(),
                        Optional.of(
                                new CaveFloorSearch(VerticalAnchor.aboveBottom(16), clearance, 16)),
                        new JigsawStructure.MaxDistance(80),
                        List.of(),
                        DimensionPadding.ZERO,
                        LiquidSettings.APPLY_WATERLOGGING));
    }

    private static void registerHeightCappedSurfaceStructure(
            BootstrapContext<Structure> context,
            HolderGetter<Biome> biomes,
            HolderGetter<StructureTemplatePool> templatePools,
            ResourceKey<Structure> structureKey,
            TagKey<Biome> biomeTag,
            ResourceKey<StructureTemplatePool> startPool) {
        context.register(
                structureKey,
                new MiaJigsawStructure(
                        new Structure.StructureSettings.Builder(biomes.getOrThrow(biomeTag))
                                .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                                .build(),
                        templatePools.getOrThrow(startPool),
                        Optional.empty(),
                        1,
                        ConstantHeight.of(VerticalAnchor.absolute(0)),
                        true,
                        Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                        Optional.empty(),
                        Optional.of(VerticalAnchor.absolute(375)),
                        new JigsawStructure.MaxDistance(80),
                        List.of(),
                        DimensionPadding.ZERO,
                        LiquidSettings.APPLY_WATERLOGGING));
    }

    private static ResourceKey<Structure> key(String path) {
        return ResourceKey.create(Registries.STRUCTURE, MementoInAbyss.asResource(path));
    }

    private MiaStructures() {}
}
