package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.OverworldAbyssBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverworldAbyssBiomeTest {
    private static HolderLookup.Provider registries;
    private static MultiNoiseBiomeSource delegate;
    private static Holder<Biome> inner;
    private static List<Holder<Biome>> fallbacks;
    private static final long[] SEEDS = {0, 12345, -85217, 42, 987654321};
    private static final LevelHeightAccessor HEIGHT = LevelHeightAccessor.create(-64, 384);

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = VanillaRegistries.createLookup();
        var bindTags =
                Holder.Reference.class.getDeclaredMethod("bindTags", java.util.Collection.class);
        bindTags.setAccessible(true);
        for (var holder : registries.lookupOrThrow(Registries.BIOME).listElements().toList())
            bindTags.invoke(holder, aquatic(holder) ? List.of(BiomeTags.IS_OCEAN) : List.of());
        delegate =
                MultiNoiseBiomeSource.createFromPreset(
                        registries
                                .lookupOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
                                .getOrThrow(
                                        ResourceKey.create(
                                                Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST,
                                                Identifier.withDefaultNamespace("overworld"))));
        // A distinct direct holder catches accidentally delegating the central habitat.
        inner =
                Holder.direct(
                        registries
                                .lookupOrThrow(Registries.BIOME)
                                .getOrThrow(Biomes.THE_VOID)
                                .value());
        fallbacks =
                List.of(
                                Biomes.FROZEN_PEAKS,
                                Biomes.JAGGED_PEAKS,
                                Biomes.STONY_PEAKS,
                                Biomes.SNOWY_SLOPES,
                                Biomes.GROVE,
                                Biomes.MEADOW)
                        .stream()
                        .map(
                                key ->
                                        (Holder<Biome>)
                                                registries
                                                        .lookupOrThrow(Registries.BIOME)
                                                        .getOrThrow(key))
                        .toList();
    }

    private static NoiseBasedChunkGenerator generator() {
        return new NoiseBasedChunkGenerator(
                delegate,
                registries
                        .lookupOrThrow(Registries.NOISE_SETTINGS)
                        .getOrThrow(NoiseGeneratorSettings.OVERWORLD));
    }

    private static OverworldAbyssShape shape(long seed) {
        return new OverworldAbyssShape(240, -64, 320, seed, 2.0, 1.25);
    }

    private static RandomState state(long seed, boolean modified) {
        var state =
                RandomState.create(
                        generator().generatorSettings().value(),
                        registries.lookupOrThrow(Registries.NOISE),
                        seed);
        if (modified)
            ((RandomStateAccessor) (Object) state)
                    .mia$setRouter(OverworldAbyss.compose(state, state.router(), shape(seed)));
        return state;
    }

    private static OverworldAbyssBiomeSource source(long seed) {
        return new OverworldAbyssBiomeSource(delegate, inner, fallbacks, shape(seed));
    }

    @Test
    void innerHabitatAppliesAtAllHeightsOnlyBelowOneAndAHalfRadii() {
        for (long seed : SEEDS) {
            var state = state(seed, true);
            var source = source(seed);
            for (int[] point : new int[][] {{0, 0}, {356, 0}, {-356, 0}, {212, 284}}) {
                for (int y : new int[] {-64, 64, 240, 316}) {
                    assertSame(
                            inner,
                            source.getNoiseBiome(
                                    QuartPos.fromBlock(point[0]),
                                    QuartPos.fromBlock(y),
                                    QuartPos.fromBlock(point[1]),
                                    state.sampler()));
                }
            }
            assertNotSame(inner, source.getNoiseBiome(90, 60, 0, state.sampler()));
        }
    }

    @Test
    void actualHighRingSelectsLandAndRetainsOriginalTemperatureAndHumidity() {
        int originallyAquatic = 0;
        double lowestTemperature = Double.POSITIVE_INFINITY;
        double highestTemperature = Double.NEGATIVE_INFINITY;
        for (long seed : SEEDS) {
            var original = state(seed, false);
            var modified = state(seed, true);
            var source = source(seed);
            var generator = generator();
            for (int[] point :
                    new int[][] {
                        {480, 0}, {-480, 0}, {0, 480}, {0, -480}, {288, 384}, {-288, -384}
                    }) {
                int y =
                        generator.getBaseHeight(
                                point[0],
                                point[1],
                                Heightmap.Types.OCEAN_FLOOR_WG,
                                HEIGHT,
                                modified);
                assertTrue(y >= 212 && y <= 260, "seed=" + seed + ", ring height=" + y);
                int qx = QuartPos.fromBlock(point[0]),
                        qy = QuartPos.fromBlock(y),
                        qz = QuartPos.fromBlock(point[1]);
                var before = original.sampler().sample(qx, qy, qz);
                var after = modified.sampler().sample(qx, qy, qz);
                assertEquals(before.temperature(), after.temperature());
                assertEquals(before.humidity(), after.humidity());
                lowestTemperature = Math.min(lowestTemperature, before.temperature());
                highestTemperature = Math.max(highestTemperature, before.temperature());
                if (aquatic(delegate.getNoiseBiome(qx, qy, qz, original.sampler())))
                    originallyAquatic++;
                var selected = source.getNoiseBiome(qx, qy, qz, modified.sampler());
                assertFalse(
                        aquatic(selected), "Aquatic ridge: seed=" + seed + ", biome=" + selected);
                assertTrue(
                        List.of(
                                        Biomes.FROZEN_PEAKS,
                                        Biomes.JAGGED_PEAKS,
                                        Biomes.STONY_PEAKS,
                                        Biomes.BADLANDS,
                                        Biomes.ERODED_BADLANDS,
                                        Biomes.WOODED_BADLANDS)
                                .stream()
                                .anyMatch(selected::is),
                        "Ridge should follow peak climate: seed=" + seed + ", biome=" + selected);
                assertNotSame(inner, selected);
            }
        }
        assertTrue(
                originallyAquatic > 0,
                "Fixture must exercise a vanilla aquatic climate on the ring");
        assertTrue(
                highestTemperature > lowestTemperature,
                "Ring retains distinct temperature climates");
    }

    @Test
    void outsideInfluenceHasExactVanillaBiomeAndClimateAndOtherStatesStayUntouched() {
        for (long seed : SEEDS) {
            var original = state(seed, false);
            var originalRouter = original.router();
            var originalSampler = original.sampler();
            var modified = state(seed, true);
            var source = source(seed);
            for (int[] point : new int[][] {{784, 0}, {-784, 0}, {0, -784}, {2048, -1024}}) {
                for (int y : new int[] {-48, 64, 240}) {
                    int qx = QuartPos.fromBlock(point[0]),
                            qy = QuartPos.fromBlock(y),
                            qz = QuartPos.fromBlock(point[1]);
                    assertEquals(
                            original.sampler().sample(qx, qy, qz),
                            modified.sampler().sample(qx, qy, qz));
                    assertSame(
                            delegate.getNoiseBiome(qx, qy, qz, original.sampler()),
                            source.getNoiseBiome(qx, qy, qz, modified.sampler()));
                }
            }
            assertSame(originalRouter, original.router());
            assertSame(originalSampler, original.sampler());
            assertNull(OverworldAbyss.shapeOf(original));
        }
    }

    @Test
    void possibleBiomesAndReplacedGeneratorFeatureSupplierIncludeInnerHabitat() throws Exception {
        var source = source(12345);
        assertTrue(source.possibleBiomes().containsAll(delegate.possibleBiomes()));
        assertTrue(source.possibleBiomes().contains(inner));
        assertTrue(source.possibleBiomes().containsAll(fallbacks));
        var sorted =
                FeatureSorter.buildFeaturesPerStep(
                        List.copyOf(source.possibleBiomes()),
                        biome -> biome.value().getGenerationSettings().features(),
                        true);
        var generator = generator();
        var field =
                net.minecraft.world.level.chunk.ChunkGenerator.class.getDeclaredField(
                        "featuresPerStep");
        field.setAccessible(true);
        var oldSupplier = field.get(generator);
        OverworldAbyss.replaceBiomeSource(generator, source);
        assertSame(source, generator.getBiomeSource());
        assertNotSame(oldSupplier, field.get(generator));
        var supplier = (java.util.function.Supplier<?>) field.get(generator);
        assertEquals(sorted.size(), ((List<?>) supplier.get()).size());
        assertInstanceOf(net.neoforged.neoforge.common.util.Lazy.class, supplier);
        ((net.neoforged.neoforge.common.util.Lazy<?>) supplier).invalidate();
        assertEquals(sorted.size(), ((List<?>) supplier.get()).size());
        assertSame(delegate, OverworldAbyssBiomeSource.unwrap(source));
    }

    @Test
    void sourceCodecPreservesDelegateAndHabitatAcrossSaveLoad() {
        var ops = registries.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        var codec = OverworldAbyssBiomeSource.CODEC.codec();
        var source = source(12345);
        var encoded = codec.encodeStart(ops, source).getOrThrow();
        var restored = codec.parse(ops, encoded).getOrThrow();
        assertEquals(encoded, codec.encodeStart(ops, restored).getOrThrow());
        var generator = generator();
        OverworldAbyss.replaceBiomeSource(generator, source);
        var generatorCodec = net.minecraft.world.level.chunk.ChunkGenerator.CODEC;
        var savedGenerator = generatorCodec.encodeStart(ops, generator).getOrThrow();
        var restoredGenerator = generatorCodec.parse(ops, savedGenerator).getOrThrow();
        assertInstanceOf(OverworldAbyssBiomeSource.class, restoredGenerator.getBiomeSource());
        assertEquals(
                savedGenerator, generatorCodec.encodeStart(ops, restoredGenerator).getOrThrow());
        var state = state(12345, true);
        assertSame(
                delegate.getNoiseBiome(256, 16, 0, state.sampler()),
                restored.getNoiseBiome(256, 16, 0, state.sampler()));
    }

    @Test
    void aquaticForeignSourceGetsLandFallbackOnlyOnRaisedRing() {
        var ocean = registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.DEEP_OCEAN);
        var fixed = new net.minecraft.world.level.biome.FixedBiomeSource(ocean);
        var source = new OverworldAbyssBiomeSource(fixed, inner, fallbacks, shape(12345));
        var state = state(12345, true);
        assertFalse(aquatic(source.getNoiseBiome(120, 60, 0, state.sampler())));
        assertSame(ocean, source.getNoiseBiome(196, 16, 0, state.sampler()));
    }

    @Test
    void actualChunkBiomeFillUsesWrappedSourceAndCachedClimateSampler() {
        var biomes =
                new net.minecraft.core.MappedRegistry<Biome>(
                        Registries.BIOME, com.mojang.serialization.Lifecycle.stable());
        registries
                .lookupOrThrow(Registries.BIOME)
                .listElements()
                .forEach(
                        holder ->
                                biomes.register(
                                        holder.key(),
                                        holder.value(),
                                        net.minecraft.core.RegistrationInfo.BUILT_IN));
        biomes.freeze();
        var palettes =
                net.minecraft.world.level.chunk.PalettedContainerFactory.create(
                        new net.minecraft.core.RegistryAccess.ImmutableRegistryAccess(
                                List.of(biomes)));
        for (int chunkX : new int[] {0, 30}) {
            var generator = generator();
            var state = state(987654321, true);
            OverworldAbyss.replaceBiomeSource(generator, source(987654321));
            var chunk =
                    new net.minecraft.world.level.chunk.ProtoChunk(
                            new net.minecraft.world.level.ChunkPos(chunkX, 0),
                            net.minecraft.world.level.chunk.UpgradeData.EMPTY,
                            HEIGHT,
                            palettes,
                            null);
            var beardifier =
                    new net.minecraft.world.level.levelgen.DensityFunctions.BeardifierOrMarker() {
                        public double compute(FunctionContext context) {
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
                            net.minecraft.world.level.levelgen.NoiseChunk.forChunk(
                                    c,
                                    state,
                                    beardifier,
                                    generator.generatorSettings().value(),
                                    (x, y, z) ->
                                            new net.minecraft.world.level.levelgen.Aquifer
                                                    .FluidStatus(
                                                    63,
                                                    net.minecraft.world.level.block.Blocks.WATER
                                                            .defaultBlockState()),
                                    net.minecraft.world.level.levelgen.blending.Blender.empty()));
            generator
                    .createBiomes(
                            state,
                            net.minecraft.world.level.levelgen.blending.Blender.empty(),
                            null,
                            chunk)
                    .join();
            chunk.setPersistedStatus(net.minecraft.world.level.chunk.status.ChunkStatus.BIOMES);
            int qx = chunkX * 4;
            if (chunkX == 0) assertSame(inner, chunk.getNoiseBiome(qx, 32, 0));
            else {
                int y =
                        generator.getBaseHeight(
                                chunkX * 16, 0, Heightmap.Types.OCEAN_FLOOR_WG, HEIGHT, state);
                var selected = chunk.getNoiseBiome(qx, QuartPos.fromBlock(y), 0);
                assertFalse(aquatic(selected));
                assertFalse(selected.is(Biomes.DRIPSTONE_CAVES));
                assertFalse(selected.is(Biomes.LUSH_CAVES));
                assertFalse(selected.is(Biomes.DEEP_DARK));
            }
        }
    }

    private static boolean aquatic(Holder<Biome> biome) {
        // Vanilla bootstrap holders do not necessarily have biome tags bound in this fixture.
        return List.of(
                        Biomes.OCEAN,
                        Biomes.DEEP_OCEAN,
                        Biomes.COLD_OCEAN,
                        Biomes.DEEP_COLD_OCEAN,
                        Biomes.FROZEN_OCEAN,
                        Biomes.DEEP_FROZEN_OCEAN,
                        Biomes.LUKEWARM_OCEAN,
                        Biomes.DEEP_LUKEWARM_OCEAN,
                        Biomes.WARM_OCEAN,
                        Biomes.RIVER,
                        Biomes.FROZEN_RIVER,
                        Biomes.BEACH,
                        Biomes.SNOWY_BEACH,
                        Biomes.STONY_SHORE)
                .stream()
                .anyMatch(biome::is);
    }
}
