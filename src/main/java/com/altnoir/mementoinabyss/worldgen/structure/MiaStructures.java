package com.altnoir.mementoinabyss.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.init.MiaTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class MiaStructures {
    public static final ResourceKey<Structure> STAR_COMPASS_RUINS = ResourceKey.create(
            Registries.STRUCTURE,
            MementoInAbyss.asResource("star_compass_ruins")
    );
    public static final ResourceKey<Structure> ABYSS_STRONGHOLD = ResourceKey.create(
            Registries.STRUCTURE,
            MementoInAbyss.asResource("abyss_stronghold")
    );

    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> pools = context.lookup(Registries.TEMPLATE_POOL);

        context.register(STAR_COMPASS_RUINS, new JigsawStructure(
                new Structure.StructureSettings.Builder(
                        biomes.getOrThrow(MiaTags.BiomeTags.HAS_STAR_COMPASS_TEMPLE.tag))
                        .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                        .spawnOverrides(Map.of(MobCategory.MONSTER, new StructureSpawnOverride(
                                StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                WeightedList.<MobSpawnSettings.SpawnerData>builder()
                                        .add(new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 1, 1), 1)
                                        .add(new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 1, 1), 1)
                                        .add(new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1), 3)
                                        .build())))
                        .build(),
                pools.getOrThrow(MiaStructurePools.STAR_COMPASS_RUINS), 1,
                ConstantHeight.of(VerticalAnchor.absolute(0)), true,
                Heightmap.Types.WORLD_SURFACE_WG));

        context.register(ABYSS_STRONGHOLD, new MiaJigsawStructure(
                new Structure.StructureSettings.Builder(biomes.getOrThrow(MiaTags.BiomeTags.HAS_ISLAND.tag))
                        .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                        .terrainAdapation(TerrainAdjustment.ENCAPSULATE)
                        .spawnOverrides(Arrays.stream(MobCategory.values()).collect(Collectors.toMap(
                                category -> category,
                                category -> new StructureSpawnOverride(
                                        StructureSpawnOverride.BoundingBoxType.PIECE,
                                        WeightedList.of()))))
                        .build(),
                pools.getOrThrow(MiaStructurePools.ABYSS_STRONGHOLD), Optional.empty(), 14,
                ConstantHeight.of(VerticalAnchor.absolute(-50)), false, Optional.empty(),
                new JigsawStructure.MaxDistance(230), List.of(), new DimensionPadding(10),
                LiquidSettings.IGNORE_WATERLOGGING));
    }

    private MiaStructures() {}
}
