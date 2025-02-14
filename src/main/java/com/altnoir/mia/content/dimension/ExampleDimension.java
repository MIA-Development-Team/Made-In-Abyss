package com.altnoir.mia.content.dimension;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.List;
import java.util.OptionalLong;

public class ExampleDimension {
    public static final ResourceKey<LevelStem> MIA_EXAMPLEDIM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            new ResourceLocation("mia_example_dim"));
    public static final ResourceKey<Level> MIA_EXAMPLEDIM_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation("mia_example_dim"));
    public static final ResourceKey<DimensionType> MIA_EXAMPLEDIM_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE,
            new ResourceLocation("mia_example_type"));
    public static void Type(BootstapContext<DimensionType> context)
    {
        context.register(MIA_EXAMPLEDIM_TYPE_KEY, new DimensionType(
                OptionalLong.of(12000),
                false,
                false,
                false,
                false,
                1.0,
                true,
                false,
                0,
                256,
                256,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                1.0f,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)
        ));
    }

    public static void Stem(BootstapContext<LevelStem> context)
    {
        var biomeRegistry = context.lookup(Registries.BIOME);
        var dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        var noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        var noiseBasedChunkGenerator = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(Pair.of(
                                        Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.FLOWER_FOREST)),
                                Pair.of(
                                        Climate.parameters(0.1F, 0.2F, 0.0F, 0.2F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.BIRCH_FOREST)),
                                Pair.of(
                                        Climate.parameters(0.3F, 0.6F, 0.1F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.OCEAN)),
                                Pair.of(
                                        Climate.parameters(0.4F, 0.3F, 0.2F, 0.1F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.DARK_FOREST))

                        ))),
                noiseGenSettings.getOrThrow(NoiseGeneratorSettings.AMPLIFIED));

        var stem = new LevelStem(dimTypes.getOrThrow(MIA_EXAMPLEDIM_TYPE_KEY), noiseBasedChunkGenerator);

        context.register(MIA_EXAMPLEDIM_KEY, stem);
    }
}
