package com.altnoir.mementoinabyss.infrastructure.worldgen.lighting;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaEnvironmentAttributes;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves dimension attributes into horizontal sky-light regions.
 * Regions are captured once for each light engine/storage instance so the
 * block-level light propagation hot path only performs arithmetic.
 */
public final class RegionalSkyLight {
    /** Six chunks give vanilla's 16 sky-light levels enough room to blend without a hard rim. */
    public static final double DEFAULT_FADE_DISTANCE = 96.0;

    // Weak keys use identity: equal dimension IDs or settings in another world must not share
    // state.
    private static final Cache<DimensionType, ResolvedRegion> RESOLVED =
            CacheBuilder.newBuilder().weakKeys().maximumSize(64).build();

    private record ResolvedRegion(double baseRadius, Region region) {}

    public static Region circle(int centerX, int centerZ, double radius) {
        return circle(centerX, centerZ, radius, DEFAULT_FADE_DISTANCE);
    }

    public static Region circle(int centerX, int centerZ, double radius, double fadeDistance) {
        double nonNegativeRadius = Math.max(0.0, radius);
        double nonNegativeFade = Math.max(0.0, fadeDistance);
        return new CircleRegion(
                centerX,
                centerZ,
                nonNegativeRadius,
                nonNegativeFade,
                nonNegativeRadius * nonNegativeRadius,
                (nonNegativeRadius + nonNegativeFade) * (nonNegativeRadius + nonNegativeFade));
    }

    public static @Nullable Region resolve(LightChunkGetter chunkSource) {
        if (!(chunkSource.getLevel() instanceof Level level)) {
            return null;
        }
        return resolve(level);
    }

    public static @Nullable Region resolve(Level level) {
        return resolve(level.dimensionType());
    }

    public static @Nullable Region resolve(DimensionType dimensionType) {
        return resolve(dimensionType, MementoInAbyss.CONFIGS.worldGenSection.abyssRadius.get());
    }

    static @Nullable Region resolve(DimensionType dimensionType, double baseRadius) {
        var attribute = MiaEnvironmentAttributes.SKY_LIGHT_REGION;
        if (!dimensionType.attributes().contains(attribute)) return null;
        if (!Double.isFinite(baseRadius) || baseRadius < 0)
            throw new IllegalArgumentException("Invalid base sky light radius");
        return RESOLVED.asMap()
                .compute(
                        dimensionType,
                        (ignored, cached) -> {
                            if (cached != null && cached.baseRadius() == baseRadius) return cached;
                            var settings =
                                    dimensionType
                                            .attributes()
                                            .applyModifier(attribute, attribute.defaultValue());
                            double radius = settings.radiusMultiplier() * baseRadius;
                            if (!Double.isFinite(radius))
                                throw new IllegalArgumentException(
                                        "Sky light radius exceeds the supported range");
                            return new ResolvedRegion(
                                    baseRadius,
                                    circle(
                                            settings.centerX(),
                                            settings.centerZ(),
                                            radius,
                                            settings.fadeDistance()));
                        })
                .region();
    }

    public static DataLayer maskDataLayer(Region region, SectionPos pos, DataLayer source) {
        if (!region.intersectsChunk(pos.x(), pos.z())) return new DataLayer();
        if (region.isFullyLitChunk(pos.x(), pos.z())) return source;

        DataLayer masked = source.copy();
        int minX = SectionPos.sectionToBlockCoord(pos.x());
        int minZ = SectionPos.sectionToBlockCoord(pos.z());
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                int maximum = region.maxSkyLight(minX + x, minZ + z);
                if (maximum >= 15) continue;
                for (int y = 0; y < 16; y++) {
                    int original = masked.get(x, y, z);
                    if (original > maximum) masked.set(x, y, z, maximum);
                }
            }
        }
        return masked;
    }

    public interface Region {
        int maxSkyLight(int blockX, int blockZ);

        default int clampSkyLight(int blockX, int blockZ, int original) {
            return Math.min(original, maxSkyLight(blockX, blockZ));
        }

        default boolean allowsSkyLight(int blockX, int blockZ) {
            return maxSkyLight(blockX, blockZ) > 0;
        }

        /** Compact representation consumed by cross-dimension LOD shaders. */
        RenderMask renderMask();

        /**
         * Called for coarse rejection before vanilla allocates or populates sky-light data.
         */
        default boolean intersectsChunk(int chunkX, int chunkZ) {
            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            for (int z = minZ; z < minZ + 16; z++) {
                for (int x = minX; x < minX + 16; x++) {
                    if (allowsSkyLight(x, z)) {
                        return true;
                    }
                }
            }
            return false;
        }

        default boolean isFullyLitChunk(int chunkX, int chunkZ) {
            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            return maxSkyLight(minX, minZ) >= 15
                    && maxSkyLight(minX + 15, minZ) >= 15
                    && maxSkyLight(minX, minZ + 15) >= 15
                    && maxSkyLight(minX + 15, minZ + 15) >= 15;
        }
    }

    private record CircleRegion(
            int centerX,
            int centerZ,
            double radius,
            double fadeDistance,
            double radiusSquared,
            double outerRadiusSquared)
            implements Region {
        @Override
        public int maxSkyLight(int blockX, int blockZ) {
            long dx = (long) blockX - centerX;
            long dz = (long) blockZ - centerZ;
            double distanceSquared = (double) dx * dx + (double) dz * dz;
            if (distanceSquared <= radiusSquared) return 15;
            if (fadeDistance <= 0.0 || distanceSquared >= outerRadiusSquared) return 0;
            double remaining = (radius + fadeDistance - Math.sqrt(distanceSquared)) / fadeDistance;
            // Quintic smootherstep has a zero first and second derivative at both ends. This
            // avoids a visible ring where the fade enters full light or reaches darkness.
            double smooth =
                    remaining
                            * remaining
                            * remaining
                            * (remaining * (remaining * 6.0 - 15.0) + 10.0);
            return Mth.clamp((int) Math.round(smooth * 15.0), 0, 15);
        }

        @Override
        public RenderMask renderMask() {
            return new RenderMask(centerX, centerZ, (float) radius, (float) fadeDistance);
        }

        @Override
        public boolean intersectsChunk(int chunkX, int chunkZ) {
            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            int nearestX = Mth.clamp(centerX, minX, minX + 15);
            int nearestZ = Mth.clamp(centerZ, minZ, minZ + 15);
            long dx = (long) nearestX - centerX;
            long dz = (long) nearestZ - centerZ;
            double nearestDistanceSquared = (double) dx * dx + (double) dz * dz;
            return fadeDistance <= 0.0
                    ? nearestDistanceSquared <= radiusSquared
                    : nearestDistanceSquared < outerRadiusSquared;
        }

        @Override
        public boolean isFullyLitChunk(int chunkX, int chunkZ) {
            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            long farthestX =
                    Math.max(Math.abs((long) minX - centerX), Math.abs((long) minX + 15 - centerX));
            long farthestZ =
                    Math.max(Math.abs((long) minZ - centerZ), Math.abs((long) minZ + 15 - centerZ));
            return (double) farthestX * farthestX + (double) farthestZ * farthestZ <= radiusSquared;
        }
    }

    public record RenderMask(float centerX, float centerZ, float radius, float fadeDistance) {}

    private RegionalSkyLight() {}
}
