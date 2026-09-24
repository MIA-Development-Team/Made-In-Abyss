package com.altnoir.mementoinabyss.impl.lod.server;

import com.altnoir.mementoinabyss.MementoInAbyss;
import com.altnoir.mementoinabyss.infrastructure.worldgen.MiaHeight;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaEnvironmentAttributes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.VerticalBoundary;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

/** Resolves cross-dimension views from dimension-type environment attributes. */
public final class CrossDimensionLodLinks {
    public static Optional<CrossDimensionLodLink> forTarget(Level viewer) {
        DimensionType targetType = viewer.dimensionType();
        EnvironmentAttributeMap attributes = targetType.attributes();
        if (attributes.contains(MiaEnvironmentAttributes.VERTICAL_BELOW)) {
            return resolve(
                    viewer.registryAccess(),
                    viewer.dimension(),
                    targetType,
                    boundary(attributes, MiaEnvironmentAttributes.VERTICAL_BELOW),
                    true);
        }
        if (attributes.contains(MiaEnvironmentAttributes.VERTICAL_ABOVE)) {
            return resolve(
                    viewer.registryAccess(),
                    viewer.dimension(),
                    targetType,
                    boundary(attributes, MiaEnvironmentAttributes.VERTICAL_ABOVE),
                    false);
        }
        return Optional.empty();
    }

    public static List<CrossDimensionLodLink> fromSource(Level source) {
        MinecraftServer server = source.getServer();
        if (server == null) return List.of();
        List<CrossDimensionLodLink> links = new ArrayList<>();
        for (Level level : server.getAllLevels()) {
            forTarget(level)
                    .filter(link -> link.source().equals(source.dimension()))
                    .ifPresent(links::add);
        }
        return links;
    }

    public static List<CrossDimensionLodLink> all(MinecraftServer server) {
        List<CrossDimensionLodLink> links = new ArrayList<>();
        for (Level level : server.getAllLevels()) {
            forTarget(level).ifPresent(links::add);
        }
        return links;
    }

    public static int radius(CrossDimensionLodLink link) {
        return MementoInAbyss.CONFIGS.graphsSection.crossDimensionLodViewDistance.get() * 16;
    }

    /** Configured radius of the center-first lazy-generation area. */
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
                sourceIsBelow ? targetType.minY() - sourceMaxY : targetMaxY - sourceType.minY();
        return Optional.of(
                new CrossDimensionLodLink(
                        boundary.dimension().identifier(),
                        boundary.dimension(),
                        target,
                        new MiaHeight(sourceType.minY(), sourceType.height(), sourceMaxY),
                        displayYOffset));
    }

    private static VerticalBoundary boundary(
            EnvironmentAttributeMap attributes, EnvironmentAttribute<VerticalBoundary> attribute) {
        return attributes.applyModifier(attribute, attribute.defaultValue());
    }

    private static DimensionType dimensionType(
            RegistryAccess registries, ResourceKey<Level> dimension) {
        ResourceKey<LevelStem> stem =
                ResourceKey.create(Registries.LEVEL_STEM, dimension.identifier());
        return registries
                .lookup(Registries.LEVEL_STEM)
                .flatMap(lookup -> lookup.get(stem))
                .map(holder -> holder.value().type().value())
                .orElse(null);
    }

    private CrossDimensionLodLinks() {}
}
