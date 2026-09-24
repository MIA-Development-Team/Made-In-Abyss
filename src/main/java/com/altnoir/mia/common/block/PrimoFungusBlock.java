package com.altnoir.mia.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 太初菌（移植自 PoopSky 的 {@code PrimoFungusBlock}）。
 * <p>
 * 与 {@link MiaFungusBlock} 的区别只有两点：<b>两段式碰撞箱</b>（伞 + 柄）与<b>弹性</b>。
 * 生长机制完全复用 {@link MiaFungusBlock} —— 骨粉时放置 {@code mia:primo_fungus} /
 * {@code mia:glow_primo_fungus} 配置特征，成功率 0.4（{@code MiaFungusBlock.isBonemealSuccess}
 * 与 PoopSky 的实现同值，所以不必再覆盖）。
 * <p>
 * 不覆盖 {@code codec()}：这与 PoopSky 一致（它也没覆盖，于是反序列化得到的是父类
 * {@code SaplingBlock}/{@code MiaFungusBlock}）。这些方块都是代码注册 + 直接给 Properties，
 * 没有走数据包定义的路径，因此没有影响。
 */
public class PrimoFungusBlock extends MiaFungusBlock {
    private static final VoxelShape SHAPE =
            Shapes.or(
                    Block.box(2.0, 8.0, 2.0, 14.0, 14.0, 14.0),
                    Block.box(5.0, 0.0, 5.0, 11.0, 8.0, 11.0));

    public PrimoFungusBlock(ResourceKey<ConfiguredFeature<?, ?>> feature, Properties properties) {
        super(feature, properties);
    }

    @Override
    protected VoxelShape getShape(
            BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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
