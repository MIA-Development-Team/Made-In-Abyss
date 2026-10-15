package com.altnoir.mementoinabyss.infrastructure.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import com.altnoir.mementoinabyss.infrastructure.worldgen.density.*;
import com.altnoir.mementoinabyss.mixin.worldgen.RandomStateAccessor;
import com.mojang.serialization.Lifecycle;
import java.nio.file.*;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.carver.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.Test;

/** Full surface pass and adversarial actual carver block calls on both ends of the seam. */
class CrossDimensionAbyssSurfaceTest {
    @Test
    void surfacesAndCarversPreserveRockAndOpenings() throws Exception {
        CrossDimensionAbyssSeamTest.bootstrap();
        var registries = CrossDimensionAbyssSeamTest.registries;
        var biomes = new MappedRegistry<Biome>(Registries.BIOME, Lifecycle.stable());
        registries
                .lookupOrThrow(Registries.BIOME)
                .listElements()
                .forEach(h -> biomes.register(h.key(), h.value(), RegistrationInfo.BUILT_IN));
        biomes.freeze();
        var access = new RegistryAccess.ImmutableRegistryAccess(List.of(biomes));
        var palettes = PalettedContainerFactory.create(access);
        var biome = registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        var carveBlock =
                WorldCarver.class.getDeclaredMethod(
                        "carveBlock",
                        CarvingContext.class,
                        CarverConfiguration.class,
                        ChunkAccess.class,
                        java.util.function.Function.class,
                        CarvingMask.class,
                        BlockPos.MutableBlockPos.class,
                        BlockPos.MutableBlockPos.class,
                        Aquifer.class,
                        MutableBoolean.class);
        carveBlock.setAccessible(true);
        var template =
                net.minecraft.data.registries.VanillaRegistries.createLookup()
                        .lookupOrThrow(Registries.CONFIGURED_CARVER)
                        .getOrThrow(net.minecraft.data.worldgen.Carvers.CAVE)
                        .value()
                        .config();
        int protectedRock = 0, openingAir = 0, excavated = 0;
        long start = System.nanoTime();
        var csv = new StringBuilder("seed,dimension,x,z,min_display_y,blocks\n");
        var lodCsv = new StringBuilder("seed,dimension,x,z,y,cell,solid\n");
        for (long seed : new long[] {-3641258292459704473L, 12345, 987654321})
            for (boolean overworld : new boolean[] {true, false}) {
                var settings =
                        overworld
                                ? CrossDimensionAbyssSeamTest.overworldSettings
                                : CrossDimensionAbyssSeamTest.abyssSettings;
                var state =
                        RandomState.create(
                                settings, registries.lookupOrThrow(Registries.NOISE), seed);
                if (overworld)
                    ((RandomStateAccessor) (Object) state)
                            .mia$setRouter(
                                    OverworldAbyss.compose(
                                            state,
                                            state.router(),
                                            new OverworldAbyssShape(320, -64, 320, seed, 1, 1.25)));
                var seam = AbyssSeamRuntime.seamOf(state);
                assertNotNull(seam);
                var generator = CrossDimensionAbyssSeamTest.generator(settings);
                int minY = settings.noiseSettings().minY();
                var height = LevelHeightAccessor.create(minY, settings.noiseSettings().height());
                // Headless registry fixtures have no loaded block tags: supply the actual terrain
                // blocks explicitly while retaining the vanilla carver implementation.
                var config =
                        new CarverConfiguration(
                                template.probability,
                                template.y,
                                template.yScale,
                                template.lavaLevel,
                                template.debugSettings,
                                HolderSet.direct(
                                        settings.defaultBlock().getBlock().builtInRegistryHolder(),
                                        Blocks.DEEPSLATE.builtInRegistryHolder(),
                                        Blocks.STONE.builtInRegistryHolder()));
                for (var position : new ChunkPos[] {new ChunkPos(21, 0), new ChunkPos(-22, -1)}) {
                    var chunk = new ProtoChunk(position, UpgradeData.EMPTY, height, palettes, null);
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
                    var noise =
                            chunk.getOrCreateNoiseChunk(
                                    c ->
                                            NoiseChunk.forChunk(
                                                    c,
                                                    state,
                                                    beardifier,
                                                    settings,
                                                    (x, y, z) ->
                                                            new Aquifer.FluidStatus(
                                                                    minY,
                                                                    Blocks.WATER
                                                                            .defaultBlockState()),
                                                    Blender.empty()));
                    generator.fillFromNoise(Blender.empty(), state, null, chunk).join();
                    generator.buildSurface(
                            chunk,
                            new WorldGenerationContext(generator, height),
                            state,
                            null,
                            new BiomeManager((x, y, z) -> biome, seed),
                            biomes,
                            Blender.empty());
                    var context =
                            new CarvingContext(
                                    generator,
                                    access,
                                    height,
                                    noise,
                                    state,
                                    settings.surfaceRule());
                    var air =
                            Aquifer.createDisabled(
                                    (x, y, z) ->
                                            new Aquifer.FluidStatus(
                                                    minY, Blocks.AIR.defaultBlockState()));
                    var mask = chunk.getOrCreateCarvingMask();
                    for (int x = position.getMinBlockX(); x <= position.getMaxBlockX(); x++)
                        for (int z = position.getMinBlockZ(); z <= position.getMaxBlockZ(); z++) {
                            for (int depth = 0; depth < 64; depth++) {
                                int y =
                                        overworld
                                                ? minY + depth
                                                : minY + height.getHeight() - 1 - depth;
                                var pos = new BlockPos.MutableBlockPos(x, y, z);
                                boolean protect =
                                        AbyssSeamRuntime.protectedFromCarver(seam, x, y, z);
                                var before = chunk.getBlockState(pos);
                                // Deliberately attempt every voxel, including the entire roof.
                                carveBlock.invoke(
                                        WorldCarver.CAVE,
                                        context,
                                        config,
                                        chunk,
                                        (java.util.function.Function<BlockPos, Holder<Biome>>)
                                                ignored -> biome,
                                        mask,
                                        pos,
                                        new BlockPos.MutableBlockPos(),
                                        air,
                                        new MutableBoolean(false));
                                var after = chunk.getBlockState(pos);
                                if (protect)
                                    assertEquals(before, after, "Carver altered protected seam");
                                else if (before.blocksMotion() && after.isAir()) excavated++;
                                if (depth < 16) {
                                    double density =
                                            seam.wall()
                                                    .compute(
                                                            new DensityFunction.SinglePointContext(
                                                                    x, y, z));
                                    if (density > .125) {
                                        assertTrue(
                                                after.blocksMotion(),
                                                "Surface/carver lost roof rock");
                                        protectedRock++;
                                    } else if (density < -.125) {
                                        assertTrue(
                                                after.isAir(),
                                                "Surface/carver filled seam opening");
                                        openingAir++;
                                    }
                                }
                            }
                            csv.append(seed)
                                    .append(',')
                                    .append(overworld ? "overworld" : "abyss")
                                    .append(',')
                                    .append(x)
                                    .append(',')
                                    .append(z)
                                    .append(',')
                                    .append(overworld ? -64 : -112)
                                    .append(',');
                            for (int y = overworld ? -64 : 336; y <= (overworld ? 32 : 383); y++)
                                csv.append(
                                        chunk.getBlockState(new BlockPos(x, y, z)).blocksMotion()
                                                ? '1'
                                                : '0');
                            csv.append('\n');
                        }
                    exportLod(chunk, seed, overworld, lodCsv);
                }
            }
        assertTrue(protectedRock > 1000);
        assertTrue(openingAir > 1000);
        assertTrue(excavated > 0, "Unprotected controls must actually be excavated");
        var directory =
                Path.of(
                        System.getProperty(
                                "abyss.reportDirectory", "build/reports/overworld-abyss"));
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("cross-dimension-surface.csv"), csv);
        Files.writeString(directory.resolve("cross-dimension-lod.csv"), lodCsv);
        Files.writeString(
                directory.resolve("cross-dimension-runtime.json"),
                "{\"seeds\":3,\"chunks\":12,\"protectedRock\":"
                        + protectedRock
                        + ",\"openingAir\":"
                        + openingAir
                        + ",\"unprotectedExcavations\":"
                        + excavated
                        + ",\"elapsedMillis\":"
                        + (System.nanoTime() - start) / 1e6
                        + "}");
    }

    private static void exportLod(ProtoChunk chunk, long seed, boolean overworld, StringBuilder csv)
            throws Exception {
        int minY = overworld ? -64 : 336;
        var ids = new java.util.ArrayList<Integer>();
        ids.add(Block.getId(Blocks.AIR.defaultBlockState()));
        short[] voxels = new short[16 * 16 * 48];
        for (int z = 0; z < 16; z++)
            for (int x = 0; x < 16; x++)
                for (int y = 0; y < 48; y++) {
                    int id =
                            Block.getId(
                                    chunk.getBlockState(
                                            new BlockPos(
                                                    chunk.getPos().getMinBlockX() + x,
                                                    minY + y,
                                                    chunk.getPos().getMinBlockZ() + z)));
                    int index = ids.indexOf(id);
                    if (index < 0) {
                        index = ids.size();
                        ids.add(id);
                    }
                    voxels[(z * 16 + x) * 48 + y] = (short) index;
                }
        var storedType =
                Class.forName(
                        "com.altnoir.mementoinabyss.impl.lod.server.MiaLodStorage$StoredChunk");
        var storedConstructor = storedType.getDeclaredConstructors()[0];
        storedConstructor.setAccessible(true);
        var stored =
                storedConstructor.newInstance(
                        chunk.getPos().x(),
                        chunk.getPos().z(),
                        1,
                        minY,
                        48,
                        ids.stream().mapToInt(Integer::intValue).toArray(),
                        voxels,
                        false,
                        1L,
                        Path.of("seam-fixture"));
        var treeType =
                Class.forName("com.altnoir.mementoinabyss.impl.lod.server.MiaLodSectionTree");
        var from = treeType.getDeclaredMethod("from", storedType);
        from.setAccessible(true);
        var changes = treeType.getDeclaredMethod("changes", int.class, long.class);
        changes.setAccessible(true);
        var tree = from.invoke(null, stored);
        var columnType =
                Class.forName("com.altnoir.mementoinabyss.impl.lod.client.CrossDimensionLodColumn");
        var columnConstructor = columnType.getDeclaredConstructors()[0];
        columnConstructor.setAccessible(true);
        var mesherType =
                Class.forName("com.altnoir.mementoinabyss.impl.lod.client.CrossDimensionLodMesher");
        var mesh = mesherType.getDeclaredMethod("buildInterior", columnType, boolean.class);
        mesh.setAccessible(true);
        for (int cell : new int[] {1, 4, 8, 16}) {
            var sections = (java.util.List<?>) changes.invoke(tree, cell, 0L);
            assertEquals(3, sections.size());
            for (var section : sections) {
                var type = section.getClass();
                var paletteMethod = type.getDeclaredMethod("palette");
                paletteMethod.setAccessible(true);
                var voxelsMethod = type.getDeclaredMethod("voxels");
                voxelsMethod.setAccessible(true);
                var indexMethod = type.getDeclaredMethod("index");
                indexMethod.setAccessible(true);
                int[] palette = (int[]) paletteMethod.invoke(section);
                short[] data = (short[]) voxelsMethod.invoke(section);
                int sectionMin = minY + 16 * (int) indexMethod.invoke(section), size = 16 / cell;
                var column =
                        columnConstructor.newInstance(
                                "seam-validation",
                                overworld ? 0 : -448,
                                960,
                                chunk.getPos().x(),
                                chunk.getPos().z(),
                                cell,
                                sectionMin,
                                size,
                                palette,
                                data);
                assertNotNull(
                        mesh.invoke(null, column, false),
                        "Actual CPU LOD mesher must accept generated section");
                // Cross-section through each column's centre, using the production server
                // reduction.
                int zIndex = 8 / cell;
                for (int x = 0; x < size; x++)
                    for (int y = 0; y < size; y++) {
                        int id = palette[Short.toUnsignedInt(data[(zIndex * size + x) * size + y])];
                        csv.append(seed)
                                .append(',')
                                .append(overworld ? "overworld" : "abyss")
                                .append(',')
                                .append(chunk.getPos().getMinBlockX() + x * cell)
                                .append(',')
                                .append(chunk.getPos().getMinBlockZ() + zIndex * cell)
                                .append(',')
                                .append(sectionMin + y * cell + (overworld ? 0 : -448))
                                .append(',')
                                .append(cell)
                                .append(',')
                                .append(Block.BLOCK_STATE_REGISTRY.byId(id).blocksMotion() ? 1 : 0)
                                .append('\n');
                    }
            }
        }
    }
}
