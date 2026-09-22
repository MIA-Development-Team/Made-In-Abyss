package com.altnoir.mia.common.block;

import com.altnoir.mia.worldgen.PrimoFeatures;
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

/**
 * 菌丝块（移植自 PoopSky 的 {@code MyceliumBlock}）。
 * <p>
 * 特性：随机冒菌丝粒子；骨粉后向相邻的可替换方块蔓延出菌丝植被
 * （{@link PrimoFeatures#MYCELIUM_PATCH_BONEMEAL}）。
 * <p>
 * <b>类名前缀 {@code Mia} 是必须的</b>：1.21.1 的原版也有
 * {@code net.minecraft.world.level.block.MyceliumBlock}，而 {@code MiaBlocks} 同时通配导入了
 * 原版与本模组的 {@code block} 包，直接叫 {@code MyceliumBlock} 会"引用不明确"编译失败。
 * 注册名仍然是 {@code mia:mycelium_block}，与原模组一致。
 * <p>
 * 之所以能蔓延，是因为它同时被放进了 {@code minecraft:dirt} 与
 * {@code mia:mycelium_replaceable} 标签 —— 见 {@code MiaBlockTagProvider}。
 */
public class MiaMyceliumBlock extends Block implements BonemealableBlock {
    public MiaMyceliumBlock(Properties properties) {
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
                    0.0
            );
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.above()).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        level.registryAccess()
                .registry(Registries.CONFIGURED_FEATURE)
                .flatMap(holder -> holder.getHolder(PrimoFeatures.MYCELIUM_PATCH_BONEMEAL))
                .ifPresent(reference -> reference.value()
                        .place(level, level.getChunkSource().getGenerator(), random, pos.above()));
    }

    @Override
    public Type getType() {
        return BonemealableBlock.Type.NEIGHBOR_SPREADER;
    }
}
