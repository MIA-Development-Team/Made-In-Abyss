package com.altnoir.mementoinabyss.content.block.stripped_rotated_pillar;

import com.altnoir.mementoinabyss.content.block.base.StrippableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class StrippedRotatedPillarBlock extends RotatedPillarBlock implements StrippableBlock {
    public final Block strippedBlock;

    public StrippedRotatedPillarBlock(Block strippedBlock, Properties properties) {
        super(properties);
        this.strippedBlock = strippedBlock;
    }


    @Override
    public @Nullable BlockState getStrippedState(BlockState state, Level level, BlockPos pos, Player player) {
        return strippedBlock.defaultBlockState().trySetValue(AXIS, state.getValue(AXIS));
    }
}
