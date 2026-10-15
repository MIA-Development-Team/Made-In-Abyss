package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.OverworldAbyssBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.CanyonCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class OverworldAbyssCarversTest {
    private static final OverworldAbyssShape SHAPE =
            new OverworldAbyssShape(320, -64, 320, 12345, 1, 1.25);
    private static final LevelHeightAccessor HEIGHT = LevelHeightAccessor.create(-64, 384);
    private static HolderLookup.Provider registries;
    private static Holder<Biome> dedicatedInner;

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = VanillaRegistries.createLookup();
        // Mod features/music require the full loader. Their unchanged JSON is checked below;
        // decode the shipped effects and carvers with the vanilla registries here.
        JsonObject definition = biomeJson("overworld/the_abyss");
        definition.add("features", new JsonArray());
        definition.add("attributes", new JsonObject());
        var inner =
                Biome.DIRECT_CODEC
                        .parse(RegistryOps.create(JsonOps.INSTANCE, registries), definition)
                        .getOrThrow();
        var registry = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
        dedicatedInner =
                registry.register(MiaBiomes.OVERWORLD_ABYSS, inner, RegistrationInfo.BUILT_IN);
        registry.freeze();
    }

    @Test
    void dedicatedDefinitionAddsSkyfogForestAndCarversWhileKeepingOriginalHabitat()
            throws Exception {
        JsonObject original = biomeJson("the_abyss/the_abyss");
        JsonObject overworld = biomeJson("overworld/the_abyss");
        assertEquals(new JsonArray(), original.getAsJsonArray("carvers"));
        assertEquals(
                JsonParser.parseString(
                        "[\"minecraft:cave\",\"minecraft:cave_extra_underground\",\"minecraft:canyon\"]"),
                overworld.remove("carvers"));
        original.remove("carvers");
        var vegetation =
                overworld
                        .getAsJsonArray("features")
                        .get(
                                net.minecraft.world.level.levelgen.GenerationStep.Decoration
                                        .VEGETAL_DECORATION
                                        .ordinal())
                        .getAsJsonArray();
        int treeIndex =
                vegetation
                        .asList()
                        .indexOf(
                                new com.google.gson.JsonPrimitive(
                                        "mementoinabyss:the_abyss/overworld/trees_skyfog"));
        for (int index = 0; index < vegetation.size(); index++) {
            String feature = vegetation.get(index).getAsString();
            if (feature.contains("grass") || feature.contains("flower"))
                assertTrue(
                        treeIndex >= 0 && treeIndex < index,
                        "Skyfog trees must be placed before ground vegetation: " + feature);
        }
        assertTrue(
                vegetation.remove(
                        new com.google.gson.JsonPrimitive(
                                "mementoinabyss:the_abyss/overworld/trees_skyfog")),
                "The overworld habitat must reuse the existing multilayer skyfog forest placement");
        assertFalse(original.getAsJsonArray("features").toString().contains("trees_skyfog"));
        assertEquals(
                original, overworld, "Habitat visuals, music and features must remain identical");
        assertEquals(
                3,
                java.util.stream.StreamSupport.stream(
                                dedicatedInner
                                        .value()
                                        .getGenerationSettings()
                                        .getCarvers()
                                        .spliterator(),
                                false)
                        .count());
    }

    private static JsonObject biomeJson(String name) throws Exception {
        String resource = "data/mementoinabyss/worldgen/biome/" + name + ".json";
        try (var input =
                OverworldAbyssCarversTest.class.getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input, resource);
            return JsonParser.parseReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }

    @Test
    void delegatesOnlyCarverSelectionAndRetainsVisibleHabitat() {
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        var plains = biomes.getOrThrow(Biomes.PLAINS);
        var inner = dedicatedInner;
        var delegate = new FixedBiomeSource(plains);
        var visible = new OverworldAbyssBiomeSource(delegate, inner, List.of(plains), SHAPE);
        var settings =
                registries
                        .lookupOrThrow(Registries.NOISE_SETTINGS)
                        .getOrThrow(NoiseGeneratorSettings.OVERWORLD)
                        .value();
        var state = RandomState.create(settings, registries.lookupOrThrow(Registries.NOISE), 12345);
        assertSame(inner, visible.getNoiseBiome(100, 0, 0, state.sampler()));
        var carvers = OverworldAbyssCarvers.sourceForCarvers(visible, SHAPE, 400, 0);
        assertSame(delegate, carvers);
        assertSame(plains, carvers.getNoiseBiome(100, 0, 0, state.sampler()));
        assertSame(inner, visible.getNoiseBiome(100, 0, 0, state.sampler()));
        assertSame(visible, OverworldAbyssCarvers.sourceForCarvers(visible, SHAPE, 1040, 0));
        assertSame(visible, OverworldAbyssCarvers.sourceForCarvers(visible, null, 400, 0));
        assertSame(delegate, OverworldAbyssCarvers.sourceForCarvers(delegate, SHAPE, 400, 0));
        assertTrue(inner.is(MiaBiomes.OVERWORLD_ABYSS));
        assertTrue(inner.value().getGenerationSettings().getCarvers().iterator().hasNext());
        assertTrue(plains.value().getGenerationSettings().getCarvers().iterator().hasNext());
    }

    @Test
    void carverSamplerKeepsOriginalClimateAcrossCompositionAndFallsBackWhenUninstalled() {
        var settings =
                registries
                        .lookupOrThrow(Registries.NOISE_SETTINGS)
                        .getOrThrow(NoiseGeneratorSettings.OVERWORLD)
                        .value();
        var uninstalled =
                RandomState.create(settings, registries.lookupOrThrow(Registries.NOISE), 2468);
        assertSame(uninstalled.sampler(), OverworldAbyssCarvers.samplerForCarvers(uninstalled));

        var state = RandomState.create(settings, registries.lookupOrThrow(Registries.NOISE), 12345);
        var originalRouter = state.router();
        var originalSampler = state.sampler();
        OverworldAbyss.compose(state, originalRouter, SHAPE);
        var composedSampler = state.sampler();
        assertNotSame(originalSampler, composedSampler);
        assertSame(originalSampler, OverworldAbyssCarvers.samplerForCarvers(state));

        OverworldAbyss.compose(state, originalRouter, SHAPE);
        assertNotSame(composedSampler, state.sampler());
        assertSame(
                originalSampler,
                OverworldAbyssCarvers.samplerForCarvers(state),
                "Repeated composition must not replace the original sampler with an abyss sampler");
    }

    @Test
    void restoredConfiguredCarversActuallyExcavateRockInTheInnerHabitat() throws Exception {
        var lookup = registries.lookupOrThrow(Registries.BIOME);
        var plains = lookup.getOrThrow(Biomes.PLAINS);
        var inner = dedicatedInner;
        var source =
                new OverworldAbyssBiomeSource(
                        new FixedBiomeSource(plains), inner, List.of(plains), SHAPE);
        var generator =
                new NoiseBasedChunkGenerator(
                        source,
                        registries
                                .lookupOrThrow(Registries.NOISE_SETTINGS)
                                .getOrThrow(NoiseGeneratorSettings.OVERWORLD));
        var settings = generator.generatorSettings().value();
        var state = RandomState.create(settings, registries.lookupOrThrow(Registries.NOISE), 12345);
        var biomeRegistry = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
        lookup.listElements()
                .forEach(
                        holder ->
                                biomeRegistry.register(
                                        holder.key(), holder.value(), RegistrationInfo.BUILT_IN));
        biomeRegistry.freeze();
        var access = new RegistryAccess.ImmutableRegistryAccess(List.of(biomeRegistry));
        var palettes = PalettedContainerFactory.create(access);
        var position = new ChunkPos(25, 0);
        var selected =
                OverworldAbyssCarvers.sourceForCarvers(
                        source, SHAPE, position.getMinBlockX(), position.getMinBlockZ());
        var selectedBiome =
                selected.getNoiseBiome(position.x() * 4, 0, position.z() * 4, state.sampler());
        int index = 0;
        for (var holder : generator.getBiomeGenerationSettings(selectedBiome).getCarvers()) {
            var carver = withHeadlessStoneReplacement(holder.value());
            var chunk = new ProtoChunk(position, UpgradeData.EMPTY, HEIGHT, palettes, null);
            for (int x = position.getMinBlockX(); x <= position.getMaxBlockX(); x++)
                for (int z = position.getMinBlockZ(); z <= position.getMaxBlockZ(); z++)
                    for (int y = -64; y < 320; y++)
                        chunk.setBlockState(
                                new BlockPos(x, y, z), Blocks.STONE.defaultBlockState());
            var beardifier =
                    new DensityFunctions.BeardifierOrMarker() {
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
            var noise =
                    NoiseChunk.forChunk(
                            chunk,
                            state,
                            beardifier,
                            settings,
                            (x, y, z) ->
                                    new Aquifer.FluidStatus(-64, Blocks.AIR.defaultBlockState()),
                            Blender.empty());
            var context =
                    new CarvingContext(
                            generator, access, HEIGHT, noise, state, settings.surfaceRule());
            var aquifer =
                    Aquifer.createDisabled(
                            (x, y, z) ->
                                    new Aquifer.FluidStatus(-64, Blocks.AIR.defaultBlockState()));
            var mask = chunk.getOrCreateCarvingMask();
            var random = new WorldgenRandom(new LegacyRandomSource(0));
            int starts = 0;
            // A particular seed may have no canyon intersecting this target chunk. Search a
            // bounded deterministic seed set without forcing the carver's start probability.
            for (int attempt = 0;
                    attempt < 16 && mask.stream(position).findAny().isEmpty();
                    attempt++) {
                for (int dx = -8; dx <= 8; dx++) {
                    for (int dz = -8; dz <= 8; dz++) {
                        var sourcePos = new ChunkPos(position.x() + dx, position.z() + dz);
                        var carverSource =
                                OverworldAbyssCarvers.sourceForCarvers(
                                        source,
                                        SHAPE,
                                        sourcePos.getMinBlockX(),
                                        sourcePos.getMinBlockZ());
                        assertSame(
                                plains,
                                carverSource.getNoiseBiome(
                                        sourcePos.x() * 4, 0, sourcePos.z() * 4, state.sampler()));
                        random.setLargeFeatureSeed(
                                12345L + attempt * 99991L + index, sourcePos.x(), sourcePos.z());
                        if (carver.isStartChunk(random)) {
                            starts++;
                            carver.carve(
                                    context,
                                    chunk,
                                    ignored -> inner,
                                    random,
                                    aquifer,
                                    sourcePos,
                                    mask);
                        }
                    }
                }
            }
            String name = holder.unwrapKey().orElseThrow().identifier().toString();
            assertTrue(starts > 0, name);
            assertTrue(mask.stream(position).count() > 0, name + " must visit the inner rock");
            int excavated = 0;
            for (int x = position.getMinBlockX(); x <= position.getMaxBlockX(); x++)
                for (int z = position.getMinBlockZ(); z <= position.getMaxBlockZ(); z++)
                    for (int y = -64; y < 320; y++)
                        if (chunk.getBlockState(new BlockPos(x, y, z)).isAir()) excavated++;
            assertTrue(
                    excavated > 0,
                    name + " must remove actual stone independently of density caves");
            System.out.println(name + " excavated " + excavated + " blocks");
            index++;
        }
        assertEquals(3, index);
    }

    private static ConfiguredWorldCarver<?> withHeadlessStoneReplacement(
            ConfiguredWorldCarver<?> original) throws Exception {
        if (original.config().replaceable.contains(Blocks.STONE.builtInRegistryHolder()))
            return original;
        // VanillaRegistries.createLookup does not load block tags. Preserve the vanilla algorithm,
        // probability, height and shape; replace only its unresolved replaceable tag in this
        // fixture.
        var configuration = original.config();
        var common =
                new CarverConfiguration(
                        configuration.probability,
                        configuration.y,
                        configuration.yScale,
                        configuration.lavaLevel,
                        configuration.debugSettings,
                        HolderSet.direct(Blocks.STONE.builtInRegistryHolder()));
        if (configuration instanceof CaveCarverConfiguration cave) {
            var field = CaveCarverConfiguration.class.getDeclaredField("floorLevel");
            field.setAccessible(true);
            var floor = (FloatProvider) field.get(cave);
            assertSame(WorldCarver.CAVE, original.worldCarver());
            return WorldCarver.CAVE.configured(
                    new CaveCarverConfiguration(
                            common,
                            cave.horizontalRadiusMultiplier,
                            cave.verticalRadiusMultiplier,
                            floor));
        }
        if (configuration instanceof CanyonCarverConfiguration canyon) {
            assertSame(WorldCarver.CANYON, original.worldCarver());
            return WorldCarver.CANYON.configured(
                    new CanyonCarverConfiguration(common, canyon.verticalRotation, canyon.shape));
        }
        throw new AssertionError("Unexpected vanilla carver " + original);
    }
}
