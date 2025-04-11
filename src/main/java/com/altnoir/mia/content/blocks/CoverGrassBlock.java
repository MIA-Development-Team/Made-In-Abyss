package com.altnoir.mia.content.blocks;

import com.altnoir.mia.BlocksRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.lighting.LightEngine;

import java.util.List;
import java.util.Optional;

public class CoverGrassBlock extends Block implements BonemealableBlock {
    public CoverGrassBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties);
    }

    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean b) {
        return levelReader.getBlockState(blockPos.above()).isAir();
    }

    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    public void performBonemeal(ServerLevel level, RandomSource source, BlockPos blockPos, BlockState blockState) {
        BlockPos $$4 = blockPos.above(); // 获取方块上方位置
        BlockState $$5 = Blocks.GRASS.defaultBlockState(); // 高草方块
        Optional<Holder.Reference<PlacedFeature>> $$6 = level.registryAccess()
                .registryOrThrow(Registries.PLACED_FEATURE)
                .getHolder(VegetationPlacements.GRASS_BONEMEAL); // 获取原版草催生特征

        label49:
        for(int $$7 = 0; $$7 < 128; ++$$7) {
            BlockPos $$8 = $$4; // 起始位置

            //瞬间偏移算法
            for(int $$9 = 0; $$9 < $$7 / 16; ++$$9) {
                $$8 = $$8.offset(source.nextInt(3) - 1, (source.nextInt(3) - 1) * source.nextInt(3) / 2, source.nextInt(3) - 1);
                // 判断当前位置是否满足条件
                if (!level.getBlockState($$8.below()).is(this) || level.getBlockState($$8).isCollisionShapeFullBlock(level, $$8)) {
                    continue label49;
                }
            }

            BlockState $$10 = level.getBlockState($$8);
            if ($$10.is($$5.getBlock()) && source.nextInt(10) == 0) {
                ((BonemealableBlock)$$5.getBlock()).performBonemeal(level, source, $$8, $$10);
            }

            if ($$10.isAir()) {
                Holder $$13;
                if (source.nextInt(2) == 0) {
                    List<ConfiguredFeature<?, ?>> $$11 = ((Biome)level.getBiome($$8).value()).getGenerationSettings().getFlowerFeatures();
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

                ((PlacedFeature)$$13.value()).place(level, level.getChunkSource().getGenerator(), source, $$8);
            }
        }
    }

    private static boolean canBeGrass(BlockState pState, LevelReader pLevelReader, BlockPos pPos) {
        BlockPos blockpos = pPos.above();
        BlockState blockstate = pLevelReader.getBlockState(blockpos);
        if (blockstate.is(Blocks.SNOW) && (Integer)blockstate.getValue(SnowLayerBlock.LAYERS) == 1) {
            return true;
        } else if (blockstate.getFluidState().getAmount() == 8) {
            return false;
        } else {
            int i = LightEngine.getLightBlockInto(pLevelReader, pState, pPos, blockstate, blockpos, Direction.UP, blockstate.getLightBlock(pLevelReader, blockpos));
            return i < pLevelReader.getMaxLightLevel();
        }
    }

    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (!canBeGrass(pState, pLevel, pPos)) {
            if (!pLevel.isAreaLoaded(pPos, 1)) {
                return;
            }

            Block targetBlock;
            if (pState.is(BlocksRegister.COVERGRASS_COBBLESTONE.get())) {
                targetBlock = Blocks.COBBLESTONE;
            } else if (pState.is(BlocksRegister.COVERGRASS_STONE.get())) {
                targetBlock = Blocks.STONE;
            } else if (pState.is(BlocksRegister.COVERGRASS_DEEPSLATE.get())) {
                targetBlock = Blocks.DEEPSLATE;
            } else if (pState.is(BlocksRegister.COVERGRASS_GRANITE.get())) {
                targetBlock = Blocks.GRANITE;
            } else if (pState.is(BlocksRegister.COVERGRASS_DIORITE.get())) {
                targetBlock = Blocks.DIORITE;
            } else if (pState.is(BlocksRegister.COVERGRASS_ANDESITE.get())) {
                targetBlock = Blocks.ANDESITE;
            }  else if (pState.is(BlocksRegister.COVERGRASS_CALCITE.get())) {
                targetBlock = Blocks.CALCITE;
            }  else if (pState.is(BlocksRegister.COVERGRASS_TUFF.get())) {
                targetBlock = Blocks.TUFF;
            }  else {
                targetBlock = this;
            }

            pLevel.setBlockAndUpdate(pPos, targetBlock.defaultBlockState());
        }
    }
}
