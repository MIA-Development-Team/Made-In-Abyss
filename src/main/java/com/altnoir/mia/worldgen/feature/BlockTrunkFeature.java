package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.worldgen.feature.configurations.BlockTrunkConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class BlockTrunkFeature extends Feature<BlockTrunkConfiguration> {
    public BlockTrunkFeature(Codec<BlockTrunkConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockTrunkConfiguration> context) {
        WorldGenLevel worldgenlevel = context.level();
        BlockTrunkConfiguration blockConfiguration = context.config();
        RandomSource randomsource = context.random();

        int i = blockConfiguration.layers().size();
        int[] aint = new int[i];
        int j = 0;

        for (int k = 0; k < i; k++) {
            aint[k] = blockConfiguration.layers().get(k).height().sample(randomsource);
            j += aint[k];
        }

        if (j == 0) {
            return false;
        } else {
            BlockPos.MutableBlockPos blockpos$mutableblockpos1 = context.origin().mutable();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos$mutableblockpos1.mutable().move(blockConfiguration.direction());
            if (worldgenlevel.getBlockState(context.origin().relative(blockConfiguration.direction().getOpposite())).is(BlockTags.LEAVES)) {
                return false;
            }
            for (int l = 0; l < j; l++) {
                if (!blockConfiguration.allowedPlacement().test(worldgenlevel, blockpos$mutableblockpos)) {
                    truncate(aint, j, l, blockConfiguration.prioritizeTip());
                    break;
                }

                blockpos$mutableblockpos.move(blockConfiguration.direction());
            }
            int posHeight = 0;
            for (int k1 = 0; k1 < i; k1++) {
                int i1 = aint[k1];
                if (i1 != 0) {
                    BlockTrunkConfiguration.Layer layer = blockConfiguration.layers().get(k1);
                    for (int j1 = 0; j1 < i1; j1++) {
                        BlockState targetState = layer.state().getState(randomsource, blockpos$mutableblockpos1);

                        if (!worldgenlevel.getBlockState(blockpos$mutableblockpos1).is(Blocks.WATER) || !(targetState.getBlock() instanceof CarpetBlock)) {
                            worldgenlevel.setBlock(
                                    blockpos$mutableblockpos1, targetState, 2
                            );
                            if (posHeight < j - 1 && targetState.isSolidRender(worldgenlevel, blockpos$mutableblockpos1) && randomsource.nextFloat() < context.config().decChance()) {
                                setDecoration(worldgenlevel, blockpos$mutableblockpos1, blockConfiguration, randomsource);
                            }
                        }

                        blockpos$mutableblockpos1.move(blockConfiguration.direction());
                        posHeight++;
                    }
                }
            }

            return true;
        }
    }

    private void setDecoration(WorldGenLevel worldGenLevel, BlockPos pos, BlockTrunkConfiguration bc, RandomSource random) {
        var dec = bc.dec().state();
        int length = bc.dec().height().sample(random);
        var decFace = bc.decFace().state();
        var face = bc.direction();

        Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
        Direction dir = directions[random.nextInt(directions.length)];
        BlockPos newPos = pos.relative(dir);

        for (int i = 0; i < length; i++) {
            if (worldGenLevel.getBlockState(newPos).isEmpty() || worldGenLevel.getBlockState(newPos).is(Blocks.WATER)) {
                BlockState state = dec.getState(random, newPos);

                if (state.hasProperty(BlockStateProperties.FACING)) {
                    state = state.setValue(BlockStateProperties.FACING, dir);
                } else if (state.hasProperty(BlockStateProperties.AXIS)) {
                    state = state.setValue(BlockStateProperties.AXIS, dir.getAxis());
                }

                worldGenLevel.setBlock(newPos, state, 2);
                if (worldGenLevel.getBlockState(newPos.mutable().move(face)).isEmpty()) {
                    worldGenLevel.setBlock(newPos.mutable().move(face), decFace.getState(random, newPos.mutable().move(face)), 2);
                }
            } else {
                break;
            }

            newPos = newPos.relative(dir);
        }
    }

    private static void truncate(int[] layerHeights, int totalHeight, int currentHeight, boolean prioritizeTip) {
        int i = totalHeight - currentHeight;
        int j = prioritizeTip ? 1 : -1;
        int k = prioritizeTip ? 0 : layerHeights.length - 1;
        int l = prioritizeTip ? layerHeights.length : -1;

        for (int i1 = k; i1 != l && i > 0; i1 += j) {
            int j1 = layerHeights[i1];
            int k1 = Math.min(j1, i);
            i -= k1;
            layerHeights[i1] -= k1;
        }
    }
}
