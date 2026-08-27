package com.altnoir.mia.worldgen.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Selects dry cave floors from a bounded number of columns in a structure's candidate chunk.
 */
public record CaveFloorSearch(VerticalAnchor minY, int clearance, int horizontalSamples) {
    public static final Codec<CaveFloorSearch> CODEC = RecordCodecBuilder.<CaveFloorSearch>create(instance -> instance.group(
                    VerticalAnchor.CODEC.fieldOf("min_y").forGetter(CaveFloorSearch::minY),
                    Codec.intRange(1, 128).fieldOf("clearance").forGetter(CaveFloorSearch::clearance),
                    Codec.intRange(1, 256).fieldOf("horizontal_samples").forGetter(CaveFloorSearch::horizontalSamples)
            )
            .apply(instance, CaveFloorSearch::new));

    public Optional<BlockPos> findStart(Structure.GenerationContext context, int maxStartY) {
        WorldGenerationContext heightContext = new WorldGenerationContext(
                context.chunkGenerator(),
                context.heightAccessor()
        );
        int minFloorY = Math.max(this.minY.resolveY(heightContext), context.heightAccessor().getMinBuildHeight());
        int boundedMaxStartY = Math.min(
                maxStartY,
                context.heightAccessor().getMaxBuildHeight() - this.clearance
        );
        if (minFloorY + 1 > boundedMaxStartY) {
            return Optional.empty();
        }

        ChunkPos chunk = context.chunkPos();
        int[] columnOffsets = shuffledColumnOffsets(context);
        List<BlockPos> candidates = new ArrayList<>();
        for (int sample = 0; sample < this.horizontalSamples; sample++) {
            int packedOffset = columnOffsets[sample];
            int x = chunk.getMinBlockX() + (packedOffset & 15);
            int z = chunk.getMinBlockZ() + (packedOffset >> 4);
            BlockColumn column = context.chunkGenerator().getBaseColumn(
                    x,
                    z,
                    context.heightAccessor(),
                    context.randomState()
            );
            for (int y : this.findStartYs(column, minFloorY, boundedMaxStartY)) {
                candidates.add(new BlockPos(x, y, z));
            }
        }

        return candidates.isEmpty()
                ? Optional.empty()
                : Optional.of(candidates.get(context.random().nextInt(candidates.size())));
    }

    /**
     * Returns every valid origin Y. Each origin is one block above a dry, sturdy floor and has the
     * configured number of dry air blocks above it.
     */
    public List<Integer> findStartYs(BlockColumn column, int minFloorY, int maxStartY) {
        List<Integer> starts = new ArrayList<>();
        int dryAirRun = 0;
        int scanTopY = maxStartY + this.clearance - 1;
        BlockPos.MutableBlockPos floorPos = new BlockPos.MutableBlockPos();

        for (int y = scanTopY; y >= minFloorY; y--) {
            var state = column.getBlock(y);
            if (state.isAir() && state.getFluidState().isEmpty()) {
                dryAirRun++;
                continue;
            }

            int startY = y + 1;
            if (startY <= maxStartY
                    && dryAirRun >= this.clearance
                    && state.getFluidState().isEmpty()
                    && state.isFaceSturdy(EmptyBlockGetter.INSTANCE, floorPos.set(0, y, 0), Direction.UP)) {
                starts.add(startY);
            }
            dryAirRun = 0;
        }

        return List.copyOf(starts);
    }

    private int[] shuffledColumnOffsets(Structure.GenerationContext context) {
        int[] offsets = new int[256];
        for (int index = 0; index < offsets.length; index++) {
            offsets[index] = index;
        }
        for (int index = 0; index < this.horizontalSamples; index++) {
            int selected = index + context.random().nextInt(offsets.length - index);
            int swap = offsets[index];
            offsets[index] = offsets[selected];
            offsets[selected] = swap;
        }
        return offsets;
    }
}
