package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseRouterData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AbyssNoiseBiomeSource extends BiomeSource {
    private static final MapCodec<Long> RADIUS_CODEC = Codec.LONG.fieldOf("abyss_radius");
    private static final MapCodec<Holder<Biome>> ENTRY_CODEC = Biome.CODEC.fieldOf("biome");
    private static final MapCodec<Climate.ParameterList<Holder<Biome>>> DIRECT_CODEC =
            AbyssClimateCodecs.parameterList(ENTRY_CODEC).fieldOf("biomes");
    private static final MapCodec<Optional<Climate.ParameterList<Holder<Biome>>>>
            ABYSS_DIRECT_CODEC =
                    AbyssClimateCodecs.parameterList(ENTRY_CODEC).optionalFieldOf("abyss_biomes");
    private static final MapCodec<Optional<Holder<Biome>>> ABYSS_CODEC =
            Biome.CODEC.optionalFieldOf("abyss_biome");

    public static final MapCodec<AbyssNoiseBiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            instance.group(
                                            RADIUS_CODEC.forGetter(AbyssNoiseBiomeSource::radius),
                                            DIRECT_CODEC.forGetter(
                                                    AbyssNoiseBiomeSource::parameters),
                                            ABYSS_DIRECT_CODEC.forGetter(
                                                    source ->
                                                            Optional.ofNullable(
                                                                    source.abyssParameters)),
                                            ABYSS_CODEC.forGetter(
                                                    source -> Optional.ofNullable(source.abyss)))
                                    .apply(
                                            instance,
                                            (radius, parameters, abyssParameters, abyss) -> {
                                                if (abyssParameters.isPresent()
                                                        && abyss.isPresent()) {
                                                    throw new IllegalStateException(
                                                            "Cannot specify both 'abyss_biomes' and"
                                                                    + " 'abyss_biome' at the same"
                                                                    + " time");
                                                }
                                                if (abyssParameters.isPresent()) {
                                                    return new AbyssNoiseBiomeSource(
                                                            radius,
                                                            parameters,
                                                            abyssParameters.get(),
                                                            null);
                                                } else if (abyss.isPresent()) {
                                                    return new AbyssNoiseBiomeSource(
                                                            radius, parameters, null, abyss.get());
                                                } else {
                                                    throw new IllegalStateException(
                                                            "Must specify either 'abyss_biomes' or"
                                                                    + " 'abyss_biome'");
                                                }
                                            }));

    private final long radius;
    private final Climate.ParameterList<Holder<Biome>> parameters;
    private final Climate.ParameterList<Holder<Biome>> abyssParameters;
    private final Holder<Biome> abyss;
    private volatile @Nullable ClimateColumns climateColumns;

    public void cacheClimateColumns(Climate.Sampler sampler, NoiseRouter router) {
        boolean temperature = declared2D(router.temperature());
        boolean humidity = declared2D(router.vegetation());
        boolean weirdness = declared2D(router.ridges());
        climateColumns =
                temperature || humidity || weirdness
                        ? new ClimateColumns(
                                sampler,
                                temperature,
                                humidity,
                                declared2D(router.continents()),
                                declared2D(router.erosion()),
                                weirdness)
                        : null;
    }

    private static boolean declared2D(DensityFunction function) {
        while (function instanceof DensityFunctions.HolderHolder holder)
            function = holder.function().value();
        if (function instanceof DensityFunctions.MarkerOrMarked marker) {
            var codec = marker.codec().codec();
            return codec == DensityFunctions.cache2d(DensityFunctions.zero()).codec().codec()
                    || codec == DensityFunctions.flatCache(DensityFunctions.zero()).codec().codec();
        }
        return function.minValue() == function.maxValue();
    }

    private Climate.TargetPoint sampleClimate(Climate.Sampler sampler, int x, int y, int z) {
        var cache = climateColumns;
        return cache == null || cache.sampler != sampler
                ? sampler.sample(x, y, z)
                : cache.sample(x, y, z);
    }

    private static final class ClimateColumns {
        private final Climate.Sampler sampler;
        private final boolean temperature, humidity, continentalness, erosion, weirdness;
        private final ThreadLocal<Long2ObjectLinkedOpenHashMap<Climate.TargetPoint>> columns =
                ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);

        ClimateColumns(
                Climate.Sampler sampler,
                boolean temperature,
                boolean humidity,
                boolean continentalness,
                boolean erosion,
                boolean weirdness) {
            this.sampler = sampler;
            this.temperature = temperature;
            this.humidity = humidity;
            this.continentalness = continentalness;
            this.erosion = erosion;
            this.weirdness = weirdness;
        }

        Climate.TargetPoint sample(int x, int y, int z) {
            long key = ((long) x << 32) ^ (z & 0xffffffffL);
            var values = columns.get();
            var point = values.getAndMoveToLast(key);
            if (point == null) {
                point = sampler.sample(x, y, z);
                if (values.size() >= 256) values.removeFirst();
                values.putAndMoveToLast(key, point);
                return point;
            }
            var context =
                    new DensityFunction.SinglePointContext(
                            QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z));
            // Unknown and Y-dependent fields retain the original float conversion and quantization.
            return new Climate.TargetPoint(
                    temperature ? point.temperature() : sample(sampler.temperature(), context),
                    humidity ? point.humidity() : sample(sampler.humidity(), context),
                    continentalness
                            ? point.continentalness()
                            : sample(sampler.continentalness(), context),
                    erosion ? point.erosion() : sample(sampler.erosion(), context),
                    sample(sampler.depth(), context),
                    weirdness ? point.weirdness() : sample(sampler.weirdness(), context));
        }

        private static long sample(
                DensityFunction function, DensityFunction.FunctionContext context) {
            return Climate.quantizeCoord((float) function.compute(context));
        }
    }

    private AbyssNoiseBiomeSource(
            long radius,
            @Nullable Climate.ParameterList<Holder<Biome>> parameters,
            @Nullable Climate.ParameterList<Holder<Biome>> abyssParameters,
            @Nullable Holder<Biome> abyss) {
        if (abyssParameters != null && abyss != null) {
            throw new IllegalArgumentException("Cannot specify both abyssParameters and abyss");
        }
        if (abyssParameters == null && abyss == null) {
            throw new IllegalArgumentException("Must specify either abyssParameters or abyss");
        }
        this.radius = radius;
        this.parameters = parameters;
        this.abyssParameters = abyssParameters;
        this.abyss = abyss;
    }

    public AbyssNoiseBiomeSource(
            long radius, Climate.ParameterList<Holder<Biome>> parameters, Holder<Biome> abyss) {
        this(radius, parameters, null, abyss);
    }

    public AbyssNoiseBiomeSource(
            long radius,
            @Nullable Climate.ParameterList<Holder<Biome>> parameters,
            @Nullable Climate.ParameterList<Holder<Biome>> abyssParameters) {
        this(radius, parameters, abyssParameters, null);
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        Stream<Holder<Biome>> abyssStream;

        if (abyss != null) {
            abyssStream = Stream.of(abyss);
        } else if (abyssParameters != null) {
            abyssStream = this.abyssParameters().values().stream().map(Pair::getSecond);
        } else {
            throw new IllegalStateException("Both abyss and abyssParameters are null");
        }

        return Stream.concat(abyssStream, this.parameters().values().stream().map(Pair::getSecond));
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    public static AbyssNoiseBiomeSource createFromList(
            long radius, Climate.ParameterList<Holder<Biome>> parameters, Holder<Biome> abyss) {
        return new AbyssNoiseBiomeSource(radius, parameters, abyss);
    }

    public static AbyssNoiseBiomeSource createFromList(
            long radius,
            Climate.ParameterList<Holder<Biome>> parameters,
            Climate.ParameterList<Holder<Biome>> abyssParameters) {
        return new AbyssNoiseBiomeSource(radius, parameters, abyssParameters);
    }

    public long radius() {
        return this.radius;
    }

    private Climate.ParameterList<Holder<Biome>> parameters() {
        return this.parameters;
    }

    private Climate.ParameterList<Holder<Biome>> abyssParameters() {
        return this.abyssParameters;
    }

    @Override
    public @Nullable Holder<Biome> getNoiseBiome(
            int x, int y, int z, Climate.@NotNull Sampler sampler) {

        int blockX = QuartPos.toBlock(x);
        int blockZ = QuartPos.toBlock(z);
        int sectionX = SectionPos.blockToSectionCoord(blockX);
        int sectionZ = SectionPos.blockToSectionCoord(blockZ);

        if ((long) sectionX * sectionX + (long) sectionZ * sectionZ <= radius()) {
            if (abyssParameters != null) {
                return this.getAbyssNoiseBiome(sampleClimate(sampler, x, y, z));
            } else {
                return abyss;
            }
        } else {
            return this.getNoiseBiome(sampleClimate(sampler, x, y, z));
        }
    }

    @VisibleForDebug
    public Holder<Biome> getNoiseBiome(Climate.TargetPoint targetPoint) {
        return this.parameters().findValue(targetPoint);
    }

    @VisibleForDebug
    public Holder<Biome> getAbyssNoiseBiome(Climate.TargetPoint targetPoint) {
        return this.abyssParameters().findValue(targetPoint);
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
                        + overworldbiomebuilder.getDebugStringForHumidity((double) f3));
    }
}
