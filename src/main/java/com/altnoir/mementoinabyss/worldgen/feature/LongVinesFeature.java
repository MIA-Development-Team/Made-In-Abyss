package com.altnoir.mementoinabyss.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public final class LongVinesFeature extends Feature<LongVinesConfiguration> {
    public LongVinesFeature(Codec<LongVinesConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<LongVinesConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        if (!level.isEmptyBlock(origin)) return false;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!VineBlock.isAcceptableNeighbour(level, origin.relative(direction), direction)) continue;
            var vine = Blocks.VINE.defaultBlockState()
                    .setValue(VineBlock.getPropertyForFace(direction), true);
            int height = context.config().height().sample(context.random());
            BlockPos.MutableBlockPos cursor = origin.mutable();
            for (int i = 0; i <= height && level.isEmptyBlock(cursor); i++) {
                level.setBlock(cursor, vine, 2);
                cursor.move(Direction.DOWN);
            }
            return true;
        }
        return false;
    }
}
