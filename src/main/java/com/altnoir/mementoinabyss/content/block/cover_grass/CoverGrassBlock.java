package com.altnoir.mementoinabyss.content.block.cover_grass;

import com.altnoir.mementoinabyss.content.block.base.TillableBlock;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

public class CoverGrassBlock extends Block implements TillableBlock, BonemealableBlock {
    public final Block defaultBlock;

    public static final MapCodec<CoverGrassBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Block.CODEC.fieldOf("defaultBlock").forGetter(block -> block.defaultBlock),
                    BlockBehaviour.Properties.CODEC.fieldOf("properties").forGetter(block -> block.properties)
            ).apply(instance, CoverGrassBlock::new)
    );

    public CoverGrassBlock(Block defaultBlock, Properties properties) {
        super(properties);
        this.defaultBlock = defaultBlock;
    }

    @Override
    public MapCodec<CoverGrassBlock> codec() {
        return CODEC;
    }

    protected static boolean canBeGrass(BlockState state, LevelReader levelReader, BlockPos pos) {
        var blockpos = pos.above();
        var blockstate = levelReader.getBlockState(blockpos);
        if (blockstate.isCollisionShapeFullBlock(levelReader, blockpos)
                || blockstate.getFluidState().isFull()) {
            return false;
        } else {
            var i = LightEngine.getLightBlockInto(
                    state, blockstate, Direction.UP, blockstate.getLightDampening());
            return i < 15;
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, level, pos)) {
            if (!level.isAreaLoaded(pos, 1)) return;
            level.setBlockAndUpdate(pos, this.defaultBlock.defaultBlockState());
        }
    }

    @Override
    public BlockState getTilledState(BlockState state, Level level, BlockPos pos, Player player) {
        return Blocks.FARMLAND.defaultBlockState();
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return levelReader.getBlockState(blockPos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        var blockpos = pos.above();

        for (int i = 0; i < 128; i++) {
            var blockpos1 = blockpos;
            var validPath = true;

            for (int j = 0; j < i / 16; j++) {
                blockpos1 = blockpos1.offset(
                        random.nextInt(3) - 1,
                        (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                        random.nextInt(3) - 1
                );

                if (!level.getBlockState(blockpos1.below()).is(this)
                        || level.getBlockState(blockpos1).isCollisionShapeFullBlock(level, blockpos1)) {
                    validPath = false;
                    break;
                }
            }

            if (!validPath) {
                continue;
            }

            var tempState = level.getBlockState(blockpos1);
            if (tempState.is(state.getBlock()) && random.nextInt(10) == 0) {
                ((BonemealableBlock) state.getBlock())
                        .performBonemeal(level, random, blockpos1, tempState);
            }
        }
    }

    @Override
    public Type getType() {
        return Type.NEIGHBOR_SPREADER;
    }
}
