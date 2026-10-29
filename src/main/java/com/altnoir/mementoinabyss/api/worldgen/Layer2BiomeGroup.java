package com.altnoir.mementoinabyss.api.worldgen;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;

/**
 * A primary alone is a standalone biome, usable in both regions unless primaryOnly is true.
 * Companions turn it into a fixed family; lists use north, east, south, west order. Transition
 * biomes require a fixed family. Standalone terrain kinds match the local primary or companion
 * terrain; fixed families match the complete layout. A standalone primary can exclude companion
 * group IDs with incompatibleCompanions. Weight zero disables this entry.
 */
public record Layer2BiomeGroup(
        Optional<Holder<Biome>> primary,
        List<Holder<Biome>> companions,
        List<Layer2TerrainKind> terrainKinds,
        int weight,
        Optional<Holder<Biome>> river,
        Optional<List<Holder<Biome>>> transition,
        int transitionWidth,
        boolean primaryOnly,
        List<Identifier> incompatibleCompanions) {
    private static final Codec<List<Holder<Biome>>> SECTORS =
            Codec.either(
                            Biome.CODEC,
                            Biome.CODEC
                                    .listOf()
                                    .validate(
                                            values ->
                                                    values.size() == 4
                                                            ? DataResult.success(values)
                                                            : DataResult.error(
                                                                    () ->
                                                                            "Expected four biome"
                                                                                    + " sectors")))
                    .xmap(
                            choice -> choice.map(b -> Collections.nCopies(4, b), List::copyOf),
                            values ->
                                    values.stream().allMatch(values.getFirst()::equals)
                                            ? Either.left(values.getFirst())
                                            : Either.right(values));

    public static final Codec<Layer2BiomeGroup> CODEC =
            RecordCodecBuilder.<Layer2BiomeGroup>create(
                            i ->
                                    i.group(
                                                    Biome.CODEC
                                                            .optionalFieldOf("primary")
                                                            .forGetter(Layer2BiomeGroup::primary),
                                                    SECTORS.optionalFieldOf("companions", List.of())
                                                            .forGetter(
                                                                    Layer2BiomeGroup::companions),
                                                    Layer2TerrainKind.CODEC
                                                            .listOf()
                                                            .validate(
                                                                    kinds ->
                                                                            kinds.isEmpty()
                                                                                    ? DataResult
                                                                                            .error(
                                                                                                    () ->
                                                                                                            "Expected"
                                                                                                                + " at least"
                                                                                                                + " one terrain"
                                                                                                                + " kind")
                                                                                    : DataResult
                                                                                            .success(
                                                                                                    kinds))
                                                            .fieldOf("terrain_kinds")
                                                            .forGetter(
                                                                    Layer2BiomeGroup::terrainKinds),
                                                    Codec.intRange(0, Integer.MAX_VALUE)
                                                            .optionalFieldOf("weight", 1)
                                                            .forGetter(Layer2BiomeGroup::weight),
                                                    Biome.CODEC
                                                            .optionalFieldOf("river")
                                                            .forGetter(Layer2BiomeGroup::river),
                                                    SECTORS.optionalFieldOf("transition")
                                                            .forGetter(
                                                                    Layer2BiomeGroup::transition),
                                                    Codec.intRange(4, 128)
                                                            .optionalFieldOf("transition_width", 64)
                                                            .forGetter(
                                                                    Layer2BiomeGroup
                                                                            ::transitionWidth),
                                                    Codec.BOOL
                                                            .optionalFieldOf("primary_only", false)
                                                            .forGetter(
                                                                    Layer2BiomeGroup::primaryOnly),
                                                    Identifier.CODEC
                                                            .listOf()
                                                            .optionalFieldOf(
                                                                    "incompatible_companions",
                                                                    List.of())
                                                            .forGetter(
                                                                    Layer2BiomeGroup
                                                                            ::incompatibleCompanions))
                                            .apply(i, Layer2BiomeGroup::new))
                    .validate(
                            group -> {
                                if (group.primary().isEmpty())
                                    return DataResult.error(
                                            () ->
                                                    "A Layer2 biome group requires primary;"
                                                        + " companions alone are not supported");
                                if (group.primaryOnly() && !group.companions().isEmpty())
                                    return DataResult.error(
                                            () ->
                                                    "primary_only requires a standalone primary"
                                                            + " without companions");
                                if (group.transition().isPresent() && group.companions().isEmpty())
                                    return DataResult.error(
                                            () ->
                                                    "A transition biome requires both primary and"
                                                            + " companions");
                                if (!group.incompatibleCompanions().isEmpty()
                                        && !group.standalone())
                                    return DataResult.error(
                                            () ->
                                                    "incompatible_companions requires a standalone"
                                                            + " primary");
                                return DataResult.success(group);
                            });

    public Layer2BiomeGroup {
        companions =
                List.copyOf(
                        companions.size() == 1
                                ? Collections.nCopies(4, companions.getFirst())
                                : companions);
        terrainKinds = terrainKinds.stream().distinct().toList();
        incompatibleCompanions = List.copyOf(incompatibleCompanions);
        transition =
                transition.map(
                        values ->
                                List.copyOf(
                                        values.size() == 1
                                                ? Collections.nCopies(4, values.getFirst())
                                                : values));
        if (!companions.isEmpty() && companions.size() != 4)
            throw new IllegalArgumentException("Expected one or four companions");
        if (terrainKinds.isEmpty())
            throw new IllegalArgumentException("Expected at least one terrain kind");
        if (weight < 0) throw new IllegalArgumentException("Biome group weight cannot be negative");
        if (transition.isPresent() && transition.orElseThrow().size() != 4)
            throw new IllegalArgumentException("Expected one or four transition biomes");
        if (transitionWidth < 4 || transitionWidth > 128)
            throw new IllegalArgumentException("Transition width must be between 4 and 128");
    }

    public Layer2BiomeGroup(
            Optional<Holder<Biome>> primary,
            List<Holder<Biome>> companions,
            List<Layer2TerrainKind> terrainKinds,
            int weight,
            Optional<Holder<Biome>> river) {
        this(
                primary,
                companions,
                terrainKinds,
                weight,
                river,
                Optional.empty(),
                64,
                false,
                List.of());
    }

    public Layer2BiomeGroup(
            Optional<Holder<Biome>> primary,
            List<Holder<Biome>> companions,
            List<Layer2TerrainKind> terrainKinds,
            int weight,
            Optional<Holder<Biome>> river,
            Optional<List<Holder<Biome>>> transition,
            int transitionWidth) {
        this(
                primary,
                companions,
                terrainKinds,
                weight,
                river,
                transition,
                transitionWidth,
                false,
                List.of());
    }

    public Layer2BiomeGroup(
            Optional<Holder<Biome>> primary,
            List<Holder<Biome>> companions,
            List<Layer2TerrainKind> terrainKinds,
            int weight,
            Optional<Holder<Biome>> river,
            Optional<List<Holder<Biome>>> transition,
            int transitionWidth,
            boolean primaryOnly) {
        this(
                primary,
                companions,
                terrainKinds,
                weight,
                river,
                transition,
                transitionWidth,
                primaryOnly,
                List.of());
    }

    public boolean standalone() {
        return companions.isEmpty();
    }

    public Stream<Holder<Biome>> possibleBiomes() {
        return Stream.concat(
                Stream.concat(primary.stream(), companions.stream()),
                Stream.concat(river.stream(), transition.stream().flatMap(List::stream)));
    }
}
