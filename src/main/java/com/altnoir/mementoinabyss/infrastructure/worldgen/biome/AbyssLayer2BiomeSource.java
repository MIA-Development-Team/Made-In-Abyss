package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.Layer2Layout;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import org.jspecify.annotations.Nullable;

public final class AbyssLayer2BiomeSource extends BiomeSource {
    public static final MapCodec<AbyssLayer2BiomeSource> CODEC =
            RecordCodecBuilder.mapCodec(
                    i ->
                            i.group(
                                            BiomeSource.CODEC
                                                    .fieldOf("original")
                                                    .forGetter(AbyssLayer2BiomeSource::original),
                                            Group.CODEC
                                                    .listOf()
                                                    .fieldOf("groups")
                                                    .forGetter(AbyssLayer2BiomeSource::groups),
                                            Biome.CODEC
                                                    .fieldOf("river")
                                                    .forGetter(AbyssLayer2BiomeSource::river))
                                    .apply(i, AbyssLayer2BiomeSource::new));
    private final BiomeSource original;
    private final @Nullable Layer2Layout layout;
    private final List<Group> groups;
    private final Holder<Biome> river;

    public AbyssLayer2BiomeSource(
            BiomeSource original,
            @Nullable Layer2Layout layout,
            List<Group> groups,
            Holder<Biome> river) {
        if (groups.isEmpty()) throw new IllegalArgumentException("Layer 2 requires a biome group");
        this.original = original;
        this.layout = layout;
        this.groups = List.copyOf(groups);
        this.river = river;
    }

    public AbyssLayer2BiomeSource(BiomeSource original, List<Group> groups, Holder<Biome> river) {
        this(original, null, groups, river);
    }

    public BiomeSource original() {
        return original;
    }

    public Layer2Layout layout() {
        return java.util.Objects.requireNonNull(layout, "Layer 2 layout is not installed");
    }

    public List<Group> groups() {
        return groups;
    }

    public Holder<Biome> river() {
        return river;
    }

    public Holder<Biome> surfaceBiome(int x, int z) {
        var region = layout().sample(x, z);
        if (region.edgeDistance() < layout().riverBankDistance()) return river;
        var group = groups.get(Math.floorMod(region.id(), groups.size()));
        return region.primary() ? group.primary() : group.companions().get(region.sector());
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(
                        original.possibleBiomes().stream(),
                        Stream.concat(
                                Stream.of(river),
                                groups.stream()
                                        .flatMap(
                                                g ->
                                                        Stream.concat(
                                                                Stream.of(g.primary()),
                                                                g.companions().stream()))))
                .distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int blockX = QuartPos.toBlock(x),
                blockY = QuartPos.toBlock(y),
                blockZ = QuartPos.toBlock(z);
        if (layout != null
                && blockY >= layout.minWaterY()
                && blockY < -32
                && Math.hypot(blockX, blockZ) >= layout.openingRadius() + 64) {
            return surfaceBiome(blockX, blockZ);
        }
        return original.getNoiseBiome(x, y, z, sampler);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }

    public record Group(Holder<Biome> primary, List<Holder<Biome>> companions) {
        public static final Codec<Group> CODEC =
                RecordCodecBuilder.create(
                        i ->
                                i.group(
                                                Biome.CODEC
                                                        .fieldOf("primary")
                                                        .forGetter(Group::primary),
                                                Biome.CODEC
                                                        .listOf()
                                                        .fieldOf("companions")
                                                        .forGetter(Group::companions))
                                        .apply(i, Group::new));

        public Group {
            companions = List.copyOf(companions);
            if (companions.size() != 4)
                throw new IllegalArgumentException("Expected four companion sectors");
        }

        public Group(Holder<Biome> primary, Holder<Biome> companion) {
            this(primary, List.of(companion, companion, companion, companion));
        }
    }
}
