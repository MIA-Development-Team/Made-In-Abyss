package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.OverworldAbyssBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssClimate;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSurface;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensionTypes;
import com.altnoir.mementoinabyss.mixin.worldgen.ChunkGeneratorAccessor;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import org.jspecify.annotations.Nullable;

public final class OverworldAbyss {
    /**
     * Identity marker instead of an {@code instanceof} check on {@code finalDensity()}: another mod
     * may wrap the router again after this one runs, and the hole then no longer sits at the top of
     * the density graph while the aquifer, the surface rules and the spawn search still need to see
     * it. {@code RandomState} has no {@code equals}, so identity is the right comparison and holds a
     * per-level object for the lifetime of the level.
     */
    private static final Map<RandomState, OverworldAbyssHole> INSTALLED = new ConcurrentHashMap<>();

    public static int overworldRadius(int baseRadius) {
        return Math.multiplyExact(baseRadius, 2);
    }

    public static int configuredRadius() {
        return overworldRadius(MementoInAbyss.CONFIGS.worldGenSection.abyssRadius.get());
    }

    public static boolean withinConnectionRange(double x, double z, int radius) {
        double limit = 2.0 * radius;
        return Double.isFinite(x) && Double.isFinite(z) && x * x + z * z <= limit * limit;
    }

    public static void install(ServerLevel level, ChunkGenerator generator, RandomState state) {
        install(
                level,
                generator,
                state,
                MementoInAbyss.CONFIGS.worldGenSection.overworldAbyssEnabled.get());
    }

    /**
     * Separated from the config lookup so the guards can be exercised without a running server.
     */
    public static boolean install(
            ServerLevel level, ChunkGenerator generator, RandomState state, boolean enabled) {
        // A saved generator can contain last session's wrapper. Rebuild with current config/seed,
        // or restore its original source when disabled, before any generation is scheduled.
        if (level.dimension().equals(Level.OVERWORLD)
                && INSTALLED.get(state) == null
                && generator.getBiomeSource() instanceof OverworldAbyssBiomeSource) {
            replaceBiomeSource(
                    generator, OverworldAbyssBiomeSource.unwrap(generator.getBiomeSource()));
        }
        Decision decision =
                decide(level.dimension(), generator, enabled, INSTALLED.get(state) != null);
        if (decision != Decision.INSTALL) {
            if (decision == Decision.NOT_NOISE_BASED) {
                MementoInAbyss.LOGGER.info(
                        "Skipping overworld abyss for generator {}",
                        generator.getClass().getName());
            }
            return false;
        }
        var noise = (NoiseBasedChunkGenerator) generator;
        NoiseRouter original = state.router();
        NoiseRouter source = C2meDensityFunctionCompat.originalRouter(original);
        var settings = noise.generatorSettings().value().noiseSettings();
        OverworldAbyssShape shape =
                new OverworldAbyssShape(
                        configuredRadius(),
                        level.dimensionType().minY(),
                        level.dimensionType().minY() + level.dimensionType().height(),
                        level.getSeed(),
                        MementoInAbyss.CONFIGS.worldGenSection.abyssOuterSlope.get(),
                        MementoInAbyss.CONFIGS.worldGenSection.abyssSlopeBand.get(),
                        level.registryAccess()
                                        .lookupOrThrow(Registries.DIMENSION_TYPE)
                                        .getOrThrow(MiaDimensionTypes.THE_ABYSS)
                                        .value()
                                        .minY()
                                + level.registryAccess()
                                        .lookupOrThrow(Registries.DIMENSION_TYPE)
                                        .getOrThrow(MiaDimensionTypes.THE_ABYSS)
                                        .value()
                                        .height());
        ((RandomStateAccessor) (Object) state)
                .mia$setRouter(
                        compose(
                                state,
                                original,
                                shape,
                                DensityFunctions.cache2d(source.preliminarySurfaceLevel()),
                                settings.getCellWidth(),
                                settings.getCellHeight()));
        var biomeRegistry = level.registryAccess().lookupOrThrow(Registries.BIOME);
        replaceBiomeSource(
                generator,
                new OverworldAbyssBiomeSource(
                        generator.getBiomeSource(),
                        biomeRegistry.getOrThrow(MiaBiomes.OVERWORLD_ABYSS),
                        List.of(
                                        Biomes.FROZEN_PEAKS,
                                        Biomes.JAGGED_PEAKS,
                                        Biomes.STONY_PEAKS,
                                        Biomes.SNOWY_SLOPES,
                                        Biomes.GROVE,
                                        Biomes.MEADOW)
                                .stream()
                                .<Holder<Biome>>map(biomeRegistry::getOrThrow)
                                .toList(),
                        shape));
        MementoInAbyss.LOGGER.info(
                "Overworld abyss enabled: bottom radius {}, mean ridge radius {}, outer radius {}",
                shape.radius(),
                shape.radius() * 2,
                shape.outerLimit());
        return true;
    }

    public static void replaceBiomeSource(ChunkGenerator generator, BiomeSource source) {
        var access = (ChunkGeneratorAccessor) (Object) generator;
        access.mia$setBiomeSource(source);
        var settings = access.mia$generationSettingsGetter();
        // The vanilla supplier captures the constructor argument, so simply replacing the field
        // would omit the new habitat's features. Keep NeoForge's invalidatable lazy contract.
        access.mia$setFeaturesPerStep(
                net.neoforged.neoforge.common.util.Lazy.of(
                        () ->
                                FeatureSorter.buildFeaturesPerStep(
                                        List.copyOf(source.possibleBiomes()),
                                        biome -> settings.apply(biome).features(),
                                        true)));
    }

    /**
     * What {@link #install} does with one candidate level; every refusal has its own reason.
     */
    public enum Decision {
        INSTALL,
        NOT_OVERWORLD,
        DISABLED,
        NOT_NOISE_BASED,
        ALREADY_INSTALLED
    }

    /**
     * Pure guard used by {@link #install}. The abyss is only ever built for the overworld of a
     * noise-based generator with the feature enabled, and only once per random state, so a level that
     * is loaded twice or a dimension that merely shares the noise settings is never wrapped.
     */
    public static Decision decide(
            ResourceKey<Level> dimension,
            ChunkGenerator generator,
            boolean enabled,
            boolean alreadyInstalled) {
        if (!Level.OVERWORLD.equals(dimension)) return Decision.NOT_OVERWORLD;
        if (!enabled) return Decision.DISABLED;
        if (alreadyInstalled) return Decision.ALREADY_INSTALLED;
        if (!(generator instanceof NoiseBasedChunkGenerator)) return Decision.NOT_NOISE_BASED;
        return Decision.INSTALL;
    }

    /**
     * @return the abyss geometry installed into this random state, or {@code null} for untouched levels
     */
    public static @Nullable OverworldAbyssShape shapeOf(@Nullable RandomState state) {
        OverworldAbyssHole hole = holeOf(state);
        return hole == null ? null : hole.shape();
    }

    /**
     * The composed hole itself, looked up by the {@link RandomState} the world actually generates with.
     *
     * <p>World generation is handed the composed router, so anything that needs the abyss - the
     * aquifer, the badlands and iceberg surface extensions, the spawn search - can reach it here
     * instead of testing {@code finalDensity()} for the wrapper type. That test breaks as soon as
     * another mod wraps the final density a second time around ours, which makes the hole silently
     * fill with water again.
     */
    public static @Nullable OverworldAbyssHole holeOf(@Nullable RandomState state) {
        return state == null ? null : INSTALLED.get(state);
    }

    /**
     * Wraps the original router in place, without replacing the dimension's noise settings document,
     * and records the result against {@code state} so that everything which generates with that
     * random state - the aquifer, the badlands and iceberg surface extensions, the spawn search - can
     * find the abyss through {@link #holeOf}.
     *
     * <p>{@code preliminary_surface_level} drives the surface-rule band, the aquifer's sampling
     * cut-off and structure placement heights, and all three sample neighbouring columns across
     * chunk borders. Reporting the carved height there would leak the abyss tens of blocks past its
     * own radius, so the outer-slope report blends back to the untouched estimate. Vanilla keeps a
     * cache inside {@code NoiseChunk} for that field, so the blend is evaluated once per column and
     * not once per block.
     *
     * @param originalSurface unmodified estimate, cached once per level; {@code NoiseChunk} replaces
     *                        its {@code Cache2D} marker with a chunk-local cache, so sharing one instance is safe
     */
    public static NoiseRouter compose(
            RandomState state,
            NoiseRouter original,
            OverworldAbyssShape shape,
            DensityFunction originalSurface) {
        return compose(state, original, shape, originalSurface, 4, 8);
    }

    public static NoiseRouter compose(
            RandomState state,
            NoiseRouter original,
            OverworldAbyssShape shape,
            DensityFunction originalSurface,
            int cellWidth,
            int cellHeight) {
        OverworldAbyssCarvers.retainOriginalSampler(state);
        NoiseRouter source = C2meDensityFunctionCompat.originalRouter(original);
        var pipeline =
                OverworldAbyssPipeline.build(source.finalDensity(), shape, cellWidth, cellHeight);
        MementoInAbyss.LOGGER.info(
                "Overworld abyss density path: {} (lattice {}x{})",
                pipeline.path(),
                cellWidth,
                cellHeight);
        OverworldAbyssHole surface =
                new OverworldAbyssHole(
                        source.finalDensity(),
                        pipeline.terrain(),
                        originalSurface,
                        DensityFunctions.zero(),
                        shape,
                        true,
                        true,
                        pipeline.sampledTerrain(),
                        false,
                        true);
        OverworldAbyssHole terrain =
                new OverworldAbyssHole(
                        source.finalDensity(),
                        pipeline.terrain(),
                        originalSurface,
                        DensityFunctions.zero(),
                        shape,
                        false,
                        true,
                        pipeline.sampledTerrain(),
                        false,
                        true);
        INSTALLED.put(state, surface);
        DensityFunction terrainSurface = new OverworldAbyssSurface(surface);
        NoiseRouter router =
                new NoiseRouter(
                        original.barrierNoise(),
                        original.fluidLevelFloodednessNoise(),
                        original.fluidLevelSpreadNoise(),
                        original.lavaNoise(),
                        new OverworldAbyssClimate(
                                source.temperature(),
                                shape,
                                OverworldAbyssClimate.Component.TEMPERATURE),
                        new OverworldAbyssClimate(
                                source.vegetation(),
                                shape,
                                OverworldAbyssClimate.Component.HUMIDITY),
                        new OverworldAbyssClimate(
                                source.continents(),
                                shape,
                                OverworldAbyssClimate.Component.CONTINENTS),
                        new OverworldAbyssClimate(
                                source.erosion(), shape, OverworldAbyssClimate.Component.EROSION),
                        new OverworldAbyssClimate(
                                source.depth(),
                                shape,
                                OverworldAbyssClimate.Component.DEPTH,
                                java.util.Optional.of(terrainSurface)),
                        new OverworldAbyssClimate(
                                source.ridges(), shape, OverworldAbyssClimate.Component.RIDGES),
                        terrainSurface,
                        terrain,
                        original.veinToggle(),
                        original.veinRidged(),
                        original.veinGap());
        DensityFunction.Visitor flatten =
                function -> {
                    if (function instanceof DensityFunctions.HolderHolder holder)
                        return holder.function().value();
                    if (function instanceof DensityFunctions.MarkerOrMarked marker)
                        return marker.wrapped();
                    return function;
                };
        ((RandomStateAccessor) (Object) state)
                .mia$setSampler(
                        new Climate.Sampler(
                                router.temperature().mapAll(flatten),
                                router.vegetation().mapAll(flatten),
                                router.continents().mapAll(flatten),
                                router.erosion().mapAll(flatten),
                                router.depth().mapAll(flatten),
                                router.ridges().mapAll(flatten),
                                state.sampler().spawnTarget()));
        return router;
    }

    public static NoiseRouter compose(
            RandomState state, NoiseRouter original, OverworldAbyssShape shape) {
        return compose(
                state,
                original,
                shape,
                DensityFunctions.cache2d(original.preliminarySurfaceLevel()));
    }

    private OverworldAbyss() {}
}
