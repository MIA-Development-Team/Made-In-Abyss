package com.altnoir.mia.worldgen.feature;

import com.altnoir.mia.worldgen.feature.configurations.LongVinesConfiguration;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class LongVinesFeature extends Feature<LongVinesConfiguration> {
    public LongVinesFeature(Codec<LongVinesConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LongVinesConfiguration> context) {
        WorldGenLevel worldgenlevel = context.level();
        BlockPos blockpos = context.origin();

        context.config();
        var height = context.config().height().sample(context.random());
        if (!worldgenlevel.isEmptyBlock(blockpos)) {
            return false;
        } else {
            for (Direction direction : Direction.values()) {
                if (direction != Direction.UP && direction != Direction.DOWN && VineBlock.isAcceptableNeighbour(worldgenlevel, blockpos.relative(direction), direction)) {
                    worldgenlevel.setBlock(
                            blockpos, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction), Boolean.TRUE), 2
                    );

                    BlockPos belowPos = blockpos.below();
                    for (int i = 0; i <= height; i++) {
                        if (worldgenlevel.isEmptyBlock(belowPos)) {
                            worldgenlevel.setBlock(
                                    belowPos, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(direction), Boolean.valueOf(true)), 2
                            );
                            belowPos = belowPos.below();
                        } else {
                            break;
                        }
                    }
                    return true;
                }
            }

            return false;
        }
    }
}
