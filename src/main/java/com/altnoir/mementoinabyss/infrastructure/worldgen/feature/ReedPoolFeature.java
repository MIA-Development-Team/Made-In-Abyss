package com.altnoir.mementoinabyss.infrastructure.worldgen.feature;

import com.altnoir.mementoinabyss.init.MiaBlocks;
import com.mojang.serialization.Codec;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.WaterloggedVegetationPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public final class ReedPoolFeature extends WaterloggedVegetationPatchFeature {
    private static final Direction[] WATER_BOUNDARIES = {
        Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN
    };

    public ReedPoolFeature(Codec<VegetationPatchConfiguration> codec) {
        super(codec);
    }

    @Override
    protected Set<BlockPos> placeGroundPatch(
            WorldGenLevel level,
            VegetationPatchConfiguration config,
            RandomSource random,
            BlockPos origin,
            Predicate<BlockState> replaceable,
            int xRadius,
            int zRadius) {
        double angle = random.nextDouble() * Math.PI * 2;
        double lobeX = Math.cos(angle) * .3;
        double lobeZ = Math.sin(angle) * .3;
        var ground = new HashSet<BlockPos>();
        var pos = origin.mutable();
        var below = origin.mutable();
        Direction inwards = config.surface.getDirection();
        Direction outwards = inwards.getOpposite();
        for (int dx = -xRadius; dx <= xRadius; dx++) {
            for (int dz = -zRadius; dz <= zRadius; dz++) {
                double x = (double) dx / xRadius;
                double z = (double) dz / zRadius;
                if (!inside(x, z, lobeX, lobeZ)) continue;
                pos.setWithOffset(origin, dx, 0, dz);
                for (int i = 0; level.isEmptyBlock(pos) && i < config.verticalRange; i++)
                    pos.move(inwards);
                for (int i = 0; !level.isEmptyBlock(pos) && i < config.verticalRange; i++)
                    pos.move(outwards);
                below.setWithOffset(pos, inwards);
                if (!level.isEmptyBlock(pos)
                        || !level.ensureCanWrite(below)
                        || !level.getBlockState(below).isFaceSturdy(level, below, outwards))
                    continue;
                int depth =
                        config.depth.sample(random)
                                + (config.extraBottomBlockChance > 0
                                                && random.nextFloat()
                                                        < config.extraBottomBlockChance
                                        ? 1
                                        : 0);
                if (level.isOutsideBuildHeight(below.getY() - depth + 1)
                        || !level.ensureCanWrite(below.below(depth - 1))) continue;
                var groundPos = below.immutable();
                if (placeGround(level, config, replaceable, random, below, depth))
                    ground.add(groundPos);
            }
        }
        var water = new HashSet<BlockPos>();
        for (var groundPos : ground) {
            if (!level.ensureCanWrite(groundPos)) continue;
            boolean enclosed = true;
            for (var direction : WATER_BOUNDARIES) {
                var adjacent = groundPos.relative(direction);
                if (!level.getBlockState(adjacent)
                        .isFaceSturdy(level, adjacent, direction.getOpposite())) {
                    enclosed = false;
                    break;
                }
            }
            if (enclosed) water.add(groundPos);
        }
        for (var waterPos : water) level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 2);
        return water;
    }

    private static boolean inside(double x, double z, double lobeX, double lobeZ) {
        return x * x + z * z <= .72
                || square(x - lobeX) + square(z - lobeZ) <= .48
                || square(x + lobeX * .7) + square(z + lobeZ * .7) <= .42;
    }

    private static double square(double value) {
        return value * value;
    }

    @Override
    protected boolean placeVegetation(
            WorldGenLevel level,
            VegetationPatchConfiguration config,
            ChunkGenerator generator,
            RandomSource random,
            BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.WATER)
                || !level.isEmptyBlock(pos.above())
                || !level.ensureCanWrite(pos.above())
                || !MiaBlocks.REED
                        .get()
                        .defaultBlockState()
                        .setValue(BlockStateProperties.WATERLOGGED, true)
                        .canSurvive(level, pos)) return false;
        return super.placeVegetation(level, config, generator, random, pos);
    }
}
