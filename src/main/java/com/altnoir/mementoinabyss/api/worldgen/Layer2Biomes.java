package com.altnoir.mementoinabyss.api.worldgen;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.TheAbyssBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.layer2.Layer2Runtime;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Register group JSONs under data/&lt;namespace&gt;/mementoinabyss/worldgen/layer2_biome_group/.
 * Datagen uses {@link #REGISTRY_KEY} with a normal RegistrySetBuilder bootstrap.
 * Biome contents remain ordinary biome data; feature placement must suit the layer's cave surfaces.
 */
public final class Layer2Biomes {
    public static final ResourceKey<Registry<Layer2BiomeGroup>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(MementoInAbyss.asResource("worldgen/layer2_biome_group"));
    public static final Identifier PRIMARY = MementoInAbyss.asResource("primary");
    public static final Identifier COMPANIONS = MementoInAbyss.asResource("companions");
    public static final Identifier TRANSITION = MementoInAbyss.asResource("transition");
    public static final Identifier RIVER = MementoInAbyss.asResource("river");
    public static final Identifier OPENING = MementoInAbyss.asResource("opening");

    /**
     * Samples the Layer2 surface family, regardless of the caller's height. Depth routing remains
     * the biome source's responsibility. The world must already have its generator initialized.
     */
    public static Layer2BiomeSelection query(ServerLevel level, int blockX, int blockZ) {
        if (!(level.getChunkSource().getGenerator().getBiomeSource()
                instanceof TheAbyssBiomeSource source))
            throw new IllegalArgumentException("This world does not use the abyss biome source");
        return source.layer2Selection(blockX, blockZ);
    }

    /** Uses the same immutable sampler during structure placement, without retaining a world. */
    public static Layer2BiomeSelection query(RandomState randomState, int blockX, int blockZ) {
        var state = Layer2Runtime.state(randomState);
        if (state == null) throw new IllegalStateException("Layer2 generator is not initialized");
        return state.biomeSelection(blockX, blockZ);
    }

    private Layer2Biomes() {}
}
