package com.altnoir.mia.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LightFeature extends Feature<NoneFeatureConfiguration> {
    public LightFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldgenlevel = context.level();
        BlockPos blockpos = context.origin();

        context.config();
        if (worldgenlevel.isEmptyBlock(blockpos)) {
            for (Direction direction : Direction.values()) {
                if (direction != Direction.DOWN && VineBlock.isAcceptableNeighbour(worldgenlevel, blockpos.relative(direction), direction)) {
                    worldgenlevel.setBlock(blockpos, Blocks.LIGHT.defaultBlockState(), 2);

                    BlockPos belowPos = blockpos.below();
                    for (int i = 0; i <= 16; i++) {
                        if (worldgenlevel.isEmptyBlock(belowPos)) {
                            worldgenlevel.setBlock(belowPos, Blocks.LIGHT.defaultBlockState(), 2);
                            belowPos = belowPos.below();
                        } else {
                            break;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
