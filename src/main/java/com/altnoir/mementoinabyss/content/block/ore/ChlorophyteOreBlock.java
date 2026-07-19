package com.altnoir.mementoinabyss.content.block.ore;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class ChlorophyteOreBlock extends DropExperienceBlock {
    private final Block spreadInto;
    public ChlorophyteOreBlock(IntProvider experience, Block spreadInto, Properties properties) {
        super(experience, properties);
        this.spreadInto = spreadInto;
    }
    @Override protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(50) != 0) return;
        var targets = new ArrayList<BlockPos>();
        for (Direction direction : Direction.values()) {
            BlockPos target = pos.relative(direction);
            if (level.getBlockState(target).is(spreadInto)) targets.add(target);
        }
        if (!targets.isEmpty()) level.setBlockAndUpdate(targets.get(random.nextInt(targets.size())), state);
    }
}
