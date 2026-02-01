package com.altnoir.mia.worldgen.feature.trunk;

import com.altnoir.mia.init.worldgen.MiaTrunkPlacerTypes;
import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;

public class InvertedForkingTrunkPlacer extends TrunkPlacer {
    public static final MapCodec<InvertedForkingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
            instance -> trunkPlacerParts(instance).apply(instance, InvertedForkingTrunkPlacer::new)
    );

    public InvertedForkingTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return MiaTrunkPlacerTypes.FORKING_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> blockSetter,
            RandomSource random,
            int freeTreeHeight,
            BlockPos pos,
            TreeConfiguration config
    ) {
        setDirtAt(level, blockSetter, random, pos.above(), config);
        List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int i = freeTreeHeight - random.nextInt(4) - 1;
        int j = 3 - random.nextInt(3);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        int k = pos.getX();
        int l = pos.getZ();
        OptionalInt optionalint = OptionalInt.empty();

        for (int i1 = 0; i1 < freeTreeHeight; i1++) {
            int j1 = pos.getY() - i1;
            if (i1 >= i && j > 0) {
                k += direction.getStepX();
                l += direction.getStepZ();
                j--;
            }

            if (this.placeLog(level, blockSetter, random, blockpos$mutableblockpos.set(k, j1, l), config)) {
                optionalint = OptionalInt.of(j1 - 1);
            }
        }

        if (optionalint.isPresent()) {
            list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(k, optionalint.getAsInt(), l), 1, false));
        }

        k = pos.getX();
        l = pos.getZ();
        Direction direction1 = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        if (direction1 != direction) {
            int j2 = i - random.nextInt(2) - 1;
            int k1 = 1 + random.nextInt(3);
            optionalint = OptionalInt.empty();

            for (int l1 = j2; l1 < freeTreeHeight && k1 > 0; k1--) {
                if (l1 >= 1) {
                    int i2 = pos.getY() - l1;
                    k += direction1.getStepX();
                    l += direction1.getStepZ();
                    if (this.placeLog(level, blockSetter, random, blockpos$mutableblockpos.set(k, i2, l), config)) {
                        optionalint = OptionalInt.of(i2 - 1);
                    }
                }

                l1++;
            }

            if (optionalint.isPresent()) {
                list.add(new FoliagePlacer.FoliageAttachment(new BlockPos(k, optionalint.getAsInt(), l), 0, false));
            }
        }

        return list;
    }
}
