package com.altnoir.mementoinabyss.mixin.worldgen;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkGenerator.class)
public interface ChunkGeneratorAccessor {
    @Mutable
    @Accessor("biomeSource")
    void mia$setBiomeSource(BiomeSource source);

    @Accessor("generationSettingsGetter")
    Function<Holder<Biome>, BiomeGenerationSettings> mia$generationSettingsGetter();

    @Mutable
    @Accessor("featuresPerStep")
    void mia$setFeaturesPerStep(Supplier<List<FeatureSorter.StepFeatureData>> features);
}
