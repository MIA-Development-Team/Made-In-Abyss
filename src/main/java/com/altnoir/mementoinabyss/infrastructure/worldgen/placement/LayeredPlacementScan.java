package com.altnoir.mementoinabyss.infrastructure.worldgen.placement;

import com.altnoir.mementoinabyss.init.MiaTags;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;

/** One eager placement invocation only; feature writes must never reuse these column cursors. */
final class LayeredPlacementScan {
    enum Surface {
        TREE,
        WATER_TREE,
        FLOOR,
        WATER,
        LAYER2,
        LAYER2_FLOOR;

        boolean matches(BlockState current, BlockState adjacent) {
            return switch (this) {
                case TREE -> treeSupport(adjacent) && treeSpace(current, false);
                case WATER_TREE ->
                        (adjacent.is(BlockTags.SUPPORTS_VEGETATION)
                                        || adjacent.is(MiaTags.BlockTags.BASE_STONE_ABYSS.tag))
                                && treeSpace(current, true);
                case FLOOR ->
                        !emptyFloor(adjacent)
                                && emptyFloor(current)
                                && !adjacent.is(Blocks.BEDROCK);
                case WATER -> current.isAir() && adjacent.is(Blocks.WATER);
                case LAYER2 ->
                        current.getFluidState().isEmpty()
                                && (current.isAir() || current.canBeReplaced())
                                && (adjacent.is(Blocks.WATER)
                                        || (!emptyFloor(adjacent) && !adjacent.is(Blocks.BEDROCK)));
                case LAYER2_FLOOR ->
                        waterOrReplaceable(current)
                                && !emptyFloor(adjacent)
                                && !adjacent.is(Blocks.BEDROCK);
            };
        }

        private static boolean treeSupport(BlockState state) {
            return state.is(BlockTags.SUPPORTS_VEGETATION)
                    || state.is(MiaTags.BlockTags.BASE_STONE_ABYSS.tag);
        }

        private static boolean treeSpace(BlockState state, boolean water) {
            return water && state.is(Blocks.WATER)
                    || state.getFluidState().isEmpty()
                            && (state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES));
        }

        static boolean waterOrReplaceable(BlockState state) {
            return state.is(Blocks.WATER)
                    || ((state.isAir() || state.canBeReplaced())
                            && (state.getFluidState().isEmpty()
                                    || state.getFluidState().is(FluidTags.WATER)));
        }

        private static boolean emptyFloor(BlockState state) {
            return state.isAir()
                    || state.is(Blocks.WATER)
                    || state.is(Blocks.LAVA)
                    || state.canBeReplaced();
        }
    }

    private static final int[] NO_HEIGHTS = new int[0];
    private final PlacementContext context;
    private final Surface surface;
    private final int minY;
    private final int maxY;
    private Column[] columns;
    private ChunkAccess[] chunks;

    LayeredPlacementScan(PlacementContext context, Surface surface) {
        this.context = context;
        this.surface = surface;
        minY = context.getMinY();
        maxY = Integer.MAX_VALUE;
    }

    /** Bounds apply to returned placement positions, including both endpoints. */
    LayeredPlacementScan(PlacementContext context, Surface surface, int minY, int maxY) {
        this.context = context;
        this.surface = surface;
        this.minY = Math.max(context.getLevel().getLevel().getMinY(), minY - 1);
        this.maxY = maxY;
    }

    int find(int x, int z, int layer) {
        int index = (x & 15) | ((z & 15) << 4);
        Column column = columns == null ? null : columns[index];
        if (column == null || column.x != x || column.z != z) {
            int top =
                    maxY == Integer.MAX_VALUE
                            ? context.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z)
                            : maxY;
            if (top <= minY) return Integer.MAX_VALUE;
            if (columns == null) {
                columns = new Column[256];
                chunks = new ChunkAccess[4];
            }
            int chunkX = SectionPos.blockToSectionCoord(x);
            int chunkZ = SectionPos.blockToSectionCoord(z);
            int chunkIndex = (chunkX & 1) | ((chunkZ & 1) << 1);
            ChunkAccess chunk = chunks[chunkIndex];
            if (chunk == null || chunk.getPos().x() != chunkX || chunk.getPos().z() != chunkZ) {
                chunk = context.getLevel().getChunk(chunkX, chunkZ);
                chunks[chunkIndex] = chunk;
            }
            column = new Column(chunk, x, z, top, minY);
            columns[index] = column;
        }
        return column.find(layer);
    }

    private final class Column {
        private final ChunkAccess chunk;
        private final LevelChunkSection[] sections;
        private final boolean directSections;
        private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        private final int x, z, minY;
        private int nextY;
        private BlockState current;
        private int[] heights = NO_HEIGHTS;
        private int found;
        private boolean exhausted;

        private Column(ChunkAccess chunk, int x, int z, int topY, int minY) {
            this.chunk = chunk;
            this.sections = chunk.getSections();
            directSections =
                    chunk.getClass() == ProtoChunk.class || chunk.getClass() == LevelChunk.class;
            this.x = x;
            this.z = z;
            this.minY = minY;
            current = chunk.getBlockState(cursor.set(x, topY, z));
            nextY = topY - 1;
        }

        private int find(int layer) {
            if (layer < found) return heights[layer];
            if (exhausted) return Integer.MAX_VALUE;
            while (nextY >= minY) {
                int index = chunk.getSectionIndex(nextY);
                LevelChunkSection section =
                        index < 0 || index >= sections.length ? null : sections[index];
                int bottom = (nextY >> 4) << 4;
                if (directSections && (section == null || section.hasOnlyAir())) {
                    // Keep the air state at the section boundary so the next rock face is tested.
                    current = Blocks.AIR.defaultBlockState();
                    nextY = bottom - 1;
                    continue;
                }
                int end = Math.max(minY, bottom);
                do {
                    // Custom chunk subclasses may synthesize states outside their section storage.
                    BlockState adjacent =
                            directSections
                                    ? section.getBlockState(x & 15, nextY & 15, z & 15)
                                    : chunk.getBlockState(cursor.set(x, nextY, z));
                    int result = nextY + 1;
                    nextY--;
                    boolean matches = surface.matches(current, adjacent);
                    current = adjacent;
                    if (matches) {
                        if (found == heights.length)
                            heights = Arrays.copyOf(heights, Math.max(4, found * 2));
                        heights[found++] = result;
                        if (layer < found) return result;
                    }
                } while (nextY >= end);
            }
            exhausted = true;
            return Integer.MAX_VALUE;
        }
    }
}
