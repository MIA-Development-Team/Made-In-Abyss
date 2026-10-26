package com.altnoir.mementoinabyss.api.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/** Resolves content after the native source has selected the Layer2 depth band and opening boundary. */
public interface Layer2BiomeResolver {
    Holder<Biome> resolve(Layer2Layout.Region region, boolean river);

    Stream<Holder<Biome>> possibleBiomes();
}
