package com.altnoir.mementoinabyss.infrastructure.worldgen.river;

import com.altnoir.mementoinabyss.infrastructure.worldgen.C2meDensityFunctionCompat;
import com.altnoir.mementoinabyss.infrastructure.worldgen.OverworldAbyssPipeline;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.AbyssRiverBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssLayer2CaveMask;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.AbyssRiverMask;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.HopperAbyssHole;
import com.altnoir.mementoinabyss.infrastructure.worldgen.dimension.MiaDimensions;
import com.altnoir.mementoinabyss.mixin.worldgen.ChunkGeneratorAccessor;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.neoforged.neoforge.common.util.Lazy;
import org.jspecify.annotations.Nullable;

public final class AbyssRivers {
    private static final Map<RandomState, AbyssRiverProfile> PROFILES =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static void install(ServerLevel level, ChunkGenerator generator, RandomState state) {
        if (!level.dimension().equals(MiaDimensions.THE_ABYSS_LEVEL)
                || !(generator instanceof NoiseBasedChunkGenerator noise)
                || PROFILES.containsKey(state)) return;
        var router = C2meDensityFunctionCompat.originalRouter(state.router());
        AbyssRiverMask[] river = new AbyssRiverMask[1];
        router.finalDensity()
                .mapAll(
                        function -> {
                            if (function instanceof AbyssRiverMask found) river[0] = found;
                            return function;
                        });
        if (river[0] == null) return;
        var settings = noise.generatorSettings().value().noiseSettings();
        var profile =
                new AbyssRiverProfile(
                        OverworldAbyssPipeline.sampleGraph(
                                river[0].support(),
                                settings.getCellWidth(),
                                settings.getCellHeight()),
                        river[0].bank(),
                        river[0].bed(),
                        river[0].supportY(),
                        river[0].clearanceY(),
                        level.getSeed(),
                        HopperAbyssHole.abyssRadius() * .3 + 32);
        ((RandomStateAccessor) (Object) state)
                .mia$setRouter(
                        router.mapAll(
                                function ->
                                        function instanceof AbyssRiverMask found
                                                ? new AbyssRiverMask(
                                                        found.support(),
                                                        found.bank(),
                                                        found.bed(),
                                                        found.supportY(),
                                                        found.clearanceY(),
                                                        profile)
                                                : function instanceof AbyssLayer2CaveMask cave
                                                        ? new AbyssLayer2CaveMask(
                                                                cave.surfaceDepth(),
                                                                cave.riverClearance(),
                                                                profile)
                                                        : function));
        var source =
                new AbyssRiverBiomeSource(
                        generator.getBiomeSource(),
                        level.registryAccess()
                                .lookupOrThrow(Registries.BIOME)
                                .getOrThrow(MiaBiomes.ABYSS_RIVER),
                        profile,
                        level.registryAccess()
                                .lookupOrThrow(Registries.BIOME)
                                .getOrThrow(MiaBiomes.PRIMO_FOREST),
                        level.registryAccess()
                                .lookupOrThrow(Registries.BIOME)
                                .getOrThrow(MiaBiomes.TEMPTATION_FOREST));
        var access = (ChunkGeneratorAccessor) (Object) generator;
        access.mia$setBiomeSource(source);
        var generationSettings = access.mia$generationSettingsGetter();
        access.mia$setFeaturesPerStep(
                Lazy.of(
                        () ->
                                FeatureSorter.buildFeaturesPerStep(
                                        List.copyOf(source.possibleBiomes()),
                                        biome -> generationSettings.apply(biome).features(),
                                        true)));
        PROFILES.put(state, profile);
    }

    public static @Nullable AbyssRiverProfile profile(RandomState state) {
        return PROFILES.get(state);
    }

    private AbyssRivers() {}
}
