package com.altnoir.mia.common.block;

import com.altnoir.mia.worldgen.biome.the_abyss.TheAbyssFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MyceliumBlock extends Block implements BonemealableBlock {
    public MyceliumBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(10) == 0) {
            level.addParticle(
                    ParticleTypes.MYCELIUM,
                    (double) pos.getX() + random.nextDouble(),
                    (double) pos.getY() + 1.1,
                    (double) pos.getZ() + random.nextDouble(),
                    0.0,
                    0.0,
                    0.0);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(
            Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(
            ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.registryAccess()
                .registry(Registries.CONFIGURED_FEATURE)
                .flatMap(holder -> holder.getHolder(TheAbyssFeatures.MYCELIUM_PATCH_BONEMEAL))
                .ifPresent(
                        reference ->
                                reference
                                        .value()
                                        .place(
                                                level,
                                                level.getChunkSource().getGenerator(),
                                                random,
                                                pos.above()));
    }

    @Override
    public Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }
}
