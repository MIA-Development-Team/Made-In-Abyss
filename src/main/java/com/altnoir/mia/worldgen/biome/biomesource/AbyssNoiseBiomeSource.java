package com.altnoir.mia.worldgen.biome.biomesource;

import com.altnoir.mia.MiaConfig;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

public class AbyssNoiseBiomeSource extends BiomeSource {
    private static final MapCodec<Holder<Biome>> ABYSS_CODEC = Biome.CODEC.fieldOf("abyss_biome");
    private static final MapCodec<Holder<Biome>> ENTRY_CODEC = Biome.CODEC.fieldOf("biome");
    public static final MapCodec<Climate.ParameterList<Holder<Biome>>> DIRECT_CODEC = Climate.ParameterList.codec(ENTRY_CODEC).fieldOf("biomes");


    public static final MapCodec<AbyssNoiseBiomeSource> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    DIRECT_CODEC.forGetter(AbyssNoiseBiomeSource::parameters),
                    ABYSS_CODEC.forGetter(source -> source.abyss)
            ).apply(instance, AbyssNoiseBiomeSource::new)
    );

    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final Holder<Biome> abyss;


    public AbyssNoiseBiomeSource(Climate.ParameterList<Holder<Biome>> parameters, Holder<Biome> abyss) {
        this.parameters = parameters;
        this.abyss = abyss;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(Stream.of(abyss), this.parameters().values().stream().map(Pair::getSecond));
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    public static AbyssNoiseBiomeSource createFromList(Climate.ParameterList<Holder<Biome>> parameters, Holder<Biome> fixedBiome) {
        return new AbyssNoiseBiomeSource(parameters, fixedBiome);
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    @Override
    public @NotNull Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.@NotNull Sampler sampler) {

        int blockX = QuartPos.toBlock(x);
        int blockZ = QuartPos.toBlock(z);
        int sectionX = SectionPos.blockToSectionCoord(blockX);
        int sectionZ = SectionPos.blockToSectionCoord(blockZ);

        if ((long) sectionX * sectionX + (long) sectionZ * sectionZ <= (long) MiaConfig.abyssRadius * 1.4F) {
            return abyss;
        } else {
            return this.getNoiseBiome(sampler.sample(x, y, z));
        }
    }

    @VisibleForDebug
    public Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint) {
        return this.parameters().findValue(targetPoint);
    }

    @Override
    public void addDebugInfo(List<String> info, BlockPos pos, Climate.Sampler sampler) {
        int i = QuartPos.fromBlock(pos.getX());
        int j = QuartPos.fromBlock(pos.getY());
        int k = QuartPos.fromBlock(pos.getZ());
        Climate.TargetPoint climate$targetpoint = sampler.sample(i, j, k);
        float f = Climate.unquantizeCoord(climate$targetpoint.continentalness());
        float f1 = Climate.unquantizeCoord(climate$targetpoint.erosion());
        float f2 = Climate.unquantizeCoord(climate$targetpoint.temperature());
        float f3 = Climate.unquantizeCoord(climate$targetpoint.humidity());
        float f4 = Climate.unquantizeCoord(climate$targetpoint.weirdness());
        double d0 = (double) NoiseRouterData.peaksAndValleys(f4);
        OverworldBiomeBuilder overworldbiomebuilder = new OverworldBiomeBuilder();
        info.add(
                "Biome builder PV: "
                        + OverworldBiomeBuilder.getDebugStringForPeaksAndValleys(d0)
                        + " C: "
                        + overworldbiomebuilder.getDebugStringForContinentalness((double) f)
                        + " E: "
                        + overworldbiomebuilder.getDebugStringForErosion((double) f1)
                        + " T: "
                        + overworldbiomebuilder.getDebugStringForTemperature((double) f2)
                        + " H: "
                        + overworldbiomebuilder.getDebugStringForHumidity((double) f3)
        );
    }

}

