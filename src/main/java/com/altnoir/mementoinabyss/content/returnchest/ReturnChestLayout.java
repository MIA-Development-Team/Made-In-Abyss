package com.altnoir.mementoinabyss.content.returnchest;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class ReturnChestLayout {
    private static final List<BlockPos> OFFSETS = createOffsets();
    static final VoxelShape OUTLINE = Shapes.box(-1, 0, -1, 2, 3, 2);

    private static final double EDGE = 2.0 / 16.0;
    private static final double INNER = 14.0 / 16.0;
    private static final VoxelShape CORNER = Shapes.block();
    private static final VoxelShape CORE =
            Shapes.box(1.0 / 16.0, 1.0 / 16.0, 1.0 / 16.0, 15.0 / 16.0, 15.0 / 16.0, 15.0 / 16.0);
    private static final VoxelShape MIDDLE_CORNER = Shapes.box(EDGE, 0.0, EDGE, INNER, 1.0, INNER);
    private static final VoxelShape BEAM_ALONG_X = Shapes.box(0.0, EDGE, EDGE, 1.0, INNER, INNER);
    private static final VoxelShape BEAM_ALONG_Z = Shapes.box(EDGE, EDGE, 0.0, INNER, INNER, 1.0);
    private static final VoxelShape EMPTY = Shapes.empty();

    static Iterable<BlockPos> volume(BlockPos base) {
        return BlockPos.betweenClosed(base.offset(-1, 0, -1), base.offset(1, 2, 1));
    }

    static List<BlockPos> parts() {
        return OFFSETS;
    }

    static ReturnChestPart partOf(BlockPos offset) {
        ReturnChestPart part = ReturnChestPart.byOffset(offset);
        if (part == null) throw new IllegalArgumentException("Not a part offset: " + offset);
        return part;
    }

    static boolean isWindow(BlockPos offset) {
        int radius = Math.abs(offset.getX()) + Math.abs(offset.getZ());
        return offset.getY() == 2 && radius == 0 || offset.getY() == 1 && radius == 1;
    }

    static VoxelShape shape(BlockPos offset) {
        int x = Math.abs(offset.getX());
        int z = Math.abs(offset.getZ());
        if (offset.getY() == 1) {
            if (x == 1 && z == 1) return MIDDLE_CORNER;
            return x == 0 && z == 0 ? CORE : EMPTY;
        }
        if (x == 1 && z == 1) return CORNER;
        if (x == 1) return BEAM_ALONG_Z;
        if (z == 1) return BEAM_ALONG_X;
        return EMPTY;
    }

    static VoxelShape shape(ReturnChestPart part) {
        return shape(part.offset());
    }

    private static List<BlockPos> createOffsets() {
        List<BlockPos> offsets = new ArrayList<>();
        offsets.add(BlockPos.ZERO);
        for (BlockPos offset : volume(BlockPos.ZERO)) {
            if (!offset.equals(BlockPos.ZERO) && !isWindow(offset)) {
                offsets.add(offset.immutable());
            }
        }
        return List.copyOf(offsets);
    }

    private ReturnChestLayout() {}
}
