package com.altnoir.mementoinabyss.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public final class ClusterFeature extends Feature<ClusterConfiguration> {
    private final boolean large;

    public ClusterFeature(Codec<ClusterConfiguration> codec, boolean large) {
        super(codec);
        this.large = large;
    }

    @Override
    public boolean place(FeaturePlaceContext<ClusterConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ClusterConfiguration config = context.config();
        BlockPos center = context.origin();
        Direction search = large || random.nextBoolean() ? Direction.DOWN : Direction.UP;
        int steps = large ? 64 : 12;
        for (int i = 0; i < steps && center.getY() > level.getMinY() + 3
                && center.getY() < level.getMaxY() - 3
                && !level.isEmptyBlock(center) && !isWater(level, center); i++) center = center.relative(search);
        if (!level.isEmptyBlock(center) && !isWater(level, center)) return false;
        int passes = large ? 1 : 3;
        boolean generated = false;
        for (int pass = 0; pass < passes; pass++) {
            int radius = random.nextInt(config.size().sample(random)) + (large ? 2 : 1);
            int halfHeight = Math.max(1, config.height().sample(random) / 2);
            int vertical = random.nextInt(Math.max(1, config.size().sample(random))) + 1 + halfHeight;
            generated |= placeEllipsoid(level, center, radius, vertical, config, random);
            center = center.offset(random.nextInt(3) - 1, -random.nextInt(2), random.nextInt(3) - 1);
        }
        return generated;
    }

    private boolean placeEllipsoid(WorldGenLevel level, BlockPos center, int radius, int height,
                                   ClusterConfiguration config, RandomSource random) {
        List<BlockPos> surface = new ArrayList<>();
        boolean generated = false;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -height, -radius),
                center.offset(radius, height, radius))) {
            double distance = Math.abs((pos.getX() - center.getX()) / (double) radius)
                    + Math.abs((pos.getY() - center.getY()) / (double) height)
                    + Math.abs((pos.getZ() - center.getZ()) / (double) radius);
            if (distance <= 1.0) {
                level.setBlock(pos, config.base().getState(level, random, pos), 4);
                surface.add(pos.immutable());
                generated = true;
            }
        }
        for (BlockPos base : surface) {
            if (random.nextFloat() >= config.crystalChance()) continue;
            if (!placeCrystal(level, base.above(), Direction.UP, config.crystalsUp(), random))
                placeCrystal(level, base.below(), Direction.DOWN, config.crystalsDown(), random);
        }
        return generated;
    }

    private boolean placeCrystal(WorldGenLevel level, BlockPos pos, Direction facing,
                                 net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider provider,
                                 RandomSource random) {
        if (!level.isEmptyBlock(pos) && !isWater(level, pos)) return false;
        BlockPos support = pos.relative(facing.getOpposite());
        if (!level.getBlockState(support).isFaceSturdy(level, support, facing)) return false;
        BlockState state = provider.getState(level, random, pos);
        if (state.hasProperty(BlockStateProperties.WATERLOGGED))
            state = state.setValue(BlockStateProperties.WATERLOGGED, isWater(level, pos));
        level.setBlock(pos, state, 2);
        return true;
    }

    private static boolean isWater(WorldGenLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(Fluids.WATER);
    }
}
