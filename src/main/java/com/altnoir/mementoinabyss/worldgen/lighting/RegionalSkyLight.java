package com.altnoir.mementoinabyss.worldgen.lighting;

import com.altnoir.mementoinabyss.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.worldgen.dimension.MiaDimensionTypes;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Defines dimensions whose sky light is restricted to a horizontal region.
 * Definitions are resolved once for each light engine/storage instance so the
 * block-level light propagation hot path only performs arithmetic.
 */
public final class RegionalSkyLight {
    /** Six chunks give vanilla's 16 sky-light levels enough room to blend without a hard rim. */
    public static final double DEFAULT_FADE_DISTANCE = 96.0;
    private static final Map<ResourceKey<Level>, Supplier<? extends Region>> DEFINITIONS =
            new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Region> RESOLVED = new ConcurrentHashMap<>();
    private static final Map<ResourceKey<Level>, Float> AMBIENT_BRIGHTNESS = new ConcurrentHashMap<>();

    static {
        Supplier<Region> abyssCenter = () -> circle(
                0, 0, HopperAbyssHole.abyssRadius(), DEFAULT_FADE_DISTANCE);
        register(MiaDimensions.THE_ABYSS_LEVEL, abyssCenter, MiaDimensionTypes.ABYSS_AMBIENT_LIGHT);
        register(MiaDimensions.GREAT_FAULT_LEVEL, abyssCenter, MiaDimensionTypes.GREAT_FAULT_AMBIENT_LIGHT);
    }

    public static void register(ResourceKey<Level> dimension, Supplier<? extends Region> definition) {
        register(dimension, definition, 0.0F);
    }

    /** Registers both the sky-light mask and the visual brightness used where sky light is absent. */
    public static void register(ResourceKey<Level> dimension, Supplier<? extends Region> definition,
                                float ambientBrightness) {
        DEFINITIONS.put(dimension, definition);
        AMBIENT_BRIGHTNESS.put(dimension, Mth.clamp(ambientBrightness, 0.0F, 1.0F));
        RESOLVED.remove(dimension);
    }

    public static void unregister(ResourceKey<Level> dimension) {
        DEFINITIONS.remove(dimension);
        AMBIENT_BRIGHTNESS.remove(dimension);
        RESOLVED.remove(dimension);
    }

    public static float ambientBrightness(ResourceKey<Level> dimension) {
        return AMBIENT_BRIGHTNESS.getOrDefault(dimension, 0.0F);
    }

    public static Region circle(int centerX, int centerZ, double radius) {
        return circle(centerX, centerZ, radius, DEFAULT_FADE_DISTANCE);
    }

    public static Region circle(int centerX, int centerZ, double radius, double fadeDistance) {
        double nonNegativeRadius = Math.max(0.0, radius);
        double nonNegativeFade = Math.max(0.0, fadeDistance);
        return new CircleRegion(centerX, centerZ, nonNegativeRadius, nonNegativeFade,
                nonNegativeRadius * nonNegativeRadius,
                (nonNegativeRadius + nonNegativeFade) * (nonNegativeRadius + nonNegativeFade));
    }

    public static @Nullable Region resolve(LightChunkGetter chunkSource) {
        if (!(chunkSource.getLevel() instanceof Level level)) {
            return null;
        }
        return resolve(level.dimension());
    }

    public static @Nullable Region resolve(ResourceKey<Level> dimension) {
        Region resolved = RESOLVED.get(dimension);
        if (resolved != null) return resolved;
        Supplier<? extends Region> definition = DEFINITIONS.get(dimension);
        if (definition == null) return null;
        Region created = definition.get();
        Region raced = RESOLVED.putIfAbsent(dimension, created);
        return raced == null ? created : raced;
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

    private record CircleRegion(int centerX, int centerZ, double radius, double fadeDistance,
                                double radiusSquared, double outerRadiusSquared) implements Region {
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
            double smooth = remaining * remaining * remaining
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
            return (double) dx * dx + (double) dz * dz < outerRadiusSquared;
        }

        @Override
        public boolean isFullyLitChunk(int chunkX, int chunkZ) {
            int minX = chunkX << 4;
            int minZ = chunkZ << 4;
            long farthestX = Math.max(Math.abs((long) minX - centerX), Math.abs((long) minX + 15 - centerX));
            long farthestZ = Math.max(Math.abs((long) minZ - centerZ), Math.abs((long) minZ + 15 - centerZ));
            return (double) farthestX * farthestX + (double) farthestZ * farthestZ <= radiusSquared;
        }
    }

    public record RenderMask(float centerX, float centerZ, float radius, float fadeDistance) {
    }

    private RegionalSkyLight() {
    }
}
