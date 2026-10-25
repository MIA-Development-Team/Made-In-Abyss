package com.altnoir.mementoinabyss.infrastructure.worldgen.biome;

import com.altnoir.mementoinabyss.infrastructure.worldgen.river.AbyssRiverProfile;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public final class AbyssRiverBiomeSource extends BiomeSource {
    public static final MapCodec<BiomeSource> CODEC =
            BiomeSource.CODEC
                    .fieldOf("original")
                    .xmap(source -> source, source -> ((AbyssRiverBiomeSource) source).original());
    private final BiomeSource original;
    private final Holder<Biome> river;
    private final Holder<Biome> primo;
    private final Holder<Biome> temptation;
    private final AbyssRiverProfile profile;

    public AbyssRiverBiomeSource(
            BiomeSource original,
            Holder<Biome> river,
            AbyssRiverProfile profile,
            Holder<Biome> primo,
            Holder<Biome> temptation) {
        this.original = original;
        this.river = river;
        this.profile = profile;
        this.primo = primo;
        this.temptation = temptation;
    }

    public BiomeSource original() {
        return original;
    }

    public Holder<Biome> riverBiome(
            int x, int y, int z, Holder<Biome> fallback, Holder<Biome> river) {
        return profile.isRiverBiome(QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z))
                ? river
                : fallback;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.concat(original.possibleBiomes().stream(), Stream.of(river)).distinct();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        var selected = original.getNoiseBiome(x, y, z, sampler);
        int blockY = QuartPos.toBlock(y);
        if (blockY >= AbyssRiverProfile.SEA_LEVEL && blockY < -32) {
            int forest = profile.nativeForest(QuartPos.toBlock(x), QuartPos.toBlock(z));
            if (forest == 1) selected = primo;
            else if (forest == 2) selected = temptation;
        }
        return riverBiome(x, y, z, selected, river);
    }

    @Override
    protected MapCodec<? extends BiomeSource> codec() {
        return CODEC;
    }
}
