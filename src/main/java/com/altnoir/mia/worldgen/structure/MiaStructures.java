package com.altnoir.mia.worldgen.structure;

import com.altnoir.mia.init.MiaBlocks;
import com.altnoir.mia.init.MiaTags;
import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.structure.pools.AbyssStrongholdPools;
import com.altnoir.mia.worldgen.structure.pools.StarCompassRuinsPools;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedRandomList;
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

public class MiaStructures {
    public static final ResourceKey<Structure> STAR_COMPASS_RUINS = createKey("star_compass_ruins");
    public static final ResourceKey<Structure> ABYSS_STRONGHOLD = createKey("abyss_stronghold");

    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biome = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> templatePool = context.lookup(Registries.TEMPLATE_POOL);

        context.register(
                STAR_COMPASS_RUINS,
                new JigsawStructure(
                        new Structure.StructureSettings.Builder(biome.getOrThrow(MiaTags.Biomes.HAS_STAR_COMPASS_TEMPLE))
                                .terrainAdapation(TerrainAdjustment.BEARD_THIN)
                                .spawnOverrides(
                                        Map.of(
                                                MobCategory.MONSTER,
                                                new StructureSpawnOverride(
                                                        StructureSpawnOverride.BoundingBoxType.STRUCTURE,
                                                        WeightedRandomList.create(
                                                                new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 1, 1, 1),
                                                                new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 1, 1, 1),
                                                                new MobSpawnSettings.SpawnerData(EntityType.SLIME, 3, 1, 1)
                                                        )
                                                )
                                        )
                                ).build(),
                        templatePool.getOrThrow(StarCompassRuinsPools.START),
                        1,
                        ConstantHeight.of(VerticalAnchor.absolute(0)),
                        true,
                        Heightmap.Types.WORLD_SURFACE_WG
                )
        );
        context.register(
                ABYSS_STRONGHOLD,
                new MiaJigsawStructure(
                        new Structure.StructureSettings.Builder(biome.getOrThrow(MiaTags.Biomes.HAS_ISLAND))
                                .generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
                                .terrainAdapation(TerrainAdjustment.ENCAPSULATE)
                                .spawnOverrides(
                                        Arrays.stream(MobCategory.values())
                                                .collect(
                                                        Collectors.toMap(
                                                                mobCategory -> mobCategory,
                                                                mobCategory -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create())
                                                        )
                                                )
                                ).build(),
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
    }

    private static ResourceKey<Structure> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, MiaUtil.miaId(name));
    }
}
