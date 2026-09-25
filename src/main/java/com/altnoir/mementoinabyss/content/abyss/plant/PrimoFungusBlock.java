package com.altnoir.mementoinabyss.content.abyss.plant;

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
 * Primo fungus sapling: two-part collision (cap + stem) and bounce. Growth reuses {@link
 * MiaFungusBlock}.
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
