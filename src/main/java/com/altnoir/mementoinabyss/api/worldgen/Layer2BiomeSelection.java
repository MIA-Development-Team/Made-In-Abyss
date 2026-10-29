package com.altnoir.mementoinabyss.api.worldgen;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * The opening has no group and sector -1. Standalone combinations return the entry chosen for this
 * region; rivers retain the primary entry's key and sector.
 */
public record Layer2BiomeSelection(
        Optional<ResourceKey<Layer2BiomeGroup>> group,
        Identifier zone,
        int sector,
        Holder<Biome> biome) {}
