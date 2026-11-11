package com.altnoir.mementoinabyss.infrastructure.worldgen.tree;

import com.altnoir.mementoinabyss.init.MiaWorldgenFeatures;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public final class DuskveilTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<DuskveilTrunkPlacer> CODEC =
            RecordCodecBuilder.mapCodec(
                    instance ->
                            trunkPlacerParts(instance).apply(instance, DuskveilTrunkPlacer::new));

    public DuskveilTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return MiaWorldgenFeatures.DUSKVEIL_TRUNK.get();
    }

    @Override
    public List<FoliageAttachment> placeTrunk(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> setter,
            RandomSource random,
            int treeHeight,
            BlockPos origin,
            TreeConfiguration config) {
        // Reserve space above the last log for the top of the crown.
        int trunkHeight = Math.max(1, treeHeight - 3);
        for (int x = 0; x <= 1; x++) {
            for (int z = 0; z <= 1; z++) {
                BlockPos column = origin.offset(x, 0, z);
                placeBelowTrunkBlock(level, setter, random, column.below(), config);
                for (int y = 0; y < trunkHeight; y++) {
                    placeLog(level, setter, random, column.above(y), config);
                }
            }
        }

        List<FoliageAttachment> crowns = new ArrayList<>(5);
        crowns.add(new FoliageAttachment(origin.above(trunkHeight - 1), 0, true));
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        for (int i = 0; i < 4; i++, direction = direction.getClockWise()) {
            int y = Math.max(0, trunkHeight - 4 - i % 2 * 2 - random.nextInt(2));
            Direction turn =
                    random.nextBoolean()
                            ? direction.getClockWise()
                            : direction.getCounterClockWise();
            int x =
                    direction.getAxis() == Direction.Axis.X
                            ? Math.max(0, direction.getStepX())
                            : random.nextInt(2);
            int z =
                    direction.getAxis() == Direction.Axis.Z
                            ? Math.max(0, direction.getStepZ())
                            : random.nextInt(2);
            BlockPos root = origin.offset(x, y, z);
            int length = 5 + random.nextInt(3);
            BlockPos tip =
                    extendBranch(level, setter, random, config, root, direction, turn, length);
            if (tip.distManhattan(root) >= 3) {
                crowns.add(new FoliageAttachment(tip, random.nextInt(2), false));
            }
        }
        return crowns;
    }

    private BlockPos extendBranch(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> setter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos root,
            Direction direction,
            Direction turn,
            int length) {
        BlockPos tip = root;
        int bendAt = 2 + random.nextInt(2);
        for (int step = 0; step < length; step++) {
            BlockPos next = tip.relative(direction);
            if (!placeBranchLog(level, setter, random, config, next, direction)) break;
            tip = next;

            if (step == bendAt) {
                next = tip.relative(turn);
                if (placeBranchLog(level, setter, random, config, next, turn)) tip = next;
            }
            if (step == 1 || step == length - 2) {
                next = tip.above();
                if (placeBranchLog(level, setter, random, config, next, Direction.UP)) tip = next;
            }
        }
        return tip;
    }

    private boolean placeBranchLog(
            WorldGenLevel level,
            BiConsumer<BlockPos, BlockState> setter,
            RandomSource random,
            TreeConfiguration config,
            BlockPos pos,
            Direction direction) {
        return level.isInsideBuildHeight(pos)
                && placeLog(
                        level,
                        setter,
                        random,
                        pos,
                        config,
                        state -> state.trySetValue(BlockStateProperties.AXIS, direction.getAxis()));
    }
}
