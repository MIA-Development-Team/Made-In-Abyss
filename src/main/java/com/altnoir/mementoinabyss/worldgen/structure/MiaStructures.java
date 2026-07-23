package com.altnoir.mementoinabyss.worldgen.structure;

import com.altnoir.mementoinabyss.MementoInAbyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Stable keys for structures that may be supplied by built-in or external datapacks. */
public final class MiaStructures {
    public static final ResourceKey<Structure> ABYSS_STRONGHOLD = ResourceKey.create(
            Registries.STRUCTURE,
            MementoInAbyss.asResource("abyss_stronghold")
    );

    private MiaStructures() {}
}
