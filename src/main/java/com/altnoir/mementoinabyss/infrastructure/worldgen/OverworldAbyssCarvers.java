package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.OverworldAbyssBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.RandomState;
import org.jspecify.annotations.Nullable;

/** Carvers follow the original overworld source independently of the visible abyss habitat. */
public final class OverworldAbyssCarvers {
    private static final Map<RandomState, Climate.Sampler> ORIGINAL_SAMPLERS = new WeakHashMap<>();

    /** Capture the vanilla climate sampler before the overworld router is composed. */
    public static synchronized void retainOriginalSampler(RandomState state) {
        ORIGINAL_SAMPLERS.putIfAbsent(state, state.sampler());
    }

    /** Use the original climate for source-biome selection, or the current sampler if uncomposed. */
    public static synchronized Climate.Sampler samplerForCarvers(RandomState state) {
        return ORIGINAL_SAMPLERS.getOrDefault(state, state.sampler());
    }

    public static BiomeSource sourceForCarvers(
            BiomeSource source, @Nullable OverworldAbyssShape shape, int blockX, int blockZ) {
        if (shape != null
                && shape.affects(blockX, blockZ)
                && source instanceof OverworldAbyssBiomeSource wrapper) {
            return wrapper.delegate();
        }
        return source;
    }

    private OverworldAbyssCarvers() {}
}
