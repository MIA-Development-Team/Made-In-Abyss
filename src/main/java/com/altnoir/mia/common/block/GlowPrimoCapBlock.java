package com.altnoir.mia.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 发光太初菌伞（移植自 PoopSky 的 {@code GlowPrimoCapBlock}）：半透明 + 发光，
 * 摔落伤害减半、落地反弹（与 {@link PrimoCapBlock} 同）。
 */
public class GlowPrimoCapBlock extends HalfTransparentBlock {
    public GlowPrimoCapBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void fallOn(
            Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance * 0.5F);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (BounceHelper.shouldBounce(entity)) {
            BounceHelper.bounceUp(entity);
        } else {
            super.updateEntityAfterFallOn(level, entity);
        }
    }
}
