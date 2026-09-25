package com.altnoir.mementoinabyss.content.abyss.plant;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mycelium mat — multiface growth like glow lichen. Waterlogging is handled by {@link
 * net.minecraft.world.level.block.MultifaceBlock} in 26.1.
 */
public class MyceliumMatBlock extends MultifaceSpreadeableBlock implements BonemealableBlock {
    public static final MapCodec<MyceliumMatBlock> CODEC = simpleCodec(MyceliumMatBlock::new);
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public MyceliumMatBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<MyceliumMatBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return Direction.stream()
                .anyMatch(
                        direction ->
                                this.spreader.canSpreadInAnyDirection(
                                        state, level, pos, direction.getOpposite()));
    }

    @Override
    public boolean isBonemealSuccess(
            Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        this.spreader.spreadFromRandomFaceTowardRandomDirection(state, level, pos, random);
    }

    @Override
    public MultifaceSpreader getSpreader() {
        return this.spreader;
    }
}
