package com.altnoir.mementoinabyss.infrastructure.worldgen.layer2;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.jspecify.annotations.Nullable;

public final class Layer2GeneratorView extends NoiseBasedChunkGenerator {
    private final NoiseBasedChunkGenerator original;
    private final Layer2Runtime.State state;
    private final @Nullable WorldGenLevel level;
    private final boolean structure;
    private final int minY;
    private final int maxY;

    public Layer2GeneratorView(
            NoiseBasedChunkGenerator original,
            Layer2Runtime.State state,
            @Nullable WorldGenLevel level,
            boolean structure,
            int minY,
            int maxY) {
        super(original.getBiomeSource(), original.generatorSettings());
        this.original = original;
        this.state = state;
        this.level = level;
        this.structure = structure;
        this.minY = minY;
        this.maxY = maxY;
    }

    public Layer2GeneratorView(
            NoiseBasedChunkGenerator original,
            Layer2Runtime.State state,
            @Nullable WorldGenLevel level,
            boolean structure) {
        this(original, state, level, structure, structure ? -112 : -128, structure ? -64 : 0);
    }

    static int surfaceHeight(
            Layer2Runtime.State state,
            @Nullable WorldGenLevel level,
            boolean structure,
            Heightmap.Types type,
            int x,
            int z,
            int minY,
            int maxY) {
        if (structure && level != null && level.hasChunk(x >> 4, z >> 4)) {
            var point = new BlockPos.MutableBlockPos(x, maxY, z);
            for (int y = Math.min(maxY, level.getMaxY() - 1);
                    y >= Math.max(minY, level.getMinY());
                    y--) {
                point.setY(y);
                var block = level.getBlockState(point);
                if (type.isOpaque().test(block)
                        && !type.isOpaque().test(level.getBlockState(point.above()))
                        && level.getFluidState(point.above()).isEmpty()) return y + 1;
            }
            return minY - 1;
        }
        int modelFloor = (int) Math.floor(state.surfaceHeight(x, z)) + 1;
        if (!structure) {
            ChunkAccess chunk = availableChunk(level, x, z);
            if (chunk != null) {
                int top = Math.min(maxY - 1, (int) Math.floor(state.roofHeight(x, z)) - 8);
                return featureHeight(chunk, type, x, z, minY, top);
            }
            if (type.isOpaque().test(Blocks.WATER.defaultBlockState())
                    && modelFloor < -112
                    && !state.opening(x, z)) modelFloor = -112;
        }
        return Math.max(minY, Math.min(maxY + 1, modelFloor));
    }

    private static @Nullable ChunkAccess availableChunk(
            @Nullable WorldGenLevel level, int x, int z) {
        while (level instanceof Layer2WorldGenLevel scoped) level = scoped.original();
        int chunkX = x >> 4, chunkZ = z >> 4;
        if (level == null || !level.hasChunk(chunkX, chunkZ)) return null;
        return level instanceof WorldGenRegion
                ? level.getChunk(chunkX, chunkZ)
                : level.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
    }

    private static int featureHeight(
            ChunkAccess chunk, Heightmap.Types type, int x, int z, int minY, int topY) {
        int top = Math.min(topY, chunk.getMaxY());
        int bottom = Math.max(Math.max(minY, chunk.getMinY()), top - 127);
        var sections = chunk.getSections();
        boolean direct =
                chunk.getClass() == ProtoChunk.class || chunk.getClass() == LevelChunk.class;
        var cursor = direct ? null : new BlockPos.MutableBlockPos();
        BlockState above = null;
        var opaque = type.isOpaque();
        // Starting below the modeled roof omits its upper face; an initial solid run is ceiling.
        for (int y = top; y >= bottom; ) {
            int index = chunk.getSectionIndex(y);
            LevelChunkSection section =
                    index < 0 || index >= sections.length ? null : sections[index];
            if (direct && (section == null || section.hasOnlyAir())) {
                above = Blocks.AIR.defaultBlockState();
                y = (y & ~15) - 1;
                continue;
            }
            BlockState current =
                    direct
                            ? section.getBlockState(x & 15, y & 15, z & 15)
                            : chunk.getBlockState(cursor.set(x, y, z));
            if (above != null && opaque.test(current) && !opaque.test(above)) return y + 1;
            above = current;
            y--;
        }
        return minY;
    }

    @Override
    public int getBaseHeight(
            int x,
            int z,
            Heightmap.Types type,
            LevelHeightAccessor heightAccessor,
            RandomState randomState) {
        WorldGenLevel physical =
                level != null
                        ? level
                        : heightAccessor instanceof WorldGenLevel world ? world : null;
        if (structure && (physical == null || !physical.hasChunk(x >> 4, z >> 4))) {
            var column = original.getBaseColumn(x, z, heightAccessor, randomState);
            int bottom = Math.max(minY, heightAccessor.getMinY());
            int top = Math.min(maxY, heightAccessor.getMaxY() - 1);
            for (int y = top; y >= bottom; y--) {
                var block = column.getBlock(y);
                var above = column.getBlock(y + 1);
                if (type.isOpaque().test(block)
                        && !type.isOpaque().test(above)
                        && above.getFluidState().isEmpty()) return y + 1;
            }
            return minY - 1;
        }
        return surfaceHeight(state, physical, structure, type, x, z, minY, maxY);
    }

    @Override
    public int getSeaLevel() {
        return -112;
    }

    @Override
    public int getMinY() {
        return structure ? original.getMinY() : minY;
    }

    @Override
    public int getGenDepth() {
        return structure ? original.getGenDepth() : maxY - minY + 1;
    }

    @Override
    public NoiseColumn getBaseColumn(
            int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        return original.getBaseColumn(x, z, heightAccessor, randomState);
    }

    @Override
    public CompletableFuture<ChunkAccess> createBiomes(
            RandomState randomState,
            Blender blender,
            StructureManager structureManager,
            ChunkAccess chunk) {
        return original.createBiomes(randomState, blender, structureManager, chunk);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunk) {
        return original.fillFromNoise(blender, randomState, structureManager, chunk);
    }

    @Override
    public void buildSurface(
            WorldGenRegion region,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunk) {
        original.buildSurface(region, structureManager, randomState, chunk);
    }

    @Override
    public void applyCarvers(
            WorldGenRegion region,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk) {
        original.applyCarvers(region, seed, randomState, biomeManager, structureManager, chunk);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
        original.spawnOriginalMobs(region);
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos) {
        original.addDebugScreenInfo(result, randomState, feetPos);
    }
}
