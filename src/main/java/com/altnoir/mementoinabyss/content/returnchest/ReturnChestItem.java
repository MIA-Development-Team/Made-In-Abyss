package com.altnoir.mementoinabyss.content.returnchest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import org.jspecify.annotations.Nullable;

public final class ReturnChestItem extends BlockItem {
    public ReturnChestItem(ReturnChestBlock block, Properties properties) {
        super(block, properties);
    }

    public static VoxelShape previewShape() {
        return ReturnChestLayout.OUTLINE;
    }

    public BlockPos getPlacementBase(BlockPlaceContext context) {
        return baseFromAnchor(placementAnchor(context), context.getClickedFace());
    }

    private static BlockPos placementAnchor(BlockPlaceContext context) {
        BlockPos target = context.getClickedPos();
        return context.getClickedFace().getAxis().isHorizontal() ? target.below() : target;
    }

    private static BlockPos baseFromAnchor(BlockPos anchor, Direction face) {
        return face.getAxis().isHorizontal() ? anchor.relative(face) : anchor;
    }

    public boolean canPlaceStructure(BlockPlaceContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        BlockPos base = getPlacementBase(context);
        BlockState shell = getBlock().defaultBlockState();
        var itemState =
                context.getItemInHand()
                        .getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
        if (ReturnChests.isCore(itemState.apply(shell))
                || !getBlock().isEnabled(level.enabledFeatures())
                || !level.isLoaded(context.getClickedPos())
                || !context.canPlace()
                || player != null && !player.mayBuild()) {
            return false;
        }

        CollisionContext collision = CollisionContext.placementContext(player);
        for (BlockPos pos : ReturnChestLayout.volume(base)) {
            if (!level.isInWorldBounds(pos)
                    || !level.getWorldBorder().isWithinBounds(pos)
                    || !level.isLoaded(pos)) {
                return false;
            }
            var existing = level.getBlockState(pos);
            var offset = pos.subtract(base);
            if (ReturnChestLayout.isWindow(offset)) {
                if (!existing.isAir()) return false;
            } else if (existing.hasBlockEntity()
                    || !existing.getFluidState().isEmpty()
                    || !BlockPlaceContext.at(context, pos, context.getClickedFace())
                            .replacingClickedOnBlock()
                    || !level.isUnobstructed(shell, pos, collision)
                    || player != null && !level.mayInteract(player, pos)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @Nullable BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        if (!canPlaceStructure(context)) return null;
        if (!context.getClickedFace().getAxis().isHorizontal()) return context;
        return BlockPlaceContext.at(context, placementAnchor(context), context.getClickedFace());
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState placementState) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return super.placeBlock(context, placementState);
        }
        BlockPos anchor = context.getClickedPos();
        BlockPos base = baseFromAnchor(anchor, context.getClickedFace());
        BlockPos center = base.above();
        UUID structure = UUID.randomUUID();
        int flags = Block.UPDATE_ALL_IMMEDIATE;
        int snapshotStart = level.capturedBlockSnapshots.size();
        List<BlockSnapshot> changed = new ArrayList<>();
        List<BlockPos> offsets = new ArrayList<>(ReturnChestLayout.parts());
        BlockPos anchorOffset = anchor.subtract(base);
        offsets.remove(anchorOffset);
        offsets.addFirst(anchorOffset);
        for (BlockPos offset : offsets) {
            BlockPos pos = base.offset(offset);
            BlockSnapshot before = BlockSnapshot.create(level.dimension(), level, pos, flags);
            BlockState state =
                    placementState.setValue(
                            ReturnChestBlock.PART, ReturnChestLayout.partOf(offset));
            if (!level.setBlock(pos, state, flags)) {
                rollback(level, changed, snapshotStart);
                return false;
            }
            changed.add(before);
            if (!(level.getBlockEntity(pos) instanceof ReturnChestBlockEntity part)) {
                rollback(level, changed, snapshotStart);
                return false;
            }
            part.link(center, structure);
        }
        ReturnChests.bind(level, center, DyeColor.LIME);
        return true;
    }

    private static void rollback(
            ServerLevel level, List<BlockSnapshot> changed, int snapshotStart) {
        boolean capturing = level.captureBlockSnapshots;
        boolean restoring = level.restoringBlockSnapshots;
        level.captureBlockSnapshots = false;
        level.restoringBlockSnapshots = true;
        try {
            for (BlockSnapshot snapshot : changed.reversed()) {
                snapshot.restore();
            }
            level.capturedBlockSnapshots
                    .subList(snapshotStart, level.capturedBlockSnapshots.size())
                    .clear();
        } finally {
            level.captureBlockSnapshots = capturing;
            level.restoringBlockSnapshots = restoring;
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(
            BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        return false;
    }
}
