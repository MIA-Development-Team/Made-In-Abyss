package com.altnoir.mementoinabyss.content.whistle;

import com.altnoir.mementoinabyss.impl.whistle.workbench.WhistleWorkbenchMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class WhistleWorkbenchBlock extends CraftingTableBlock {
    public static final MapCodec<WhistleWorkbenchBlock> CODEC =
            simpleCodec(WhistleWorkbenchBlock::new);
    private static final VoxelShape SHAPE =
            Shapes.or(
                    box(0, 0, 0, 16, 4, 16),
                    box(3, 4, 3, 8, 8, 8),
                    box(5, 4, 5, 11, 10, 11),
                    box(8, 8, 8, 13, 10, 13),
                    box(0, 10, 0, 16, 16, 16));
    private static final Component TITLE =
            Component.translatable("container.mementoinabyss.whistle_workbench");

    public WhistleWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<WhistleWorkbenchBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inventory, player) ->
                        new WhistleWorkbenchMenu(
                                containerId, inventory, ContainerLevelAccess.create(level, pos)),
                TITLE);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            player.openMenu(getMenuProvider(state, level, pos));
        }
        return InteractionResult.SUCCESS;
    }
}
