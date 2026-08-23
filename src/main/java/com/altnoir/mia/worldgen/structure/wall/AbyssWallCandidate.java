package com.altnoir.mia.worldgen.structure.wall;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;

public record AbyssWallCandidate(
        int id,
        double angle,
        int y,
        double predictedRadius,
        BlockPos anchor,
        ChunkPos startChunk,
        Orientation orientation
) {
    public enum TemplateKind {
        STRAIGHT,
        TILT
    }

    public record Orientation(TemplateKind templateKind, Rotation rotation) {
    }
}
