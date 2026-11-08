package com.altnoir.mementoinabyss.content.artifact;

import com.altnoir.mementoinabyss.impl.artifact.enhancement.ArtifactEnhancementMenu;
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

public final class ArtifactSmithingTableBlock extends CraftingTableBlock {
    public static final MapCodec<ArtifactSmithingTableBlock> CODEC =
            simpleCodec(ArtifactSmithingTableBlock::new);
    private static final VoxelShape SHAPE =
            Shapes.or(
                    box(3, 5, 3, 13, 11, 13),
                    box(1, 0, 1, 15, 5, 15),
                    box(0, 11, 0, 16, 16, 16),
                    box(11, 0, 0, 16, 6, 5),
                    box(0, 0, 0, 5, 6, 5),
                    box(11, 0, 11, 16, 6, 16),
                    box(0, 0, 11, 5, 6, 16));
    private static final Component TITLE =
            Component.translatable("container.mementoinabyss.artifact_enhancement");

    public ArtifactSmithingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ArtifactSmithingTableBlock> codec() {
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
                        new ArtifactEnhancementMenu(
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
