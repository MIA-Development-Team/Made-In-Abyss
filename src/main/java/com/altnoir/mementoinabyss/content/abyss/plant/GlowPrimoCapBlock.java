package com.altnoir.mementoinabyss.content.abyss.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Glowing translucent primo fungus cap with bounce behaviour. */
public class GlowPrimoCapBlock extends HalfTransparentBlock {
    public GlowPrimoCapBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(
            Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance * 0.5);
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        if (BounceHelper.shouldBounce(entity)) {
            BounceHelper.bounceUp(entity);
        } else {
            super.updateEntityMovementAfterFallOn(level, entity);
        }
    }
}
