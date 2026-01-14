package com.altnoir.mia.worldgen.structure;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.AncientCityStructurePieces;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MiaStructures {
    public static final ResourceKey<Structure> ABYSS_STRONGHOLD = createKey("abyss_stronghold");

    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biome = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> templatePool = context.lookup(Registries.TEMPLATE_POOL);

        context.register(
                ABYSS_STRONGHOLD,
                new JigsawStructure(
                        new Structure.StructureSettings.Builder(biome.getOrThrow(BiomeTags.IS_OVERWORLD))
                                .spawnOverrides(
                                        Arrays.stream(MobCategory.values())
                                                .collect(
                                                        Collectors.toMap(
                                                                mobCategory -> mobCategory,
                                                                mobCategory -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create())
                                                        )
                                                )
                                )
                                .generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION)
                                .terrainAdapation(TerrainAdjustment.BURY)
                                .build(),
                        templatePool.getOrThrow(AncientCityStructurePieces.START),
                        Optional.of(ResourceLocation.withDefaultNamespace("city_anchor")),
                        7,
                        ConstantHeight.of(VerticalAnchor.absolute(-27)),
                        false,
                        Optional.empty(),
                        116,
                        List.of(),
                        JigsawStructure.DEFAULT_DIMENSION_PADDING,
                        JigsawStructure.DEFAULT_LIQUID_SETTINGS
                )
        );
    }

    private static ResourceKey<Structure> createKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, MiaUtil.miaId(name));
    }
}
