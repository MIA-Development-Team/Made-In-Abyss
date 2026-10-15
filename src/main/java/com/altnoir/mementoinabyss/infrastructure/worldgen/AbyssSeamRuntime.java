package com.altnoir.mementoinabyss.infrastructure.worldgen;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssSeamDensity;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.SharedAbyssWall;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.*;
import org.jspecify.annotations.Nullable;

/** Runtime constraints read their parameters from the installed density graph. */
public final class AbyssSeamRuntime {
    private AbyssSeamRuntime() {}

    private record CachedSeam(NoiseRouter router, @Nullable AbyssSeamDensity seam) {}

    private static final java.util.Map<RandomState, CachedSeam> CACHE =
            java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());

    public static NoiseRouter bindRouter(NoiseRouter router, long seed) {
        return bindRouter(router, seed, OverworldAbyss.configuredRadius());
    }

    public static NoiseRouter bindRouter(NoiseRouter router, long seed, double layoutRadius) {
        return router.mapAll(
                function -> {
                    if (function instanceof SharedAbyssWall wall && !wall.bound())
                        return SharedAbyssWall.forOverworld(layoutRadius, seed, wall.phaseOffset());
                    return function;
                });
    }

    /** Called during level construction, before chunk generation is scheduled. */
    public static void configure(ServerLevel level, RandomState state) {
        var installed = seamOf(state);
        int boundary = level.dimensionType().minY() + level.dimensionType().height();
        // The overworld already carries both actual boundaries in its world-local shape.
        // Most abyss worlds also use the serialized boundary, so leave their bound graph intact.
        if (installed == null || installed.overworld() || installed.boundaryY() == boundary) return;
        ((RandomStateAccessor) (Object) state)
                .mia$setRouter(
                        state.router()
                                .mapAll(
                                        function -> {
                                            if (function instanceof AbyssSeamDensity seam
                                                    && !seam.overworld())
                                                return seam.withBoundaryY(boundary);
                                            return function;
                                        }));
    }

    public static @Nullable AbyssSeamDensity seamOf(RandomState state) {
        CachedSeam cached = CACHE.get(state);
        if (cached != null && cached.router() == state.router()) return cached.seam();
        AbyssSeamDensity[] found = new AbyssSeamDensity[1];
        state.router()
                .finalDensity()
                .mapAll(
                        function -> {
                            if (function instanceof AbyssSeamDensity seam) found[0] = seam;
                            return function;
                        });
        CACHE.put(state, new CachedSeam(state.router(), found[0]));
        return found[0];
    }

    public static int depth(AbyssSeamDensity seam, int y) {
        return seam.overworld() ? y - seam.boundaryY() : seam.boundaryY() - 1 - y;
    }

    public static boolean protectedFromCarver(AbyssSeamDensity seam, int x, int y, int z) {
        int depth = depth(seam, y);
        if (depth < 0 || depth >= 48) return false;
        if (!seam.overworld() && depth < 16) return true;
        return Math.hypot(x, z) < seam.wall().layoutRadius() + 64;
    }

    public static boolean isOpening(
            AbyssSeamDensity seam, DensityFunction.FunctionContext context) {
        int depth = depth(seam, context.blockY());
        return depth >= 0
                && depth < 48
                && seam.wall().compute(context) <= 0
                && seam.compute(context) <= 0;
    }
}
