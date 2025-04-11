package com.altnoir.mia.content.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;
import java.util.Optional;

public class AbyssGrassBlock extends SpreadingSnowyDirtBlock implements BonemealableBlock {
    public AbyssGrassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean b) {
        return levelReader.getBlockState(blockPos.above()).isAir();
    }

    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    public void performBonemeal(ServerLevel p_221270_, RandomSource p_221271_, BlockPos p_221272_, BlockState p_221273_) {
        BlockPos $$4 = p_221272_.above(); // 获取方块上方位置
        BlockState $$5 = Blocks.GRASS.defaultBlockState(); // 高草方块
        Optional<Holder.Reference<PlacedFeature>> $$6 = p_221270_.registryAccess()
                .registryOrThrow(Registries.PLACED_FEATURE)
                .getHolder(VegetationPlacements.GRASS_BONEMEAL); // 获取原版草催生特征

        label49:
        for(int $$7 = 0; $$7 < 128; ++$$7) {
            BlockPos $$8 = $$4; // 起始位置

            //瞬间偏移算法
            for(int $$9 = 0; $$9 < $$7 / 16; ++$$9) {
                $$8 = $$8.offset(p_221271_.nextInt(3) - 1, (p_221271_.nextInt(3) - 1) * p_221271_.nextInt(3) / 2, p_221271_.nextInt(3) - 1);
                // 判断当前位置是否满足条件
                if (!p_221270_.getBlockState($$8.below()).is(this) || p_221270_.getBlockState($$8).isCollisionShapeFullBlock(p_221270_, $$8)) {
                    continue label49;
                }
            }

            BlockState $$10 = p_221270_.getBlockState($$8);
            if ($$10.is($$5.getBlock()) && p_221271_.nextInt(10) == 0) {
                ((BonemealableBlock)$$5.getBlock()).performBonemeal(p_221270_, p_221271_, $$8, $$10);
            }

            if ($$10.isAir()) {
                Holder $$13;
                if (p_221271_.nextInt(2) == 0) {
                    List<ConfiguredFeature<?, ?>> $$11 = ((Biome)p_221270_.getBiome($$8).value()).getGenerationSettings().getFlowerFeatures();
                    if ($$11.isEmpty()) {
                        continue;
                    }

                    $$13 = ((RandomPatchConfiguration)((ConfiguredFeature)$$11.get(0)).config()).feature();
                } else {
                    if (!$$6.isPresent()) {
                        continue;
                    }

                    $$13 = (Holder)$$6.get();
                }

                ((PlacedFeature)$$13.value()).place(p_221270_, p_221270_.getChunkSource().getGenerator(), p_221271_, $$8);
            }
        }

    }
}

