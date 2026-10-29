package com.altnoir.mementoinabyss.infrastructure.worldgen.feature;

import com.altnoir.mementoinabyss.infrastructure.worldgen.layer2.Layer2GeneratorView;
import com.altnoir.mementoinabyss.infrastructure.worldgen.layer2.Layer2Runtime;
import com.altnoir.mementoinabyss.infrastructure.worldgen.layer2.Layer2WorldGenLevel;
import com.mojang.serialization.Codec;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class Layer2Feature extends Feature<Layer2FeatureConfiguration> {
    public Layer2Feature(Codec<Layer2FeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<Layer2FeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkGenerator generator = context.chunkGenerator();
        var realLevel = level.getLevel();
        var state = Layer2Runtime.state(realLevel.getChunkSource().randomState());
        if (context.config().allowFluids() && level instanceof Layer2WorldGenLevel view)
            level = view.withFluidWrites();
        if (state != null
                && (!(level instanceof Layer2WorldGenLevel) || context.config().submerged())) {
            if (context.config().submerged() && level instanceof Layer2WorldGenLevel view)
                level = view.original();
            ChunkGenerator source = generator;
            if (!(source instanceof NoiseBasedChunkGenerator))
                source = realLevel.getChunkSource().getGenerator();
            if (source instanceof NoiseBasedChunkGenerator original) {
                // Aquatic vanilla features re-query heightmaps; exclude dry ledges and the roof.
                int maxY = context.config().submerged() ? state.layout().waterLevel() : 0;
                level =
                        new Layer2WorldGenLevel(
                                level, state, false, -128, maxY, context.config().allowFluids());
                generator = new Layer2GeneratorView(original, state, level, false, -128, maxY);
            }
        }
        return context.config()
                .feature()
                .value()
                .place(level, generator, context.random(), context.origin());
    }
}
