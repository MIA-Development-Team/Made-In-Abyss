package com.altnoir.mementoinabyss.impl.lod.server;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.MiaHeight;
import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaEnvironmentAttributes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.VerticalBoundary;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.VerticalDimensionCoordinates;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.attribute.EnvironmentAttribute;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.jspecify.annotations.Nullable;

/**
 * Resolves declared vertical views and the default overworld view into the Abyss below it.
 */
public final class CrossDimensionLodLinks {
    public static Optional<CrossDimensionLodLink> forTarget(Level viewer) {
        return forTarget(viewer.registryAccess(), viewer.dimension(), viewer.dimensionType());
    }

    static Optional<CrossDimensionLodLink> forTarget(
            RegistryAccess registries, ResourceKey<Level> target, DimensionType targetType) {
        return forTargets(registries, target, targetType).stream().findFirst();
    }

    public static List<CrossDimensionLodLink> forTargets(Level viewer) {
        return forTargets(viewer.registryAccess(), viewer.dimension(), viewer.dimensionType());
    }

    static List<CrossDimensionLodLink> forTargets(
            RegistryAccess registries, ResourceKey<Level> target, DimensionType targetType) {
        List<CrossDimensionLodLink> links = new ArrayList<>();
        EnvironmentAttributeMap attributes = targetType.attributes();
        if (attributes.contains(MiaEnvironmentAttributes.VERTICAL_BELOW)) {
            resolve(
                            registries,
                            target,
                            targetType,
                            boundary(attributes, MiaEnvironmentAttributes.VERTICAL_BELOW),
                            true)
                    .ifPresent(links::add);
        }
        if (attributes.contains(MiaEnvironmentAttributes.VERTICAL_ABOVE)) {
            resolve(
                            registries,
                            target,
                            targetType,
                            boundary(attributes, MiaEnvironmentAttributes.VERTICAL_ABOVE),
                            false)
                    .ifPresent(links::add);
        }
        // Keep the original dimension type; the central connection's teleport uses this same shift.
        if (target.equals(Level.OVERWORLD)
                && !attributes.contains(MiaEnvironmentAttributes.VERTICAL_BELOW)
                && !attributes.contains(MiaEnvironmentAttributes.VERTICAL_ABOVE)) {
            resolve(
                            registries,
                            target,
                            targetType,
                            new VerticalBoundary(MiaDimensions.THE_ABYSS_LEVEL, 0.0),
                            true)
                    .ifPresent(links::add);
        }
        if (target.equals(MiaDimensions.THE_ABYSS_LEVEL)
                && !attributes.contains(MiaEnvironmentAttributes.VERTICAL_ABOVE)) {
            resolve(registries, target, targetType, new VerticalBoundary(Level.OVERWORLD, 0), false)
                    .ifPresent(links::add);
        }
        return List.copyOf(links);
    }

    /**
     * Only the implicit overworld visual connection has a horizontal activation boundary.
     */
    public static boolean activeAt(Level viewer, double x, double z) {
        return activeAt(
                viewer.dimension(),
                viewer.dimensionType().attributes(),
                x,
                z,
                MementoInAbyss.CONFIGS.worldGenSection.overworldAbyssEnabled.get(),
                OverworldAbyss.configuredRadius());
    }

    static boolean activeAt(
            ResourceKey<Level> target,
            EnvironmentAttributeMap attributes,
            double x,
            double z,
            boolean enabled,
            int radius) {
        if (!target.equals(Level.OVERWORLD)
                || attributes.contains(MiaEnvironmentAttributes.VERTICAL_BELOW)
                || attributes.contains(MiaEnvironmentAttributes.VERTICAL_ABOVE)) return true;
        return enabled && OverworldAbyss.withinConnectionRange(x, z, radius);
    }

    public static List<CrossDimensionLodLink> fromSource(Level source) {
        MinecraftServer server = source.getServer();
        if (server == null) return List.of();
        List<CrossDimensionLodLink> links = new ArrayList<>();
        for (Level level : server.getAllLevels()) {
            forTargets(level).stream()
                    .filter(link -> link.source().equals(source.dimension()))
                    .forEach(links::add);
        }
        return links;
    }

    public static List<CrossDimensionLodLink> all(MinecraftServer server) {
        List<CrossDimensionLodLink> links = new ArrayList<>();
        for (Level level : server.getAllLevels()) {
            links.addAll(forTargets(level));
        }
        return links;
    }

    public static int radius(CrossDimensionLodLink link) {
        return MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodViewDistance.get() * 16;
    }

    /**
     * Configured radius of the center-first lazy-generation area.
     */
    public static int centralGenerationRadius() {
        return Mth.ceil(HopperAbyssHole.abyssRadius());
    }

    private static Optional<CrossDimensionLodLink> resolve(
            RegistryAccess registries,
            ResourceKey<Level> target,
            DimensionType targetType,
            VerticalBoundary boundary,
            boolean sourceIsBelow) {
        DimensionType sourceType = dimensionType(registries, boundary.dimension());
        if (sourceType == null) return Optional.empty();
        int sourceMaxY = sourceType.minY() + sourceType.height();
        int targetMaxY = targetType.minY() + targetType.height();
        int displayYOffset =
                sourceIsBelow
                        ? VerticalDimensionCoordinates.displayOffset(targetType.minY(), sourceMaxY)
                        : VerticalDimensionCoordinates.displayOffset(targetMaxY, sourceType.minY());
        var id = boundary.dimension().identifier();
        if (target.equals(Level.OVERWORLD)
                && boundary.dimension().equals(MiaDimensions.THE_ABYSS_LEVEL))
            id = MementoInAbyss.asResource("the_abyss_in_overworld");
        else if (target.equals(MiaDimensions.THE_ABYSS_LEVEL)
                && boundary.dimension().equals(Level.OVERWORLD))
            id = MementoInAbyss.asResource("overworld_in_the_abyss");
        else if (!sourceIsBelow
                && targetType.attributes().contains(MiaEnvironmentAttributes.VERTICAL_BELOW)
                && boundary(targetType.attributes(), MiaEnvironmentAttributes.VERTICAL_BELOW)
                        .dimension()
                        .equals(boundary.dimension()))
            id =
                    id.withPath(
                            path ->
                                    path
                                            + "/above_in/"
                                            + target.identifier().getNamespace()
                                            + "/"
                                            + target.identifier().getPath());
        return Optional.of(
                new CrossDimensionLodLink(
                        id,
                        boundary.dimension(),
                        target,
                        new MiaHeight(sourceType.minY(), sourceType.height(), sourceMaxY),
                        displayYOffset));
    }

    private static VerticalBoundary boundary(
            EnvironmentAttributeMap attributes, EnvironmentAttribute<VerticalBoundary> attribute) {
        return attributes.applyModifier(attribute, attribute.defaultValue());
    }

    /**
     * Resolves the source dimension's type for LOD height/offset math.
     *
     * <p>{@link Registries#LEVEL_STEM} is worldgen-only and is not networked to clients.
     * {@link Registries#DIMENSION_TYPE} is. Falls back to the {@code <dimension>_type} naming used
     * by this mod's datapack when the stem registry is absent.
     */
    private static @Nullable DimensionType dimensionType(
            RegistryAccess registries, ResourceKey<Level> dimension) {
        ResourceKey<LevelStem> stem =
                ResourceKey.create(Registries.LEVEL_STEM, dimension.identifier());
        var fromStem =
                registries
                        .lookup(Registries.LEVEL_STEM)
                        .flatMap(lookup -> lookup.get(stem))
                        .map(holder -> holder.value().type().value());
        if (fromStem.isPresent()) return fromStem.get();

        ResourceKey<DimensionType> typeKey =
                ResourceKey.create(
                        Registries.DIMENSION_TYPE,
                        dimension.equals(Level.OVERWORLD)
                                ? dimension.identifier()
                                : dimension.identifier().withPath(path -> path + "_type"));
        return registries
                .lookup(Registries.DIMENSION_TYPE)
                .flatMap(lookup -> lookup.get(typeKey))
                .map(Holder.Reference::value)
                .orElse(null);
    }

    private CrossDimensionLodLinks() {}
}
