package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssSeamDensity;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssCaveGuard;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssCliff;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssInterpolated;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSlide;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSloped;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssSurface;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssTerrain;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.SharedAbyssWall;
import com.altnoir.mementoinabyss.mixin.worldgen.BlendDensityAccessor;
import com.altnoir.mementoinabyss.mixin.worldgen.DensityArgumentAccessor;
import com.altnoir.mementoinabyss.mixin.worldgen.DensityGradientAccessor;
import com.altnoir.mementoinabyss.mixin.worldgen.DensityInputAccessor;
import com.altnoir.mementoinabyss.mixin.worldgen.DensityPairAccessor;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;

/** Adapt a known terrain stage, or keep an unknown mod's graph entirely intact. */
public final class OverworldAbyssPipeline {
    private static final AtomicBoolean UNSUPPORTED_CODEC_WARNING_LOGGED = new AtomicBoolean();

    public record Result(DensityFunction terrain, DensityFunction sampledTerrain, String path) {}

    public static Result build(
            DensityFunction original, OverworldAbyssShape shape, int width, int height) {
        // Datapack decoding wraps even inline constants/functions in HolderHolder. Flatten
        // these transparent references before matching vanilla stages: their parent mapAll
        // then canonicalizes constant arithmetic exactly as the programmatic bootstrap does.
        // Keep the untouched original graph for any unrecognized generator's fallback.
        DensityFunction normalized = original.mapAll(OverworldAbyssPipeline::unwrap);
        Set<BlendedNoise> bases = Collections.newSetFromMap(new IdentityHashMap<>());
        int[] stages = {0, 0};
        DensityFunction[] originalSloped = {null};
        normalized.mapAll(
                function -> {
                    if (function instanceof BlendedNoise base) bases.add(base);
                    if (isTerrainStage(function)) stages[0]++;
                    if (isSlopedCheese(function)) {
                        stages[1]++;
                        if (originalSloped[0] == null) originalSloped[0] = function;
                    }
                    return function;
                });
        DensityFunction base =
                bases.size() == 1
                        ? bases.iterator().next()
                        : BlendedNoise.createUnseeded(0.25, 0.125, 80, 160, 8)
                                .withNewRandom(RandomSource.create(shape.seed()));
        DensityFunction terrain;
        String path;
        boolean vanillaPostProcess = hasVanillaPostProcess(normalized);
        if (stages[0] == 1 && stages[1] > 0 && bases.size() == 1 && vanillaPostProcess) {
            // Follow the original rock envelope. Cave/NOODLE cutouts are not a surface-height
            // reference: aquifer pressure can fill those cutouts with rock later, and their
            // discontinuous floors would otherwise be magnified into steps on the outer slope.
            DensityFunction originalRock =
                    DensityFunctions.interpolated(DensityFunctions.blendDensity(originalSloped[0]));
            DensityFunction referenceHeight =
                    new OverworldAbyssSurface(
                            DensityFunctions.add(
                                    DensityFunctions.findTopSurface(
                                            sampleGraph(originalRock, width, height),
                                            DensityFunctions.constant(shape.maxY() - 1),
                                            shape.minY(),
                                            1),
                                    DensityFunctions.constant(1)));
            terrain =
                    normalized.mapAll(
                            function -> {
                                if (isSlopedCheese(function))
                                    return DensityFunctions.cacheOnce(
                                            new OverworldAbyssSloped(
                                                    function,
                                                    base,
                                                    referenceHeight,
                                                    shape,
                                                    slopedFactor(function)));
                                if (isTopFade(function))
                                    return new OverworldAbyssSlide(function, shape);
                                return function;
                            });
            path = "before_vanilla_interpolation";
            DensityFunction solidRaw =
                    DensityFunctions.cacheOnce(
                            new OverworldAbyssSloped(
                                    originalSloped[0],
                                    base,
                                    referenceHeight,
                                    shape,
                                    slopedFactor(originalSloped[0])));
            DensityFunction solid =
                    DensityFunctions.mul(
                                    DensityFunctions.interpolated(
                                            DensityFunctions.blendDensity(solidRaw)),
                                    DensityFunctions.constant(0.64))
                            .squeeze();
            // Retain all cave nodes, including the outer NOODLE min, below a continuous skin.
            // Otherwise world-height cave entrances can sever the newly raised mountain cap.
            terrain = new OverworldAbyssCaveGuard(terrain, solid, solidRaw, shape);
        } else {
            MementoInAbyss.LOGGER.info(
                    "Overworld abyss fallback: interpolation stages={}, terrain inputs={}, base"
                            + " noises={}, vanilla processing={}",
                    stages[0],
                    stages[1],
                    bases.size(),
                    vanillaPostProcess);
            // The local branch has no radial fade: the final blend below applies it once.
            DensityFunction local =
                    new OverworldAbyssTerrain(DensityFunctions.zero(), base, shape, false, true);
            DensityFunction processed =
                    DensityFunctions.mul(
                                    DensityFunctions.interpolated(
                                            DensityFunctions.blendDensity(local)),
                                    DensityFunctions.constant(0.64))
                            .squeeze();
            terrain = new OverworldAbyssTerrain(original, processed, shape, true);
            path = "independent_processed_branch";
        }
        var wall = SharedAbyssWall.forOverworld(shape.radius(), shape.seed(), shape.wallYOffset());
        // Evaluate the analytic circle after each world's own interpolation; interpolate only
        // the shared noise on its common lattice, never the radius on two different grids.
        terrain = new OverworldAbyssCliff(terrain, base, shape, java.util.Optional.of(wall));
        terrain = AbyssSeamDensity.overworld(wall, terrain, shape.minY());
        return new Result(terrain, sampleGraph(terrain, width, height), path);
    }

    private static boolean isTerrainStage(DensityFunction function) {
        return function instanceof DensityFunctions.MarkerOrMarked marker
                && interpolated(marker)
                && marker.wrapped() instanceof BlendDensityAccessor;
    }

    private static DensityFunction unwrap(DensityFunction function) {
        while (function instanceof DensityFunctions.HolderHolder holder)
            function = holder.function().value();
        return function;
    }

    private static boolean isSlopedCheese(DensityFunction function) {
        if (!(function instanceof DensityPairAccessor pair) || !isType(function, "add"))
            return false;
        DensityFunction first = unwrap(pair.mia$first()), second = unwrap(pair.mia$second());
        DensityFunction gradient =
                first instanceof BlendedNoise
                        ? second
                        : second instanceof BlendedNoise ? first : null;
        return gradient instanceof DensityArgumentAccessor scale
                && scale.mia$argument() == 4
                && isType(gradient, "mul")
                && gradient instanceof DensityInputAccessor input
                && isType(unwrap(input.mia$input()), "quarter_negative");
    }

    private static boolean isTopFade(DensityFunction function) {
        return function instanceof DensityGradientAccessor gradient
                && gradient.mia$fromValue() == 1
                && gradient.mia$toValue() == 0
                && gradient.mia$fromY() >= 128
                && gradient.mia$toY() >= 256;
    }

    private static DensityFunction slopedFactor(DensityFunction function) {
        var pair = (DensityPairAccessor) function;
        DensityFunction first = unwrap(pair.mia$first()), second = unwrap(pair.mia$second());
        DensityFunction gradient = first instanceof BlendedNoise ? second : first;
        DensityFunction quarter = unwrap(((DensityInputAccessor) gradient).mia$input());
        DensityFunction product = unwrap(((DensityInputAccessor) quarter).mia$input());
        if (product instanceof DensityPairAccessor factors) return factors.mia$second();
        if (product instanceof DensityArgumentAccessor constant)
            return DensityFunctions.constant(constant.mia$argument());
        return DensityFunctions.constant(5);
    }

    private static boolean hasVanillaPostProcess(DensityFunction function) {
        while (function instanceof DensityFunctions.HolderHolder holder)
            function = holder.function().value();
        if (isType(function, "squeeze") && function instanceof DensityInputAccessor squeeze) {
            DensityFunction scale = squeeze.mia$input();
            return isType(scale, "mul")
                    && scale instanceof DensityArgumentAccessor constant
                    && constant.mia$argument() == 0.64
                    && scale instanceof DensityInputAccessor input
                    && isTerrainStage(input.mia$input());
        }
        // Vanilla's noodle constraint surrounds the processed main terrain branch.
        if (isType(function, "min") && function instanceof DensityPairAccessor pair)
            return hasVanillaPostProcess(pair.mia$first())
                    || hasVanillaPostProcess(pair.mia$second());
        return false;
    }

    private static boolean isType(DensityFunction function, String path) {
        try {
            var key = BuiltInRegistries.DENSITY_FUNCTION_TYPE.getKey(function.codec().codec());
            return key != null
                    && key.getNamespace().equals("minecraft")
                    && key.getPath().equals(path);
        } catch (UnsupportedOperationException unsupportedCodec) {
            if (UNSUPPORTED_CODEC_WARNING_LOGGED.compareAndSet(false, true))
                MementoInAbyss.LOGGER.warn(
                        "Density function {} does not expose a codec; using the conservative "
                                + "overworld abyss pipeline fallback",
                        function.getClass().getName());
            return false;
        }
    }

    private static boolean interpolated(DensityFunctions.MarkerOrMarked marker) {
        return marker.codec().codec()
                == DensityFunctions.interpolated(DensityFunctions.zero()).codec().codec();
    }

    /** NoiseInterpolator bypasses interpolation for SinglePointContext, so surface scans need this. */
    public static DensityFunction sampleGraph(DensityFunction graph, int width, int height) {
        return graph.mapAll(
                function -> {
                    if (function instanceof DensityFunctions.HolderHolder holder)
                        return holder.function().value();
                    if (function instanceof DensityFunctions.MarkerOrMarked marker)
                        return interpolated(marker)
                                ? new OverworldAbyssInterpolated(marker.wrapped(), width, height)
                                : marker.wrapped();
                    return function;
                });
    }

    private OverworldAbyssPipeline() {}
}
