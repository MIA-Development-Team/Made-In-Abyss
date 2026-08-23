package com.altnoir.mia.worldgen.structure.wall;

import com.altnoir.mia.MIA;
import com.altnoir.mia.MiaConfig;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntPredicate;

public final class AbyssWallPlanner {
    private static final int FORBIDDEN_MIN_Y = -3;
    private static final int FORBIDDEN_MAX_Y = 25;
    private static final int FINAL_DENSITY_TOLERANCE = 3;
    private static final int EMBED_SALT = 0x57414C4C;
    private static final double FULL_CIRCLE = Math.PI * 2.0;
    private static final double SECTOR_SIZE = Math.PI / 4.0;
    private static final double HALF_SECTOR = SECTOR_SIZE / 2.0;
    private static final Cache<RandomState, Map<PlanKey, CachedPlan>> WORLD_PLANS = CacheBuilder.newBuilder()
            .weakKeys()
            .maximumSize(16)
            .build();

    private AbyssWallPlanner() {
    }

    @FunctionalInterface
    public interface RadiusPredictor {
        OptionalDouble radiusAt(double angle, int y);
    }

    public static List<AbyssWallCandidate> createPlan(long seed, AbyssWallPlanConfig config, RadiusPredictor predictor) {
        return createPlan(seed, config, predictor, predictor);
    }

    private static List<AbyssWallCandidate> createPlan(
            long seed,
            AbyssWallPlanConfig config,
            RadiusPredictor preliminaryPredictor,
            RadiusPredictor preferredPredictor
    ) {
        List<AbyssWallCandidate> candidates = new ArrayList<>(config.candidateCount());
        Set<ChunkPos> claimedChunks = new HashSet<>();
        long bufferedMinimumDistance = config.planningAnchorDistance();
        long minimumDistanceSquared = bufferedMinimumDistance * bufferedMinimumDistance;
        int allowedYCount = allowedYCount(config);
        if (allowedYCount == 0) {
            return List.of();
        }

        for (int band = 0; band < config.candidateCount(); band++) {
            int bandMinIndex = band * allowedYCount / config.candidateCount();
            int bandMaxIndex = (band + 1) * allowedYCount / config.candidateCount() - 1;
            if (bandMaxIndex < bandMinIndex) {
                continue;
            }
            WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
            random.setLargeFeatureWithSalt(seed, band, config.contractVersion(), config.salt());

            for (int attempt = 0; attempt < config.attemptsPerBand(); attempt++) {
                int y = allowedYAt(config, Mth.nextInt(random, bandMinIndex, bandMaxIndex));
                double angle = random.nextDouble() * FULL_CIRCLE;
                OptionalDouble preliminaryRadius = preliminaryPredictor.radiusAt(angle, y);
                if (!isUsableRadius(preliminaryRadius)) {
                    continue;
                }

                BlockPos preliminaryAnchor = blockPos(angle, y, preliminaryRadius.getAsDouble());
                if (claimedChunks.contains(new ChunkPos(preliminaryAnchor))
                        || isTooClose(preliminaryAnchor, candidates, minimumDistanceSquared)) {
                    continue;
                }

                OptionalDouble predictedRadius = preferredPredictor.radiusAt(angle, y);
                if (!isUsableRadius(predictedRadius)) {
                    continue;
                }

                BlockPos anchor = blockPos(angle, y, predictedRadius.getAsDouble());
                ChunkPos startChunk = new ChunkPos(anchor);
                if (claimedChunks.contains(startChunk) || isTooClose(anchor, candidates, minimumDistanceSquared)) {
                    continue;
                }

                candidates.add(new AbyssWallCandidate(
                        band,
                        angle,
                        y,
                        predictedRadius.getAsDouble(),
                        anchor,
                        startChunk,
                        orientationFor(angle)
                ));
                claimedChunks.add(startChunk);
                break;
            }
        }

        return List.copyOf(candidates);
    }

    public static List<AbyssWallCandidate> planFor(long seed, RandomState randomState, AbyssWallPlanConfig config) {
        return cachedPlanFor(seed, randomState, config).candidates();
    }

    private static CachedPlan cachedPlanFor(long seed, RandomState randomState, AbyssWallPlanConfig config) {
        int abyssRadius = currentAbyssRadius();
        Map<PlanKey, CachedPlan> plans = WORLD_PLANS.asMap()
                .computeIfAbsent(randomState, ignored -> new ConcurrentHashMap<>());
        return plans.computeIfAbsent(new PlanKey(seed, config, abyssRadius), ignored -> {
            DensityFunction finalDensity = randomState.router().finalDensity();
            RadiusPredictor macroPredictor = (angle, y) -> OptionalDouble.of(nominalRadius(y, abyssRadius));
            RadiusPredictor finalDensityPredictor = (angle, y) -> {
                double macroRadius = nominalRadius(y, abyssRadius);
                return OptionalDouble.of(preferredRadius(
                        macroRadius,
                        FINAL_DENSITY_TOLERANCE,
                        radius -> sampleDensity(finalDensity, angle, y, radius) > 0.0
                ));
            };
            List<AbyssWallCandidate> plan = createPlan(
                    seed, config, macroPredictor, finalDensityPredictor
            );
            MIA.LOGGER.debug("Abyss wall candidates for seed {}: {}", seed, plan);
            return CachedPlan.create(plan);
        });
    }

    public static AbyssWallCandidate candidateForChunk(
            long seed,
            RandomState randomState,
            AbyssWallPlanConfig config,
            ChunkPos chunkPos
    ) {
        return cachedPlanFor(seed, randomState, config).byChunk().get(chunkPos);
    }

    public static AbyssWallCandidate.Orientation orientationFor(double angle) {
        double normalized = Mth.positiveModulo(angle, FULL_CIRCLE);
        int sector = Mth.floor((normalized + HALF_SECTOR) / SECTOR_SIZE) & 7;
        if ((sector & 1) == 0) {
            return new AbyssWallCandidate.Orientation(AbyssWallCandidate.TemplateKind.STRAIGHT, rotationFromSouth(sector));
        }
        return new AbyssWallCandidate.Orientation(AbyssWallCandidate.TemplateKind.TILT, rotationFromSouthEast(sector));
    }

    public static BlockPos blockPos(double angle, int y, double radius) {
        return new BlockPos(
                Mth.floor(Math.cos(angle) * radius + 0.5),
                y,
                Mth.floor(Math.sin(angle) * radius + 0.5)
        );
    }

    public static OptionalInt findFirstStableBoundary(int minRadius, int maxRadius, IntPredicate isSolid) {
        for (int radius = minRadius + 3; radius <= maxRadius - 3; radius++) {
            boolean stableAir = !isSolid.test(radius - 3)
                    && !isSolid.test(radius - 2)
                    && !isSolid.test(radius - 1);
            boolean stableRock = isSolid.test(radius)
                    && isSolid.test(radius + 1)
                    && isSolid.test(radius + 2)
                    && isSolid.test(radius + 3);
            if (stableAir && stableRock) {
                return OptionalInt.of(radius);
            }
        }
        return OptionalInt.empty();
    }

    public static OptionalInt findRefinedBoundary(double predictedRadius, int maxCorrection, IntPredicate isSolid) {
        int allowedMinimum = Math.max(1, (int) Math.floor(predictedRadius) - maxCorrection);
        int allowedMaximum = (int) Math.ceil(predictedRadius) + maxCorrection;
        return findFirstStableBoundary(
                Math.max(1, allowedMinimum - 3),
                allowedMaximum + 3,
                isSolid
        );
    }

    public static double preferredRadius(double macroRadius, int tolerance, IntPredicate isSolid) {
        int maximumBoundary = Math.max(1, (int) Math.floor(macroRadius + tolerance));
        Map<Integer, Boolean> solidity = new HashMap<>();
        OptionalInt boundary = findFirstStableBoundary(
                1,
                maximumBoundary + 3,
                radius -> solidity.computeIfAbsent(radius, ignored -> isSolid.test(radius))
        );
        return boundary.isPresent() ? boundary.getAsInt() : macroRadius;
    }

    public static int wallAnchorOffset(long seed, AbyssWallCandidate candidate, AbyssWallPlanConfig config) {
        if (config.embedDepth() == 0) {
            return 0;
        }
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, candidate.id(), config.contractVersion(), config.salt() ^ EMBED_SALT);
        return random.nextInt(config.embedDepth() + 1);
    }

    private static boolean isTooClose(BlockPos anchor, List<AbyssWallCandidate> candidates, long minimumDistanceSquared) {
        for (AbyssWallCandidate candidate : candidates) {
            if (anchor.distSqr(candidate.anchor()) < minimumDistanceSquared) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUsableRadius(OptionalDouble radius) {
        return radius.isPresent() && Double.isFinite(radius.getAsDouble()) && radius.getAsDouble() > 0.0;
    }

    private static Rotation rotationFromSouth(int sector) {
        return switch (sector) {
            case 0 -> Rotation.COUNTERCLOCKWISE_90;
            case 2 -> Rotation.NONE;
            case 4 -> Rotation.CLOCKWISE_90;
            case 6 -> Rotation.CLOCKWISE_180;
            default -> throw new IllegalArgumentException("Not a cardinal sector: " + sector);
        };
    }

    private static Rotation rotationFromSouthEast(int sector) {
        return switch (sector) {
            case 1 -> Rotation.NONE;
            case 3 -> Rotation.CLOCKWISE_90;
            case 5 -> Rotation.CLOCKWISE_180;
            case 7 -> Rotation.COUNTERCLOCKWISE_90;
            default -> throw new IllegalArgumentException("Not a diagonal sector: " + sector);
        };
    }

    private static int currentAbyssRadius() {
        return MiaConfig.abyssRadius > 0 ? MiaConfig.abyssRadius : 160;
    }

    private static double nominalRadius(int y, int abyssRadius) {
        double radius = abyssRadius;
        return y < FORBIDDEN_MIN_Y
                ? radius * 0.6
                : radius * Mth.clamp(1.0 + y / 512.0, 1.0, 2.0);
    }

    private static int allowedYCount(AbyssWallPlanConfig config) {
        int total = config.maxY() - config.minY() + 1;
        int excludedMin = Math.max(config.minY(), FORBIDDEN_MIN_Y);
        int excludedMax = Math.min(config.maxY(), FORBIDDEN_MAX_Y);
        return total - Math.max(0, excludedMax - excludedMin + 1);
    }

    private static int allowedYAt(AbyssWallPlanConfig config, int index) {
        int lowerAllowedCount = Math.max(0, Math.min(config.maxY(), FORBIDDEN_MIN_Y - 1) - config.minY() + 1);
        return index < lowerAllowedCount
                ? config.minY() + index
                : Math.max(config.minY(), FORBIDDEN_MAX_Y + 1) + index - lowerAllowedCount;
    }

    private static double sampleDensity(DensityFunction density, double angle, int y, int radius) {
        BlockPos pos = blockPos(angle, y, radius);
        return density.compute(new DensityFunction.SinglePointContext(pos.getX(), pos.getY(), pos.getZ()));
    }

    private record PlanKey(long seed, AbyssWallPlanConfig config, int abyssRadius) {
    }

    private record CachedPlan(List<AbyssWallCandidate> candidates, Map<ChunkPos, AbyssWallCandidate> byChunk) {
        private static CachedPlan create(List<AbyssWallCandidate> candidates) {
            Map<ChunkPos, AbyssWallCandidate> byChunk = new ConcurrentHashMap<>();
            for (AbyssWallCandidate candidate : candidates) {
                byChunk.put(candidate.startChunk(), candidate);
            }
            return new CachedPlan(candidates, Map.copyOf(byChunk));
        }
    }
}
