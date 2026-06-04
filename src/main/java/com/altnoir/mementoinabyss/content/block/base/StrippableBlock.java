package com.altnoir.mementoinabyss.content.block.base;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface StrippableBlock {
    @Nullable BlockState getStrippedState(BlockState state, Level level, BlockPos pos, Player player);
}
