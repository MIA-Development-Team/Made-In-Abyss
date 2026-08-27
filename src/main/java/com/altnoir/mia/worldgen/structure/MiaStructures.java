package com.altnoir.mia.worldgen.structure;

import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.structure.pools.AbyssStrongholdPools;
import com.altnoir.mia.worldgen.structure.pools.AbyssWindmillPools;
import com.altnoir.mia.worldgen.structure.pools.CompassRuinsPools;
import com.altnoir.mia.worldgen.structure.pools.PetrifiedShipPools;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallPlanConfig;
import com.altnoir.mia.worldgen.structure.wall.AbyssWindmillStructure;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MiaStructures {
    public static final ResourceKey<Structure> ANCIENT_BABYLON_COMPASS_RUINS = createKey("ancient_babylon_compass_ruins");
    public static final ResourceKey<Structure> ANCIENT_MAYA_COMPASS_RUINS = createKey("ancient_maya_compass_ruins");
    public static final ResourceKey<Structure> ANCIENT_ROMAN_COMPASS_RUINS = createKey("ancient_roman_compass_ruins");
    public static final ResourceKey<Structure> ANCIENT_TRIAL_COMPASS_RUINS = createKey("ancient_trial_compass_ruins");
    public static final ResourceKey<Structure> ANCIENT_ANGKOR_COMPASS_RUINS = createKey("ancient_angkor_compass_ruins");
    public static final ResourceKey<Structure> PETRIFIED_SHIP = createKey("petrified_ship");
    public static final ResourceKey<Structure> ABYSS_STRONGHOLD = createKey("abyss_stronghold");
    public static final ResourceKey<Structure> ABYSS_WINDMILL = createKey("abyss_windmill");

    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biome = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> templatePool = context.lookup(Registries.TEMPLATE_POOL);

        registerSurfaceCompassRuins(
                context,
                biome,
                templatePool,
                ANCIENT_BABYLON_COMPASS_RUINS,
                MiaTags.Biomes.HAS_ANCIENT_BABYLON_COMPASS_RUINS,
                CompassRuinsPools.ANCIENT_BABYLON
        );
        registerSurfaceCompassRuins(
                context,
                biome,
                templatePool,
                ANCIENT_MAYA_COMPASS_RUINS,
                MiaTags.Biomes.HAS_ANCIENT_MAYA_COMPASS_RUINS,
                CompassRuinsPools.ANCIENT_MAYA
        );
        registerSurfaceCompassRuins(
                context,
                biome,
                templatePool,
                ANCIENT_ROMAN_COMPASS_RUINS,
                MiaTags.Biomes.HAS_ANCIENT_ROMAN_COMPASS_RUINS,
                CompassRuinsPools.ANCIENT_ROMAN
        );
        registerSurfaceCompassRuins(
                context,
                biome,
                templatePool,
                ANCIENT_ANGKOR_COMPASS_RUINS,
                MiaTags.Biomes.HAS_ANCIENT_ANGKOR_COMPASS_RUINS,
                CompassRuinsPools.ANCIENT_ANGKOR
        );
        context.register(
                ANCIENT_TRIAL_COMPASS_RUINS,
                new JigsawStructure(
                        new Structure.StructureSettings.Builder(biome.getOrThrow(MiaTags.Biomes.HAS_ANCIENT_TRIAL_COMPASS_RUINS))
                                .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                                .terrainAdapation(TerrainAdjustment.ENCAPSULATE)
                                .spawnOverrides(emptySpawnOverrides())
                                .build(),
                        templatePool.getOrThrow(CompassRuinsPools.ANCIENT_TRIAL),
                        Optional.empty(),
                        1,
                        UniformHeight.of(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(-20)),
                        false,
                        Optional.empty(),
                        116,
                        List.of(),
                        new DimensionPadding(10),
                        LiquidSettings.IGNORE_WATERLOGGING
                )
        );
        context.register(
                PETRIFIED_SHIP,
                new MiaJigsawStructure(
                        new Structure.StructureSettings.Builder(biome.getOrThrow(MiaTags.Biomes.HAS_PETRIFIED_SHIP))
                                .generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                                .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                                .build(),
                        templatePool.getOrThrow(PetrifiedShipPools.START),
                        Optional.empty(),
                        8,
                        ConstantHeight.of(VerticalAnchor.absolute(0)),
                        false,
                        Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                        128,
                        List.of(),
                        DimensionPadding.ZERO,
                        LiquidSettings.IGNORE_WATERLOGGING
                )
        );
        context.register(
                ABYSS_STRONGHOLD,
                new MiaJigsawStructure(
                        new Structure.StructureSettings.Builder(biome.getOrThrow(MiaTags.Biomes.HAS_ISLAND))
                                .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                                .terrainAdapation(TerrainAdjustment.ENCAPSULATE)
                                .spawnOverrides(emptySpawnOverrides())
                                .build(),
                        templatePool.getOrThrow(AbyssStrongholdPools.START),
                        Optional.empty(),
                        14,
                        ConstantHeight.of(VerticalAnchor.absolute(-50)),
                        false,
                        Optional.empty(),
                        230,
                        List.of(),
                        new DimensionPadding(10),
                        LiquidSettings.IGNORE_WATERLOGGING
                )
        );
        context.register(
                ABYSS_WINDMILL,
                new AbyssWindmillStructure(
                        new Structure.StructureSettings.Builder(biome.getOrThrow(MiaTags.Biomes.HAS_ABYSS_WINDMILL))
                                .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                                .terrainAdapation(TerrainAdjustment.NONE)
                                .build(),
                        templatePool.getOrThrow(AbyssWindmillPools.STRAIGHT),
                        templatePool.getOrThrow(AbyssWindmillPools.TILT),
                        MiaUtil.miaId("abyss_wall_anchor"),
                        AbyssWallPlanConfig.DEFAULT
                )
        );
    }

    private static ResourceKey<Structure> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, MiaUtil.miaId(name));
    }

    private static void registerSurfaceCompassRuins(
            BootstrapContext<Structure> context,
            HolderGetter<Biome> biomes,
            HolderGetter<StructureTemplatePool> templatePools,
            ResourceKey<Structure> structureKey,
            TagKey<Biome> biomeTag,
            ResourceKey<StructureTemplatePool> startPool
    ) {
        context.register(
                structureKey,
                new JigsawStructure(
                        new Structure.StructureSettings.Builder(biomes.getOrThrow(biomeTag))
                                .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                                .spawnOverrides(surfaceSpawnOverrides())
                                .build(),
                        templatePools.getOrThrow(startPool),
                        1,
                        ConstantHeight.of(VerticalAnchor.absolute(0)),
                        true,
                        Heightmap.Types.WORLD_SURFACE_WG
                )
        );
    }

    private static Map<MobCategory, StructureSpawnOverride> surfaceSpawnOverrides() {
        return Map.of(
                MobCategory.MONSTER,
                new StructureSpawnOverride(
                        StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                        WeightedRandomList.create(
                                new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 1, 1, 1),
                                new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 1, 1, 1),
                                new MobSpawnSettings.SpawnerData(EntityType.SLIME, 3, 1, 1)
                        )
                )
        );
    }

    private static Map<MobCategory, StructureSpawnOverride> emptySpawnOverrides() {
        return Arrays.stream(MobCategory.values())
                .collect(Collectors.toMap(
                        mobCategory -> mobCategory,
                        mobCategory -> new StructureSpawnOverride(
                                StructureSpawnOverride.BoundingBoxType.PIECE,
                                WeightedRandomList.create()
                        )
                ));
    }
}
