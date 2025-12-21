package com.altnoir.mia.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class AbyssNoiseChunkGenerator extends NoiseBasedChunkGenerator {
    public AbyssNoiseChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, settings);
    }
    private static Aquifer.FluidPicker createFluidPicker(NoiseGeneratorSettings settings) {
        Aquifer.FluidStatus lavaStatus = new Aquifer.FluidStatus(-64, Blocks.LAVA.defaultBlockState());
        int seaLevel = settings.seaLevel();
        Aquifer.FluidStatus defaultFluidStatus = new Aquifer.FluidStatus(seaLevel, settings.defaultFluid());
        return (x, y, z) -> y < Math.min(-64, seaLevel) ? lavaStatus : defaultFluidStatus;
    }
}
