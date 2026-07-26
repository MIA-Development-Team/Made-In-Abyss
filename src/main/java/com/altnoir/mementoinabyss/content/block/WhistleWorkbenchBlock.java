package com.altnoir.mementoinabyss.content.block;

import com.altnoir.mementoinabyss.impl.whistle.workbench.WhistleWorkbenchMenu;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class WhistleWorkbenchBlock extends CraftingTableBlock {
    public static final MapCodec<WhistleWorkbenchBlock> CODEC =
            simpleCodec(WhistleWorkbenchBlock::new);
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
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (containerId, inventory, player) -> new WhistleWorkbenchMenu(
                        containerId,
                        inventory,
                        ContainerLevelAccess.create(level, pos)
                ),
                TITLE
        );
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hit
    ) {
        if (!level.isClientSide()) {
            player.openMenu(getMenuProvider(state, level, pos));
        }
        return InteractionResult.SUCCESS;
    }
}
