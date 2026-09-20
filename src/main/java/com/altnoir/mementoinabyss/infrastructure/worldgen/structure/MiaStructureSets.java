package com.altnoir.mementoinabyss.infrastructure.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public final class MiaStructureSets {
    public static final ResourceKey<StructureSet> STAR_COMPASS_RUINS = key("star_compass_ruins");
    public static final ResourceKey<StructureSet> ABYSS_STRONGHOLDS = key("abyss_strongholds");
    public static final ResourceKey<StructureSet> ABYSSAL_RUINS = key("abyssal_ruins");
    public static final ResourceKey<StructureSet> CAVE_RAIDER_HUTS = key("cave_raider_huts");
    public static final ResourceKey<StructureSet> FISHERMAN_HUTS = key("fisherman_huts");

    public static void bootstrap(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        context.register(
                STAR_COMPASS_RUINS,
                new StructureSet(
                        structures.getOrThrow(MiaStructures.STAR_COMPASS_RUINS),
                        new RandomSpreadStructurePlacement(
                                32, 8, RandomSpreadType.LINEAR, 70387317)));
        context.register(
                ABYSS_STRONGHOLDS,
                new StructureSet(
                        structures.getOrThrow(MiaStructures.ABYSS_STRONGHOLD),
                        new ConcentricRingsStructurePlacement(
                                32, 11, 64, biomes.getOrThrow(BiomeTags.IS_OVERWORLD))));
        context.register(
                ABYSSAL_RUINS,
                new StructureSet(
                        List.of(
                                StructureSet.entry(
                                        structures.getOrThrow(MiaStructures.ABYSSAL_RUINS_01), 1),
                                StructureSet.entry(
                                        structures.getOrThrow(MiaStructures.ABYSSAL_RUINS_02), 1),
                                StructureSet.entry(
                                        structures.getOrThrow(MiaStructures.ABYSSAL_RUINS_03), 1),
                                StructureSet.entry(
                                        structures.getOrThrow(MiaStructures.ABYSSAL_RUINS_04), 1),
                                StructureSet.entry(
                                        structures.getOrThrow(MiaStructures.ABYSSAL_RUINS_05), 1),
                                StructureSet.entry(
                                        structures.getOrThrow(MiaStructures.ABYSSAL_RUINS_06), 1)),
                        new RandomSpreadStructurePlacement(
                                16, 4, RandomSpreadType.LINEAR, 70387321)));
        context.register(
                CAVE_RAIDER_HUTS,
                new StructureSet(
                        List.of(
                                StructureSet.entry(
                                        structures.getOrThrow(MiaStructures.CAVE_RAIDER_HUT), 1),
                                StructureSet.entry(
                                        structures.getOrThrow(MiaStructures.RUINED_CAVE_RAIDER_HUT),
                                        1)),
                        new RandomSpreadStructurePlacement(
                                32, 8, RandomSpreadType.LINEAR, 70387322)));
        context.register(
                FISHERMAN_HUTS,
                new StructureSet(
                        structures.getOrThrow(MiaStructures.FISHERMAN_HUT),
                        new RandomSpreadStructurePlacement(
                                32, 8, RandomSpreadType.LINEAR, 70387323)));
    }

    private static ResourceKey<StructureSet> key(String path) {
        return ResourceKey.create(Registries.STRUCTURE_SET, MementoInAbyss.asResource(path));
    }

    private MiaStructureSets() {}
}
