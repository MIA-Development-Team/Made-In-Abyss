package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import com.mojang.serialization.Lifecycle;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Real SurfaceSystem on stacked stone platforms: every lower floor is sheltered by a roof. */
class OverworldAbyssGrassSurfaceTest {
    private static final int[] FLOORS = {-48, -16, 0, 32, 49, 64, 128, 256};
    private static final LevelHeightAccessor HEIGHT = LevelHeightAccessor.create(-64, 384);
    private static HolderLookup.Provider registries;
    private static Registry<Biome> biomes;
    private static PalettedContainerFactory palettes;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = VanillaRegistries.createLookup();
        var plains = registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS).value();
        var mutable = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
        mutable.register(Biomes.PLAINS, plains, RegistrationInfo.BUILT_IN);
        // Keep identical climate/effects to isolate biome identity from surface eligibility.
        mutable.register(MiaBiomes.OVERWORLD_ABYSS, fixtureBiome(), RegistrationInfo.BUILT_IN);
        mutable.register(MiaBiomes.THE_ABYSS, fixtureBiome(), RegistrationInfo.BUILT_IN);
        biomes = mutable.freeze();
        palettes =
                PalettedContainerFactory.create(
                        new RegistryAccess.ImmutableRegistryAccess(List.of(biomes)));
    }

    private static Biome fixtureBiome() {
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(.8F)
                .downfall(.4F)
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .generationSettings(
                        new BiomeGenerationSettings.Builder(
                                        registries.lookupOrThrow(Registries.PLACED_FEATURE),
                                        registries.lookupOrThrow(Registries.CONFIGURED_CARVER))
                                .build())
                .specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).build())
                .build();
    }

    private static ProtoChunk surface(Holder<Biome> biome, boolean installed) {
        var settings =
                registries
                        .lookupOrThrow(Registries.NOISE_SETTINGS)
                        .getOrThrow(NoiseGeneratorSettings.OVERWORLD);
        var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(biome), settings);
        var state =
                RandomState.create(
                        settings.value(), registries.lookupOrThrow(Registries.NOISE), 12345);
        if (installed)
            ((RandomStateAccessor) (Object) state)
                    .mia$setRouter(
                            OverworldAbyss.compose(
                                    state,
                                    state.router(),
                                    new OverworldAbyssShape(320, -64, 320, 12345, 1, 1.25)));
        var chunk = new ProtoChunk(new ChunkPos(25, 0), UpgradeData.EMPTY, HEIGHT, palettes, null);
        for (int floor : FLOORS)
            for (int x = 400; x < 416; x++)
                for (int z = 0; z < 16; z++)
                    for (int y = floor - 5; y <= floor; y++)
                        chunk.setBlockState(
                                new BlockPos(x, y, z), Blocks.STONE.defaultBlockState());
        // Water-covered floor in an otherwise identical column is an explicit negative control.
        chunk.setBlockState(new BlockPos(414, 33, 14), Blocks.WATER.defaultBlockState());
        chunk.setBlockState(new BlockPos(414, 34, 14), Blocks.WATER.defaultBlockState());
        Heightmap.primeHeightmaps(
                chunk,
                EnumSet.of(Heightmap.Types.WORLD_SURFACE_WG, Heightmap.Types.OCEAN_FLOOR_WG));
        var beardifier =
                new DensityFunctions.BeardifierOrMarker() {
                    public double compute(FunctionContext c) {
                        return 0;
                    }

                    public double minValue() {
                        return 0;
                    }

                    public double maxValue() {
                        return 0;
                    }
                };
        chunk.getOrCreateNoiseChunk(
                c ->
                        NoiseChunk.forChunk(
                                c,
                                state,
                                beardifier,
                                settings.value(),
                                (x, y, z) ->
                                        new Aquifer.FluidStatus(
                                                -64, Blocks.WATER.defaultBlockState()),
                                Blender.empty()));
        generator.buildSurface(
                chunk,
                new WorldGenerationContext(generator, HEIGHT),
                state,
                null,
                new BiomeManager((x, y, z) -> biome, 12345),
                biomes,
                Blender.empty());
        return chunk;
    }

    @Test
    void dryFloorsGrowGrassAtEveryHeightEvenUnderStoneRoofs() {
        var chunk = surface(biomes.getOrThrow(MiaBiomes.OVERWORLD_ABYSS), true);
        for (int floor : FLOORS) {
            assertTrue(
                    chunk.getBlockState(new BlockPos(408, floor, 8)).is(Blocks.GRASS_BLOCK),
                    "Grass at Y=" + floor);
            for (int depth = 1; depth <= 3; depth++)
                assertTrue(
                        chunk.getBlockState(new BlockPos(408, floor - depth, 8)).is(Blocks.DIRT),
                        "Soil below Y=" + floor);
            assertFalse(
                    chunk.getBlockState(new BlockPos(408, floor - 5, 8)).is(Blocks.GRASS_BLOCK),
                    "Ceiling underside must not be treated as a grassy floor");
            assertTrue(
                    chunk.getBlockState(new BlockPos(408, floor + 1, 8)).isAir(),
                    "Do not add blocks into openings");
        }
        assertFalse(
                chunk.getBlockState(new BlockPos(414, 32, 14)).is(Blocks.GRASS_BLOCK),
                "Submerged floor stays ungrassed");
    }

    @Test
    void otherBiomesAndUninstalledGeneratorsKeepOriginalSurfaceRules() {
        var plains = surface(biomes.getOrThrow(Biomes.PLAINS), true);
        var originalAbyss = surface(biomes.getOrThrow(MiaBiomes.THE_ABYSS), true);
        var disabled = surface(biomes.getOrThrow(MiaBiomes.OVERWORLD_ABYSS), false);
        for (int y : new int[] {-48, 0, 32})
            for (var chunk : new ProtoChunk[] {plains, originalAbyss, disabled})
                assertFalse(
                        chunk.getBlockState(new BlockPos(408, y, 8)).is(Blocks.GRASS_BLOCK),
                        "Local override leaked");
    }
}
