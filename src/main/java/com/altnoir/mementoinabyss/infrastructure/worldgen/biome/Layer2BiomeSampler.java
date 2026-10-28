package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.api.worldgen.Layer2BiomeSelection;
import com.altnoir.mementoinabyss.api.worldgen.Layer2Biomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/** Shared by the biome source and structure state; contains no world or RandomState reference. */
public final class Layer2BiomeSampler {
    private final Layer2Layout layout;
    private final Layer2BiomePalette palette;
    private final Holder<Biome> openingBiome;

    Layer2BiomeSampler(
            Layer2Layout layout, Layer2BiomePalette palette, Holder<Biome> openingBiome) {
        this.layout = layout;
        this.palette = palette;
        this.openingBiome = openingBiome;
    }

    public Holder<Biome> biome(int x, int z) {
        if (opening(x, z)) return openingBiome;
        var region = layout.sample(x, z);
        return palette.resolve(region, region.edgeDistance() < layout.riverBankDistance());
    }

    public Layer2BiomeSelection query(int x, int z) {
        if (opening(x, z))
            return new Layer2BiomeSelection(
                    Optional.empty(), Layer2Biomes.OPENING, -1, openingBiome);
        var region = layout.sample(x, z);
        return palette.selection(region, region.edgeDistance() < layout.riverBankDistance());
    }

    private boolean opening(int x, int z) {
        return Math.hypot(x, z) < layout.openingRadius() + 48;
    }
}
