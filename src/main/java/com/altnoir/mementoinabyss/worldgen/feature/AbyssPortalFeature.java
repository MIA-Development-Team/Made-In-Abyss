package com.altnoir.mementoinabyss.worldgen.feature;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AbyssPortalFeature {
    private static final int RADIUS = 8;

    public static void clearPortalLayer(ServerLevelAccessor level, BlockPos center, int height) {
        buildLayer(level, center, Blocks.AIR, Blocks.AIR, height, true);
    }

    public static void createPortal(ServerLevelAccessor level, BlockPos center) {
        buildLayer(level, center, MiaBlocks.ABYSS_PORTAL.get(), MiaBlocks.ABYSS_PORTAL_FRAME.get(), 0, false);
    }

    private static void buildLayer(ServerLevelAccessor level, BlockPos center, Block fill, Block frame,
                                   int height, boolean dropBlocks) {
        BlockPos.MutableBlockPos cursor = center.mutable();
        int radiusSquared = RADIUS * RADIUS;
        for (int x = -RADIUS - 1; x <= RADIUS + 1; x++) {
            for (int z = -RADIUS - 1; z <= RADIUS + 1; z++) {
                int distance = x * x + z * z;
                boolean inside = distance < radiusSquared;
                boolean edge = !inside && (x * x + (z - 1) * (z - 1) < radiusSquared
                        || (x - 1) * (x - 1) + z * z < radiusSquared
                        || x * x + (z + 1) * (z + 1) < radiusSquared
                        || (x + 1) * (x + 1) + z * z < radiusSquared);
                if (!inside && !edge) {
                    continue;
                }
                cursor.set(center).move(x, height, z);
                BlockState target;
                if (x == 0 && z == 0) {
                    target = MiaBlocks.FOSSILIZED_WOOD.get().defaultBlockState();
                } else if (inside) {
                    target = fill.defaultBlockState();
                    if (dropBlocks && !level.getBlockState(cursor).isAir()) {
                        level.destroyBlock(cursor, true, null);
                    }
                } else if (frame == Blocks.AIR) {
                    target = switch (level.getRandom().nextInt(3)) {
                        case 0 -> MiaBlocks.FOSSILIZED_WOOD.get().defaultBlockState();
                        case 1 -> Blocks.POLISHED_TUFF.defaultBlockState();
                        default -> Blocks.TUFF.defaultBlockState();
                    };
                } else {
                    target = frame.defaultBlockState();
                }
                level.setBlock(cursor, target, Block.UPDATE_ALL);
            }
        }
    }

    private AbyssPortalFeature() {
    }
}
