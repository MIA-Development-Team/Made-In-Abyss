package com.altnoir.mia.worldgen.structure;

import com.altnoir.mia.util.MiaUtil;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallPlanConfig;
import com.altnoir.mia.worldgen.structure.wall.AbyssWallStructurePlacement;
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

import java.util.List;

public interface MiaStructureSets {
    ResourceKey<StructureSet> ANCIENT_JUNGLE_COMPASS_RUINS = register("ancient_jungle_compass_ruins");
    ResourceKey<StructureSet> ANCIENT_ROMAN_COMPASS_RUINS = register("ancient_roman_compass_ruins");
    ResourceKey<StructureSet> ANCIENT_TRIAL_COMPASS_RUINS = register("ancient_trial_compass_ruins");
    ResourceKey<StructureSet> ANCIENT_ANGKOR_COMPASS_RUINS = register("ancient_angkor_compass_ruins");
    ResourceKey<StructureSet> PETRIFIED_SHIPS = register("petrified_ships");
    ResourceKey<StructureSet> ABYSS_STRONGHOLDS = register("abyss_strongholds");
    ResourceKey<StructureSet> ABYSS_WINDMILLS = register("abyss_windmills");

    static void bootstrap(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structure = context.lookup(Registries.STRUCTURE);
        HolderGetter<Biome> biome = context.lookup(Registries.BIOME);

        context.register(
                ANCIENT_JUNGLE_COMPASS_RUINS,
                new StructureSet(
                        List.of(
                                StructureSet.entry(structure.getOrThrow(MiaStructures.ANCIENT_BABYLON_COMPASS_RUINS), 3),
                                StructureSet.entry(structure.getOrThrow(MiaStructures.ANCIENT_MAYA_COMPASS_RUINS), 1)
                        ),
                        new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 70387317)
                )
        );
        context.register(
                ANCIENT_ROMAN_COMPASS_RUINS,
                new StructureSet(
                        structure.getOrThrow(MiaStructures.ANCIENT_ROMAN_COMPASS_RUINS),
                        new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 70387318)
                )
        );
        context.register(
                ANCIENT_TRIAL_COMPASS_RUINS,
                new StructureSet(
                        structure.getOrThrow(MiaStructures.ANCIENT_TRIAL_COMPASS_RUINS),
                        new RandomSpreadStructurePlacement(34, 12, RandomSpreadType.LINEAR, 94251328)
                )
        );
        context.register(
                ANCIENT_ANGKOR_COMPASS_RUINS,
                new StructureSet(
                        structure.getOrThrow(MiaStructures.ANCIENT_ANGKOR_COMPASS_RUINS),
                        new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 70387319)
                )
        );
        context.register(
                PETRIFIED_SHIPS,
                new StructureSet(
                        structure.getOrThrow(MiaStructures.PETRIFIED_SHIP),
                        new RandomSpreadStructurePlacement(16, 4, RandomSpreadType.LINEAR, 70387320)
                )
        );
        context.register(
                ABYSS_STRONGHOLDS,
                new StructureSet(
                        structure.getOrThrow(MiaStructures.ABYSS_STRONGHOLD),
                        new ConcentricRingsStructurePlacement(32, 11, 64, biome.getOrThrow(BiomeTags.IS_OVERWORLD))
                )
        );
        context.register(
                ABYSS_WINDMILLS,
                new StructureSet(
                        structure.getOrThrow(MiaStructures.ABYSS_WINDMILL),
                        new AbyssWallStructurePlacement(AbyssWallPlanConfig.DEFAULT)
                )
        );
    }

    private static ResourceKey<StructureSet> register(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, MiaUtil.miaId(name));
    }
}
