package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.jspecify.annotations.Nullable;

final class AbyssClimateSampler {
    private volatile @Nullable Columns columns;

    void bind(Climate.Sampler sampler, NoiseRouter router) {
        boolean temperature = declared2D(router.temperature());
        boolean humidity = declared2D(router.vegetation());
        boolean continentalness = declared2D(router.continents());
        boolean erosion = declared2D(router.erosion());
        boolean weirdness = declared2D(router.ridges());
        columns =
                temperature || humidity || continentalness || erosion || weirdness
                        ? new Columns(
                                sampler, temperature, humidity, continentalness, erosion, weirdness)
                        : null;
    }

    static long depth(Climate.Sampler sampler, int x, int y, int z) {
        return sample(sampler.depth(), context(x, y, z));
    }

    Climate.TargetPoint sample(Climate.Sampler sampler, int x, int y, int z, long depth) {
        var cache = columns;
        return cache == null || cache.sampler != sampler
                ? fields(sampler, context(x, y, z), depth)
                : cache.sample(x, y, z, depth);
    }

    private static DensityFunction.SinglePointContext context(int x, int y, int z) {
        return new DensityFunction.SinglePointContext(
                QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z));
    }

    private static long sample(DensityFunction function, DensityFunction.FunctionContext context) {
        return Climate.quantizeCoord((float) function.compute(context));
    }

    private static Climate.TargetPoint fields(
            Climate.Sampler sampler, DensityFunction.FunctionContext context, long depth) {
        return new Climate.TargetPoint(
                sample(sampler.temperature(), context),
                sample(sampler.humidity(), context),
                sample(sampler.continentalness(), context),
                sample(sampler.erosion(), context),
                depth,
                sample(sampler.weirdness(), context));
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

    private static final class Columns {
        private final Climate.Sampler sampler;
        private final boolean temperature, humidity, continentalness, erosion, weirdness;
        private final ThreadLocal<Long2ObjectLinkedOpenHashMap<Climate.TargetPoint>> values =
                ThreadLocal.withInitial(Long2ObjectLinkedOpenHashMap::new);

        private Columns(
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

        Climate.TargetPoint sample(int x, int y, int z, long depth) {
            long key = ((long) x << 32) ^ (z & 0xffffffffL);
            var cache = values.get();
            var point = cache.getAndMoveToLast(key);
            var context = context(x, y, z);
            if (point == null) {
                point = fields(sampler, context, depth);
                if (cache.size() >= 256) cache.removeFirst();
                cache.putAndMoveToLast(key, point);
                return point;
            }
            return new Climate.TargetPoint(
                    temperature
                            ? point.temperature()
                            : AbyssClimateSampler.sample(sampler.temperature(), context),
                    humidity
                            ? point.humidity()
                            : AbyssClimateSampler.sample(sampler.humidity(), context),
                    continentalness
                            ? point.continentalness()
                            : AbyssClimateSampler.sample(sampler.continentalness(), context),
                    erosion
                            ? point.erosion()
                            : AbyssClimateSampler.sample(sampler.erosion(), context),
                    depth,
                    weirdness
                            ? point.weirdness()
                            : AbyssClimateSampler.sample(sampler.weirdness(), context));
        }
    }
}
