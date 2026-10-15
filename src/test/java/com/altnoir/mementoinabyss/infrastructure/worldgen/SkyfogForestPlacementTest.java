package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.MiaBiomes;
import com.altnoir.mementoinabyss.infrastructure.worldgen.biome.OverworldAbyssBiomeSource;
import com.altnoir.mementoinabyss.infrastructure.worldgen.density.OverworldAbyssShape;
import com.altnoir.mementoinabyss.infrastructure.worldgen.feature.MiaAbyssPlacements;
import com.altnoir.mementoinabyss.infrastructure.worldgen.placement.TreeOnEveryLayerPlacement;
import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.placement.*;
import org.junit.jupiter.api.Test;

class SkyfogForestPlacementTest {
    @Test
    void flowerCoveredFloorsYieldRealGroundCandidatesAndGrowActualSkyfogLogsAndLeaves()
            throws Exception {
        verifyForest(false);
    }

    @Test
    void actualOverworldShoulderGrowsSkyfogThroughBiomeFilteredPlacement() throws Exception {
        verifyForest(true);
    }

    @Test
    void migratedCoveredStoneFloorsStillGrowSkyfog() throws Exception {
        verifyForest(false, true);
    }

    private void verifyForest(boolean actualTerrain) throws Exception {
        verifyForest(actualTerrain, false);
    }

    private void verifyForest(boolean actualTerrain, boolean coveredStone) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var registries = SkyfogBiomeRegistryTest.registries();
        var habitat =
                registries.lookupOrThrow(Registries.BIOME).getOrThrow(MiaBiomes.OVERWORLD_ABYSS);
        var forest =
                registries
                        .lookupOrThrow(Registries.PLACED_FEATURE)
                        .getOrThrow(MiaAbyssPlacements.OVERWORLD_TREES_SKYFOG)
                        .value();
        var biomes =
                new MappedRegistry<Biome>(
                        Registries.BIOME, com.mojang.serialization.Lifecycle.stable());
        var plains = registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS).value();
        biomes.register(Biomes.PLAINS, plains, RegistrationInfo.BUILT_IN);
        biomes.register(MiaBiomes.OVERWORLD_ABYSS, habitat.value(), RegistrationInfo.BUILT_IN);
        biomes.freeze();
        var placedRegistry =
                new MappedRegistry<PlacedFeature>(
                        Registries.PLACED_FEATURE, com.mojang.serialization.Lifecycle.stable());
        registries
                .lookupOrThrow(Registries.PLACED_FEATURE)
                .listElements()
                .forEach(
                        h ->
                                placedRegistry.register(
                                        h.key(), h.value(), RegistrationInfo.BUILT_IN));
        placedRegistry.freeze();
        var structures =
                new MappedRegistry<net.minecraft.world.level.levelgen.structure.Structure>(
                        Registries.STRUCTURE, com.mojang.serialization.Lifecycle.stable());
        structures.freeze();
        var access =
                new RegistryAccess.ImmutableRegistryAccess(
                        List.of(biomes, placedRegistry, structures));
        var palettes = PalettedContainerFactory.create(access);
        var chunks = new HashMap<Long, ProtoChunk>();
        java.util.function.BiFunction<Integer, Integer, ProtoChunk> chunkAt =
                (x, z) ->
                        chunks.computeIfAbsent(
                                ChunkPos.pack(x, z),
                                ignored ->
                                        new ProtoChunk(
                                                new ChunkPos(x, z),
                                                UpgradeData.EMPTY,
                                                LevelHeightAccessor.create(-64, 384),
                                                palettes,
                                                null));
        java.util.function.Function<BlockPos, BlockState> get =
                pos ->
                        chunkAt.apply(Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16))
                                .getBlockState(pos);
        java.util.function.BiConsumer<BlockPos, BlockState> set =
                (pos, state) ->
                        chunkAt.apply(Math.floorDiv(pos.getX(), 16), Math.floorDiv(pos.getZ(), 16))
                                .setBlockState(pos, state);
        for (int floor : new int[] {-32, 32, 128})
            for (int x = 0; x < 16; x++)
                for (int z = 0; z < 16; z++) {
                    set.accept(
                            new BlockPos(x, floor, z),
                            (coveredStone
                                            ? (floor < 100
                                                    ? MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get()
                                                    : MiaBlocks.COVERGRASS_TUFF.get())
                                            : floor < 0
                                                    ? Blocks.GRASS_BLOCK
                                                    : floor < 100 ? Blocks.PODZOL : Blocks.MYCELIUM)
                                    .defaultBlockState());
                    set.accept(
                            new BlockPos(x, floor + 1, z),
                            (x % 2 == 0 ? Blocks.SHORT_GRASS : Blocks.DANDELION)
                                    .defaultBlockState());
                }
        var bind = Holder.Reference.class.getDeclaredMethod("bindTags", Collection.class);
        bind.setAccessible(true);
        var restored = new LinkedHashMap<Holder.Reference<Block>, List<TagKey<Block>>>();
        try {
            bindShippedGroundTag(bind, restored, BlockTags.DIRT);
            bindShippedGroundTag(bind, restored, BlockTags.GRASS_BLOCKS);
            assertFalse(
                    Blocks.GRASS_BLOCK.defaultBlockState().is(BlockTags.DIRT),
                    "26.1 grass is not dirt: fixtures must not reintroduce the old tag membership");
            assertTrue(Blocks.GRASS_BLOCK.defaultBlockState().is(BlockTags.GRASS_BLOCKS));
            for (var block :
                    List.of(
                            MiaBlocks.COVERGRASS_ABYSS_ANDESITE.get(),
                            MiaBlocks.COVERGRASS_TUFF.get())) {
                assertTrue(block.defaultBlockState().is(BlockTags.GRASS_BLOCKS));
                assertFalse(block.defaultBlockState().is(BlockTags.DIRT));
            }
            addTag(bind, restored, Blocks.SHORT_GRASS, BlockTags.REPLACEABLE_BY_TREES);
            addTag(bind, restored, Blocks.DANDELION, BlockTags.REPLACEABLE_BY_TREES);
            addTag(bind, restored, MiaBlocks.SKYFOG_LOG.get(), BlockTags.LOGS);
            addTag(bind, restored, MiaBlocks.SKYFOG_LEAVES.get(), BlockTags.LEAVES);
            addTag(bind, restored, MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get(), BlockTags.LEAVES);
            var random = RandomSource.create(12345);
            var biomeManager =
                    new BiomeManager(
                            (qx, qy, qz) ->
                                    chunkAt.apply(qx >> 2, qz >> 2).getNoiseBiome(qx, qy, qz),
                            BiomeManager.obfuscateSeed(-1261428676705104120L));
            var level =
                    (WorldGenLevel)
                            Proxy.newProxyInstance(
                                    WorldGenLevel.class.getClassLoader(),
                                    new Class[] {WorldGenLevel.class},
                                    (proxy, method, args) ->
                                            switch (method.getName()) {
                                                case "getMinY" -> -64;
                                                case "getHeight" ->
                                                        args == null || args.length == 0
                                                                ? 384
                                                                : chunkAt.apply(
                                                                                        Math
                                                                                                .floorDiv(
                                                                                                        (int)
                                                                                                                args[
                                                                                                                        1],
                                                                                                        16),
                                                                                        Math
                                                                                                .floorDiv(
                                                                                                        (int)
                                                                                                                args[
                                                                                                                        2],
                                                                                                        16))
                                                                                .getHeight(
                                                                                        (Heightmap
                                                                                                        .Types)
                                                                                                args[
                                                                                                        0],
                                                                                        (int)
                                                                                                args[
                                                                                                        1],
                                                                                        (int)
                                                                                                args[
                                                                                                        2])
                                                                        + 1;
                                                case "getMaxY" -> 319;
                                                case "getChunk" ->
                                                        chunkAt.apply((int) args[0], (int) args[1]);
                                                case "getBlockState" ->
                                                        get.apply((BlockPos) args[0]);
                                                case "getFluidState" ->
                                                        get.apply((BlockPos) args[0])
                                                                .getFluidState();
                                                case "setBlock" -> {
                                                    set.accept(
                                                            (BlockPos) args[0],
                                                            (BlockState) args[1]);
                                                    yield true;
                                                }
                                                case "isStateAtPosition" ->
                                                        ((java.util.function.Predicate<BlockState>)
                                                                        args[1])
                                                                .test(
                                                                        get.apply(
                                                                                (BlockPos)
                                                                                        args[0]));
                                                case "isFluidAtPosition" ->
                                                        ((java.util.function.Predicate<
                                                                                net.minecraft.world
                                                                                        .level
                                                                                        .material
                                                                                        .FluidState>)
                                                                        args[1])
                                                                .test(
                                                                        get.apply(
                                                                                        (BlockPos)
                                                                                                args[
                                                                                                        0])
                                                                                .getFluidState());
                                                case "getRawBrightness",
                                                        "getBrightness",
                                                        "getMaxLocalRawBrightness" ->
                                                        15;
                                                case "canSeeSky" -> true;
                                                case "registryAccess" -> access;
                                                case "setCurrentlyGenerating" -> null;
                                                case "getRandom" -> random;
                                                case "getBiome" ->
                                                        actualTerrain
                                                                ? biomeManager.getBiome(
                                                                        (BlockPos) args[0])
                                                                : habitat;
                                                case "getBlockEntity" -> null;
                                                case "getSeed" -> 12345L;
                                                case "scheduleTick",
                                                        "blockUpdated",
                                                        "destroyBlockProgress",
                                                        "levelEvent" ->
                                                        null;
                                                case "getLevel" -> null;
                                                default -> {
                                                    if (method.isDefault())
                                                        yield InvocationHandler.invokeDefault(
                                                                proxy, method, args);
                                                    throw new UnsupportedOperationException(
                                                            method.toString());
                                                }
                                            });
            var generator =
                    new NoiseBasedChunkGenerator(
                            actualTerrain
                                    ? MultiNoiseBiomeSource.createFromPreset(
                                            registries
                                                    .lookupOrThrow(
                                                            Registries
                                                                    .MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST)
                                                    .getOrThrow(
                                                            net.minecraft.resources.ResourceKey
                                                                    .create(
                                                                            Registries
                                                                                    .MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST,
                                                                            net.minecraft.resources
                                                                                    .Identifier
                                                                                    .withDefaultNamespace(
                                                                                            "overworld"))))
                                    : new FixedBiomeSource(
                                            registries
                                                    .lookupOrThrow(Registries.BIOME)
                                                    .getOrThrow(Biomes.PLAINS)),
                            registries
                                    .lookupOrThrow(Registries.NOISE_SETTINGS)
                                    .getOrThrow(NoiseGeneratorSettings.OVERWORLD));
            if (actualTerrain) {
                // Isolate the production vegetation pass from unrelated structures and ore
                // features.
                var vegetation = new BiomeGenerationSettings.PlainBuilder();
                habitat.value()
                        .getGenerationSettings()
                        .features()
                        .get(GenerationStep.Decoration.VEGETAL_DECORATION.ordinal())
                        .forEach(
                                h ->
                                        vegetation.addFeature(
                                                GenerationStep.Decoration.VEGETAL_DECORATION, h));
                var settingsGetter =
                        ChunkGenerator.class.getDeclaredField("generationSettingsGetter");
                settingsGetter.setAccessible(true);
                var habitatSettings = vegetation.build();
                settingsGetter.set(
                        generator,
                        (java.util.function.Function<Holder<Biome>, BiomeGenerationSettings>)
                                b ->
                                        b.equals(habitat)
                                                ? habitatSettings
                                                : b.value().getGenerationSettings());
            }
            OverworldAbyss.replaceBiomeSource(
                    generator,
                    new OverworldAbyssBiomeSource(
                            generator.getBiomeSource(),
                            habitat,
                            List.of(),
                            new OverworldAbyssShape(320, -64, 320, 12345, 1, 1.25)));
            int originX = actualTerrain ? 400 : 0;
            if (actualTerrain) {
                long seed = -1261428676705104120L;
                var settings = generator.generatorSettings().value();
                var state =
                        RandomState.create(
                                settings, registries.lookupOrThrow(Registries.NOISE), seed);
                var shape = new OverworldAbyssShape(320, -64, 320, seed, 1, 1.25);
                ((com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor) (Object) state)
                        .mia$setRouter(OverworldAbyss.compose(state, state.router(), shape));
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
                for (int cx = 24; cx <= 26; cx++)
                    for (int cz = -1; cz <= 1; cz++) {
                        var chunk = chunkAt.apply(cx, cz);
                        chunk.getOrCreateNoiseChunk(
                                c ->
                                        NoiseChunk.forChunk(
                                                c,
                                                state,
                                                beardifier,
                                                settings,
                                                (x, y, z) ->
                                                        new Aquifer.FluidStatus(
                                                                -64,
                                                                Blocks.WATER.defaultBlockState()),
                                                net.minecraft.world.level.levelgen.blending.Blender
                                                        .empty()));
                        generator
                                .createBiomes(
                                        state,
                                        net.minecraft.world.level.levelgen.blending.Blender.empty(),
                                        null,
                                        chunk)
                                .join();
                        generator
                                .fillFromNoise(
                                        net.minecraft.world.level.levelgen.blending.Blender.empty(),
                                        state,
                                        null,
                                        chunk)
                                .join();
                        chunk.setPersistedStatus(
                                net.minecraft.world.level.chunk.status.ChunkStatus.BIOMES);
                        generator.buildSurface(
                                chunk,
                                new WorldGenerationContext(
                                        generator, LevelHeightAccessor.create(-64, 384)),
                                state,
                                null,
                                new BiomeManager((x, y, z) -> habitat, seed),
                                biomes,
                                net.minecraft.world.level.levelgen.blending.Blender.empty());
                    }
            }
            assertTrue(generator.getBiomeSource().possibleBiomes().contains(habitat));
            assertTrue(generator.getBiomeGenerationSettings(habitat).hasFeature(forest));
            var context = new PlacementContext(level, generator, Optional.empty());
            var candidates =
                    TreeOnEveryLayerPlacement.of(8)
                            .getPositions(context, random, new BlockPos(originX, 0, 0))
                            .toList();
            if (!actualTerrain)
                assertEquals(
                        24, candidates.size(), "All three flower-covered floors must be found");
            assertFalse(candidates.isEmpty());
            for (var pos : candidates) {
                if (!actualTerrain)
                    assertTrue(
                            Set.of(-31, 33, 129).contains(pos.getY()),
                            "Candidate must be above actual grass, not above a flower");
                assertTrue(get.apply(pos.below()).is(BlockTags.GRASS_BLOCKS));
            }
            if (actualTerrain) {
                var actualBiomes = new HashSet<Holder<Biome>>();
                for (int cx = 24; cx <= 26; cx++)
                    for (int cz = -1; cz <= 1; cz++)
                        for (var section : chunkAt.apply(cx, cz).getSections())
                            section.getBiomes().getAll(actualBiomes::add);
                actualBiomes.retainAll(generator.getBiomeSource().possibleBiomes());
                assertTrue(
                        actualBiomes.contains(habitat),
                        "Real decoration biome collection must retain the abyss habitat");
                var structuresDisabled =
                        new StructureManager(
                                null,
                                new net.minecraft.world.level.levelgen.WorldOptions(
                                        -1261428676705104120L, false, false),
                                null);
                generator.applyBiomeDecoration(level, chunkAt.apply(25, 0), structuresDisabled);
            } else {
                assertTrue(
                        forest.placeWithBiomeCheck(
                                level,
                                generator,
                                RandomSource.create(42),
                                new BlockPos(originX, -64, 0)),
                        "Complete placed-feature pipeline must pass the habitat's BiomeFilter");
            }
            int logs = 0, leaves = 0;
            for (int x = originX - 16; x < originX + 32; x++)
                for (int z = -16; z < 32; z++)
                    for (int y = actualTerrain ? -63 : 129; y < 160; y++) {
                        var state = get.apply(new BlockPos(x, y, z));
                        if (state.is(MiaBlocks.SKYFOG_LOG.get())) logs++;
                        if (state.is(MiaBlocks.SKYFOG_LEAVES.get())
                                || state.is(MiaBlocks.SKYFOG_LEAVES_WITH_FRUITS.get())) leaves++;
                    }
            assertTrue(logs >= 6, "Actual skyfog trunk must be placed");
            assertTrue(leaves > 20, "Actual skyfog canopy must be placed");
            System.out.println(
                    "Skyfog actualTerrain="
                            + actualTerrain
                            + " candidates="
                            + candidates.size()
                            + " logs="
                            + logs
                            + " leaves="
                            + leaves);
        } finally {
            for (var entry : restored.entrySet()) bind.invoke(entry.getKey(), entry.getValue());
        }
    }

    private static void bindShippedGroundTag(
            Method bind,
            Map<Holder.Reference<Block>, List<TagKey<Block>>> restored,
            TagKey<Block> tag)
            throws Exception {
        String resource =
                "data/"
                        + tag.location().getNamespace()
                        + "/tags/block/"
                        + tag.location().getPath()
                        + ".json";
        var resources = SkyfogForestPlacementTest.class.getClassLoader().getResources(resource);
        var entries = new LinkedHashSet<String>();
        while (resources.hasMoreElements()) {
            try (var reader =
                    new InputStreamReader(
                            resources.nextElement().openStream(), StandardCharsets.UTF_8)) {
                var definition = JsonParser.parseReader(reader).getAsJsonObject();
                if (definition.has("replace") && definition.get("replace").getAsBoolean())
                    entries.clear();
                for (var entry : definition.getAsJsonArray("values")) {
                    assertTrue(
                            entry.isJsonPrimitive(),
                            "Ground tags in this fixture contain direct required block IDs");
                    entries.add(entry.getAsString());
                }
            }
        }
        assertFalse(entries.isEmpty(), resource);
        for (var id : entries) {
            assertFalse(id.startsWith("#"), "Update fixture for nested ground tag: " + id);
            var block =
                    net.minecraft.core.registries.BuiltInRegistries.BLOCK
                            .get(net.minecraft.resources.Identifier.parse(id))
                            .orElseThrow()
                            .value();
            addTag(bind, restored, block, tag);
        }
    }

    private static void addTag(
            Method bind,
            Map<Holder.Reference<Block>, List<TagKey<Block>>> restored,
            Block block,
            TagKey<Block> tag)
            throws Exception {
        var holder = block.builtInRegistryHolder();
        restored.putIfAbsent(holder, holder.tags().toList());
        var tags = new HashSet<>(holder.tags().toList());
        tags.add(tag);
        bind.invoke(holder, tags);
    }
}
