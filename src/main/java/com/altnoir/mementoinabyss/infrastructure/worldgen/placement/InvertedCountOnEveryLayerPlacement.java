package com.altnoir.mementoinabyss.infrastructure.worldgen.placement;

import com.altnoir.mementoinabyss.infrastructure.worldgen.tree.InvertedTreeFeature;
import com.altnoir.mementoinabyss.init.MiaPlacementModifiers;
import com.mojang.serialization.MapCodec;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class InvertedCountOnEveryLayerPlacement extends PlacementModifier {
    public static final MapCodec<InvertedCountOnEveryLayerPlacement> CODEC =
            IntProviders.codec(0, 256)
                    .fieldOf("count")
                    .xmap(InvertedCountOnEveryLayerPlacement::new, p_191611_ -> p_191611_.count);
    private final IntProvider count;

    public InvertedCountOnEveryLayerPlacement(IntProvider count) {
        this.count = count;
    }

    public static InvertedCountOnEveryLayerPlacement of(IntProvider count) {
        return new InvertedCountOnEveryLayerPlacement(count);
    }

    public static InvertedCountOnEveryLayerPlacement of(int count) {
        return of(ConstantInt.of(count));
    }

    @Override
    public Stream<BlockPos> getPositions(
            PlacementContext context, RandomSource random, BlockPos pos) {
        Stream.Builder<BlockPos> builder = Stream.builder();
        CeilingScan scan = new CeilingScan(context);
        int attempts = count.sample(random);
        for (int j = 0; j < attempts; j++) {
            int x = random.nextInt(16) + pos.getX();
            int z = random.nextInt(16) + pos.getZ();
            int y = scan.find(x, z);
            if (y != Integer.MAX_VALUE) builder.add(new BlockPos(x, y, z));
        }

        return builder.build();
    }

    @Override
    public PlacementModifierType<?> type() {
        return MiaPlacementModifiers.INVERTED_COUNT_ON_EVERY_LAYER.get();
    }

    // The snapshot is confined to this eager placement call, before any feature writes.
    private static final class CeilingScan {
        private static final Predicate<BlockState> ROOTED_DIRT =
                state -> state.is(Blocks.ROOTED_DIRT);
        private final PlacementContext context;
        private final PlacedFeature topFeature;
        private final boolean tree;
        private final boolean checkBiome;
        private final int minimumHeight;
        private final Column[] columns = new Column[256];
        private final ChunkScan[] chunks = new ChunkScan[4];

        private CeilingScan(PlacementContext context) {
            this.context = context;
            topFeature = context.topFeature().orElse(null);
            var configured = topFeature == null ? null : topFeature.feature().value();
            TreeConfiguration config =
                    configured != null
                                    && configured.feature() instanceof InvertedTreeFeature
                                    && configured.config() instanceof TreeConfiguration c
                            ? c
                            : null;
            tree = config != null;
            minimumHeight = tree ? config.trunkPlacer.getBaseHeight() : 1;
            checkBiome =
                    tree && topFeature.placement().stream().anyMatch(p -> p instanceof BiomeFilter);
        }

        private int find(int x, int z) {
            int index = (x & 15) | ((z & 15) << 4);
            Column column = columns[index];
            if (column == null || column.x != x || column.z != z) {
                int chunkX = SectionPos.blockToSectionCoord(x);
                int chunkZ = SectionPos.blockToSectionCoord(z);
                int chunkIndex = (chunkX & 1) | ((chunkZ & 1) << 1);
                ChunkScan chunk = chunks[chunkIndex];
                if (chunk == null
                        || chunk.chunk.getPos().x() != chunkX
                        || chunk.chunk.getPos().z() != chunkZ) {
                    chunk = new ChunkScan(context.getLevel().getChunk(chunkX, chunkZ));
                    chunks[chunkIndex] = chunk;
                }
                column = new Column(chunk, x, z);
                columns[index] = column;
            }
            return column.find();
        }

        private static final class ChunkScan {
            private final ChunkAccess chunk;
            private final LevelChunkSection[] sections;
            private final byte[] rootedSections;
            private final boolean directSections;

            private ChunkScan(ChunkAccess chunk) {
                this.chunk = chunk;
                sections = chunk.getSections();
                rootedSections = new byte[sections.length];
                directSections =
                        chunk.getClass() == ProtoChunk.class
                                || chunk.getClass() == LevelChunk.class;
            }

            private boolean canContainRoot(int index) {
                if (!directSections) return true;
                if (index < 0 || index >= sections.length) return false;
                if (rootedSections[index] == 0) {
                    LevelChunkSection section = sections[index];
                    rootedSections[index] =
                            (byte)
                                    (section != null
                                                    && !section.hasOnlyAir()
                                                    && section.maybeHas(ROOTED_DIRT)
                                            ? 2
                                            : 1);
                }
                return rootedSections[index] == 2;
            }
        }

        private final class Column {
            private final ChunkScan scan;
            private final int x, z, topY;
            private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            private int nextY;
            private int firstCeiling = Integer.MAX_VALUE;
            private BlockState below;

            private Column(ChunkScan scan, int x, int z) {
                this.scan = scan;
                this.x = x;
                this.z = z;
                topY = context.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                nextY = context.getMinY() + 1;
            }

            private BlockState stateAt(int y) {
                if (!scan.directSections) return scan.chunk.getBlockState(cursor.set(x, y, z));
                int index = scan.chunk.getSectionIndex(y);
                LevelChunkSection section =
                        index < 0 || index >= scan.sections.length ? null : scan.sections[index];
                return section == null
                        ? Blocks.AIR.defaultBlockState()
                        : section.getBlockState(x & 15, y & 15, z & 15);
            }

            private int find() {
                if (!tree && firstCeiling != Integer.MAX_VALUE) return firstCeiling;
                while (nextY <= topY) {
                    int index = scan.chunk.getSectionIndex(nextY);
                    if (!scan.canContainRoot(index)) {
                        nextY = (nextY | 15) + 1;
                        below = null;
                        continue;
                    }
                    int rootY = nextY++;
                    BlockState root = stateAt(rootY);
                    BlockState previous = below;
                    below = root;
                    if (!root.is(Blocks.ROOTED_DIRT)) continue;
                    if (previous == null) previous = stateAt(rootY - 1);
                    if (tree ? !InvertedTreeFeature.canReplace(previous) : !isEmpty(previous))
                        continue;
                    int y = rootY - 1;
                    if (tree) {
                        if (y - minimumHeight - 1 < context.getMinY()) continue;
                        if (checkBiome
                                && !context.generator()
                                        .getBiomeGenerationSettings(
                                                context.getLevel().getBiome(cursor.set(x, y, z)))
                                        .hasFeature(topFeature)) continue;
                        boolean fits = true;
                        for (int offset = 1; offset < minimumHeight; offset++) {
                            if (!InvertedTreeFeature.canReplace(stateAt(y - offset))) {
                                fits = false;
                                break;
                            }
                        }
                        if (!fits) continue;
                    }
                    firstCeiling = y;
                    return y;
                }
                return Integer.MAX_VALUE;
            }
        }
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }
}
