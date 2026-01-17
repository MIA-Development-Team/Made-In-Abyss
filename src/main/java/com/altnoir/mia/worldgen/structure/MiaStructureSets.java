package com.altnoir.mia.worldgen.structure;

import com.altnoir.mia.util.MiaUtil;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;

public interface MiaStructureSets {
    ResourceKey<StructureSet> ABYSS_STRONGHOLDS = register("abyss_strongholds");

    static void bootstrap(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structure = context.lookup(Registries.STRUCTURE);
        HolderGetter<Biome> biome = context.lookup(Registries.BIOME);

        context.register(
                ABYSS_STRONGHOLDS,
                new StructureSet(
                        structure.getOrThrow(MiaStructures.ABYSS_STRONGHOLD),
                        new ConcentricRingsStructurePlacement(16, 3, 128, biome.getOrThrow(BiomeTags.IS_OVERWORLD))
                )
        );
    }

    private static ResourceKey<StructureSet> register(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, MiaUtil.miaId(name));
    }
}
