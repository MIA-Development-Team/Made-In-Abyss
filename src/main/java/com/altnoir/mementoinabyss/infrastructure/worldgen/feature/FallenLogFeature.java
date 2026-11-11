package com.altnoir.mementoinabyss.infrastructure.worldgen.feature;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

public final class FallenLogFeature extends Feature<FallenLogConfiguration> {
    public FallenLogFeature(Codec<FallenLogConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FallenLogConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        FallenLogConfiguration config = context.config();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int length = config.length().sample(random);
        BlockPos origin = context.origin();
        List<BlockPos> positions = new ArrayList<>(length);
        int gaps = 0;

        for (int i = 0; i < length; i++) {
            BlockPos pos = origin.relative(direction, i);
            if (!level.isInsideBuildHeight(pos)
                    || !level.isInsideBuildHeight(pos.below())
                    || !level.ensureCanWrite(pos)) return false;
            BlockState existing = level.getBlockState(pos);
            if (!existing.canBeReplaced()
                    || existing.hasBlockEntity()
                    || existing.is(BlockTags.LEAVES)
                    || !existing.getFluidState().isEmpty()) return false;

            BlockPos below = pos.below();
            boolean supported = level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
            if (!supported && (i == 0 || i == length - 1 || ++gaps > 1)) return false;
            positions.add(pos);
        }

        List<BlockState> states = new ArrayList<>(length);
        for (BlockPos pos : positions) {
            BlockState state = config.log().getState(level, random, pos);
            if (!state.hasProperty(BlockStateProperties.AXIS)) return false;
            states.add(state.setValue(BlockStateProperties.AXIS, direction.getAxis()));
        }

        Set<BlockPos> logs = new LinkedHashSet<>(positions);
        for (int i = 0; i < length; i++) {
            level.setBlock(positions.get(i), states.get(i), Block.UPDATE_ALL);
        }

        TreeDecorator.Context decoration =
                new TreeDecorator.Context(
                        level,
                        (pos, state) -> {
                            if (level.isInsideBuildHeight(pos)
                                    && level.ensureCanWrite(pos)
                                    && level.isEmptyBlock(pos)
                                    && state.canSurvive(level, pos)) {
                                level.setBlock(pos, state, Block.UPDATE_ALL);
                            }
                        },
                        random,
                        logs,
                        Set.of(),
                        Set.of());
        for (TreeDecorator decorator : config.decorators()) decorator.place(decoration);
        return true;
    }
}
